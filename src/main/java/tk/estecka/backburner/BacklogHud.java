package tk.estecka.backburner;

import java.util.HashMap;
import java.util.Map;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import tk.estecka.backburner.mixin.IDrawableHelperMixin;

public class BacklogHud 
extends DrawableHelper
{
	static public final Map<Identifier,PatchInfo> patches = new HashMap<Identifier,PatchInfo>();
	static private final Identifier ICON_ID   = new Identifier(Backburner.MODID, "textures/gui/backlog/icon.png"  );
	static private final Identifier HEADER_ID = new Identifier(Backburner.MODID, "textures/gui/backlog/header.png");
	static private final Identifier ITEM_ID   = new Identifier(Backburner.MODID, "textures/gui/backlog/item.png"  );

	static public boolean isHidden = false;

	private final MinecraftClient client;
	private final TextRenderer textRenderer;

	static private final int maxWidth = 128;
	static private final int z = 0;

	public BacklogHud(MinecraftClient client){
		this.client = client;
		this.textRenderer = client.textRenderer;
	}

	public void	Render(MatrixStack matrices, float tickDelta){
		final var items = BacklogData.instance.content;
		if (items == null || items.isEmpty())
			return;

		int x = 9;
		int y = 32;

		if (isHidden){
			PatchInfo patch = patches.getOrDefault(ICON_ID, PatchInfo.DEFAULT);
			RenderSystem.setShaderTexture(0, ICON_ID);
			RenderSystem.enableBlend();
			Draw9Patch(matrices, x, y, patch.baseWidth, patch.baseHeight, patch);
			return;
		}
		
		y = DrawHeader(matrices, x, y, maxWidth, "Backlog");
		x+=2;

		for (int i=0; i<items.size(); i++){
			y = DrawItem(matrices, x, y+1, maxWidth, String.format("%d • %s", i, items.get(i)));
		}
	}

	/**
	 * @return The y coordinate of the element's bottom
	 */
	public int	DrawHeader(MatrixStack matrices, int anchorX, int anchorY, int elementWdt, String title){
		PatchInfo patch = patches.getOrDefault(HEADER_ID, PatchInfo.DEFAULT);
		RenderSystem.setShaderTexture(0, HEADER_ID);
		RenderSystem.enableBlend();
		int width  = patch.minWidth  + textRenderer.getWidth(title);
		int height = patch.minHeight + textRenderer.fontHeight;

		Draw9Patch(matrices, anchorX, anchorY, width, height, patch);
		drawTextWithShadow(matrices, textRenderer, title, anchorX+patch.textX , anchorY+patch.textY, 0xffffffbb);
		return anchorY + height;
	}

	/**
	 * @return The y coordinate of the element's bottom
	 */
	public int	DrawItem(MatrixStack matrices, int anchorX, int anchorY, int textWidth, String text){
		PatchInfo patch = patches.getOrDefault(ITEM_ID, PatchInfo.DEFAULT);
		RenderSystem.setShaderTexture(0, ITEM_ID);
		RenderSystem.enableBlend();

		var lines = textRenderer.wrapLines(Text.literal(text), textWidth);

		int totalWidth  = patch.minWidth  + textWidth;
		int totalHeight = patch.minHeight + (textRenderer.fontHeight * lines.size());

		Draw9Patch(matrices, anchorX, anchorY, totalWidth, totalHeight, patch);

		var vProv = VertexConsumerProvider.immediate(Tessellator.getInstance().getBuffer());
		var m = matrices.peek().getPositionMatrix();
		int outerlineColor = 0x440088ff;
		int outlineColor   = 0xbb0044bb;
		int light = LightmapTextureManager.MAX_LIGHT_COORDINATE;
		int textX = anchorX + patch.textX;
		int textY = anchorY + patch.textY;
		for (var l : lines){
			textRenderer.drawWithOutline(l, textX-1, textY-1, outlineColor, outerlineColor, m, vProv, light);
			textRenderer.drawWithOutline(l, textX-1, textY+1, outlineColor, outerlineColor, m, vProv, light);
			textRenderer.drawWithOutline(l, textX+1, textY-1, outlineColor, outerlineColor, m, vProv, light);
			textRenderer.drawWithOutline(l, textX+1, textY+1, outlineColor, outerlineColor, m, vProv, light);
			textRenderer.drawWithOutline(l, textX, textY, 0xffffffff, outlineColor, m, vProv, light);
			textY += textRenderer.fontHeight;
		}
		vProv.draw();

		return anchorY + totalHeight;
	}

	public void	Draw9Patch(MatrixStack matrices, int originX, int originY, int totalW, int totalH, PatchInfo patch) {
		int[] x = PatchInfo.GetPatchPositions(new int[4], originX, totalW, patch.patch.left(), patch.patch.right ());
		int[] y = PatchInfo.GetPatchPositions(new int[4], originY, totalH, patch.patch.top (), patch.patch.bottom());
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
}
