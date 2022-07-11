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

        public WorldChunk(ServerWorld world, ChunkPos pos) {
            this.world = world;
            this.pos = pos;
        }

        public WorldChunk(TameableEntity entity) {
            this.world = (ServerWorld) entity.world;
            this.pos = entity.getChunkPos();
        }

        @Override
        public String toString() {
            return "'" + world.toString() + "'/" + world.getDimensionKey().getValue() + " @ " + pos.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            WorldChunk that = (WorldChunk) o;
            return world.equals(that.world) && pos.equals(that.pos);
        }

    }

    private static HashMap<TameableEntity, WorldChunk> loaded = new HashMap<>();

    public static void register(TameableEntity entity) {
        ServerWorld world = (ServerWorld) entity.getWorld();
//        LoadedChunk l = loaded.computeIfAbsent(entity, k -> new LoadedChunk());

        if (!loaded.containsKey(entity)) {
            PetLoaderMod.logger.info("Tracking entity " + entity);
            WorldChunk chunk = new WorldChunk(entity);
            loaded.put(entity, chunk);
            load(chunk, entity);
        }

//        ChunkPos currPos = entity.getChunkPos();
//        ChunkPos prevPos = loaded.getOrDefault(entity, null);
//
//        if (!Objects.equals(currPos, prevPos)) {
//            // TODO unload prevPos
//            // TODO load currPos
//        }
//
//        loaded.put(entity, currPos);
    }

    public static void remove(TameableEntity entity) {
        ServerWorld world = (ServerWorld) entity.getWorld();
//        HashMap<TameableEntity, ChunkPos> loaded = worldLoaded.computeIfAbsent(world, k -> new HashMap<>());

        if (loaded.containsKey(entity)) {
            PetLoaderMod.logger.info("Untracked entity " + entity);
            WorldChunk chunk = loaded.remove(entity);
            unload(chunk, entity);
        }
    }

    public static void update(MinecraftServer server) {
        for (Map.Entry<TameableEntity, WorldChunk> entry : loaded.entrySet()) {
            TameableEntity entity = entry.getKey();
            WorldChunk prevChunk = entry.getValue();
            WorldChunk currChunk = new WorldChunk(entity);

            if (!Objects.equals(currChunk, prevChunk)) {
                PetLoaderMod.logger.debug("Entity " + entity + " moved from " + prevChunk + " to " + currChunk);
                unload(prevChunk, entity);
                load(currChunk, entity);
                entry.setValue(currChunk);
            }

//            server.getPlayerManager().getPlayer()

            // TODO
            // [x] setChunkForced
            // [ ] verify moving dimensions, old world unloaded?
            // [x] unload on entity death
            // [x] still tracked when sitting with different player
        }
    }

    private static void load(WorldChunk chunk, TameableEntity cause) {
        // TODO
        PetLoaderMod.logger.debug("Loading " + chunk + " due to " + cause);
        setChunkLoaded(true, chunk);
    }

    private static void unload(WorldChunk chunk, TameableEntity cause) {
        if (chunk == null)
            return;

        // TODO
        PetLoaderMod.logger.debug("Unloading chunk " + chunk + " due to " + cause);
        setChunkLoaded(false, chunk);
    }

    private static void setChunkLoaded(boolean loaded, WorldChunk chunk) {
        ChunkPos pos = chunk.pos;
        chunk.world.setChunkForced(pos.x, pos.z, loaded);
    }

}
