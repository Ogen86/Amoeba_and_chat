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
	String[][] steps = new String[BSIZE][BSIZE];
	
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
		for (int i = 0; i <= BMAX; i++) {
			for (int j = 0; j <= BMAX; j++) {
				steps[i][j] = "";
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
			int x = Integer.parseInt(tmp[2]);
			int y = Integer.parseInt(tmp[3]);
			steps[x][y] = tmp[1];                //az x,y -dik elem a l�p� neve
			if (tmp[1] == username) {            //ha saj�t l�p�s volt, akkor ellen�rizni kell a gy�zelmet
				checkWinner(x, y);				 //ha saj�t gy�zelem, akkor elk�ldi a szervernek
			}
			meNext = !tmp[1].equals(username);   //ha a l�p� neve nem egyenl� a saj�t n�vvel, akkor az
		}										 //ellenf�l l�pett, teh�t �n j�v�k	
	}
	
	private void checkWinner(int x, int y) {
		if (horizFive(x, y) || vertFive(x, y) || upDownFive(x, y) || downUpFive(x, y)) {
			sendToServer(WIN + ";" + username); 
		}
	}
	
	//sor ellen�rz�se
	private boolean horizFive(int x, int y) {
		int ctr = 0;
		for (int i = -4; i <= 4; i++) {
			if (((x + i) >= 0) && ((x + i) <= BMAX)) {    //ha a p�ly�n van a vizsg�lt mez� 
				if (steps[x + i][y].equals(username)) {   //ha saj�t l�p�s
					ctr += 1;                             //akkor eggyel t�bb saj�t mez� van abban a sorban
					if (ctr == 5) {
						return true;                      //ha tal�lt 5-�t, akkor le�ll
					}
				}	
				else {                                    //ha nem saj�t l�p�s (vagy �res), akkor null�zni kell a sz�ml�l�t
					ctr = 0;
					if (i >= 0) {                         //a most vizsg�lt mez� nem saj�t, teh�t 
						return false;                     //m�r csak max 4 foglalt lehet, nem �rdemes tov�bb sz�molni
					}
				}
			}
		}
		return false;
	}
	
	//oszlop ellen�rz�se
	private boolean vertFive(int x, int y) {
		int ctr = 0;
		for (int i = -4; i <= 4; i++) {
			if (((y + i) >= 0) && ((y + i) <= BMAX)) {    //ha a p�ly�n van a vizsg�lt mez� 
				if (steps[x][y + i].equals(username)) {   //ha saj�t l�p�s
					ctr += 1;                             //akkor eggyel t�bb saj�t mez� van abban a sorban
					if (ctr == 5) {
						return true;                      //ha tal�lt 5-�t, akkor le�ll
					}
				}	
				else {                                    //ha nem saj�t l�p�s (vagy �res), akkor null�zni kell a sz�ml�l�t
					ctr = 0;
					if (i >= 0) {                         //a most vizsg�lt mez� nem saj�t, teh�t 
						return false;                     //m�r csak max 4 foglalt lehet, nem �rdemes tov�bb sz�molni
					}
				}
			}
		}
		return false;  
	}
	
	// '\' �tl� ellen�rz�se
	private boolean upDownFive(int x, int y) {
		int ctr = 0;
		for (int i = -4; i <= 4; i++) {
			if (  ((y + i) >= 0) && ((y + i) <= BMAX) && ((x + i) >= 0) && ((x + i) <= BMAX)   ) {      //ha a p�ly�n van a vizsg�lt mez� 
				if (steps[x + i][y + i].equals(username)) { //ha saj�t l�p�s
					ctr += 1;                               //akkor eggyel t�bb saj�t mez� van abban a sorban
					if (ctr == 5) {
						return true;                        //ha tal�lt 5-�t, akkor le�ll
					}
				}	
				else {                                      //ha nem saj�t l�p�s (vagy �res), akkor null�zni kell a sz�ml�l�t
					ctr = 0;
					if (i >= 0) {                           //ha most vizsg�lt mez� nem saj�t, akkor 
						return false;                       //m�r csak max 4 foglalt lehet, nem �rdemes tov�bb sz�molni
					}
				}
			}
		}
		return false;  
	}
	
	// '/' �tl� ellen�rz�se
	private boolean downUpFive(int x, int y) {
		int ctr = 0;
		for (int i = -4; i <= 4; i++) {
			if ( ((x + i) >= 0) && ((x + i) <= BMAX) &&((y - i) >= 0) && ((y - i) <= BMAX) ) { //ha a p�ly�n van a vizsg�lt mez� 
				if (steps[x + i][y - i].equals(username)) { //ha saj�t l�p�s
					ctr += 1;                               //akkor eggyel t�bb saj�t mez� van abban a sorban
					if (ctr == 5) {
						return true;                        //ha tal�lt 5-�t, akkor le�ll
					}
				}	
				else {                                      //ha nem saj�t l�p�s (vagy �res), akkor null�zni kell a sz�ml�l�t
					ctr = 0;
					if (i >= 0) {                           //ha most vizsg�lt mez� nem saj�t, akkor 
						return false;                       //m�r csak max 4 foglalt lehet, nem �rdemes tov�bb sz�molni
					}
				}
			}
		}
		return false;  
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
		if ((meNext) && (steps[x][y].equals("") )) {  //ha �n l�pek �s �res a mez�, csak akkor k�ldi el a l�p�st
			sendToServer(STEP + ";" + username + ";" +  x.toString() + y.toString());
		}
	}
	
	public void sendChat(String msg) {
		sendToServer(CHAT + username + ";" + msg);
	}
	
	//j�t�k abbahagy�sa
	public void breakGame() {
		sendToServer(BREAK);
	}
	
}
