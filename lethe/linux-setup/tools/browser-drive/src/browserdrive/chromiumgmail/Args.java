package browserdrive.chromiumgmail;

import static browserdrive.Main.printUsageAndExit;

import java.io.PrintStream;

class Args {

	final String user_data_dir, username;
	final boolean incognito;

	private Args(String user_data_dir, String username, boolean incognito) {
		if (user_data_dir == null || username == null) {
			printUsageAndExit(1);
			throw new RuntimeException("Compiler wants this");
		}
		this.user_data_dir = user_data_dir;
		this.username = username;
		this.incognito = incognito;
	}

	static void printHelp(PrintStream s) {
		s.println("[options] user_data_dir username");
		s.println("    user_data_dir: same as for ‘chromium’ command above");
		s.println("    username: Gmail username (Chromium should have a saved password for it)");
		s.println("    --incognito: run Chromium in incognito mode");
	}

	static Args getArgsOrExit(String[] args) {
		if (args.length == 0) {
			printUsageAndExit(1);
			throw new RuntimeException("Compiler wants this");
		}

		String user_data_dir = null, username = null;
		boolean incognito = false;

		for (String arg : args) {
			if (arg.startsWith("-")) {
				switch (arg) {
				case "--incognito":
					incognito = true;
					break;
				default:
					printUsageAndExit(1);
					throw new RuntimeException("Compiler wants this");
				}
			} else {
				if (user_data_dir == null) {
					user_data_dir = arg;
				} else if (username == null) {
					username = arg;
				} else {
					printUsageAndExit(1);
					throw new RuntimeException("Compiler wants this");
				}
			}
		}

		return new Args(user_data_dir, username, incognito);
	}

}
