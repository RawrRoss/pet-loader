package rawrross.petloader.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
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
        if (this.world.isClient || !this.isTamed())
            return;

        // Determines if the owner is online and in the same dimension as the pet.
        // The pet will always sit if its owner is not present.
        // Note: isSitting() returns false if the pet was standing when the owner left.
        ServerPlayerEntity owner = null;
        for (ServerPlayerEntity player : ((ServerWorld) this.world).getPlayers()) {
            if (player.getUuid().equals(this.getOwnerUuid())) {
                owner = player;
                break;
            }
        }

        boolean entityIsValid = !(this.isDead() || this.isRemoved() || this.isSitting() || this.hasVehicle());
        boolean shouldRegister = entityIsValid && owner != null;
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
