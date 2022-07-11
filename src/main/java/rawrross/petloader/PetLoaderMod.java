package rawrross.petloader;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PetLoaderMod implements ModInitializer {

    // TODO
    // [x] wolf
    // [ ] cat
    // [ ] parrot

    public static final String MODID = "petloader";
    public static final Logger logger = LoggerFactory.getLogger("PetLoader");

    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(this::endServerTick);
    }

    private void endServerTick(MinecraftServer server) {
        ChunkLoader.update(server);
    }

}
