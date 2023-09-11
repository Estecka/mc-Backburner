package tk.estecka.backburner;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import java.util.concurrent.CompletableFuture;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;

public class BacklogCommands
{
	static public final Identifier ID = new Identifier("backburner", "stack");

	static public final String BOOL_ARG  = "bool";
	static public final String INDEX_ARG = "index";
	static public final String OFFSET_ARG = "offset";
	static public final String VALUE_ARG = "text";
	static public final String SRC_ARG = "from";
	static public final String DST_ARG = "to";

	static private final Text ADDED_FEEDBACK   = Text.literal("Added: ").formatted(Formatting.BOLD).formatted(Formatting.AQUA);
	static private final Text REMOVED_FEEDBACK = Text.literal("Removed: ").formatted(Formatting.BOLD).formatted(Formatting.GOLD);
	
	static public void	Register(){
		ClientCommandRegistrationCallback.EVENT.register(ID, BacklogCommands::RegisterWith);
	}

	static public void	RegisterWith(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess){
		var root = literal("note");

		// root.executes(BacklogCommand::Root);

		root.then(literal("reload")
			.executes(BacklogCommands::Reload)
		);
		root.then(literal("save")
			.executes(BacklogCommands::Save)
		);


		root.then(literal("insert")
			.then(argument(INDEX_ARG, integer(0))
				.then(argument(VALUE_ARG, greedyString())
					.executes(BacklogCommands::Insert)
				)
			)
		);
		// root.then(literal("push")
		// 	.then(argument(INDEX_ARG, integer(0))
		// 		.then(argument(VALUE_ARG, greedyString())
		// 			.executes(BacklogCommand::Insert)
		// 		)
		// 	)
		// );
		root.then(literal("push")
			.then(argument(VALUE_ARG, greedyString())
				.executes(BacklogCommands::Push)
			)
		);
		root.then(literal("queue")
			.then(argument(VALUE_ARG, greedyString())
				.executes(BacklogCommands::Enqueue)
			)
		);

		root.then(literal("remove")
			.then(argument(INDEX_ARG, integer(0))
				.suggests(BacklogCommands::IndexAutofill)
				.executes(BacklogCommands::Remove)
			)
		);
		root.then(literal("pop")
			.then(argument(INDEX_ARG, integer(0))
				.suggests(BacklogCommands::IndexAutofill)
				.executes(BacklogCommands::Remove)
			)
		);
		root.then(literal("pop")
			.executes(BacklogCommands::Pop)
		);
		root.then(literal("shift")
			.executes(BacklogCommands::Shift)
		);

		// root.then(argument(VALUE_ARG, greedyString())
		// 	.executes(BacklogCommand::Push)
		// );

		root.then(literal("hide")
			.executes(BacklogCommands::HideToogle)
		);
		root.then(literal("hide")
			.then(argument(BOOL_ARG, bool())
				.executes(BacklogCommands::Hide)
			)
		);

		root.then(literal("bump")
			.then(argument(INDEX_ARG, integer(0))
				.suggests(BacklogCommands::IndexAutofill)
				.executes(BacklogCommands::Bump)
			)
		);
		root.then(literal("bump")
			.then(argument(INDEX_ARG, integer(0))
				.suggests(BacklogCommands::IndexAutofill)
				.then(argument(OFFSET_ARG, integer())
					.executes(BacklogCommands::BumpOffset)
				)
			)
		);
		root.then(literal("move")
			.then(argument(SRC_ARG, integer(0))
				.suggests(BacklogCommands::IndexAutofill)
				.then(argument(DST_ARG, integer(0))
					.executes(BacklogCommands::Move)
				)
			)
		);
		root.then(literal("edit")
			.then(argument(INDEX_ARG, integer(0))
				.suggests(BacklogCommands::EntryAutofill)
				.then(argument(VALUE_ARG, greedyString())
					.executes(BacklogCommands::Set)
					.suggests(BacklogCommands::ValueAutofill)
				)
			)
		);

		dispatcher.register(root);
	}


/******************************************************************************/
/* # Autofill                                                                 */
/******************************************************************************/
	
	static private CompletableFuture<Suggestions> EntryAutofill(final CommandContext<FabricClientCommandSource> context, final SuggestionsBuilder builder){
		final var items = BacklogData.instance.content;
		for (int i=0; i<items.size(); i++)
			builder.suggest(String.format("%d %s", i, items.get(i)));
		return builder.buildFuture();
	}
	
	static private CompletableFuture<Suggestions> IndexAutofill(final CommandContext<FabricClientCommandSource> context, final SuggestionsBuilder builder){
		final var items = BacklogData.instance.content;
		for (int i=0; i<items.size(); i++)
			builder.suggest(i, new LiteralMessage(items.get(i)));
		return builder.buildFuture();
	}

	static private CompletableFuture<Suggestions> ValueAutofill(final CommandContext<FabricClientCommandSource> context, final SuggestionsBuilder builder){
		final var items = BacklogData.instance.content;
		int i = getInteger(context, INDEX_ARG);
		if (0 <= i && i < items.size())
			builder.suggest(items.get(i));
		return builder.buildFuture();
	}


/******************************************************************************/
/* # Command Handlers                                                         */
/******************************************************************************/

	// static private int	Root(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
	// 	context.getSource().sendFeedback(Text.literal("Main"));
	// 	return 0;
	// }

	static private int	Reload(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		return BacklogData.Reload() ? 1 : -1;
	}

	static private int	Save(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		return BacklogData.TrySave() ? 1 : -1;
	}

	static private int	Push(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		return Insert(context, 0, getString(context, VALUE_ARG));
	}

	static private int	Pop(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		return Remove(context, 0);
	}

	static private int	Enqueue(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		return Insert(context, BacklogData.instance.content.size(), getString(context, VALUE_ARG));
	}

	static private int	Shift(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		return Remove(context, BacklogData.instance.content.size()-1);
	}

	static private int	Insert(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		return Insert(context, getInteger(context, INDEX_ARG), getString(context, VALUE_ARG));
	}

	static private int	Remove(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		return Remove(context, getInteger(context, INDEX_ARG));
	}

	static private int	Hide(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		BacklogHud.isHidden = getBool(context, BOOL_ARG);
		return 1;
	}

	static private int	HideToogle(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		BacklogHud.isHidden = !BacklogHud.isHidden;
		return 1;
	}

	static private int	Bump(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		int i = getInteger(context, INDEX_ARG);
		return Move(context, i, i-1);
	}

	static private int	BumpOffset(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		int i = getInteger(context, INDEX_ARG);
		return Move(context, i, i-getInteger(context, OFFSET_ARG));
	}

	static private int	Move(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		return Move(context, getInteger(context, SRC_ARG), getInteger(context, DST_ARG));
	}

	static private int	Set(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		return Set(context, getInteger(context, INDEX_ARG), getString(context, VALUE_ARG));
	}

/******************************************************************************/
/* # Command Logic                                                            */
/******************************************************************************/

	static private void	PrintEntry(CommandContext<FabricClientCommandSource> context, Text prefix, int index, String value) {
		var msg = MutableText.of(Text.empty().getContent())
			.append(prefix)
			.append(Text.literal(String.format("#%d ", index)).formatted(Formatting.YELLOW))
			.append(Text.literal(value))
		;
		context.getSource().sendFeedback(msg);
	}

	static int	Insert(CommandContext<FabricClientCommandSource> context, int index, String value){
		final var items = BacklogData.instance.content;
		if (index < 0){
			context.getSource().sendError(Text.literal(String.format("%d is an invalid index. Pushing item to the front.", index)));
			index = 0;
		}
		
		if (index > items.size()){
			context.getSource().sendError(Text.literal(String.format("%d is out of bound. Pushing item to the back.", index)));
			index = items.size();
		}

		PrintEntry(context, ADDED_FEEDBACK, index, value);
		items.add(index, value);
		BacklogData.TrySave();
		return 1;
	}

	static int	Set(CommandContext<FabricClientCommandSource> context, int index, String value){
		final var items = BacklogData.instance.content;
		if (items.isEmpty()){
			context.getSource().sendError(Text.literal("Nothing to edit."));
			return 0;
		}

		if (index < 0 || index > items.size()-1){
			context.getSource().sendError(Text.literal(String.format("Index %d out of bounds.", index)));
			return -1;
		}

		PrintEntry(context, REMOVED_FEEDBACK, index, items.get(index));
		items.remove(index);
		PrintEntry(context, ADDED_FEEDBACK, index, value);
		items.add(index, value);
		BacklogData.TrySave();
		return 1;
	}

	static int	Remove(CommandContext<FabricClientCommandSource> context, int index){
		final var items = BacklogData.instance.content;
		if (items.isEmpty()){
			context.getSource().sendError(Text.literal("Nothing to remove."));
			return 0;
		}
		
		if (index < 0 || index > items.size()){
			context.getSource().sendError(Text.literal(String.format("Index %d out of bounds. Max %d.", index, items.size()-1)));
			return -1;
		}
		

		PrintEntry(context, REMOVED_FEEDBACK, index, items.get(index));
		items.remove(index);
		BacklogData.TrySave();
		return 1;
	}

	static int	Move(CommandContext<FabricClientCommandSource> context, int src, int dst){
		final var items = BacklogData.instance.content;

		if (src < 0 || src > items.size()-1){
			context.getSource().sendError(Text.literal(String.format("Index %d out of bounds. Max %d", src, items.size()-1)));
			return -1;
		}

		if (dst<0)
			dst=0;
		else if (dst > items.size()-1)
			dst = items.size()-1;

		if (src == dst)
			return 0;

		String value = items.get(src);

		
		items.remove(src);
		items.add(dst, value);
		BacklogData.TrySave();
		return 1;
	}

}
