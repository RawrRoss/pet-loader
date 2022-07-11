package rawrross.petloader.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import rawrross.petloader.ChunkLoader;

@Mixin(WolfEntity.class)
public abstract class MixinWolfEntity extends TameableEntity {

    protected MixinWolfEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void injectTickMovement(CallbackInfo ci) {
//        System.out.println("==========");
//        System.out.println("isTamed: " + this.isTamed());
//        System.out.println("isSitting: " + this.isSitting());
//        System.out.println("isInSittingPose: " + this.isInSittingPose());

        if (this.world.isClient || !this.isTamed() || this.isDead())
            return;



//        this.isSitting()
//        this.getChunkPos()
//        world.setChunkForced()

        // if !sitting: register self with chunkloader
        // else: remove self

//        boolean isStanding = !this.isInSittingPose();

        ServerWorld world = (ServerWorld) this.world;
        ServerPlayerEntity owner = world.getServer().getPlayerManager().getPlayer(this.getOwnerUuid());
        // Owner will be null if they're offline, and the pet will always sit.
        // Note: isSitting() returns false if the pet was standing when the owner left.

//        System.out.println(owner);

        boolean shouldRegister = !this.isSitting() && owner != null;

        if (shouldRegister)
            ChunkLoader.register(this);
        else
            ChunkLoader.remove(this);
    }

    @Inject(method = "onDeath", at = @At("TAIL"))
    private void injectOnDeath(DamageSource damageSource, CallbackInfo ci) {
        if (this.world.isClient || !this.isTamed())
            return;

        ChunkLoader.remove(this);
    }

}
