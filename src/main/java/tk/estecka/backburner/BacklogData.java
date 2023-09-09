package tk.estecka.backburner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.WorldSavePath;

public class BacklogData 
{
	static public BacklogData	instance = null;

	static private final Gson gson;
	static public final Type contentType = new TypeToken<List<String>>(){}.getType();
	
	static {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		gson = builder.create();
	}

	private final File saveFile;
	@NotNull public List<String> content = new ArrayList<String>();

	public BacklogData(File saveFile){
		this.saveFile = saveFile;
	}

	static public boolean	Reload(){
		final var client = MinecraftClient.getInstance();
		final var info = client.getCurrentServerEntry();
		Path savePath;

		if (info == null) {
			IntegratedServer local = client.getServer();
			savePath = local.getSavePath(WorldSavePath.ROOT).resolve("backlog.json");
		}
		else {
			String address = info.address.replace(':', ' ');
			savePath = client.runDirectory.toPath().resolve("remote_backlogs/"+address+".json");
		}

		Backburner.LOGGER.info("Backlog will be saved in: {}", savePath);
		File saveFile = savePath.toFile();
		instance = new BacklogData(saveFile);
		if (saveFile.exists()) try {
			instance.Load();
		}
		catch (FileNotFoundException|JsonIOException|JsonSyntaxException e){
			String msg = """
				Error reading backlog data. If you have any important data in there, you might want to get this sorted out before pushing any new note.
				You can use the subcommand `reload` to hot-reload the file after fixing it.
				""";
			client.inGameHud.getChatHud().addMessage(Text.literal(msg).formatted(Formatting.RED));
			Backburner.LOGGER.error("Errors reading file {}\n{}", saveFile, e);
			return false;
		}

		return true;
	}

	static public boolean TrySave(){
		try {
			instance.Save();
			return true;
		}
		catch (IOException e){
			final MinecraftClient client = MinecraftClient.getInstance();
			String msg = """
				Unable to save the backlog. See game log for more info.
				""";
			client.inGameHud.getChatHud().addMessage(Text.literal(msg).formatted(Formatting.RED));
			Backburner.LOGGER.error("Error writing file {}\n {}", instance.saveFile, e);
			return false;
		}
	}

	public List<String>	Load()
	throws FileNotFoundException, JsonIOException, JsonSyntaxException
	{
		JsonReader reader = new JsonReader(new FileReader(this.saveFile));
		this.content = gson.fromJson(reader, contentType);
		if (this.content == null)
			this.content = new ArrayList<String>();
		return this.content;
	}

	public void	Save()
	throws IOException, JsonIOException
	{
		this.saveFile.getParentFile().mkdirs();

		try ( var writer = new FileWriter(this.saveFile) ){
			gson.toJson(this.content, writer);
			// Otherwise required, but implied by the try-with-resource
			// writer.flush();
			// writer.close();
		}
		catch (IOException e){
			throw e;
		}
	}
}
