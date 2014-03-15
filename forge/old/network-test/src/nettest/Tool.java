package nettest;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import nettest.wrap.ConnectionEventListener;
import nettest.wrap.ConnectionManager;

public class Tool {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	static JPanel listenPanel, connectPanel;
	static JTextField listenPortTxt, remoteHostTxt, remotePortTxt;
	static JButton listenBtn, connectBtn;
	static JTextArea readTA;

	static ConnectionManager connMgr;

	private static void createAndShowGUI() {
		JFrame mainFrame = new JFrame("Nettest Tool");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Container frameContentPane = mainFrame.getContentPane();
		frameContentPane.setLayout(new GridBagLayout());

		GridBagConstraints c;

		makeListenPanel();
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.LINE_START;
		frameContentPane.add(listenPanel, c);

		makeConnectPanel();
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.LINE_START;
		frameContentPane.add(connectPanel, c);

		readTA = new JTextArea();
		readTA.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(readTA);
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		frameContentPane.add(scrollPane, c);

		listenBtn.setActionCommand("listen");
		connectBtn.setActionCommand("connect");

		ActionListener actListener = getActionListener(getConnEventListener());
		listenBtn.addActionListener(actListener);
		connectBtn.addActionListener(actListener);

		mainFrame.pack();
		mainFrame.setVisible(true);
	}

	private static ConnectionEventListener getConnEventListener() {
		return new ConnectionEventListener() {

			@Override
			public void writeException(IOException e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void readException(IOException e) {
				readTA.append("Read Exception: " + e + "\n");
			}

			@Override
			public void readEOF() {
				readTA.append("END OF FILE reached on input stream\n");
			}

			@Override
			public void dataWritten(byte[] barr) {
				// TODO Auto-generated method stub

			}

			@Override
			public void dataRead(final byte[] barr) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						try {
							readTA.append("Received: "
									+ new String(barr, "ascii"));
							readTA.append("\n");
						} catch (UnsupportedEncodingException e) {
							readTA.append("Unsupported Encoding: " + e);
						}

					}
				});
			}
		};
	}

	private static ActionListener getActionListener(
			final ConnectionEventListener listener) {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				if (ae.getActionCommand().equals("listen")) {
					final String portText = listenPortTxt.getText();
					new Thread(new Runnable() {

						@Override
						public void run() {
							try {
								int port = Integer.parseInt(portText);
								readTA.append("Creating server socket..\n");
								ServerSocket servSock = new ServerSocket(port);
								readTA.append("bound and listening on port "
										+ servSock.getLocalPort() + "\n");
								final Socket sock = servSock.accept();
								readTA.append("connection accepted from "
										+ sock + "\n");

								SwingUtilities.invokeLater(new Runnable() {
									@Override
									public void run() {
										// set connMgr from the GUI Thread
										// all future threads (e.g. Reader,
										// Writer)
										// will be started by the GUI Thread
										// creating a `happens-before-relation'
										// between setting connMgr
										// and ALL threads that will exist;
										// connMgr will NOT change again
										connMgr = new ConnectionManager(sock,
												listener);
										try {
											connMgr.startReader();
										} catch (IOException e) {
											readTA.append("Error: " + e);
										}
									}
								});
							} catch (NumberFormatException e) {
								readTA.append("Invalid listen port \""
										+ portText + "\", " + e + "\n");
							} catch (IOException e) {
								readTA.append("Error: " + e);
							}
						}
					}).start();
				} else if (ae.getActionCommand().equals("connect")) {
					System.err.println("Not implemented: connect");
				} else {
					System.err.println("Unknown action command: "
							+ ae.getActionCommand());
				}
			}
		};
	}

	private static void makeListenPanel() {
		listenPanel = new JPanel();
		listenPanel.setLayout(new GridBagLayout());

		GridBagConstraints c;

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		listenPanel.add(new JLabel("Listen on port:"), c);

		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		listenPortTxt = new JTextField(5);
		listenPanel.add(listenPortTxt, c);

		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 0;
		listenBtn = new JButton("Listen");
		listenPanel.add(listenBtn, c);
	}

	private static void makeConnectPanel() {
		connectPanel = new JPanel();
		connectPanel.setLayout(new GridBagLayout());

		GridBagConstraints c;

		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		connectPanel.add(new JLabel("Connect to host:"), c);

		c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = 0;
		c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		remoteHostTxt = new JTextField(10);
		connectPanel.add(remoteHostTxt, c);

		c = new GridBagConstraints();
		c.gridx = 2;
		c.gridy = 0;
		connectPanel.add(new JLabel("port:"), c);

		c = new GridBagConstraints();
		c.gridx = 3;
		c.gridy = 0;
		remotePortTxt = new JTextField(5);
		connectPanel.add(remotePortTxt, c);

		c = new GridBagConstraints();
		c.gridx = 4;
		c.gridy = 0;
		connectBtn = new JButton("Connect");
		connectPanel.add(connectBtn, c);

		connectBtn.setActionCommand("abc");
	}

}
