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

import static ft.Util.approxSize;
import static ft.Util.ascii;
import static ft.Util.readASCIILine;
import static ft.Util.scanner;
import static ft.Util.transferBytes;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

class Receiver {

	static void run(InputStream sockIn, OutputStream sockOut,
			boolean noPrealloc, boolean yes) throws IOException,
			BadASCIIException, NumberFormatException {

		int fileCount = getFileCount(sockIn);
		if (fileCount == -1)
			return;

		List<FileInfo> fileInfos = new ArrayList<FileInfo>(fileCount);
		for (int i = 0; i < fileCount; i++) {
			FileInfo crt = getFileInfo(sockIn, i);
			if (crt == null)
				return;
			fileInfos.add(crt);
		}

		System.out.println();
		printAllFiles(fileInfos);

		System.out.print("Receive all files? [Yes/select/no] ");
		String answer;
		if (yes) {
			answer = "";
			System.out.println("Yes");
		} else {
			answer = scanner.nextLine().trim().toUpperCase();
		}

		boolean select;
		if ("YES".startsWith(answer)) {
			select = false;
		} else if ("SELECT".startsWith(answer)) {
			select = true;
		} else {
			return;
		}

		sockOut.write("SEND FILES\n".getBytes(ascii));
		sockOut.flush();

		for (int i = 0; i < fileCount; i++) {
			boolean success = getFile(sockIn, sockOut, i, fileInfos.get(i),
					select, noPrealloc, fileInfos.size());
			if (!success)
				return;
		}
	}

	private static int getFileCount(InputStream sockIn) throws IOException,
			BadASCIIException {
		int maxBytes = "FILE COUNT 100\n".getBytes(ascii).length;
		String msg = readASCIILine(sockIn, maxBytes);
		if (!msg.endsWith("\n") || !msg.startsWith("FILE COUNT ")) {
			System.err.println("Sender didn't send valid FILE COUNT msg");
			return -1;
		}

		msg = msg.substring("FILE COUNT ".length(), msg.length() - 1);
		try {
			int count = Integer.valueOf(msg);
			if (count < 1 || count > 100) {
				System.err.println("Bad file count received: " + count
						+ ". Must be between 1 and 100.");
				return -1;
			}
			return count;
		} catch (NumberFormatException e) {
			System.err.println("Invalid file count received: " + msg);
			return -1;
		}
	}

	private static FileInfo getFileInfo(InputStream sockIn, int i)
			throws IOException, BadASCIIException {
		String expected = (i + 1) + "#\n";
		String got = readASCIILine(sockIn, expected.getBytes(ascii).length);
		if (!expected.equals(got)) {
			System.err.println("Expected file info not received");
			return null;
		}

		String name = readASCIILine(sockIn, 256);
		String sizeStr = readASCIILine(sockIn, 14);

		if (name.length() < 2 || sizeStr.length() < 2 || !name.endsWith("\n")
				|| !sizeStr.endsWith("\n")) {
			System.err.println("Expected file info not received");
			return null;
		}

		name = name.substring(0, name.length() - 1);
		sizeStr = sizeStr.substring(0, sizeStr.length() - 1);

		try {
			long size = Long.valueOf(sizeStr);
			if (size < 0) {
				System.err.println("Invalid file size (negative): " + size);
				return null;
			}
			return new FileInfo(null, name, size);
		} catch (NumberFormatException e) {
			System.err.println("Invalid file size: " + sizeStr);
			return null;
		}
	}

	private static void printAllFiles(List<FileInfo> fileInfos) {
		int width = 0;
		for (FileInfo fi : fileInfos)
			if (fi.name.length() > width)
				width = fi.name.length();
		width = Math.min(width, 70);
		String fmtStr = "%-" + width + "." + width + "s %9s";

		long totalSize = 0;
		for (FileInfo fi : fileInfos) {
			totalSize += fi.size;
			String size = approxSize(fi.size);
			System.out.println(String.format(fmtStr, fi.name, size));
		}

		System.out.println("Total: " + fileInfos.size() + " files, "
				+ approxSize(totalSize));
	}

	private static boolean getFile(InputStream sockIn, OutputStream sockOut,
			int i, FileInfo fileInfo, boolean select, boolean noPrealloc,
			int totalFiles) throws IOException, BadASCIIException {

		String expected = "OFFER " + (i + 1) + "\n";
		String got = readASCIILine(sockIn, expected.getBytes(ascii).length);
		if (!expected.equals(got)) {
			System.err.println("Expected file offer not received.");
			return false;
		}

		String localName = fileInfo.name;

		if (select) {
			System.out.println();
			System.out.println("Name: " + fileInfo.name);
			System.out.println("Size: " + approxSize(fileInfo.size));

			System.out.print("Download file (" + (i + 1) + " of " + totalFiles
					+ ")? [Yes/rename/no]");
			String answer = scanner.nextLine().trim().toUpperCase();
			if ("YES".startsWith(answer)) {
				// do nothing
			} else if ("RENAME".startsWith(answer)) {
				System.out.print("Save as (blank for original name): ");
				answer = scanner.nextLine();
				if (!answer.isEmpty())
					localName = answer;
			} else {
				sockOut.write(("SKIP " + (i + 1) + "\n").getBytes(ascii));
				sockOut.flush();
				return true;
			}
		}

		if (new File(localName).exists()) {
			System.err.println("error: " + localName + " exists");
			return false;
		}

		String tmpName = localName + ".ft!";

		RandomAccessFile randAccFile = null;
		OutputStream fileOut = null;
		try {
			if (noPrealloc) {
				fileOut = new FileOutputStream(localName);
			} else {
				randAccFile = new RandomAccessFile(tmpName, "rw");
				System.out.print(String.format("%-41.41s preallocating...",
						localName));
				randAccFile.setLength(fileInfo.size);
				System.out.print("done");
				fileOut = new FileOutputStream(randAccFile.getFD());
			}
			fileOut = new BufferedOutputStream(fileOut);

			sockOut.write(("ACCEPT " + (i + 1) + "\n").getBytes(ascii));
			sockOut.flush();

			long bytesReceived = transferBytes(sockIn, fileOut, fileInfo.size,
					localName);

			if (bytesReceived != fileInfo.size) {
				System.err.println("Error: sender only sent " + bytesReceived
						+ " bytes out of " + fileInfo.size);
				return false;
			}
		} finally {
			if (fileOut != null)
				fileOut.close();
			if (randAccFile != null)
				randAccFile.close();
		}

		if (!noPrealloc && !new File(tmpName).renameTo(new File(localName))) {
			System.err.println("Error: unable to rename " + tmpName + " to "
					+ localName + " (transfer to " + tmpName
					+ " completed successfully)");
		}

		sockOut.write(("COMPLETED " + (i + 1) + "\n").getBytes(ascii));
		sockOut.flush();

		return true;
	}

}
