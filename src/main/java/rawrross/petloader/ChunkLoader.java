package rawrross.petloader;

import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChunkLoader {

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

    }

    private static final HashMap<TameableEntity, WorldChunk> loadedEntities = new HashMap<>();

    public static void register(TameableEntity entity) {
        if (!loadedEntities.containsKey(entity)) {
            WorldChunk chunk = new WorldChunk(entity);
            PetLoaderMod.logger.info("Tracking entity " + entity + "/" + chunk.getDimensionString());

            loadedEntities.put(entity, chunk);
            load(chunk, entity);
        }
    }

    public static void remove(TameableEntity entity) {
        if (loadedEntities.containsKey(entity)) {
            WorldChunk chunk = loadedEntities.remove(entity);
            PetLoaderMod.logger.info("Untracked entity " + entity + "/" + chunk.getDimensionString());

            unload(chunk, entity);
        }
    }

    public static void update(MinecraftServer server) {
        for (Map.Entry<TameableEntity, WorldChunk> entry : loadedEntities.entrySet()) {
            TameableEntity entity = entry.getKey();
            WorldChunk prevChunk = entry.getValue();
            WorldChunk currChunk = new WorldChunk(entity);

            if (!Objects.equals(currChunk, prevChunk)) {
                PetLoaderMod.logger.info("Entity " + entity + " moved from " + prevChunk + " to " + currChunk);

                unload(prevChunk, entity);
                load(currChunk, entity);
                entry.setValue(currChunk);
            }

            // TODO
            // [ ] potential fix if pet unloads chunk while another is in it
            // [x] don't track pets standing in vehicles
            // [x] verify moving dimensions, old world unloaded?
            // [x] setChunkForced
            // [x] unload on entity death
            // [x] still tracked when sitting with different player
        }
    }

    private static void load(WorldChunk chunk, TameableEntity cause) {
        PetLoaderMod.logger.info("Loading " + chunk + " due to " + cause);
        setChunkLoaded(true, chunk);
    }

    private static void unload(WorldChunk chunk, TameableEntity cause) {
        if (chunk == null)
            return;

        PetLoaderMod.logger.info("Unloading chunk " + chunk + " due to " + cause);
        setChunkLoaded(false, chunk);
    }

    private static void setChunkLoaded(boolean loaded, WorldChunk chunk) {
        ChunkPos pos = chunk.pos;
        chunk.world.setChunkForced(pos.x, pos.z, loaded);
    }

}
