/*
 * Copyright © Mihai Borobocea 2010
 * 
 * This file is part of JEncConv.
 * 
 * JEncConv is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * JEncConv is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with JEncConv.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.sf.jencconv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

public class ArgsParser {

	// flags
	private static boolean f_help, f_print_encodings, f_replace, f_force,
			f_print_plugins;
	private static String detect_arg, in_enc_name, out_enc_name,
			chain_of_plugins;
	private static List<String> fileNameList = new ArrayList<String>();

	public static void main(String[] args) {
		if (args.length == 0) {
			GUI.start();
			return;
		}

		parseArgs(args);

		if (f_help) {
			printUsage(System.out);
			return;
		}

		if (f_print_encodings) {
			System.out.println("name\t\taliases");
			for (Charset cs : Charset.availableCharsets().values()) {
				System.out.print(cs.name() + "\t");

				boolean first = true;
				for (String alias : cs.aliases()) {
					if (first)
						first = false;
					else
						System.out.print(" ");
					System.out.print(alias);
				}

				System.out.println();
			}
			return;
		}

		if (f_print_plugins) {
			boolean first = true;

			for (ReaderFactory plugin : ServiceLoader.load(ReaderFactory.class)) {
				if (first)
					first = false;
				else
					System.out.println();

				System.out.println(plugin.toString());

				String s = "\t{" + plugin.getClass().getName() + "}\n"
						+ plugin.getDescription().trim();
				s = s.replaceAll("\n", "\n\t");

				System.out.println(s);
			}
			return;
		}

		if (detect_arg != null) {
			try {
				Charset[] charsets = Converter.getPossibleCharsets(detect_arg);
				System.out.print(charsets.length);
				System.out.print(" possible encoding(s) found for ");
				System.out.println(detect_arg);

				for (Charset cs : charsets)
					System.out.println(cs.name());
			} catch (FileNotFoundException fnfe) {
				System.err.println(fnfe);
				System.exit(1);
			}
			return;
		}

		if (fileNameList.size() == 0) {
			System.err.println("error: no file arguments given");
			System.err.println("run with '--help' to print usage");
			System.exit(1);
		}

		if (!f_replace && fileNameList.size() % 2 != 0) {
			System.err.println("error: even number of file arguments required");
			System.err.println("run with '--help' to print usage");
			System.exit(1);
		}

		// get encodings
		Charset in_cs = Charset.defaultCharset(), out_cs;

		if (in_enc_name != null)
			in_cs = Charset.forName(in_enc_name);

		// Every Java platform implementation must to support UTF-8
		// however, the program can run in one that doesn't
		// by not calling Charset.forName("UTF-8") (which throws an exception)
		// if the user specifies an output encoding
		if (out_enc_name != null)
			out_cs = Charset.forName(out_enc_name);
		else
			out_cs = Charset.forName("UTF-8");

		// get plugins
		ReaderFactory[] plugins = getPlugins(chain_of_plugins);

		// convert
		for (int i = 0; i < fileNameList.size(); i++) {
			try {
				String inFileName, outFileName;

				if (f_replace) {
					String origName = fileNameList.get(i);
					String bakName = origName + ".bak";

					File origFile = new File(origName);
					File bakFile = new File(bakName);

					if (!origFile.exists()) {
						throw new IOException("File '" + origName
								+ "' not found");
					}

					if (!f_force && bakFile.exists()) {
						throw new IOException("Backup file '" + bakName
								+ "' exists. Use --force to overwrite.");
					}

					// try to move original to .bak
					if (!origFile.renameTo(bakFile)) {
						throw new IOException("Cannot move '" + origName
								+ "' to '" + bakName + "'");
					}

					inFileName = bakName;
					outFileName = origName;
				} else {
					inFileName = fileNameList.get(i);
					i++;
					outFileName = fileNameList.get(i);

					if (!f_force && new File(outFileName).exists()) {
						throw new IOException("Destination file '"
								+ outFileName
								+ "' exists. Use --force to overwrite.");
					}
				}

				Converter.convert(inFileName, in_cs.newDecoder(), outFileName,
						out_cs.newEncoder(), plugins);
			} catch (FileNotFoundException e) {
				System.err.println(e);
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}

	private static void printUsage(PrintStream p) {
		p.println("Usage: java -jar jencconv.jar "
				+ "[options] file [file2 file3 ...]");
		p.println("  or:  java -jar jencconv.jar");
		p.println("Convert files (file args interpretation "
				+ "depends on -r flag) or start GUI.");
		p.println();
		p.println("Options:");
		p.println("-h, --help\tshow this help message and exit");
		p.println("-e, --encodings\tprint known encodings and exit");
		p.println("-d, --detect  file");
		p.println("\t\tdetect possible encodings of file and exit");
		p.println("-i encoding\tinput encoding, your default is "
				+ Charset.defaultCharset().name());
		p.println("-o encoding\toutput encoding, default is UTF-8");
		p.println("-r, --replace\tIf present, overwrite each file "
				+ "(saving original to .bak).");
		p.println("\t\tIf absent, the files are interpreted as pairs");
		p.println("\t\t(src1 dest1 src2 dest2...) "
				+ "and their number must be even.");
		p.println("-f, --force"
				+ "\tWith --replace, overwrite existing .bak files.");
		p.println("\t\tWithout --replace, "
				+ "overwrite existing destination files.");
		p.println("-p, --plugins\tprint available plugins and exit");
		p.println("-c, --chain  plugin1[#plugin2#plugin3]");
		p.println("\t\tChain specified plugins (parts of plugin class names).");
		p.println("\t\tA name matching no plugin "
				+ "is re-checked case-insensitive.");
		p.println("\t\tExactly 1 plugin's "
				+ "fully qualified class name must match.");
		p.println("\t\tFor x.y, x.y.z: "
				+ "x is ambiguous, z and x.y aren't (full match).");
	}

	private static void parseArgs(String[] args) {
		for (int i = 0; i < args.length; i++) {
			String s = args[i];
			if (s.equals("-h") || s.equals("--help") || s.equals("-help")) {
				f_help = true;
			} else if (s.equals("-e") || s.equals("--encodings")) {
				f_print_encodings = true;
			} else if (s.equals("-r") || s.equals("--replace")) {
				f_replace = true;
			} else if (s.equals("-f") || s.equals("--force")) {
				f_force = true;
			} else if (s.equals("-d") || s.equals("--detect")) {
				i++;
				if (i < args.length) {
					detect_arg = args[i];
				} else {
					System.err.println("error: --detect option "
							+ "requires an argument");
					System.err.println("run with '--help' to print usage");
					System.exit(1);
				}
			} else if (s.equals("-i")) {
				i++;
				if (i < args.length) {
					in_enc_name = args[i];
				} else {
					System.err.println("error: -i option requires an argument");
					System.err.println("run with '--help' to print usage");
					System.exit(1);
				}
			} else if (s.equals("-o")) {
				i++;
				if (i < args.length) {
					out_enc_name = args[i];
				} else {
					System.err.println("error: -o option requires an argument");
					System.err.println("run with '--help' to print usage");
					System.exit(1);
				}
			} else if (s.equals("-p") || s.equals("--plugins")) {
				f_print_plugins = true;
			} else if (s.equals("-c") || s.equals("--chain")) {
				i++;
				if (i < args.length) {
					chain_of_plugins = args[i];
				} else {
					System.err.println("error: " + args[i - 1]
							+ " option requires an argument");
					System.err.println("run with '--help' to print usage");
					System.exit(1);
				}
			} else if (s.startsWith("-")) {
				System.err.println("error: no such option " + s);
				System.err.println("run with '--help' to print usage");
				System.exit(1);
			} else {
				fileNameList.add(s);
			}
		}
	}

	private static ReaderFactory[] getPlugins(String chain_of_plugins) {
		if (chain_of_plugins == null)
			return new ReaderFactory[0];

		ServiceLoader<ReaderFactory> sl = ServiceLoader
				.load(ReaderFactory.class);
		String[] names = chain_of_plugins.split("#");
		List<ReaderFactory> pluginsUsed = new LinkedList<ReaderFactory>();

		for (String name : names) {
			ReaderFactory match = null;

			/*
			 * Full name
			 */

			// case sensitive
			for (ReaderFactory plugin : sl) {
				if (!plugin.getClass().getName().equals(name))
					continue;
				if (match == null) {
					match = plugin;
				} else {
					System.err.println("Ambiguous plugin name '" + name + "'");
					System.err.println("both " + match.getClass().getName()
							+ " and " + plugin.getClass().getName() + " match");
					System.exit(1);
				}
			}

			if (match == null) {
				// case insensitive
				for (ReaderFactory plugin : sl) {
					if (!plugin.getClass().getName().equalsIgnoreCase(name))
						continue;
					if (match == null) {
						match = plugin;
					} else {
						System.err.println("Ambiguous plugin name '" + name
								+ "'");
						System.err.println("both " + match.getClass().getName()
								+ " and " + plugin.getClass().getName()
								+ " match");
						System.exit(1);
					}
				}
			}

			/*
			 * Part of name
			 */

			if (match == null) {
				// case-sensitive
				for (ReaderFactory plugin : sl) {
					if (plugin.getClass().getName().indexOf(name) == -1)
						continue;
					if (match == null) {
						match = plugin;
					} else {
						System.err.println("Ambiguous plugin name '" + name
								+ "'");
						System.err.println("both " + match.getClass().getName()
								+ " and " + plugin.getClass().getName()
								+ " match");
						System.exit(1);
					}
				}
			}

			if (match == null) {
				// case-insensitive
				String nameLower = name.toLowerCase();

				for (ReaderFactory plugin : sl) {
					if (plugin.getClass().getName().toLowerCase().indexOf(
							nameLower) == -1)
						continue;
					if (match == null) {
						match = plugin;
					} else {
						System.err.println("Ambiguous plugin name '" + name
								+ "'");
						System.err.println("both " + match.getClass().getName()
								+ " and " + plugin.getClass().getName()
								+ " match");
						System.exit(1);
					}
				}
			}

			if (match == null) {
				System.err.println("Invalid plugin name: '" + name + "'");
				System.exit(1);
			}

			pluginsUsed.add(match);
		}

		return pluginsUsed.toArray(new ReaderFactory[0]);
	}

}
