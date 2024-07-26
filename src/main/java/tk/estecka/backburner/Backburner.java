package tk.estecka.backburner;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import tk.estecka.backburner.config.Config;
import tk.estecka.backburner.config.ConfigIO;
import tk.estecka.backburner.hud.GuiSpriteReloadListener;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Backburner implements ClientModInitializer 
{
	static public final String MODID = "backburner";
	static public final Logger LOGGER = LoggerFactory.getLogger("Back-burner");

	static public final ConfigIO CONFIG_IO = new ConfigIO(MODID+".properties");
	static public final Config CONFIG = new Config();

	@Override
	public void onInitializeClient() {
		try {
			CONFIG_IO.GetIfExists(CONFIG);
		}
		catch (IOException e){
			LOGGER.error("{}", e);
		}

		BacklogCommands.Register();
		ClientPlayConnectionEvents.JOIN.register(Identifier.of(MODID, "reload"), (handler, packet, client)->BacklogData.Reload());
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new GuiSpriteReloadListener());

		var mod = FabricLoader.getInstance().getModContainer(MODID).get();
		ResourceManagerHelper.registerBuiltinResourcePack(Identifier.of(MODID, "notebook"), mod, Text.literal("Note-Book"     ), ResourcePackActivationType.NORMAL);
		ResourceManagerHelper.registerBuiltinResourcePack(Identifier.of(MODID, "questlog"), mod, Text.literal("Final Questlog"), ResourcePackActivationType.NORMAL);
	}
}
