package tk.estecka.backburner;

public class PatchInfo {
	static public final PatchInfo DEFAULT = new PatchInfo(new PatchMeta());

	/**
	 * The base size of the texture.
	 */
	public final int baseWidth, baseHeight;
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
	public final boolean[][] hasPatch;

	public PatchInfo(PatchMeta mcmeta){
		this.baseWidth  = mcmeta.base.width ();
		this.baseHeight = mcmeta.base.height();

		this.textX = mcmeta.textarea.left();
		this.textY = mcmeta.textarea.top ();

		this.minWidth  = mcmeta.textarea.left() + mcmeta.textarea.right ();
		this.minHeight = mcmeta.textarea.top () + mcmeta.textarea.bottom();

		this.patch = mcmeta.ninepatch;

		int[] uInt = GetPatchPositions(new int[4], 0, baseWidth,  mcmeta.ninepatch.left(), mcmeta.ninepatch.right ());
		int[] vInt = GetPatchPositions(new int[4], 0, baseHeight, mcmeta.ninepatch.top (), mcmeta.ninepatch.bottom());
		this.u = new float[4];
		this.v = new float[4];
		for (int i=0; i<4; ++i){
			u[i] = uInt[i] / (float)baseWidth;
			v[i] = vInt[i] / (float)baseHeight;
		}

		this.hasPatch = new boolean[3][3];
		for (int x=0; x<3; ++x)
		for (int y=0; y<3; ++y) {
			this.hasPatch[x][y] = (x==1) || (y==1) || (u[x]<u[x+1]) && (v[y]<v[y+1]);
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
		result[2] = origin + totalSize - secondBorder;
		result[3] = origin + totalSize;
		return result;
	}
}
