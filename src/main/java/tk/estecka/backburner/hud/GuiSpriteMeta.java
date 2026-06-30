package tk.estecka.backburner.hud;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;

public class GuiSpriteMeta
{
	static public record Basis(int width, int height, boolean fill) {
		static public final Basis DEFAULT = new Basis(16, 16, false);
		static public final Codec<Basis> CODEC = RecordCodecBuilder.create(i->i.group(
				Codec.INT.fieldOf("width").orElse(DEFAULT.width).forGetter(Basis::width),
				Codec.INT.fieldOf("height").orElse(DEFAULT.height).forGetter(Basis::height),
				Codec.BOOL.fieldOf("fill").orElse(DEFAULT.fill).forGetter(Basis::fill)
			).apply(i, Basis::new)
		);
	}
	static public record Margin(int left, int top, int right, int bottom) {
		static public final Margin DEFAULT = new Margin(0, 0, 0, 0);
		static public final Codec<Margin> CODEC = RecordCodecBuilder.create(i->i.group(
				Codec.INT.fieldOf("left").orElse(DEFAULT.left).orElse(0).forGetter(Margin::left),
				Codec.INT.fieldOf("top").orElse(DEFAULT.top).orElse(0).forGetter(Margin::top),
				Codec.INT.fieldOf("right").orElse(DEFAULT.right).orElse(0).forGetter(Margin::right),
				Codec.INT.fieldOf("bottom").orElse(DEFAULT.bottom).orElse(0).forGetter(Margin::bottom)
			).apply(i, Margin::new)
		);
	}
	static public record Colour(String colour, String outline, String outerline, boolean shadow) {
		static public final Colour DEFAULT = new Colour("#ff000000", "#00000000", "#00000000", false);
		static public final Codec<Colour> CODEC = RecordCodecBuilder.create(i->i.group(
				Codec.STRING.fieldOf("colour").orElse(DEFAULT.colour).forGetter(Colour::colour),
				Codec.STRING.fieldOf("outline").orElse(DEFAULT.outline).forGetter(Colour::outline),
				Codec.STRING.fieldOf("outerline").orElse(DEFAULT.outerline).forGetter(Colour::outerline),
				Codec.BOOL.fieldOf("shadow").orElse(DEFAULT.shadow).forGetter(Colour::shadow)
			).apply(i, Colour::new)
		);
	}

	static public final ResourceMetadataSerializer<Basis>  BASIS_READER    = new ResourceMetadataSerializer<Basis> ("basis"    , Basis.CODEC);
	static public final ResourceMetadataSerializer<Margin> PATCH_READER    = new ResourceMetadataSerializer<Margin>("ninepatch", Margin.CODEC);
	static public final ResourceMetadataSerializer<Margin> PADDING_READER  = new ResourceMetadataSerializer<Margin>("padding"  , Margin.CODEC);
	static public final ResourceMetadataSerializer<Margin> TEXTAREA_READER = new ResourceMetadataSerializer<Margin>("textarea" , Margin.CODEC);
	static public final ResourceMetadataSerializer<Colour> COLOUR_READER   = new ResourceMetadataSerializer<Colour>("text"     , Colour.CODEC);

	public Basis basis = Basis.DEFAULT;
	public Margin padding = Margin.DEFAULT;
	public Margin ninepatch = Margin.DEFAULT;
	public Margin textarea = Margin.DEFAULT;
	public Colour text = Colour.DEFAULT;

	static public GuiSpriteMeta	Decode(ResourceMetadata meta){
		var r = new GuiSpriteMeta();
		r.basis     = meta.decode(BASIS_READER)   .orElse(Basis.DEFAULT);
		r.ninepatch = meta.decode(PATCH_READER)   .orElse(Margin.DEFAULT);
		r.padding   = meta.decode(PADDING_READER) .orElse(Margin.DEFAULT);
		r.textarea  = meta.decode(TEXTAREA_READER).orElse(Margin.DEFAULT);
		r.text      = meta.decode(COLOUR_READER)  .orElse(Colour.DEFAULT);
		return r;
	}

	public String	toString(){
		return "( "+basis+", "+padding+", "+ninepatch+", "+textarea+" )";
	}

}
