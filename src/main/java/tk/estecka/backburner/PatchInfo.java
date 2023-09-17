package tk.estecka.backburner;

public class PatchInfo {
	/**
	 * The base size of the texture.
	 */
	public final int regionWidth, regionHeight;

	/**
	 * The minimum size of the GUI element, when containing no text.
	 */
	public final int minWidth, minHeight;
	
	/**
	 * The position of the textArea, from the GUI element's origin.
	 */
	public final int textX, textY;

	/**
	 * The four positions of the patch's slices on the texture.
	 * Normalized between 0 and 1.
	 */
	public final float[] u, v;

	public PatchInfo(PatchMeta mcmeta){
		this.regionWidth  = mcmeta.base.width ();
		this.regionHeight = mcmeta.base.height();

		this.textX = mcmeta.textarea.left();
		this.textY = mcmeta.textarea.top ();

		this.minWidth  = mcmeta.textarea.left() + mcmeta.textarea.right ();
		this.minHeight = mcmeta.textarea.top () + mcmeta.textarea.bottom();

		int[] uInt = GetPatchPositions(new int[4], 0, regionWidth,  mcmeta.ninepatch.left(), mcmeta.ninepatch.right ());
		int[] vInt = GetPatchPositions(new int[4], 0, regionHeight, mcmeta.ninepatch.top (), mcmeta.ninepatch.bottom());
		this.u = new float[4];
		this.v = new float[4];
		for (int i=0; i<4; ++i){
			u[i] = uInt[i] / regionWidth;
			v[i] = vInt[i] / regionHeight;
		}
	}

	/**
	 * Computes the positions of the all patches on a single dimension.
	 * The implementation is naive, and does not handle low sizes in any special
	 * way.
	 * 
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
		result[1] = origin + totalSize - secondBorder;
		result[3] = origin + totalSize;
		return result;
	}
}
