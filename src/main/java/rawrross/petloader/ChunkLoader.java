package rawrross.petloader;

import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;

import java.util.*;

public class ChunkLoader {

    public static void queryEntity(TameableEntity pet) {
        if (pet.world.isClient || !pet.isTamed())
            return;

        // Determines if the owner is online and in the same dimension as the pet.
        // The pet will always sit if its owner is not present.
        // Note: isSitting() returns false if the pet was standing when the owner left.
        ServerPlayerEntity owner = null;
        for (ServerPlayerEntity player : ((ServerWorld) pet.world).getPlayers()) {
            if (player.getUuid().equals(pet.getOwnerUuid())) {
                owner = player;
                break;
            }
        }

        boolean entityIsValid = !(pet.isDead() || pet.isRemoved() || pet.isSitting() || pet.hasVehicle() || pet.isLeashed());
        boolean shouldRegister = entityIsValid && owner != null;
        if (shouldRegister)
            ChunkLoader.register(pet);
        else
            ChunkLoader.remove(pet);
    }

    private static class TrackedChunk {

        public ServerWorld world;
        public ChunkPos pos;

        public TrackedChunk(TameableEntity entity) {
            this.world = (ServerWorld) entity.world;
            this.pos = entity.getChunkPos();
        }

        public String getDimensionString() {
            return world.getDimensionKey().getValue().toString();
        }

        @Override
        public String toString() {
            return "'" + world.toString() + "'/" + getDimensionString() + " @ " + pos.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TrackedChunk that = (TrackedChunk) o;
            boolean worldNameEqual = Objects.equals(world.toString(), that.world.toString()); // why is world.worldProperties.getLevelName() private???
            boolean posEqual = pos.equals(that.pos);

            return worldNameEqual && posEqual;
        }

        @Override
        public int hashCode() {
            return Objects.hash(world, pos);
        }

    }

    private static final HashMap<TameableEntity, TrackedChunk> loadedEntities = new HashMap<>();

    private static void register(TameableEntity entity) {
        if (!loadedEntities.containsKey(entity)) {
            TrackedChunk chunk = new TrackedChunk(entity);
            PetLoaderMod.logger.info("Tracking entity " + entity + "/" + chunk.getDimensionString());

            load(chunk);
            loadedEntities.put(entity, chunk);
        }
    }

    private static void remove(TameableEntity entity) {
        if (loadedEntities.containsKey(entity)) {
            TrackedChunk chunk = loadedEntities.remove(entity);
            PetLoaderMod.logger.info("Untracked entity " + entity + "/" + chunk.getDimensionString());

            if (!getActiveChunks().contains(chunk)) {
                unload(chunk);
            } else {
                PetLoaderMod.logger.debug("Another entity prevented " + chunk + " from being unloaded");
            }
        }
    }

    private static HashSet<TrackedChunk> getActiveChunks() {
        HashSet<TrackedChunk> activeChunks = new HashSet<>();
        for (Map.Entry<TameableEntity, TrackedChunk> entry : loadedEntities.entrySet()) {
            activeChunks.add(entry.getValue());
        }
        return activeChunks;
    }

    public static void update(MinecraftServer server) {
        HashSet<TrackedChunk> activeChunks = new HashSet<>();
        ArrayList<TrackedChunk> chunksToUnload = new ArrayList<>();

        for (Map.Entry<TameableEntity, TrackedChunk> entry : loadedEntities.entrySet()) {
            TameableEntity entity = entry.getKey();
            TrackedChunk prevChunk = entry.getValue();
            TrackedChunk currChunk = new TrackedChunk(entity);

            if (!Objects.equals(currChunk, prevChunk)) {
                PetLoaderMod.logger.debug("Entity " + entity + " moved from " + prevChunk + " to " + currChunk);

                chunksToUnload.add(prevChunk);

                load(currChunk);
                entry.setValue(currChunk);
                activeChunks.add(currChunk);
            } else {
                activeChunks.add(prevChunk);
            }
        }

        for (TrackedChunk chunk : chunksToUnload) {
            if (!activeChunks.contains(chunk)) {
                unload(chunk);
            } else {
                PetLoaderMod.logger.debug("Another entity prevented " + chunk + " from being unloaded");
            }
        }
    }

    private static void load(TrackedChunk chunk) {
        PetLoaderMod.logger.debug("Loading " + chunk);
        setChunkLoaded(true, chunk);
    }

    private static void unload(TrackedChunk chunk) {
        if (chunk == null)
            return;

        PetLoaderMod.logger.debug("Unloading chunk " + chunk);
        setChunkLoaded(false, chunk);
    }

    private static void setChunkLoaded(boolean loaded, TrackedChunk chunk) {
        ChunkPos pos = chunk.pos;
        chunk.world.setChunkForced(pos.x, pos.z, loaded);
    }

}
