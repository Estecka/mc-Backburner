package tk.estecka.backburner;

import java.util.Collection;
import java.util.function.Supplier;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

public class IndexArgumentType
implements ArgumentType<Integer>
{
	private final Supplier<Integer> last;

	public IndexArgumentType(Supplier<Integer> lastIndex){
		this.last = lastIndex;
	}
	
	static public IndexArgumentType index(Supplier<Integer> lastIndex){
		return new IndexArgumentType(lastIndex);
	}

	private IntegerArgumentType AsIntArg(){
		return IntegerArgumentType.integer(0, last.get());
	}

	@Override
	public Integer parse(StringReader reader) throws CommandSyntaxException {
		try {
			return this.AsIntArg().parse(reader);
		}
		catch (CommandSyntaxException e) {
			int start = reader.getCursor();
			String value = reader.readString();

			if (value.equals("first"))
				return 0;
			if (value.equals("last"))
				return last.get();

			reader.setCursor(start);
			throw e;
		}
	}

	@Override
	public Collection<String> getExamples(){
		return this.AsIntArg().getExamples();
	}
}
