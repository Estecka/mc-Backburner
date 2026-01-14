package tk.estecka.backburner.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

@Mixin(DrawContext.class)
public interface IDrawContextMixin {
	@Invoker
	void callDrawTexturedQuad(Identifier texture, int xMin, int xMax, int yMin, int yMax, int z, float uMin, float uMax, float vMin, float vMax);
}
