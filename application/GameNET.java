package application;

import static application.Constants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class GameNET implements Runnable {
	
	private ArrayList<String> messages = new ArrayList<>();
	private Socket sock;
	private BufferedReader reader;
	private PrintWriter writer;
	private String username;
	private String othername;
	private boolean inplay = false;
	private boolean meNext = true;
	private boolean connected = false;
	int[][] steps = new int[BSIZE][BSIZE];
	
	public boolean isInPlay() {
		return inplay;
	}
	
	public void setInPlay(boolean in) {
		inplay = in;
		meNext = true;
	}
	
	public void connect() throws Exception {
		try {
			sock = new Socket(InetAddress.getLocalHost(), SOCKET);
			reader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			writer = new PrintWriter(sock.getOutputStream(), true);
			Thread th = new Thread(this);
			th.setDaemon(false);
			th.start();
			connected = true;
		} catch (IOException e) {
			throw new Exception("Csatlakoz�si hiba:\n" + e.getMessage());
		}
	}
	
	public boolean isConnected() {
		return connected;
	}

	@Override
	public void run() {
		boolean run = true;
		while (run) {
			try {
				String msg = reader.readLine();
				synchronized (this) {
					processMessage(msg);
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
		try {
			connect();
		} catch (Exception e) {
			Alert a = new Alert(AlertType.ERROR);
			a.setTitle("Gomoku");
			a.setHeaderText("Hiba a csatlakoz�sn�l");
			a.setContentText(e.getMessage());
			a.showAndWait();
			System.exit(1);
		}
	}
	
	public void startGame() {
		for (int i = 0; i <= BMAX; i++) {
			for (int j = 0; j <= BMAX; j++) {
				steps[i][j] = FREECELL;
			}
		}
	}
	
	public void stopGame() {
		sendToServer("B;" + username);
		inplay = false;
	}

	public String getMessage() {
		String str = messages.get(messages.size() - 1);
		messages.remove(messages.size() - 1);
		return str;
	}
	
	private boolean inPlayMessage(String msg) {
		return (msg.equals(CHAT) || msg.contentEquals(STEP) || msg.contentEquals(BREAK) || msg.contentEquals(WIN));
	}
	
	private void processMessage(String msg) {
		String[] tmp = msg.split(";");
		if (tmp[0].contentEquals(ACCEPT)) {  //elfogadta a j�t�kot
			othername = tmp[1];           //be�ll�tja az ellenf�l nev�t
		}
		
		if (tmp[0].equals(STEP)) {
			int x = Integer.parseInt(tmp[2]);
			int y = Integer.parseInt(tmp[3]);
			
			if (tmp[1].equals(username)) {       //ha �n l�ptem
				steps[x][y] = OWNSTEP;
				checkWinner(x, y);				 //ha saj�t gy�zelem, akkor elk�ldi a szervernek
			}
			else {
				steps[x][y] = OTHERSTEP;
			}
			
			meNext = (steps[x][y] == OTHERSTEP); //ha a m�sik l�pett, akkor �n j�v�k
		}										 //ellenf�l l�pett, teh�t �n j�v�k
		
		if (inplay) {                           //ha j�t�kban van: csak chat/l�p�s/break/win �zenetet kezel
			if (inPlayMessage(tmp[0])) {        //�s j�t�k k�zbeni �zenetet kap
				messages.add(0, msg);           //akkor elt�rolja
			}	
		}
		else {                                 //ha nincs j�t�kban, akkor minden m�st, csak chat/break/step-et nem
			if (!inPlayMessage(tmp[0])) {      //nem j�t�k k�zbeni �zenetet kap
				messages.add(0, msg);          //akkor t�rolja el
			}	
		}
	}
	
	public String getOtherName() {
		return othername;
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
				if (steps[x + i][y] == OWNSTEP) {         //ha saj�t l�p�s
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
				if (steps[x][y + i] == OWNSTEP) {   //ha saj�t l�p�s
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
				if (steps[x + i][y + i] == OWNSTEP) { //ha saj�t l�p�s
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
				if (steps[x + i][y - i] == OWNSTEP) { //ha saj�t l�p�s
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
	
	public String getUsername() {
		return username;
	}

	// bel�p�s a szerverre a megadot n�vvel (a kapcsolat m�r megvan)
	public void join(String name, String password) {
		sendToServer(JOIN + ";" + name + ";" + password);
		username = name;
	}
	
	//megh�v�s j�t�kra: name = akit megh�vtak
	public void playWith(String name) {
		sendToServer(PLAY + ";" + name);
	}
	
	//megh�v�s elfogad�sa
	public void acceptPlay(String name) {
		sendToServer(ACCEPT + ";" + name);
	}
	
	//megh�v�s elutas�t�sa
	public void declinePlay(String name) {
		sendToServer(DECLINE + ";" + name);
	}
	
	//l�p�s
	public void stepTo(Integer x, Integer y) {
		if ((meNext) && (steps[x][y] == FREECELL )) {  //ha �n l�pek �s �res a mez�, csak akkor k�ldi el a l�p�st
			sendToServer(STEP + ";" + username + ";" +  x.toString() + ";" + y.toString());
		}
	}
	
	public void sendChat(String msg) {
		sendToServer(CHAT + ";" + username + ";" + msg);
	}
	
	//j�t�k abbahagy�sa
	public void breakGame() {
		sendToServer(BREAK);
	}
	
	public void register(String name, String password) {
		sendToServer(REGISTER + ";" + name + ";" + password);
		username = name;
	}
	
}
