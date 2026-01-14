package tk.estecka.backburner.hud;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joml.Matrix3x2fStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import tk.estecka.backburner.Backburner;
import tk.estecka.backburner.BacklogData;
import tk.estecka.backburner.BacklogEntry;

import static tk.estecka.backburner.Backburner.CONFIG;

public class BacklogHud 
{
	static public final Map<Identifier,GuiSpriteInfo> sprites = new HashMap<Identifier,GuiSpriteInfo>();
	static private final Identifier ICON_ID   = Identifier.of(Backburner.MODID, "textures/gui/backlog/icon.png"  );
	static private final Identifier HEADER_ID = Identifier.of(Backburner.MODID, "textures/gui/backlog/header.png");
	static private final Identifier ITEM_ID   = Identifier.of(Backburner.MODID, "textures/gui/backlog/item.png"  );
	static private final MutableText HEADER_TITLE = Text.translatable("backburner.header.title");

	static public boolean isHidden = false;

	private final MinecraftClient client;
	private final TextRenderer textRenderer;

	public BacklogHud(){
		this.client = MinecraftClient.getInstance();
		this.textRenderer = client.textRenderer;
	}

	public void	Render(DrawContext context){
		final List<BacklogEntry> items;
		if (BacklogData.instance==null || (items=BacklogData.instance.content) == null || items.isEmpty())
			return;

		final int guiScale = (int)client.getWindow().getScaleFactor();
		float effectiveScale = guiScale * CONFIG.hudScale;
		if (!CONFIG.allowFractional)
			effectiveScale = Math.max(1, Math.round(effectiveScale));

		float effectiveMultiplier = effectiveScale / (float)guiScale;
		Matrix3x2fStack matrices = context.getMatrices();
		matrices.pushMatrix();
		matrices.scale(effectiveMultiplier, effectiveMultiplier);

		int x = (CONFIG.anchorX <= 0.5) ? CONFIG.hudX : -CONFIG.hudX;
		int y = CONFIG.hudY;
		x += CONFIG.anchorX * client.getWindow().getScaledWidth() / effectiveMultiplier;
		x -= CONFIG.anchorX * CONFIG.hudWdt;

		if (isHidden){
			GuiSpriteInfo patch = sprites.getOrDefault(ICON_ID, GuiSpriteInfo.DEFAULT);
			Draw9Patch( ICON_ID, context, x+patch.padding.left(), y+patch.padding.top(), patch.baseWidth, patch.baseHeight, patch );
		}
		else {
			y = DrawTextBox( context, x, y, HEADER_ID, HEADER_TITLE );
	
			for (int i=0; i<items.size(); i++)
				y = DrawTextBox( context, x, y, ITEM_ID, Text.literal(String.format("%d • ", i)).append(items.get(i).displayText()) );
		}

		matrices.popMatrix();
	}

	/**
	 * @return The y coordinate of the element's bottom
	 */
	private int	DrawTextBox(DrawContext context,int anchorX, int anchorY, Identifier sprite, StringVisitable text){
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
	public void	Draw9Patch(Identifier sprite, DrawContext context, int originX, int originY, int totalW, int totalH, GuiSpriteInfo patch) {
		GuiSpriteInfo.GetPatchPositions(x, originX, totalW, patch.patch.left(), patch.patch.right ());
		GuiSpriteInfo.GetPatchPositions(y, originY, totalH, patch.patch.top (), patch.patch.bottom());
		float[] u = patch.u;
		float[] v = patch.v;

		for (int tileX=0; tileX<3; ++tileX)
		for (int tileY=0; tileY<3; ++tileY)
		if  (x[tileX]<x[tileX+1] && y[tileY]<y[tileY+1])
		{
			context.drawTexturedQuad(
				sprite,
				x[tileX],   y[tileY],
				x[tileX+1], y[tileY+1],
				u[tileX], u[tileX+1],
				v[tileY], v[tileY+1]
			);
			// int debugColor = 0xff000000 + tileX*0x00550000 + tileY*0x00005500;
			// context.drawBorder(
			// 	x[tileX], y[tileY], 
			// 	x[tileX+1] - x[tileX],
			// 	y[tileY+1] - y[tileY],
			// 	debugColor
			// );
		}
	}

	private void	DrawStyledText(DrawContext context, OrderedText text, int x, int y, GuiSpriteInfo style)
	{
		if  (!style.textShadow && 0 != (0xff000000 & style.outerlineColour))
		for (int offX=-2; offX<=2; ++offX)
		for (int offY=-2; offY<=2; ++offY)
		if  (offX==2 || offX==-2 || offY==-2 || offY==2)
		{
			context.drawText(textRenderer, text, x+offX, y+offY, style.outerlineColour, false);
		}

		if  (!style.textShadow && 0 != (0xff000000 & style.outlineColour))
		for (int offX=-1; offX<=1; ++offX)
		for (int offY=-1; offY<=1; ++offY)
		if  (offX==1 || offX==-1 || offY==-1 || offY==1)
		{
			context.drawText(textRenderer, text, x+offX, y+offY, style.outlineColour, false);
		}

		context.drawText(textRenderer, text, x, y, style.textColour, style.textShadow);
	}
}
