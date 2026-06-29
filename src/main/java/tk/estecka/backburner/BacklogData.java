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

import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.NotNull;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Gui;

public class BacklogData
{
	static public BacklogData	instance = null;

	static private final Type contentType = new TypeToken<List<String>>(){}.getType();
	static private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	private final File saveFile;
	@NotNull public List<BacklogEntry> content = new ArrayList<>();

	public BacklogData(File saveFile){
		this.saveFile = saveFile;
	}

	static public boolean	Reload(){
		final var client = Minecraft.getInstance();
		final var info = client.getCurrentServer();
		final var local = client.getSingleplayerServer();
		Path savePath;

		if (local != null) {
			savePath = local.getServerDirectory().resolve("backlog.json");
		}
		else if (info != null) {
			String address = info.ip.replace(':', ' ');
			savePath = client.gameDirectory.toPath().resolve("remote_backlogs/"+address+".json");
		}
		else {
			Backburner.LOGGER.error("Unable to find backburner's backlog save path. You can ignore this error if it occured during a Replay.");
			return false;
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
			client.gui.hud.getChat().addMessage(Component.literal(msg).withStyle(ChatFormatting.RED));
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
			final Minecraft client = Minecraft.getInstance();
			String msg = """
				Unable to save the backlog. See game log for more info.
				""";
			client.gui.hud.getChat().addMessage(Component.literal(msg).withStyle(ChatFormatting.RED));
			Backburner.LOGGER.error("Error writing file {}\n {}", instance.saveFile, e);
			return false;
		}
	}

	public List<BacklogEntry> Load()
	throws FileNotFoundException, JsonIOException, JsonSyntaxException
	{
		JsonReader reader = new JsonReader(new FileReader(this.saveFile));
		List<String> rawText = gson.fromJson(reader, contentType);
		this.content = new ArrayList<>();
		if  (rawText != null)
		for (String rawEntry : rawText) {
			this.content.add(new BacklogEntry(rawEntry));
		}

		return this.content;
	}

	public void	Save()
	throws IOException, JsonIOException
	{
		this.saveFile.getParentFile().mkdirs();
		List<String> rawText = new ArrayList<>();
		for (BacklogEntry entry : this.content){
			rawText.add(entry.rawString());
		}

		try ( var writer = new FileWriter(this.saveFile) ){
			gson.toJson(rawText, writer);
			// Otherwise required, but implied by the try-with-resource
			// writer.flush();
			// writer.close();
		}
		catch (IOException e){
			throw e;
		}
	}
}
