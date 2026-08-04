package tk.estecka.backburner;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import tk.estecka.backburner.config.EWriteAction;
import tk.estecka.backburner.hud.BacklogHud;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.Nullable;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.argument;
import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static tk.estecka.backburner.Backburner.CONFIG;
import static tk.estecka.backburner.IndexArgumentType.index;

public class BacklogCommands
{
	static public final Identifier ID = Identifier.fromNamespaceAndPath("backburner", "stack");

	static public final String ROOT_COMMAND = CONFIG.rootCommand;
	static public final String BOOL_ARG  = "bool";
	static public final String INDEX_ARG = "index";
	static public final String OFFSET_ARG = "offset";
	static public final String VALUE_ARG = "text";
	static public final String SRC_ARG = "from";
	static public final String DST_ARG = "to";

	static private final Component ADDED_FEEDBACK   = Component.literal("Added: ").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.AQUA);
	static private final Component REMOVED_FEEDBACK = Component.literal("Removed: ").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GOLD);

	static public void	Register(){
		ClientCommandRegistrationCallback.EVENT.register(ID, BacklogCommands::RegisterWith);
	}

	static private IndexArgumentType indexBeforeLast(){ return index(() -> BacklogData.instance.content.size() - 1 ); }
	static private IndexArgumentType indexAfterLast (){ return index(() -> BacklogData.instance.content.size()     ); }

	static public void	RegisterWith(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess){
		var root = literal(ROOT_COMMAND);

		// root.executes(BacklogCommand::Root);

		root.then(literal("reload")
			.executes(BacklogCommands::Reload)
		);
		root.then(literal("save")
			.executes(BacklogCommands::Save)
		);


		root.then(literal("insert")
			.then(argument(INDEX_ARG, indexAfterLast())
				.suggests(BacklogCommands::IndexAutofill)
				.then(argument(VALUE_ARG, greedyString())
					.executes(BacklogCommands::Insert)
				)
			)
		);
		root.then(literal("add")
			.then(argument(INDEX_ARG, indexAfterLast())
				.suggests(BacklogCommands::IndexAutofill)
				.then(argument(VALUE_ARG, greedyString())
					.executes(BacklogCommands::Insert)
				)
			)
		);

		root.then(literal("write")
			.then(argument(VALUE_ARG, greedyString())
				.executes(ctx-> CONFIG.writeAction==EWriteAction.BOTTOM ? Enqueue(ctx) : Push(ctx))
			)
		);
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
			.then(argument(INDEX_ARG, indexBeforeLast())
				.suggests(BacklogCommands::EntryAutofill)
				.executes(BacklogCommands::Remove)
				.then(argument(VALUE_ARG, greedyString())
					.executes(BacklogCommands::RemoveStrict)
				)
			)
		);
		root.then(literal("pop")
			.executes(BacklogCommands::Pop)
			.then(argument(INDEX_ARG, indexBeforeLast())
				.suggests(BacklogCommands::EntryAutofill)
				.executes(BacklogCommands::Remove)
				.then(argument(VALUE_ARG, greedyString())
					.executes(BacklogCommands::RemoveStrict)
				)
			)
		);
		root.then(literal("shift")
			.executes(BacklogCommands::Shift)
		);

		root.then(literal("hide")
			.executes(BacklogCommands::HideToogle)
			.then(argument(BOOL_ARG, bool())
				.executes(BacklogCommands::Hide)
			)
		);

		root.then(literal("bump")
			.then(argument(INDEX_ARG, indexBeforeLast())
				.suggests(BacklogCommands::IndexAutofill)
				.executes(BacklogCommands::Bump)
				.then(argument(OFFSET_ARG, integer())
					.executes(BacklogCommands::BumpOffset)
				)
			)
		);
		root.then(literal("move")
			.then(argument(SRC_ARG, indexBeforeLast())
				.suggests(BacklogCommands::IndexAutofill)
				.then(argument(DST_ARG, indexBeforeLast())
					.executes(BacklogCommands::Move)
					.suggests(BacklogCommands::IndexAutofill)
				)
			)
		);
		root.then(literal("edit")
			.then(argument(INDEX_ARG, indexBeforeLast())
				.suggests(BacklogCommands::EntryAutofill)
				.then(argument(VALUE_ARG, greedyString())
					.executes(BacklogCommands::Set)
					.suggests(BacklogCommands::ValueAutofill)
				)
			)
		);

		root.then(literal("clear")
			.executes(BacklogCommands::Clear)
		);

		dispatcher.register(root);
	}


/******************************************************************************/
/* # Autofill                                                                 */
/******************************************************************************/
	
	static private CompletableFuture<Suggestions> EntryAutofill(final CommandContext<FabricClientCommandSource> context, final SuggestionsBuilder builder){
		final var items = BacklogData.instance.content;
		for (int i=0; i<items.size(); i++)
			builder.suggest(String.format("%d %s", i, items.get(i).rawString()));
		return builder.buildFuture();
	}
	
	static private CompletableFuture<Suggestions> IndexAutofill(final CommandContext<FabricClientCommandSource> context, final SuggestionsBuilder builder){
		final var items = BacklogData.instance.content;
		final String input = builder.getRemaining();

		builder.suggest("first");
		builder.suggest("last");

		char first;
		if  (input.isEmpty() || (!input.isBlank() && ((first=input.charAt(0)) < 'a' || 'z' < first)))
		for (int i=0; i<items.size(); i++)
			builder.suggest(i, new LiteralMessage(items.get(i).displayText().getString()));

		return builder.buildFuture();
	}

	static private CompletableFuture<Suggestions> ValueAutofill(final CommandContext<FabricClientCommandSource> context, final SuggestionsBuilder builder){
		final var items = BacklogData.instance.content;
		int i = getInteger(context, INDEX_ARG);
		if (0 <= i && i < items.size())
				builder.suggest(items.get(i).rawString());
		return builder.buildFuture();
	}


/******************************************************************************/
/* # Command Handlers                                                         */
/******************************************************************************/

	// static private int	Root(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
	// 	context.getSource().sendFeedback(Component.literal("Main"));
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
		return Remove(context, 0, null);
	}

	static private int	Enqueue(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		return Insert(context, BacklogData.instance.content.size(), getString(context, VALUE_ARG));
	}

	static private int	Shift(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		return Remove(context, BacklogData.instance.content.size()-1, null);
	}

	static private int	Insert(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		return Insert(context, getInteger(context, INDEX_ARG), getString(context, VALUE_ARG));
	}

	static private int	Remove(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		return Remove(context, getInteger(context, INDEX_ARG), null);
	}

	static private int	RemoveStrict(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
		return Remove(context, getInteger(context, INDEX_ARG), getString(context, VALUE_ARG));
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

	static private void	PrintEntry(CommandContext<FabricClientCommandSource> context, Component prefix, int index, Component value) {
		var msg = MutableComponent.create(Component.empty().getContents())
			.append(prefix)
			.append(Component.literal(String.format("#%d ", index)).withStyle(ChatFormatting.YELLOW))
			.append(value)
		;
		context.getSource().sendFeedback(msg);
	}

	static int	Insert(CommandContext<FabricClientCommandSource> context, int index, String rawValue){
		final var items = BacklogData.instance.content;
		if (index < 0){
			context.getSource().sendError(Component.literal(String.format("%d is an invalid index. Pushing item to the front.", index)));
			index = 0;
		}
		
		if (index > items.size()){
			context.getSource().sendError(Component.literal(String.format("%d is out of bound. Pushing item to the back.", index)));
			index = items.size();
		}

		BacklogEntry value = new BacklogEntry(rawValue);
		if (CONFIG.addFeedback) PrintEntry(context, ADDED_FEEDBACK, index, value.displayText());
		items.add(index, value);
		BacklogData.TrySave();
		return 1;
	}

	static int Set(CommandContext<FabricClientCommandSource> context, int index, String rawValue){
		final var items = BacklogData.instance.content;
		if (items.isEmpty()){
			context.getSource().sendError(Component.literal("Nothing to edit."));
			return 0;
		}

		if (index < 0 || index > items.size()-1){
			context.getSource().sendError(Component.literal(String.format("Index %d out of bounds.", index)));
			return -1;
		}

		BacklogEntry value = new BacklogEntry(rawValue);
		if (CONFIG.delFeedback) PrintEntry(context, REMOVED_FEEDBACK, index, items.get(index).displayText());
		items.remove(index);
		if (CONFIG.addFeedback) PrintEntry(context, ADDED_FEEDBACK, index, value.displayText());
		items.add(index, value);
		BacklogData.TrySave();
		return 1;
	}

	static int	Remove(CommandContext<FabricClientCommandSource> context, int index, @Nullable String expectedValue){
		final var items = BacklogData.instance.content;
		if (items.isEmpty()){
			context.getSource().sendError(Component.literal("Nothing to remove."));
			return 0;
		}
		
		if (index < 0 || index > items.size()-1){
			context.getSource().sendError(Component.literal(String.format("Index %d out of bounds. Max %d.", index, items.size()-1)));
			return -1;
		}
		
		if (expectedValue != null && !items.get(index).rawString().equals(expectedValue)) {
			context.getSource().sendError(Component.literal(String.format("Entry %d does not have the expected value.", index)));
			return -1;
		}
		

		if (CONFIG.delFeedback) PrintEntry(context, REMOVED_FEEDBACK, index, items.get(index).displayText());
		items.remove(index);
		BacklogData.TrySave();
		return 1;
	}

	static int Clear(CommandContext<FabricClientCommandSource> context){
		final var items = BacklogData.instance.content;
		if (items.isEmpty()){
			context.getSource().sendError(Component.literal("Nothing to remove."));
			return 0;
		}

		items.clear();
		context.getSource().sendFeedback(Component.translatable("backburner.feedback.clear", Component.literal("/"+CONFIG.rootCommand+" reload")));
		return 1;
	}

	static int	Move(CommandContext<FabricClientCommandSource> context, int src, int dst){
		final var items = BacklogData.instance.content;

		if (src < 0 || src > items.size()-1){
			context.getSource().sendError(Component.literal(String.format("Index %d out of bounds. Max %d", src, items.size()-1)));
			return -1;
		}

		if (dst<0)
			dst=0;
		else if (dst > items.size()-1)
			dst = items.size()-1;

		if (src == dst)
			return 0;

		BacklogEntry value = items.get(src);
		items.remove(src);
		items.add(dst, value);
		BacklogData.TrySave();
		return 1;
	}

}
