package tk.estecka.backburner;

import java.util.ArrayList;
import java.util.List;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

/**
 * Keeps a text along with a string pre-encoded version of it.
 * When editing an entry, this makes it possible to give the user the exact same
 * value they entered, which will be more user-friendly than re-encoding the
 * text into full-blown NBT every time.
 */
public record BacklogEntry(
	String rawString,
	Component displayText
){
	public BacklogEntry(String raw){
		this(raw, TextFromRaw(raw));
	}

	public BacklogEntry(Component text){
		this(RawFromText(text), text);
	}


/******************************************************************************/
/* # Codec                                                                    */
/******************************************************************************/

	static public final Codec<Component> ELT_CODEC = Codec.withAlternative(
		ComponentSerialization.CODEC,
		Codec.STRING.xmap(Component::literal, Component::getString)
	);
	static public final Codec<Component> ARRAY_CODEC = ELT_CODEC.listOf().xmap(BacklogEntry::combine, Component::getSiblings);
	static public final Codec<Component> CODEC = Codec.withAlternative(ARRAY_CODEC, ELT_CODEC);

	private static MutableComponent combine(List<Component> texts) {
		MutableComponent mutableText = texts.get(0).copy();
		for (int i = 1; i < texts.size(); ++i) {
			mutableText.append(texts.get(i));
		}
		return mutableText;
	}

	static public Component TextFromRaw(String raw){
		final TagParser READER = new TagParser();
		Tag nbt;
		try {
			nbt = (Tag) READER.parseFully(raw);
		}
		catch (CommandSyntaxException e){
			return Component.literal(raw);
		}

		if (nbt instanceof ListTag nbtList)
		{
			List<Component> siblings = new ArrayList<>();
			for (Tag subNbt : nbtList)
				siblings.add(TextFromNbt(subNbt, "<error>"));
			return combine(siblings);
		}
		else
			return TextFromNbt(nbt, raw);
	}

	static public Component TextFromNbt(Tag nbt, String fallback){
		var result = CODEC.parse(NbtOps.INSTANCE, nbt);
		if (result.isError())
			return Component.literal(fallback);
		else
			return result.getOrThrow();
	}

	static public String RawFromText(Component text){
		return NbtOps.INSTANCE.withEncoder(ComponentSerialization.CODEC).apply(text).getOrThrow().toString();
	}
}
