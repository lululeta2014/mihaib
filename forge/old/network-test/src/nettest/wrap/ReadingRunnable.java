package nettest.wrap;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

class ReadingRunnable implements Runnable {

	private final InputStream in;
	private final ConnectionManager mgr;

	ReadingRunnable(InputStream in, ConnectionManager mgr) {
		this.mgr = mgr;
		this.in = in;
	}

	@Override
	public void run() {
		byte[] barr = new byte[1024];
		int len;
		try {
			while ((len = in.read(barr)) != -1) {
				mgr.dataRead(Arrays.copyOf(barr, len));
			}
			mgr.readEOF();
		} catch (IOException e) {
			mgr.readException(e);
		}
	}

}
