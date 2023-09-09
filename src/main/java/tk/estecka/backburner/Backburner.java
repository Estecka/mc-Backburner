package tk.estecka.backburner;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Backburner implements ClientModInitializer 
{
	static public final Logger LOGGER = LoggerFactory.getLogger("Back-burner");

	@Override
	public void onInitializeClient() {
		BacklogCommands.Register();
		ClientPlayConnectionEvents.JOIN.register(new Identifier("backburner", "reload"), (handler, packet, client)->BacklogData.Reload());
	}
}
