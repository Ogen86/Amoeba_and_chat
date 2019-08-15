package application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import static application.Constants.*;

public class GameNET implements Runnable {
	
	private ArrayList<String> messages = new ArrayList<>();
	private Socket sock;
	private BufferedReader reader;
	private PrintWriter writer;
	private String username;
	private boolean meNext = true;
	boolean[][] steps = new boolean[20][20];
	
	public void connect() throws Exception {
		try {
			sock = new Socket(InetAddress.getLocalHost(), SOCKET);
			reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			writer = new PrintWriter(sock.getOutputStream(), true);
			Thread th = new Thread(this);
			th.setDaemon(true);
			th.start();
		} catch (IOException e) {
			throw new Exception("Csatlakoz�si hiba:\n" + e.getMessage());
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
	
	public boolean hasMessage() {
		return messages.size() > 0;
	}
	
	public GameNET() {
		clearSteps();
	}
	
	private void clearSteps() {
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j < 20; j++) {
				steps[i][j] = false;
			}
		}
		
	}

	public String getMessage() {
		String str = messages.get(messages.size() - 1);
		messages.remove(messages.size() - 1);
		return str;
	}
	
	private void storeMessage(String msg) {
		messages.add(0, msg);
		String[] tmp = msg.split(";");
		if (tmp[0]  == STEP) {
			meNext = !tmp[1].equals(username);  //ha a l�p� neve nem egyenl� a saj�t n�vvel, akkor az
		}										//ellenf�l l�pett, teh�t �n j�v�k	
	}
	
	private void sendToServer(String msg) {
		writer.println(msg);
		writer.flush();
	}
	
	
	// bel�p�s a szerverre a megadot n�vvel (a kapcsolat m�r megvan)
	
	public void logIn(String name) {
		this.username = name;
		sendToServer(JOIN + ";" + username);
	}
	
	//megh�v�s j�t�kra: name = akit megh�vtak
	public void playWith(String name) {
		sendToServer(PLAY + ";" + username);
	}
	
	//megh�v�s elfogad�sa
	public void acceptPlay(String name) {
		sendToServer(ACCEPT + ";" + username);
	}
	
	//megh�v�s elutas�t�sa
	public void declinePlay(String name) {
		sendToServer(DECLINE + ";" + username);
	}
	
	//l�p�s
	public void stepTo(Integer x, Integer y) {
		if ((meNext) && (steps[x][y] == false)) {  //ha �n l�pek �s nem foglalt a mez�, akkor k�ldi el a l�p�st
			sendToServer(STEP + ";" + x.toString() + y.toString());
		}
	}
	
	public void registerStep(int x, int y) {
		steps[x][y] = true;
	}
	
	public void sendChat(String msg) {
		sendToServer(CHAT + username + ";" + msg);
	}
	
	//j�t�k abbahagy�sa
	public void breakGame() {
		sendToServer(BREAK);
	}
	
}
