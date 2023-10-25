package tk.estecka.backburner.config;


/*
 * Copyright (c) 2023 Estecka
 *
 * Permission is hereby granted, free of charge,  to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction,  including without limitation the rights
 * to use, copy,  modify, merge,  publish,  distribute, sublicense,  and/or sell
 * copies  of  the Software, and  to  permit  persons  to whom  the  Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above  copyright notice  and this permission notice  shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE  IS PROVIDED  "AS IS", WITHOUT WARRANTY  OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING  BUT NOT LIMITED  TO  THE WARRANTIES  OF  MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE  AND NONINFRINGEMENT. IN NO EVENT  SHALL THE
 * AUTHORS  OR  COPYRIGHT HOLDERS  BE  LIABLE  FOR  ANY CLAIM, DAMAGES  OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION  WITH THE SOFTWARE  OR THE USE  OR OTHER DEALINGS  IN
 * THE SOFTWARE.
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.fabricmc.loader.api.FabricLoader;

public class ConfigIO
{
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

	
	static public interface Codec {
		Map<String, Property<?>> GetProperties();
	}

	static public record Property<T>(Supplier<T> getter, Consumer<T> setter, Function<String, T> parser, Function<T, String> encoder) 
	{
		static public Property<String>  String (Supplier<String>  getter, Consumer<String>  setter) { return new Property<>(getter, setter, s->s, s->s); }
		static public Property<Integer> Integer(Supplier<Integer> getter, Consumer<Integer> setter) { return new Property<>(getter, setter, Integer::parseInt, i->i.toString()); }
		static public Property<Float>   Float  (Supplier<Float>   getter, Consumer<Float>   setter) { return new Property<>(getter, setter, Float::parseFloat, f->f.toString()); }
		static public Property<Boolean> Boolean(Supplier<Boolean> getter, Consumer<Boolean> setter) { return new Property<>(getter, setter, Boolean::parseBoolean, b->b.toString()); }

		public void Decode(String s){ this.setter.accept(this.parser.apply(s)); }
		public String Encode() { return this.encoder.apply(this.getter.get()); }
	}

	/**
	 * @param config A config object prefilled  with default values. This object
	 * will be filled with the new values from the config file.
	 * @throws IOException
	 */
	public void	GetOrCreate(Codec config)
	throws IOException
	{
		if (!this.file.exists())
			this.Write(config);
		else {
			var properties = ReadFile(this.file);
			var codec = config.GetProperties();
			for (var key : codec.keySet())
				if (properties.containsKey(key))
					codec.get(key).Decode(properties.get(key));
		}
	}

	public void Write(Codec config)
	throws IOException
	{
		var properties = new LinkedHashMap<String, String>();
		for (var entry : config.GetProperties().entrySet()){
			var p = entry.getValue();
			properties.put(entry.getKey(), p.Encode());
		}
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
