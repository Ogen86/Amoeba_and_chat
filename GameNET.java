package application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

public class GameNET implements Runnable {
	
	private final int SOCKET = 10001;
	private ArrayList<String> messages = new ArrayList<>();
	private Socket sock;
	private BufferedReader reader;
	private PrintWriter writer;
	private String username;
	
	
	public GameNET(String user) {
		this.username = user;
	}
	
	public void connect() throws Exception {
		try {
			sock = new Socket(InetAddress.getLocalHost(), SOCKET);
			reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			writer = new PrintWriter(sock.getOutputStream(), true);
			Thread th = new Thread(this);
			th.setDaemon(true);
			th.start();
		} catch (IOException e) {
			throw new Exception("Csatlakozási hiba:\n" + e.getMessage());
		}
	}

	@Override
	public void run() {
		boolean run = true;
		while (run) {
			try {
				String msg = reader.readLine();
				synchronized (this) {
					storeMessage(msg);
					this.notifyAll();
				}
			}
			catch (IOException e) {
				System.out.println(e.getMessage());
				run = false;
			}
		}
		closeConnections();
	}
	
	private void closeConnections() {
		try {
			writer.close();
			reader.close();
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public boolean hasNextMessage() {
		return messages.size() > 0;
	}
	
	public String getMessage() {
		String str = messages.get(messages.size() - 1);
		messages.remove(messages.size() - 1);
		return str;
	}
	
	private void storeMessage(String msg) {
		messages.add(0, msg);
	}
	
	private void sendToServer(String msg) {
		writer.println(msg);
		writer.flush();
	}
	
	public void sendChat(String msg) {
		sendToServer("C;" + username + ";" + msg);
	}
	
}
