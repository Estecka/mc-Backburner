package tk.estecka.backburner.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import java.io.IOException;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.network.chat.Component;
import tk.estecka.backburner.Backburner;
import static tk.estecka.backburner.Backburner.CONFIG;

public class ModMenu
implements ModMenuApi
{
	static private final Config DEFAULT = new Config();

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory(){
		return parent -> {
			final var builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Component.literal("Backburner"));
			final var entries = builder.entryBuilder();

			final var HUD = builder.getOrCreateCategory(Component.translatable("backburner.config.category.hud"));
			final var CMD = builder.getOrCreateCategory(Component.translatable("backburner.config.category.command"));

			CMD.addEntry(
				entries.startStrField(Component.translatable("backburner.config.commandRoot"), CONFIG.rootCommand)
				.setSaveConsumer(v -> CONFIG.rootCommand = v)
				.setDefaultValue(DEFAULT.rootCommand)
				.setTooltip(Component.translatable("backburner.config.commandRoot.tooltip"))
				.build()
				);
			CMD.addEntry(
				entries.startTextDescription(Component.translatable("backburner.config.commandRoot.description"))
					.build()
			);

			CMD.addEntry(
				entries.startEnumSelector(Component.translatable("backburner.config.writeAction"), EWriteAction.class, CONFIG.writeAction)
					.setEnumNameProvider(e->((EWriteAction)e).TranslatableName())
					.setTooltipSupplier(EWriteAction::Tooltip)
					.setSaveConsumer(v -> CONFIG.writeAction = v)
					.setDefaultValue(DEFAULT.writeAction)
					.build()
			);

			CMD.addEntry(
				entries.startBooleanToggle(Component.translatable("backburner.config.feedback.addition"), CONFIG.addFeedback)
					.setSaveConsumer(v -> CONFIG.addFeedback=v)
					.setDefaultValue(DEFAULT.addFeedback)
					.build()
			);
			CMD.addEntry(
				entries.startBooleanToggle(Component.translatable("backburner.config.feedback.removal"), CONFIG.delFeedback)
					.setSaveConsumer(v -> CONFIG.delFeedback=v)
					.setDefaultValue(DEFAULT.delFeedback)
					.build()
			);


			HUD.addEntry(
				entries.startFloatField(Component.translatable("backburner.config.anchorX"), CONFIG.anchorX)
					.setSaveConsumer(v -> CONFIG.anchorX = v)
					.setDefaultValue(DEFAULT.anchorX)
					.setTooltip(Component.translatable("backburner.config.anchorX.tooltip"))
					.build()
			);
			HUD.addEntry(
				entries.startIntField(Component.translatable("backburner.config.hudX"), CONFIG.hudX)
					.setSaveConsumer(v -> CONFIG.hudX = v)
					.setDefaultValue(DEFAULT.hudX)
					.setTooltip(Component.translatable("backburner.config.hudX.tooltip"))
					.build()
			);
			HUD.addEntry(
				entries.startIntField(Component.translatable("backburner.config.hudY"), CONFIG.hudY)
					.setSaveConsumer(v -> CONFIG.hudY = v)
					.setDefaultValue(DEFAULT.hudY)
					.setTooltip(Component.translatable("backburner.config.hudY.tooltip"))
					.build()
			);
			HUD.addEntry(
				entries.startIntField(Component.translatable("backburner.config.hudWidth"), CONFIG.hudWdt)
					.setSaveConsumer(v -> CONFIG.hudWdt = v)
					.setDefaultValue(DEFAULT.hudWdt)
					.setTooltip(Component.translatable("backburner.config.hudWidth.tooltip"))
					.build()
			);
			HUD.addEntry(
				entries.startFloatField(Component.translatable("backburner.config.hudScale"), CONFIG.hudScale)
					.setSaveConsumer(v -> CONFIG.hudScale = v)
					.setDefaultValue(DEFAULT.hudScale)
					.setTooltip(Component.translatable("backburner.config.hudScale.tooltip"))
					.build()
			);
			HUD.addEntry(
				entries.startBooleanToggle(Component.translatable("backburner.config.hudScale.fractional"), CONFIG.allowFractional)
					.setSaveConsumer(v -> CONFIG.allowFractional = v)
					.setDefaultValue(DEFAULT.allowFractional)
					.setTooltip(Component.translatable("backburner.config.hudScale.fractional.tooltip"))
					.build()
			);

			builder.setSavingRunnable(()->{
				try {
					Backburner.CONFIG_IO.Write(CONFIG);
				}
				catch (IOException e) {
					Backburner.LOGGER.error("Unable to save config: {}", e);
				}
			});

			return builder.build();
		};
	}
}
