package tk.estecka.backburner.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.text.Text;
import tk.estecka.backburner.Backburner;

public class ModMenu
implements ModMenuApi
{
	static private final Config defaultConfig = new Config();

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory(){
		return parent -> {
			final Config CONFIG = Backburner.GetConfig();
			final var builder = ConfigBuilder.create().setParentScreen(parent).setTitle(Text.literal("Backburner"));
			final var entries = builder.entryBuilder();

			final var HUD = builder.getOrCreateCategory(Text.literal("HUD"));
			final var CMD = builder.getOrCreateCategory(Text.literal("Command"));

			CMD.addEntry(
				entries.startStrField(Text.literal("Root command"), CONFIG.rootCommand)
					.setSaveConsumer(v -> CONFIG.rootCommand = v)
					.setDefaultValue(defaultConfig.rootCommand)
					.setTooltip(Text.literal("Requires restart to take effect"))
					.build()
			);


			HUD.addEntry(
				entries.startIntField(Text.literal("Position X"), CONFIG.hudX)
					.setSaveConsumer(v -> CONFIG.hudX = v)
					.setDefaultValue(defaultConfig.hudX)
					.build()
			);
			HUD.addEntry(
				entries.startIntField(Text.literal("Position Y"), CONFIG.hudY)
					.setSaveConsumer(v -> CONFIG.hudY = v)
					.setDefaultValue(defaultConfig.hudY)
					.build()
			);
			HUD.addEntry(
				entries.startIntField(Text.literal("Base Width"), CONFIG.hudWdt)
					.setSaveConsumer(v -> CONFIG.hudWdt = v)
					.setDefaultValue(defaultConfig.hudWdt)
					.build()
			);
			HUD.addEntry(
				entries.startFloatField(Text.literal("HUD Scale Multiplier"), CONFIG.hudScale)
					.setSaveConsumer(v -> CONFIG.hudScale = v)
					.setDefaultValue(defaultConfig.hudScale)
					.setTooltip(Text.literal("Scales the backlog's GUI Scale vanilla."))
					.build()
			);
			HUD.addEntry(
				entries.startBooleanToggle(Text.literal("Allow fractional GUI Scale"), CONFIG.allowFractional)
					.setSaveConsumer(v -> CONFIG.allowFractional = v)
					.setDefaultValue(defaultConfig.allowFractional)
					.setTooltip(Text.literal("Requires restart to take effect"))
					.build()
			);


			return builder.build();
		};
	}
}
