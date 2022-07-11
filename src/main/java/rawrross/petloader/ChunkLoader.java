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

    private static class WorldChunk {

        public ServerWorld world;
        public ChunkPos pos;

        public WorldChunk(TameableEntity entity) {
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

            WorldChunk that = (WorldChunk) o;
            boolean worldNameEqual = Objects.equals(world.toString(), that.world.toString()); // why is world.worldProperties.getLevelName() private???
            boolean posEqual = pos.equals(that.pos);

            return worldNameEqual && posEqual;
        }

        @Override
        public int hashCode() {
            return Objects.hash(world, pos);
        }

    }

    private static final HashMap<TameableEntity, WorldChunk> loadedEntities = new HashMap<>();

    private static void register(TameableEntity entity) {
        if (!loadedEntities.containsKey(entity)) {
            WorldChunk chunk = new WorldChunk(entity);
            PetLoaderMod.logger.info("Tracking entity " + entity + "/" + chunk.getDimensionString());

            load(chunk);
            loadedEntities.put(entity, chunk);
        }
    }

    private static void remove(TameableEntity entity) {
        if (loadedEntities.containsKey(entity)) {
            WorldChunk chunk = loadedEntities.remove(entity);
            PetLoaderMod.logger.info("Untracked entity " + entity + "/" + chunk.getDimensionString());

            if (!getActiveChunks().contains(chunk)) {
                unload(chunk);
            } else {
                PetLoaderMod.logger.info("Another entity prevented " + chunk + " from being unloaded");
            }
        }
    }

    private static HashSet<WorldChunk> getActiveChunks() {
        HashSet<WorldChunk> activeChunks = new HashSet<>();
        for (Map.Entry<TameableEntity, WorldChunk> entry : loadedEntities.entrySet()) {
            activeChunks.add(entry.getValue());
        }
        return activeChunks;
    }

    public static void update(MinecraftServer server) {
        HashSet<WorldChunk> activeChunks = new HashSet<>();
        ArrayList<WorldChunk> chunksToUnload = new ArrayList<>();

        for (Map.Entry<TameableEntity, WorldChunk> entry : loadedEntities.entrySet()) {
            TameableEntity entity = entry.getKey();
            WorldChunk prevChunk = entry.getValue();
            WorldChunk currChunk = new WorldChunk(entity);

            if (!Objects.equals(currChunk, prevChunk)) {
                PetLoaderMod.logger.info("Entity " + entity + " moved from " + prevChunk + " to " + currChunk);

                chunksToUnload.add(prevChunk);

                load(currChunk);
                entry.setValue(currChunk);
                activeChunks.add(currChunk);
            } else {
                activeChunks.add(prevChunk);
            }
        }

        for (WorldChunk chunk : chunksToUnload) {
            if (!activeChunks.contains(chunk)) {
                unload(chunk);
            } else {
                PetLoaderMod.logger.info("Another entity prevented " + chunk + " from being unloaded");
            }
        }
    }

    private static void load(WorldChunk chunk) {
        PetLoaderMod.logger.info("Loading " + chunk);
        setChunkLoaded(true, chunk);
    }

    private static void unload(WorldChunk chunk) {
        if (chunk == null)
            return;

        PetLoaderMod.logger.info("Unloading chunk " + chunk);
        setChunkLoaded(false, chunk);
    }

    private static void setChunkLoaded(boolean loaded, WorldChunk chunk) {
        ChunkPos pos = chunk.pos;
        chunk.world.setChunkForced(pos.x, pos.z, loaded);
    }

}
