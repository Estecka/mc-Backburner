package tk.estecka.backburner.mixin;

import java.util.function.Function;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

@Mixin(DrawContext.class)
public interface IDrawContextMixin {
	@Invoker
	void callDrawTexturedQuad(Function<Identifier, RenderLayer> renderLayer, Identifier texture, int xMin, int xMax, int yMin, int yMax, float uMin, float uMax, float vMin, float vMax, int color);
}
