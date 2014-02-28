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

import static ft.Util.ascii;
import static ft.Util.readASCIILine;
import static ft.Util.transferBytes;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

class Sender {

	static void run(InputStream sockIn, OutputStream sockOut,
			List<String> fileArgs) throws IOException, BadASCIIException {
		List<FileInfo> fileInfos = getFileInfos(fileArgs);

		if (!announceFiles(sockIn, sockOut, fileInfos))
			return;

		sendFiles(sockIn, sockOut, fileInfos);
	}

	private static List<FileInfo> getFileInfos(List<String> fileArgs) {
		List<FileInfo> fileInfos = new ArrayList<FileInfo>(fileArgs.size());

		for (String fileArg : fileArgs) {
			File file = new File(fileArg);
			String name = file.getName();
			long size = file.length();

			fileInfos.add(new FileInfo(fileArg, name, size));
		}

		return fileInfos;
	}

	private static boolean announceFiles(InputStream sockIn,
			OutputStream sockOut, List<FileInfo> fileInfos) throws IOException,
			BadASCIIException {
		if (fileInfos.size() > 100) {
			System.err.println("Protocol can't transfer more than 100 files.");
			return false;
		}

		// start writing to socket

		String msg = "FILE COUNT " + fileInfos.size() + "\n";
		sockOut.write(msg.getBytes(ascii));

		for (int i = 0; i < fileInfos.size(); i++) {
			StringBuilder sb = new StringBuilder();
			sb.append(i + 1);
			sb.append("#\n");

			FileInfo crt = fileInfos.get(i);
			sb.append(crt.name);
			sb.append('\n');
			sb.append(crt.size);
			sb.append('\n');

			sockOut.write(sb.toString().getBytes(ascii));
		}

		// flush socket
		sockOut.flush();

		String expected = "SEND FILES\n";
		String got = readASCIILine(sockIn, expected.getBytes(ascii).length);
		if (!expected.equals(got)) {
			System.err.println("Receiver didn't show interest in files.");
			return false;
		}

		return true;
	}

	private static void sendFiles(InputStream sockIn, OutputStream sockOut,
			List<FileInfo> fileInfos) throws IOException, BadASCIIException {
		for (int i = 0; i < fileInfos.size(); i++) {
			String offerMsg = "OFFER " + (i + 1) + "\n";
			sockOut.write(offerMsg.getBytes(ascii));
			sockOut.flush();

			String yesMsg = "ACCEPT " + (i + 1) + "\n";
			String noMsg = "SKIP " + (i + 1) + "\n";
			int yesBytes = yesMsg.getBytes(ascii).length;
			int noBytes = noMsg.getBytes(ascii).length;
			String got = readASCIILine(sockIn, Math.max(yesBytes, noBytes));

			if (noMsg.equals(got)) {
				System.out.println(String.format("%-41.41s SKIPPED", fileInfos
						.get(i).name));
				continue;
			} else if (yesMsg.equals(got)) {
				boolean success = sendFileContents(sockOut, fileInfos.get(i));
				if (!success)
					return;

				String expected = "COMPLETED " + (i + 1) + "\n";
				got = readASCIILine(sockIn, expected.getBytes(ascii).length);
				if (!expected.equals(got)) {
					System.err.println("receiver didn't confirm completion");
					return;
				}
			} else {
				System.err.println("Invalid response to offer");
				return;
			}
		}
	}

	private static boolean sendFileContents(OutputStream sockOut,
			FileInfo fileInfo) throws IOException {
		InputStream fileIn = null;
		try {
			fileIn = new BufferedInputStream(new FileInputStream(
					fileInfo.cmdLineArg));

			long bytesSent = transferBytes(fileIn, sockOut, fileInfo.size,
					fileInfo.name);
			sockOut.flush();

			if (bytesSent != fileInfo.size) {
				System.err.println("Problem detected: file has " + bytesSent
						+ " bytes instead of " + fileInfo.size);
				return false;
			}

			return true;
		} finally {
			if (fileIn != null)
				fileIn.close();
		}
	}

}
