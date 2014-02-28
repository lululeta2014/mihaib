package browserdrive.firefoxgmail;

import static browserdrive.Main.printUsageAndExit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

class Args {

	final String profile_name, username, password;

	private Args(String profile_name, String username, boolean pass_via_stdin) {
		if (profile_name == null || username == null) {
			printUsageAndExit(1);
			throw new RuntimeException("Compiler wants this");
		}
		String pass = null;
		if (pass_via_stdin) {
			try {
				pass = new BufferedReader(new InputStreamReader(System.in))
						.readLine();
			} catch (IOException e) {
				System.err.println(e);
			}
		}
		this.profile_name = profile_name;
		this.username = username;
		this.password = pass;
	}

	static void printHelp(PrintStream s) {
		s.println("profile_name username");
		s.println("    --password-via-stdin");
		s.println("    profile_name: this appears to be copied (under /tmp) not directly used");
		s.println("    username: Gmail username");
	}

	static Args getArgsOrExit(String[] args) {
		if (args.length == 0) {
			printUsageAndExit(1);
			throw new RuntimeException("Compiler wants this");
		}

		String profile_name = null, username = null;
		boolean pass_via_stdin = false;

		for (String arg : args) {
			if (arg.startsWith("-")) {
				switch (arg) {
				case "--password-via-stdin":
					pass_via_stdin = true;
					break;
				default:
					printUsageAndExit(1);
					throw new RuntimeException("Compiler wants this");
				}
			} else {
				if (profile_name == null) {
					profile_name = arg;
				} else if (username == null) {
					username = arg;
				} else {
					printUsageAndExit(1);
					throw new RuntimeException("Compiler wants this");
				}
			}
		}

		return new Args(profile_name, username, pass_via_stdin);
	}

}
