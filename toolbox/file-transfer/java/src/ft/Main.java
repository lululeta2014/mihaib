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

import java.io.IOException;
import java.net.ServerSocket;

public class Main {

	public static void main(String[] args) {
		try {
			if (args.length == 0) {
				Args.printUsage();
				return;
			}

			Args params = Args.parse(args);

			if (params.f_help) {
				Args.printUsage();
				return;
			}

			if (params.listen_arg != null) {
				int port = Integer.valueOf(params.listen_arg);
				ServerSocket servSock = Server.getLocalServerSocket(port);
				Server.run(servSock, params.fileNames, params.f_noPrealloc,
						params.f_reverseLookup, params.f_yes);
			} else if (params.socks5_arg != null) {
				String[] items = params.socks5_arg.split(":");
				if (items.length < 2 || items.length > 4)
					throw new ArgsException("Invalid argument "
							+ "for --socks5 option");

				String host = items[0];
				int port = Integer.valueOf(items[1]);
				String peerHost = items.length > 2 ? items[2] : "0.0.0.0";
				int peerPort = items.length > 3 ? Integer.valueOf(items[3]) : 0;

				ServerSocket servSock = Server.getSocks5ServerSocket(host,
						port, peerHost, peerPort);
				Server.run(servSock, params.fileNames, params.f_noPrealloc,
						params.f_reverseLookup, params.f_yes);
			} else {
				String[] hostPort = params.connect_arg.split(":");
				if (hostPort.length != 2)
					throw new ArgsException("Invalid argument "
							+ "for --connect option");

				String host = hostPort[0];
				int port = Integer.valueOf(hostPort[1]);
				Client.run(host, port, params.fileNames, params.f_noPrealloc,
						params.f_yes);
			}
		} catch (ArgsException e) {
			System.err.println(e.getMessage());
			System.err.println("Run with --help to print usage");
			System.exit(1);
		} catch (NumberFormatException e) {
			System.err.println(e);
			System.exit(1);
		} catch (IOException e) {
			System.err.println(e);
			System.exit(1);
		} catch (BadASCIIException e) {
			System.err.println(e);
			System.exit(1);
		}
	}
}
