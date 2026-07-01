package tk.estecka.backburner;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.packs.PackType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import tk.estecka.backburner.config.Config;
import tk.estecka.backburner.config.ConfigIO;
import tk.estecka.backburner.hud.GuiSpriteReloadListener;
import tk.estecka.backburner.hud.BacklogHud;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Backburner implements ClientModInitializer {
	static public final String MODID = "backburner";
	static public final Logger LOGGER = LoggerFactory.getLogger("Back-burner");

	static public final ConfigIO CONFIG_IO = new ConfigIO(MODID + ".properties");
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
		ClientPlayConnectionEvents.JOIN.register(Identifier.fromNamespaceAndPath(MODID, "reload"), (handler, packet, client) -> BacklogData.Reload());
		ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new GuiSpriteReloadListener());

		var mod = FabricLoader.getInstance().getModContainer(MODID).get();
		ResourceLoader.registerBuiltinPack(Identifier.fromNamespaceAndPath(MODID, "notebook"), mod, Component.literal("Note-Book"), PackActivationType.NORMAL);
		ResourceLoader.registerBuiltinPack(Identifier.fromNamespaceAndPath(MODID, "questlog"), mod, Component.literal("Final Questlog"), PackActivationType.NORMAL);

		HudElementRegistry.attachElementAfter(VanillaHudElements.HOTBAR, Identifier.fromNamespaceAndPath(MODID, "after_chat"), new BacklogHud()::Render);
	}
}