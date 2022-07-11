package rawrross.petloader.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
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
        ChunkLoader.queryEntity(this);
    }

    @Inject(method = "onDeath", at = @At("TAIL"))
    private void injectOnDeath(DamageSource damageSource, CallbackInfo ci) {
        ChunkLoader.queryEntity(this);
    }

}
