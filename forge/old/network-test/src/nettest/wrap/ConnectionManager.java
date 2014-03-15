package nettest.wrap;

import java.io.IOException;
import java.net.Socket;

public class ConnectionManager {

	private final Socket sock;
	private final ConnectionEventListener listener;

	public ConnectionManager(Socket sock, ConnectionEventListener listener) {
		this.sock = sock;
		this.listener = listener;
	}

	public synchronized void startReader() throws IOException {
		new Thread(new ReadingRunnable(sock.getInputStream(), this)).start();
	}

	/** Called by reader thread. */
	synchronized void dataRead(byte[] barr) {
		listener.dataRead(barr);
	}

	/** Called by writer thread. */
	synchronized void dataWritten(byte[] barr) {
		listener.dataWritten(barr);
	}

	synchronized void readException(IOException e) {
		listener.readException(e);
	}

	synchronized void writeException(IOException e) {
		listener.writeException(e);
	}

	synchronized void readEOF() {
		listener.readEOF();
	}

}
