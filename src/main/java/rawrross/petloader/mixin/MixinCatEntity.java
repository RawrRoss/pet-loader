package rawrross.petloader.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import rawrross.petloader.ChunkLoader;

@Mixin(CatEntity.class)
public abstract class MixinCatEntity extends TameableEntity {

    protected MixinCatEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        ChunkLoader.queryEntity(this);
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        super.onDeath(damageSource);
        ChunkLoader.queryEntity(this);
    }

}
