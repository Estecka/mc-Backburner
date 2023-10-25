package tk.estecka.backburner.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import net.fabricmc.loader.api.FabricLoader;

public class ConfigIO
{
	static public interface Codecable {
		void ReadProperties (Map<String, String> properties);
		void WriteProperties(Map<String, String> properties);
	}

	private final File file;

	public ConfigIO(File file){
		this.file = file;
	}
	public ConfigIO(Path path){
		this(path.toFile());
	}
	public ConfigIO(String fileName){
		this(FabricLoader.getInstance().getConfigDir().resolve(fileName));
	}

	/**
	 * @param config A config object prefilled  with default values. This object
	 * will be filled with the new values from the config file.
	 * @throws IOException
	 */
	public void	GetOrCreate(Codecable config)
	throws IOException
	{
		if (!this.file.exists())
			this.Overwrite(config);
		else {
			var properties = ReadFile(this.file);
			config.WriteProperties(properties);
		}
	}

	public void Overwrite(Codecable config)
	throws IOException
	{
		var properties = new LinkedHashMap<String, String>();
		config.WriteProperties(properties);
		WriteFile(this.file, properties);
	}

	static public Map<String, String>	ReadFile(File file)
	throws IOException
	{
		var properties = new HashMap<String, String>();
		try (Scanner scanner = new Scanner(file))
		{
			for (int lineNo=0; scanner.hasNextLine(); ++lineNo) {
				String line = scanner.nextLine();
				int split = line.indexOf('=');
				if (line.isEmpty() || line.startsWith("#"))
					continue;
				else if (split < 0 || line.length() <= split+1)
					throw new RuntimeException(String.format("Missing value at line %d", lineNo));
				else {
					properties.put(
						line.substring(0, split),
						line.substring(split+1, line.length())
					);
				}
			}
		}
		catch (IOException e){
			throw e;
		}
		return properties;
	}

	static public void	WriteFile(File file, Map<String,String> properties)
	throws IOException
	{
		try (FileOutputStream out = new FileOutputStream(file, false))
		{
			final PrintWriter writer = new PrintWriter(out);
			for (var entry : properties.entrySet()){
				writer.write(entry.getKey());
				writer.write('=');
				writer.write(entry.getValue());
				writer.write('\n');
				writer.flush();
			}
		}
		catch (IOException e){
			throw e;
		}
	}
}
