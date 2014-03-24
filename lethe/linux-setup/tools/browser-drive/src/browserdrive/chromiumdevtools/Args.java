package browserdrive.chromiumdevtools;

import static browserdrive.Main.printUsageAndExit;

import java.io.PrintStream;

class Args {

	final String user_data_dir;

	private Args(String user_data_dir) {
		if (user_data_dir == null) {
			printUsageAndExit(1);
			throw new RuntimeException("Compiler wants this");
		}
		this.user_data_dir = user_data_dir;
	}

	static Args getArgsOrExit(String[] args) {
		if (args.length == 0) {
			printUsageAndExit(1);
			throw new RuntimeException("Compiler wants this");
		}

		String user_data_dir = null;

		for (String arg : args) {
			if (user_data_dir != null) {
				System.err.println("Too many positional args: " + arg);
				System.exit(1);
				throw new RuntimeException("Compiler wants this");
			}
			user_data_dir = arg;
		}

		return new Args(user_data_dir);
	}

	static void printHelp(PrintStream s) {
		s.println("[options] user_data_dir");
		s.println("    ensure Dev Tools have been opened, to create their sqlite file");
		s.println("    user_data_dir: if ~/.config/x, Chromium makes ~/.config/x and ~/.cache/x");
		s.println("                   If /other/dir, everything is stored in /other/dir");
	}

}
