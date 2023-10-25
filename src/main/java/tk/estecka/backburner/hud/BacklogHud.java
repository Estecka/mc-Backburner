package tk.estecka.backburner.hud;

import java.util.HashMap;
import java.util.Map;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.font.TextRenderer.TextLayerType;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import tk.estecka.backburner.Backburner;
import tk.estecka.backburner.BacklogData;
import tk.estecka.backburner.mixin.IDrawableHelperMixin;

import static tk.estecka.backburner.Backburner.CONFIG;

public class BacklogHud 
extends DrawableHelper
{
	static public final Map<Identifier,GuiSpriteInfo> sprites = new HashMap<Identifier,GuiSpriteInfo>();
	static private final Identifier ICON_ID   = new Identifier(Backburner.MODID, "textures/gui/backlog/icon.png"  );
	static private final Identifier HEADER_ID = new Identifier(Backburner.MODID, "textures/gui/backlog/header.png");
	static private final Identifier ITEM_ID   = new Identifier(Backburner.MODID, "textures/gui/backlog/item.png"  );
	static private final MutableText HEADER_TITLE = Text.translatable("backburner.header.title");

	static public boolean isHidden = false;
	static private final int z = 0;

	private final MinecraftClient client;
	private final TextRenderer textRenderer;

	public BacklogHud(MinecraftClient client){
		this.client = client;
		this.textRenderer = client.textRenderer;
	}

	public void	Render(MatrixStack matrices, float tickDelta){
		final var items = BacklogData.instance.content;
		if (items == null || items.isEmpty())
			return;

		final int guiScale = (int)client.getWindow().getScaleFactor();
		float effectiveScale = guiScale * CONFIG.hudScale;
		if (!CONFIG.allowFractional)
			effectiveScale = Math.max(1, Math.round(effectiveScale));

		float effectiveMultiplier = effectiveScale / (float)guiScale;
		matrices.push();
		matrices.scale(effectiveMultiplier, effectiveMultiplier, 1);

		int x = CONFIG.hudX;
		int y = CONFIG.hudY;

		if (isHidden){
			GuiSpriteInfo patch = sprites.getOrDefault(ICON_ID, GuiSpriteInfo.DEFAULT);
			RenderSystem.setShaderTexture(0, ICON_ID);
			RenderSystem.enableBlend();
			Draw9Patch(matrices, x+patch.padding.left(), y+patch.padding.top(), patch.baseWidth, patch.baseHeight, patch);
			matrices.pop();
			return;
		}
		
		y = DrawTextBox( matrices, x, y, HEADER_ID, HEADER_TITLE );

		for (int i=0; i<items.size(); i++)
			y = DrawTextBox( matrices, x, y, ITEM_ID, Text.literal(String.format("%d • %s", i, items.get(i))) );

		matrices.pop();
	}

	/**
	 * @return The y coordinate of the element's bottom
	 */
	private int	DrawTextBox(MatrixStack matrices,int anchorX, int anchorY, Identifier sprite, StringVisitable text){
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

		RenderSystem.setShaderTexture(0, sprite);
		RenderSystem.enableBlend();
		Draw9Patch(matrices, imgX, imgY, imgWdt, imgHgt, patch);

		for (var l : lines){
			DrawStyledText(matrices, l, textX, textY, patch);
			textY += textRenderer.fontHeight;
		}

		return anchorY + imgHgt + patch.paddingVertical;
	}

	private static int[] x=new int[4], y=new int[4];
	public void	Draw9Patch(MatrixStack matrices, int originX, int originY, int totalW, int totalH, GuiSpriteInfo patch) {
		GuiSpriteInfo.GetPatchPositions(x, originX, totalW, patch.patch.left(), patch.patch.right ());
		GuiSpriteInfo.GetPatchPositions(y, originY, totalH, patch.patch.top (), patch.patch.bottom());
		float[] u = patch.u;
		float[] v = patch.v;

		for (int tileX=0; tileX<3; ++tileX)
		for (int tileY=0; tileY<3; ++tileY)
		if  (x[tileX]<x[tileX+1] && y[tileY]<y[tileY+1])
		{
			IDrawableHelperMixin.callDrawTexturedQuad(
				matrices.peek().getPositionMatrix(),
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

	private void	DrawStyledText(MatrixStack matrices, OrderedText text, int x, int y, GuiSpriteInfo style){
		var m = matrices.peek().getPositionMatrix();
		int light = LightmapTextureManager.MAX_LIGHT_COORDINATE;
		var vProv = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());

		if (0 != (0xff000000 & style.outerlineColour)){
			textRenderer.drawWithOutline(text, x-1, y-1, style.outlineColour, style.outerlineColour, m, vProv, light);
			textRenderer.drawWithOutline(text, x-1, y+1, style.outlineColour, style.outerlineColour, m, vProv, light);
			textRenderer.drawWithOutline(text, x+1, y-1, style.outlineColour, style.outerlineColour, m, vProv, light);
			textRenderer.drawWithOutline(text, x+1, y+1, style.outlineColour, style.outerlineColour, m, vProv, light);
		}
		if (0 != (0xff000000 & style.outlineColour))
			textRenderer.drawWithOutline(text, x, y, style.textColour, style.outlineColour, m, vProv, light);
		textRenderer.draw(text, x, y, style.textColour, style.textShadow, m, vProv, TextLayerType.NORMAL, 0x0, light);
		vProv.draw();
	}
}
