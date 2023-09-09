package tk.estecka.backburner.mixin;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.client.gui.DrawableHelper;

@Mixin(DrawableHelper.class)
public interface IDrawableHelperMixin {
	@Invoker
	static void callDrawTexturedQuad(Matrix4f matrix, int xMin, int xMax, int yMin, int yMax, int z, float uMin, float uMax, float vMin, float vMax){
		throw new AssertionError();
	}
}
