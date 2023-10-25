package tk.estecka.backburner.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import tk.estecka.backburner.hud.BacklogHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.systems.RenderSystem;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin 
{
	@Shadow MinecraftClient client;
	@Unique final BacklogHud backlogHud = new BacklogHud(client = client);
	// Cursed shadow assignation ensures `client` is initialized when I need it.


	@Inject( method="render", at=@At(value="INVOKE", target="net/minecraft/client/network/ClientPlayerInteractionManager.getCurrentGameMode ()Lnet/minecraft/world/GameMode;", ordinal=0) )
	void	backburner$Render(DrawContext context, float tickDelta, CallbackInfo info){
		if (!client.options.hudHidden){
			this.backlogHud.Render(context, tickDelta);
			RenderSystem.enableBlend();
		}
	}

}
