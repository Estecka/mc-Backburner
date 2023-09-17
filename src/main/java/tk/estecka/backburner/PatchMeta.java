package tk.estecka.backburner;

public class PatchMeta 
{
	static public record Size(int width, int height) {}
	static public record Margin(int left, int top, int right, int bottom) {}

	public Size	base = new Size(64, 16);

	public Margin padding   = new Margin(0, 0, 0, 0);
	public Margin ninepatch = new Margin(0, 0, 0, 0);
	public Margin textarea  = new Margin(0, 0, 0, 0);
}
