package browserdrive.chromium;

import static browserdrive.Main.printUsageAndExit;

import java.io.PrintStream;

class Args {

	final String user_data_dir;
	final boolean save_passwords;
	final boolean enable_translate;

	private Args(String user_data_dir, boolean save_passwords,
			boolean enable_translate) {
		if (user_data_dir == null) {
			printUsageAndExit(1);
			throw new RuntimeException("Compiler wants this");
		}
		this.user_data_dir = user_data_dir;
		this.save_passwords = save_passwords;
		this.enable_translate = enable_translate;
	}

	static Args getArgsOrExit(String[] args) {
		if (args.length == 0) {
			printUsageAndExit(1);
			throw new RuntimeException("Compiler wants this");
		}

		String user_data_dir = null;
		boolean save_passwords = false;
		boolean enable_translate = false;

		for (String arg : args) {
			if (arg.startsWith("-")) {
				switch (arg) {
				case "--save-pass":
					save_passwords = true;
					break;
				case "--enable-translate":
					enable_translate = true;
					break;
				default:
					printUnknownOption(arg);
					System.exit(1);
					throw new RuntimeException("Compiler wants this");
				}
			} else {
				if (user_data_dir != null) {
					System.err.println("Too many positional args: " + arg);
					System.exit(1);
					throw new RuntimeException("Compiler wants this");
				}
				user_data_dir = arg;
			}
		}

		return new Args(user_data_dir, save_passwords, enable_translate);
	}

	static void printHelp(PrintStream s) {
		s.println("[options] user_data_dir");
		s.println("    user_data_dir: if ~/.config/x, Chromium makes ~/.config/x and ~/.cache/x");
		s.println("                   If /other/dir, everything is stored in /other/dir");
		s.println("    --save-pass\tConfigure Chromium to save passwords");
		s.println("    --enable-translate\tOffer to translate pages");
	}

	private static void printUnknownOption(String arg) {
		System.err.println("Unknown option: " + arg);
	}

}
