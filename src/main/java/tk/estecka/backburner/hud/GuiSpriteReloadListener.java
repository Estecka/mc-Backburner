package tk.estecka.backburner.hud;

import java.io.IOException;
import java.util.Map.Entry;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.resources.Identifier;
import tk.estecka.backburner.Backburner;

public class GuiSpriteReloadListener
implements SimpleSynchronousResourceReloadListener
{
	public Identifier	getFabricId(){
		return Identifier.fromNamespaceAndPath(Backburner.MODID, "gui_mcmeta");
	}

	static private boolean	AcceptsFile(Identifier id){
		return id.getNamespace().equals(Backburner.MODID)
		    && id.getPath().endsWith(".png")
		    ;
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		BacklogHud.sprites.clear();
		for (Entry<Identifier, Resource> entry : manager.listResources("textures/gui/backlog", GuiSpriteReloadListener::AcceptsFile).entrySet()){
			Identifier id = entry.getKey();
			try {
				GuiSpriteMeta mcmeta = GuiSpriteMeta.Decode(entry.getValue().metadata());
				// Backburner.LOGGER.warn("{} {}", id, mcmeta);
				BacklogHud.sprites.put(id, new GuiSpriteInfo(mcmeta));
			} catch (IOException e) {
				Backburner.LOGGER.error("{} : {}", id, e);
			}
		}
	}
}
