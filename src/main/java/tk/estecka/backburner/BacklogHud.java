package tk.estecka.backburner;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import tk.estecka.backburner.mixin.IDrawContextMixin;

public class BacklogHud 
{
	static private final Identifier TEXTURE_ID = new Identifier("backburner", "textures/gui/backlog.png");
	static private final Rect2i ICON_REGION = new Rect2i(7, 8, 11, 10);

	static private final Rect2i HEADER_REGION = new Rect2i( 7, 26, 83, 15);
	static private final Rect2i HEADER_PATCH  = new Rect2i(20,  10, 61, 1);
	static private final Rect2i HEADER_TEXT   = new Rect2i(13,  2, 61,  9);

	static private final Rect2i ITEM_REGION = new Rect2i(6, 42, 87, 15);
	// static private final Rect2i ITEM_PATCH  = new Rect2i(3, 3, 81,  9);
	static private final Rect2i ITEM_PATCH  = new Rect2i(0, 0, 87, 15);
	static private final Rect2i ITEM_TEXT   = new Rect2i(3, 3, 81, 10);

	static public boolean isHidden = false;

	private final MinecraftClient client;
	private final TextRenderer textRenderer;

	static private final int maxWidth = 128;
	static private final int z = 0;

	public BacklogHud(MinecraftClient client){
		this.client = client;
		this.textRenderer = client.textRenderer;
	}

	public void	Render(DrawContext context, float tickDelta){
		final var items = BacklogData.instance.content;
		if (items == null || items.isEmpty())
			return;

		int x = 9;
		int y = 32;

		if (isHidden){
			RenderSystem.enableBlend();
			context.drawTexture(TEXTURE_ID, x, y, ICON_REGION.getX(), ICON_REGION.getY(), ICON_REGION.getWidth(), ICON_REGION.getHeight());
			return;
		}
		
		y = DrawHeader(context, x, y, maxWidth, "Backlog");
		x+=2;

		for (int i=0; i<items.size(); i++){
			y = DrawItem(context, x, y+1, maxWidth, String.format("%d • %s", i, items.get(i)));
		}
	}

	/**
	 * @return The y coordinate of the element's bottom
	 */
	public int	DrawHeader(DrawContext context, int anchorX, int anchorY, int elementWdt, String title){
		RenderSystem.setShaderTexture(0, TEXTURE_ID);
		RenderSystem.enableBlend();
		int width  = (HEADER_REGION.getWidth()  - HEADER_TEXT.getWidth() ) + textRenderer.getWidth(title);
		int height = (HEADER_REGION.getHeight() - HEADER_TEXT.getHeight()) + textRenderer.fontHeight;

		// drawTexture(matrices, anchorX, anchorY, HEADER_REGION.getX(), HEADER_REGION.getY(), HEADER_REGION.getWidth(), HEADER_REGION.getHeight());
		Draw9Patch(context, anchorX, anchorY, width, height, HEADER_REGION, HEADER_PATCH);
		context.drawTextWithShadow(textRenderer, title, anchorX+HEADER_TEXT.getX() , anchorY+HEADER_TEXT.getY(), 0xffffffbb);
		return anchorY + height;
	}

	/**
	 * @return The y coordinate of the element's bottom
	 */
	public int	DrawItem(DrawContext context, int anchorX, int anchorY, int textWidth, String text){
		RenderSystem.setShaderTexture(0, TEXTURE_ID);
		RenderSystem.enableBlend();

		var lines = textRenderer.wrapLines(Text.literal(text), textWidth);

		int totalWidth  = (ITEM_REGION.getWidth()  - ITEM_TEXT.getWidth() ) + textWidth;
		int totalHeight = (ITEM_REGION.getHeight() - ITEM_TEXT.getHeight()) + (textRenderer.fontHeight * lines.size());

		Draw9Patch(context, anchorX, anchorY, totalWidth, totalHeight, ITEM_REGION, ITEM_PATCH);

		var vProv = context.getVertexConsumers();
		var m = context.getMatrices().peek().getPositionMatrix();
		int outerlineColor = 0x440088ff;
		int outlineColor   = 0xbb0044bb;
		int light = LightmapTextureManager.MAX_LIGHT_COORDINATE;
		int textX = anchorX + ITEM_TEXT.getX();
		int textY = anchorY + ITEM_TEXT.getY();
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

	public void	Draw9Patch(DrawContext context, int baseX, int baseY, int totalW, int totalH, Rect2i region, Rect2i patch){
		IDrawContextMixin contextpp = (IDrawContextMixin)context;
		int[] x = new int[4];
		int[] y = new int[4];
		int[] u = new int[4];
		int[] v = new int[4];

		u[0] = region.getX();
		v[0] = region.getY();
		u[3] = region.getX() + region.getWidth();
		v[3] = region.getY() + region.getHeight();

		u[1] = region.getX() + patch.getX();
		v[1] = region.getY() + patch.getY();
		u[2] = u[1] + patch.getWidth();
		v[2] = v[1] + patch.getHeight();

		x[0] = baseX;
		y[0] = baseY;
		x[3] = baseX + totalW;
		y[3] = baseY + totalH;

		x[1] = baseX + patch.getX();
		y[1] = baseY + patch.getY();
		x[2] = x[3] - (u[3] - u[2]);
		y[2] = y[3] - (v[3] - v[2]);

		for (int tileX=0; tileX<3; ++tileX)
		for (int tileY=0; tileY<3; ++tileY) 
		{
			contextpp.callDrawTexturedQuad(
				TEXTURE_ID,
				x[tileX], x[tileX+1],
				y[tileY], y[tileY+1],
				z,
				u[tileX]/256f, u[tileX+1]/256f,
				v[tileY]/256f, v[tileY+1]/256f
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
