/*
 * Copyright © Mihai Borobocea 2010
 * 
 * This file is part of File Transfer.
 * 
 * File Transfer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * File Transfer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with File Transfer.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package ft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class Args {

	final boolean f_help;
	final boolean f_noPrealloc, f_reverseLookup, f_yes;
	final String listen_arg, connect_arg, socks5_arg;
	final List<String> fileNames;

	Args(boolean f_help, boolean f_noPrealloc, boolean f_reverseLookup,
			boolean f_yes, String listen_arg, String connect_arg,
			String socks5_arg, List<String> fileNames) throws ArgsException {
		if (f_help) {
			this.f_help = true;
			this.f_noPrealloc = this.f_reverseLookup = this.f_yes = false;
			this.listen_arg = this.connect_arg = this.socks5_arg = null;
			this.fileNames = Collections
					.unmodifiableList(new ArrayList<String>());
			return;
		}

		int net_options = 0;
		for (String opt : new String[] { listen_arg, connect_arg, socks5_arg })
			if (opt != null)
				net_options++;

		if (net_options == 0)
			throw new ArgsException("None of "
					+ "--connect, --listen, --socks5 given");

		if (net_options != 1)
			throw new ArgsException("Several of --connect, --listen, --socks5 "
					+ "given. Only one permitted.");

		this.f_help = f_help;
		this.f_noPrealloc = f_noPrealloc;
		this.f_reverseLookup = f_reverseLookup;
		this.f_yes = f_yes;
		this.listen_arg = listen_arg;
		this.connect_arg = connect_arg;
		this.socks5_arg = socks5_arg;
		this.fileNames = Collections.unmodifiableList(fileNames);
	}

	static Args parse(String[] args) throws ArgsException {
		boolean f_help = false;
		boolean f_noPrealloc = false, f_reverseLookup = false, f_yes = false;
		String listen_arg = null, connect_arg = null, socks5_arg = null;
		List<String> fileNames = new ArrayList<String>();

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];

			if (arg.equals("-h") || arg.equals("--help") || arg.equals("-help")) {
				f_help = true;
			} else if (arg.equals("--no-prealloc")) {
				f_noPrealloc = true;
			} else if (arg.equals("-r") || arg.equals("--reverse-lookup")) {
				f_reverseLookup = true;
			} else if (arg.equals("-y") || arg.equals("--yes")) {
				f_yes = true;
			} else if (arg.equals("-l") || arg.equals("--listen")) {
				i++;
				if (i >= args.length)
					throw new ArgsException("--listen option "
							+ "requires port argument");

				if (listen_arg != null)
					throw new ArgsException("--listen option "
							+ "given multiple times");

				listen_arg = args[i];
			} else if (arg.equals("-c") || arg.equals("--connect")) {
				i++;
				if (i >= args.length)
					throw new ArgsException("--connect option "
							+ "requires host:port argument");

				if (connect_arg != null)
					throw new ArgsException("--connect option "
							+ "given multiple times");

				connect_arg = args[i];
			} else if (arg.equals("--socks5")) {
				i++;
				if (i >= args.length)
					throw new ArgsException("--socks5 option requires "
							+ "host:port[:peerHost[:peerPort]] argument");

				if (socks5_arg != null)
					throw new ArgsException("--socks5 option "
							+ "given multiple times");

				socks5_arg = args[i];
			} else if (arg.startsWith("-")) {
				throw new ArgsException("Unknown option " + arg);
			} else {
				fileNames.add(arg);
			}
		}

		if (f_noPrealloc && fileNames.size() > 0)
			throw new ArgsException("Flag --no-prealloc invalid for sender");

		if (f_reverseLookup && connect_arg != null)
			throw new ArgsException("Flag --reverse-lookup invalid for client");

		return new Args(f_help, f_noPrealloc, f_reverseLookup, f_yes,
				listen_arg, connect_arg, socks5_arg, fileNames);
	}

	static void printUsage() {
		System.out.println("Usage: java -jar ft.jar [options] file1 [file2..]");
		System.out.println("  or:  java -jar ft.jar [options]");
		System.out.println("Send files (if given as arguments) "
				+ "or receive files (if no file args given).");
		System.out.println("One peer (either sender or receiver) "
				+ "must act as server, the other as client.");

		System.out.println();
		System.out.println("Options:");
		System.out.println("-h, --help\t\tprint this help message and exit");
		System.out.println("-l, --listen  port\t"
				+ "listen for connection on port (act as server)");
		System.out.println("-c, --connect host:port\t"
				+ "connect to host:port (act as client)");

		System.out.println();
		System.out.println("--socks5 host:port[:peerHost]");
		System.out.println("\t\tListen for connection (act as server) "
				+ "from behind NAT/firewall.");
		System.out.println("\t\tAsk SOCKS5 server at host:port "
				+ "to relay a connection from");
		System.out.println("\t\tpeerHost (which SHOULD NOT be omitted, "
				+ "defaults to 0.0.0.0).");
		System.out.println("\t\tThe newHost:newPort "
				+ "where the file-transfer client must connect");
		System.out.println("\t\twill be printed on the screen.");
		System.out.println();

		System.out.println("--no-prealloc\t\t"
				+ "don't preallocate files; valid only for receiver");
		System.out.println("\t\t\t"
				+ "(using this flag may increase fragmentation)");
		System.out.println("-r, --reverse-lookup\t"
				+ "lookup client's host name; valid only for server");
		System.out.println("-y, --yes\t\tgive default answer to all questions "
				+ "(non-interactive)");
	}

}
