package rawrross.petloader.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ParrotEntity.class)
public abstract class MixinParrotEntity extends TameableEntity {

    protected MixinParrotEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

}
