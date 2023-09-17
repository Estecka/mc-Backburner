package tk.estecka.backburner;

import java.io.IOException;
import java.util.Map.Entry;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class PatchReloadListener 
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
		BacklogHud.patches.clear();
		for (Entry<Identifier, Resource> entry : manager.findResources("textures/gui/backlog", PatchReloadListener::AcceptsFile).entrySet()){
			Identifier id = entry.getKey();
			try {
				PatchMeta mcmeta = PatchMeta.Decode(entry.getValue().getMetadata());
				// Backburner.LOGGER.warn("{} {}", id, mcmeta);
				BacklogHud.patches.put(id, new PatchInfo(mcmeta));
			} catch (IOException e) {
				Backburner.LOGGER.error("{} : {}", id, e);
			}
		}

	}

}