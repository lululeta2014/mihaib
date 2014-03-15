package nettest.wrap;

import java.io.IOException;

public interface ConnectionEventListener {

	void dataRead(byte[] barr);

	void readException(IOException e);

	void readEOF();

	void dataWritten(byte[] barr);

	void writeException(IOException e);

}
