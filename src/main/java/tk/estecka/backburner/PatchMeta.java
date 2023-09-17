package tk.estecka.backburner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadataReader;

public class PatchMeta 
{
	static public record Size(int width, int height) {}
	static public record Margin(int left, int top, int right, int bottom) {}

	static private final Gson gson = new Gson();
	static public final ResourceMetadataReader<Size>   BASE_READER    = getReader(new TypeToken<Size>  (){}, "base"     );
	static public final ResourceMetadataReader<Margin> PATCH_READER   = getReader(new TypeToken<Margin>(){}, "ninepatch");
	static public final ResourceMetadataReader<Margin> PADDING_READER = getReader(new TypeToken<Margin>(){}, "margin"   );
	static public final ResourceMetadataReader<Margin> TEXT_READER    = getReader(new TypeToken<Margin>(){}, "textarea" );
	static private final PatchMeta DEFAULT = new PatchMeta();

	public Size	base = new Size(64, 16);
	public Margin padding   = new Margin(0, 0, 0, 0);
	public Margin ninepatch = new Margin(0, 0, 0, 0);
	public Margin textarea  = new Margin(0, 0, 0, 0);

	static public PatchMeta	Decode(ResourceMetadata meta){
		var r = new PatchMeta();
		r.base      = meta.decode(BASE_READER)   .orElse(DEFAULT.base);
		r.ninepatch = meta.decode(PATCH_READER)  .orElse(DEFAULT.ninepatch);
		r.padding   = meta.decode(PADDING_READER).orElse(DEFAULT.padding);
		r.textarea  = meta.decode(TEXT_READER)   .orElse(DEFAULT.textarea);
		return r;
	}

	static public final <T> ResourceMetadataReader<T> getReader(TypeToken<T> type, String key) {
		return new ResourceMetadataReader<T>() {
			public String	getKey(){ return key; }
			public T	fromJson(JsonObject json){ return gson.fromJson(json, type); }
		};
	};

}
