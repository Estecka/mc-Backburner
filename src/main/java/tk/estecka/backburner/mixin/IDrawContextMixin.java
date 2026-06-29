package tk.estecka.backburner.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

@Mixin(GuiGraphicsExtractor.class)
public interface IDrawContextMixin {
	@Invoker
	void blit(Identifier texture, int xMin, int xMax, int yMin, int yMax, int z, float uMin, float uMax, float vMin, float vMax);
}
