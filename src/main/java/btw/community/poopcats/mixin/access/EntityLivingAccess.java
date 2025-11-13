package btw.community.poopcats.mixin.access;

import net.minecraft.src.EntityAITasks;
import net.minecraft.src.EntityLiving;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityLiving.class)
public interface EntityLivingAccess {
	@Accessor("tasks")
	EntityAITasks getTasks();
}
