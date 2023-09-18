package tk.estecka.backburner;

import java.util.OptionalInt;
import org.jetbrains.annotations.Nullable;
import com.google.common.primitives.UnsignedInts;

public class PatchInfo {
	static public final PatchInfo DEFAULT = new PatchInfo(new PatchMeta());

	/**
	 * The base size of the texture.
	 */
	public final int baseWidth, baseHeight;
	public final PatchMeta.Margin padding;
	/**
	 * The total size of the padding on each axis.
	 */
	public final int paddingVertical, paddingHorizontal;
	/**
	 * The minimum size of the GUI element, when containing no text.
	 */
	public final int minWidth, minHeight;
	/**
	 * The position of the textArea, from the GUI element's origin.
	 */
	public final int textX, textY;
	public final PatchMeta.Margin patch;
	/**
	 * The four positions of the patch's slices on the texture.
	 * Normalized between 0 and 1.
	 */
	public final float[] u, v;
	public final int textColour, outlineColour, outerlineColour;
	public final boolean textShadow;

	public PatchInfo(PatchMeta mcmeta){
		this.baseWidth  = mcmeta.base.width ();
		this.baseHeight = mcmeta.base.height();

		this.textX = mcmeta.textarea.left();
		this.textY = mcmeta.textarea.top ();

		this.minWidth  = mcmeta.textarea.left() + mcmeta.textarea.right ();
		this.minHeight = mcmeta.textarea.top () + mcmeta.textarea.bottom();

		this.padding = mcmeta.padding;
		this.paddingHorizontal = padding.left() + padding.right ();
		this.paddingVertical   = padding.top () + padding.bottom();

		this.patch = mcmeta.ninepatch;
		int[] uInt = GetPatchPositions(new int[4], 0, baseWidth,  patch.left(), patch.right ());
		int[] vInt = GetPatchPositions(new int[4], 0, baseHeight, patch.top (), patch.bottom());
		this.u = new float[4];
		this.v = new float[4];
		for (int i=0; i<4; ++i){
			u[i] = uInt[i] / (float)baseWidth;
			v[i] = vInt[i] / (float)baseHeight;
		}

		this.textColour      = colourOf(mcmeta.text.colour())   .orElse(0xffffffff);
		this.outlineColour   = colourOf(mcmeta.text.outline())  .orElse(0x0);
		this.outerlineColour = colourOf(mcmeta.text.outerline()).orElse(0x0);
		this.textShadow = mcmeta.text.shadow();
	}

	static private OptionalInt	colourOf(@Nullable String hex){
		if (hex == null)
			return OptionalInt.empty();
		try {
			return OptionalInt.of(UnsignedInts.parseUnsignedInt(hex.substring(1), 16));
		}
		catch (NumberFormatException e){
			Backburner.LOGGER.error("Invalid color code: {}\n{}", hex, e);
			return OptionalInt.empty();
		}
	}

	/**
	 * Computes the positions of the all patches on a single axis.
	 * This implementation is naive, it does not handle low sizes in any special
	 * way.
	 * @param origin
	 * @param totalSize
	 * @param firstBorder The size of the patch border closest to the origin.
	 * @param secondBorder The size of the patch border farthest from the origin.
	 * @param result Outputs the result in a pre-allocated array. Must be at least 4 in length.
	 * @return `result`
	 */
	static public int[]	GetPatchPositions(int[] result, int origin, int totalSize, int firstBorder, int secondBorder){
		result[0] = origin;
		result[1] = origin + firstBorder;
		result[2] = origin + totalSize - secondBorder;
		result[3] = origin + totalSize;
		return result;
	}
}
