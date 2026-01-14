package tk.estecka.backburner;

import java.util.ArrayList;
import java.util.List;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

/**
 * Keeps a text along with a string pre-encoded version of it.
 * When editing an entry, this makes it possible to give the user the exact same
 * value they entered, which will be more user-friendly than re-encoding the
 * text into full-blown NBT every time.
 */
public record BacklogEntry(
	String rawString,
	Text displayText
){
	public BacklogEntry(String raw){
		this(raw, TextFromRaw(raw));
	}

	public BacklogEntry(Text text){
		this(RawFromText(text), text);
	}


/******************************************************************************/
/* # Codec                                                                    */
/******************************************************************************/

	static public final Codec<Text> ELT_CODEC = Codec.withAlternative(
		TextCodecs.CODEC,
		Codec.STRING.xmap(Text::literal, Text::getString)
	);
	static public final Codec<Text> ARRAY_CODEC = ELT_CODEC.listOf().xmap(BacklogEntry::combine, Text::getSiblings);
	static public final Codec<Text> CODEC = Codec.withAlternative(ARRAY_CODEC, ELT_CODEC);

	private static MutableText combine(List<Text> texts) {
		MutableText mutableText = texts.get(0).copy();
		for (int i = 1; i < texts.size(); ++i) {
			mutableText.append(texts.get(i));
		}
		return mutableText;
	}

	static public Text TextFromRaw(String raw){
		final StringNbtReader READER = new StringNbtReader(new StringReader(raw));
		NbtElement nbt;
		try {
			nbt = READER.parseElement();
		}
		catch (CommandSyntaxException e){
			return Text.literal(raw);
		}

		if (nbt instanceof NbtList nbtList)
		{
			List<Text> siblings = new ArrayList<>();
			for (NbtElement subNbt : nbtList)
				siblings.add(TextFromNbt(subNbt, "<error>"));
			return combine(siblings);
		}
		else
			return TextFromNbt(nbt, raw);
	}

	static public Text TextFromNbt(NbtElement nbt, String fallback){
		var result = CODEC.parse(NbtOps.INSTANCE, nbt);
		if (result.isError())
			return Text.literal(fallback);
		else
			return result.getOrThrow();
	}

	static public String RawFromText(Text text){
		return NbtOps.INSTANCE.withEncoder(TextCodecs.CODEC).apply(text).getOrThrow().toString();
	}
}
