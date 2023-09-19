package tk.estecka.backburner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadataReader;

public class PatchMeta 
{
	static public record Size(int width, int height, boolean fill) {}
	static public record Margin(int left, int top, int right, int bottom) {}
	static public record Colour(String colour, String outline, String outerline, boolean shadow) {}

	static private final Gson gson = new Gson();
	static public final ResourceMetadataReader<Size>   BASE_READER     = getReader(new TypeToken<Size>  (){}, "base"     );
	static public final ResourceMetadataReader<Margin> PATCH_READER    = getReader(new TypeToken<Margin>(){}, "ninepatch");
	static public final ResourceMetadataReader<Margin> PADDING_READER  = getReader(new TypeToken<Margin>(){}, "padding"  );
	static public final ResourceMetadataReader<Margin> TEXTAREA_READER = getReader(new TypeToken<Margin>(){}, "textarea" );
	static public final ResourceMetadataReader<Colour> COLOUR_READER   = getReader(new TypeToken<Colour>(){}, "text"     );
	static private final PatchMeta DEFAULT = new PatchMeta();

	public Size	base = new Size(16, 16, false);
	public Margin padding   = new Margin(0, 0, 0, 0);
	public Margin ninepatch = new Margin(0, 0, 0, 0);
	public Margin textarea  = new Margin(0, 0, 0, 0);
	public Colour text      = new Colour("#ff000000", "#0000000", "#00000000", true);

	static public PatchMeta	Decode(ResourceMetadata meta){
		var r = new PatchMeta();
		r.base      = meta.decode(BASE_READER)    .orElse(DEFAULT.base);
		r.ninepatch = meta.decode(PATCH_READER)   .orElse(DEFAULT.ninepatch);
		r.padding   = meta.decode(PADDING_READER) .orElse(DEFAULT.padding);
		r.textarea  = meta.decode(TEXTAREA_READER).orElse(DEFAULT.textarea);
		r.text      = meta.decode(COLOUR_READER)  .orElse(DEFAULT.text);
		return r;
	}

	static public final <T> ResourceMetadataReader<T> getReader(TypeToken<T> type, String key) {
		return new ResourceMetadataReader<T>() {
			public String	getKey(){ return key; }
			public T	fromJson(JsonObject json){ return gson.fromJson(json, type); }
		};
	};

	public String	toString(){
		return "( "+base+", "+padding+", "+ninepatch+", "+textarea+" )";
	}

}
