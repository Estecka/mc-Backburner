package tk.estecka.backburner.hud;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.Lightmap;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import tk.estecka.backburner.Backburner;
import tk.estecka.backburner.BacklogData;
import tk.estecka.backburner.BacklogEntry;
import tk.estecka.backburner.mixin.IDrawContextMixin;

import static tk.estecka.backburner.Backburner.CONFIG;

public class BacklogHud 
{
	static public final Map<Identifier,GuiSpriteInfo> sprites = new HashMap<Identifier,GuiSpriteInfo>();
	static private final Identifier ICON_ID   = Identifier.fromNamespaceAndPath(Backburner.MODID, "textures/gui/backlog/icon.png"  );
	static private final Identifier HEADER_ID = Identifier.fromNamespaceAndPath(Backburner.MODID, "textures/gui/backlog/header.png");
	static private final Identifier ITEM_ID   = Identifier.fromNamespaceAndPath(Backburner.MODID, "textures/gui/backlog/item.png"  );
	static private final MutableComponent HEADER_TITLE = Component.translatable("backburner.header.title");

	static public boolean isHidden = false;
	static private final int z = 0;

	private final Minecraft client;
	private final Font textRenderer;

	public BacklogHud(){
		this.client = Minecraft.getInstance();
		this.textRenderer = client.font;
	}

	public void	Render(GuiGraphicsExtractor context){
		final List<BacklogEntry> items;
		if (BacklogData.instance==null || (items=BacklogData.instance.content) == null || items.isEmpty())
			return;

		final int guiScale = (int)client.getWindow().getGuiScale();
		float effectiveScale = guiScale * CONFIG.hudScale;
		if (!CONFIG.allowFractional)
			effectiveScale = Math.max(1, Math.round(effectiveScale));

		float effectiveMultiplier = effectiveScale / (float)guiScale;
		PoseStack matrices = context.getMatrices();
		matrices.push();
		matrices.scale(effectiveMultiplier, effectiveMultiplier, 1);

		int x = (CONFIG.anchorX <= 0.5) ? CONFIG.hudX : -CONFIG.hudX;
		int y = CONFIG.hudY;
		x += CONFIG.anchorX * client.getWindow().getScaledWidth() / effectiveMultiplier;
		x -= CONFIG.anchorX * CONFIG.hudWdt;

		if (isHidden){
			GuiSpriteInfo patch = sprites.getOrDefault(ICON_ID, GuiSpriteInfo.DEFAULT);
			RenderSystem.setShaderTexture(0, ICON_ID);
			RenderSystem.enableBlend();
			Draw9Patch( ICON_ID, context, x+patch.padding.left(), y+patch.padding.top(), patch.baseWidth, patch.baseHeight, patch );
			matrices.pop();
			return;
		}
		
		y = DrawTextBox( context, x, y, HEADER_ID, HEADER_TITLE );

		for (int i=0; i<items.size(); i++)
			y = DrawTextBox( context, x, y, ITEM_ID, Component.literal(String.format("%d • ", i)).append(items.get(i).displayText()) );

		matrices.pop();
	}

	/**
	 * @return The y coordinate of the element's bottom
	 */
	private int	DrawTextBox(GuiGraphicsExtractor context,int anchorX, int anchorY, Identifier sprite, FormattedText text){
		GuiSpriteInfo patch = sprites.getOrDefault(sprite, GuiSpriteInfo.DEFAULT);

		int imgX = anchorX + patch.padding.left();
		int imgY = anchorY + patch.padding.top();
		int textX = imgX + patch.textX;
		int textY = imgY + patch.textY;

		int imgWdt  = CONFIG.hudWdt - patch.paddingHorizontal;
		int textWdt = imgWdt - patch.minWidth;
		var lines = textRenderer.wrapLines(text, textWdt);
		int imgHgt = patch.minHeight + (textRenderer.fontHeight * lines.size());

		if (!patch.fill) {
			textWdt = 0;
			for (var l : lines)
				textWdt = Math.max(textWdt, textRenderer.getWidth(l));
			imgWdt = textWdt + patch.minWidth;
		}

		Draw9Patch(sprite, context, imgX, imgY, imgWdt, imgHgt, patch);

		for (var l : lines){
			DrawStyledText(context, l, textX, textY, patch);
			textY += textRenderer.fontHeight;
		}

		return anchorY + imgHgt + patch.paddingVertical;
	}

	private static int[] x=new int[4], y=new int[4];
	public void	Draw9Patch(Identifier sprite, GuiGraphicsExtractor context, int originX, int originY, int totalW, int totalH, GuiSpriteInfo patch) {
		IDrawContextMixin contextpp = (IDrawContextMixin)context;
		GuiSpriteInfo.GetPatchPositions(x, originX, totalW, patch.patch.left(), patch.patch.right ());
		GuiSpriteInfo.GetPatchPositions(y, originY, totalH, patch.patch.top (), patch.patch.bottom());
		float[] u = patch.u;
		float[] v = patch.v;

		RenderSystem.enableBlend();
		for (int tileX=0; tileX<3; ++tileX)
		for (int tileY=0; tileY<3; ++tileY)
		if  (x[tileX]<x[tileX+1] && y[tileY]<y[tileY+1])
		{
			contextpp.blit(
				sprite,
				x[tileX], x[tileX+1],
				y[tileY], y[tileY+1],
				z,
				u[tileX], u[tileX+1],
				v[tileY], v[tileY+1]
			);
			// int debugColor = 0xff000000 + tileX*0x00550000 + tileY*0x00005500;
			// drawBorder(matrices, 
			// 	x[tileX], y[tileY], 
			// 	x[tileX+1] - x[tileX],
			// 	y[tileY+1] - y[tileY],
			// 	debugColor
			// );
		}
	}

	private void	DrawStyledText(GuiGraphicsExtractor context, FormattedCharSequence text, int x, int y, GuiSpriteInfo style){
		var m = context.getMatrices().peek().getPositionMatrix();
		int light = Lightmap.MAX_LIGHT_COORDINATE;
		var vProv = context.getVertexConsumers();

		if (0 != (0xff000000 & style.outerlineColour)){
			textRenderer.drawWithOutline(text, x-1, y-1, style.outlineColour, style.outerlineColour, m, vProv, light);
			textRenderer.drawWithOutline(text, x-1, y+1, style.outlineColour, style.outerlineColour, m, vProv, light);
			textRenderer.drawWithOutline(text, x+1, y-1, style.outlineColour, style.outerlineColour, m, vProv, light);
			textRenderer.drawWithOutline(text, x+1, y+1, style.outlineColour, style.outerlineColour, m, vProv, light);
		}
		if (0 != (0xff000000 & style.outlineColour))
			textRenderer.drawWithOutline(text, x, y, style.textColour, style.outlineColour, m, vProv, light);
		textRenderer.draw(text, x, y, style.textColour, style.textShadow, m, vProv, DisplayMode.NORMAL, 0x0, light);
		vProv.draw();
	}
}
