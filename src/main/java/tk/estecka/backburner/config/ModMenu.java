package tk.estecka.backburner.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import java.io.IOException;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.text.Text;
import tk.estecka.backburner.Backburner;
import static tk.estecka.backburner.Backburner.CONFIG;

public class ModMenu
implements ModMenuApi
{
	static private final Config defaultConfig = new Config();

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory(){
		return parent -> {
			final var builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Text.literal("Backburner"));
			final var entries = builder.entryBuilder();

			final var HUD = builder.getOrCreateCategory(Text.translatable("backburner.config.category.hud"));
			final var CMD = builder.getOrCreateCategory(Text.translatable("backburner.config.category.command"));


			CMD.addEntry(
				entries.startStrField(Text.translatable("backburner.config.commandRoot").append("*"), CONFIG.rootCommand)
					.setSaveConsumer(v -> CONFIG.rootCommand = v)
					.setDefaultValue(defaultConfig.rootCommand)
					.setTooltip(Text.translatable("backburner.config.commandRoot.tooltip"))
					.build()
			);


			HUD.addEntry(
				entries.startIntField(Text.translatable("backburner.config.hudX"), CONFIG.hudX)
					.setSaveConsumer(v -> CONFIG.hudX = v)
					.setDefaultValue(defaultConfig.hudX)
					.build()
			);
			HUD.addEntry(
				entries.startIntField(Text.translatable("backburner.config.hudY"), CONFIG.hudY)
					.setSaveConsumer(v -> CONFIG.hudY = v)
					.setDefaultValue(defaultConfig.hudY)
					.build()
			);
			HUD.addEntry(
				entries.startIntField(Text.translatable("backburner.config.hudWidth"), CONFIG.hudWdt)
					.setSaveConsumer(v -> CONFIG.hudWdt = v)
					.setDefaultValue(defaultConfig.hudWdt)
					.build()
			);
			HUD.addEntry(
				entries.startFloatField(Text.translatable("backburner.config.hudScale"), CONFIG.hudScale)
					.setSaveConsumer(v -> CONFIG.hudScale = v)
					.setDefaultValue(defaultConfig.hudScale)
					.setTooltip(Text.translatable("backburner.config.hudScale.tooltip"))
					.build()
			);
			HUD.addEntry(
				entries.startBooleanToggle(Text.translatable("backburner.config.hudScale.fractional"), CONFIG.allowFractional)
					.setSaveConsumer(v -> CONFIG.allowFractional = v)
					.setDefaultValue(defaultConfig.allowFractional)
					.setTooltip(Text.translatable("backburner.config.hudScale.fractional.tooltip"))
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
