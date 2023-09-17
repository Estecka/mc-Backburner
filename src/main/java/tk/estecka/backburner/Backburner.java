package tk.estecka.backburner;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Backburner implements ClientModInitializer 
{
	static public final String MODID = "backburner";
	static public final Logger LOGGER = LoggerFactory.getLogger("Back-burner");

	@Override
	public void onInitializeClient() {
		BacklogCommands.Register();
		ClientPlayConnectionEvents.JOIN.register(new Identifier(MODID, "reload"), (handler, packet, client)->BacklogData.Reload());
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new PatchReloadListener());
	}
}
