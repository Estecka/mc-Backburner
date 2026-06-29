package tk.estecka.backburner.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.DeltaTracker;
import tk.estecka.backburner.hud.BacklogHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.RenderPass;

@Mixin(Gui.class)
public abstract class InGameHudMixin 
{
	@Unique final BacklogHud backlogHud = new BacklogHud();

	@Inject( method="renderHotbarAndDecorations", at=@At("HEAD") )
	private void RenderBacklogHud(GuiGraphicsExtractor context, DeltaTracker tickDelta, CallbackInfo info){
		this.backlogHud.Render(context);

		//TODO: RenderSystem has been deprecated.
		// 		This needs to use RenderPass and RenderPipeline. :(
		RenderSystem.enableBlend();
	}

}
