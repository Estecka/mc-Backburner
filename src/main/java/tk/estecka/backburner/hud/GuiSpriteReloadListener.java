package tk.estecka.backburner.hud;

import java.io.IOException;
import java.util.Map.Entry;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import tk.estecka.backburner.Backburner;

public class GuiSpriteReloadListener 
implements SimpleSynchronousResourceReloadListener
{
	public Identifier	getFabricId(){
		return new Identifier(Backburner.MODID, "gui_mcmeta");
	}

	static private boolean	AcceptsFile(Identifier id){
		return id.getNamespace().equals(Backburner.MODID)
		    && id.getPath().endsWith(".png")
		    ;
	}

	public void	reload(ResourceManager manager){
		BacklogHud.sprites.clear();
		for (Entry<Identifier, Resource> entry : manager.findResources("textures/gui/backlog", GuiSpriteReloadListener::AcceptsFile).entrySet()){
			Identifier id = entry.getKey();
			try {
				GuiSpriteMeta mcmeta = GuiSpriteMeta.Decode(entry.getValue().getMetadata());
				// Backburner.LOGGER.warn("{} {}", id, mcmeta);
				BacklogHud.sprites.put(id, new GuiSpriteInfo(mcmeta));
			} catch (IOException e) {
				Backburner.LOGGER.error("{} : {}", id, e);
			}
		}

	}

}
