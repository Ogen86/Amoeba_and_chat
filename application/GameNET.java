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
			throw new Exception("Csatlakozási hiba:\n" + e.getMessage());
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
			a.setHeaderText("Hiba a csatlakozásnál");
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
		if (tmp[0].contentEquals(ACCEPT)) {  //elfogadta a játékot
			othername = tmp[1];           //beállítja az ellenfél nevét
		}
		
		if (tmp[0].equals(STEP)) {
			int x = Integer.parseInt(tmp[2]);
			int y = Integer.parseInt(tmp[3]);
			
			if (tmp[1].equals(username)) {       //ha én léptem
				steps[x][y] = OWNSTEP;
				checkWinner(x, y);				 //ha saját gyõzelem, akkor elküldi a szervernek
			}
			else {
				steps[x][y] = OTHERSTEP;
			}
			
			meNext = (steps[x][y] == OTHERSTEP); //ha a másik lépett, akkor én jövök
		}										 //ellenfél lépett, tehát én jövök
		
		if (inplay) {                           //ha játékban van: csak chat/lépés/break/win üzenetet kezel
			if (inPlayMessage(tmp[0])) {        //és játék közbeni üzenetet kap
				messages.add(0, msg);           //akkor eltárolja
			}	
		}
		else {                                 //ha nincs játékban, akkor minden mást, csak chat/break/step-et nem
			if (!inPlayMessage(tmp[0])) {      //nem játék közbeni üzenetet kap
				messages.add(0, msg);          //akkor tárolja el
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
	
	//sor ellenõrzése
	private boolean horizFive(int x, int y) {
		int ctr = 0;
		for (int i = -4; i <= 4; i++) {
			if (((x + i) >= 0) && ((x + i) <= BMAX)) {    //ha a pályán van a vizsgált mezõ 
				if (steps[x + i][y] == OWNSTEP) {         //ha saját lépés
					ctr += 1;                             //akkor eggyel több saját mezõ van abban a sorban
					if (ctr == 5) {
						return true;                      //ha talált 5-öt, akkor leáll
					}
				}	
				else {                                    //ha nem saját lépés (vagy üres), akkor nullázni kell a számlálót
					ctr = 0;
					if (i >= 0) {                         //a most vizsgált mezõ nem saját, tehát 
						return false;                     //már csak max 4 foglalt lehet, nem érdemes tovább számolni
					}
				}
			}
		}
		return false;
	}
	
	//oszlop ellenõrzése
	private boolean vertFive(int x, int y) {
		int ctr = 0;
		for (int i = -4; i <= 4; i++) {
			if (((y + i) >= 0) && ((y + i) <= BMAX)) {    //ha a pályán van a vizsgált mezõ 
				if (steps[x][y + i] == OWNSTEP) {   //ha saját lépés
					ctr += 1;                             //akkor eggyel több saját mezõ van abban a sorban
					if (ctr == 5) {
						return true;                      //ha talált 5-öt, akkor leáll
					}
				}	
				else {                                    //ha nem saját lépés (vagy üres), akkor nullázni kell a számlálót
					ctr = 0;
					if (i >= 0) {                         //a most vizsgált mezõ nem saját, tehát 
						return false;                     //már csak max 4 foglalt lehet, nem érdemes tovább számolni
					}
				}
			}
		}
		return false;  
	}
	
	// '\' átló ellenõrzése
	private boolean upDownFive(int x, int y) {
		int ctr = 0;
		for (int i = -4; i <= 4; i++) {
			if (  ((y + i) >= 0) && ((y + i) <= BMAX) && ((x + i) >= 0) && ((x + i) <= BMAX)   ) {      //ha a pályán van a vizsgált mezõ 
				if (steps[x + i][y + i] == OWNSTEP) { //ha saját lépés
					ctr += 1;                               //akkor eggyel több saját mezõ van abban a sorban
					if (ctr == 5) {
						return true;                        //ha talált 5-öt, akkor leáll
					}
				}	
				else {                                      //ha nem saját lépés (vagy üres), akkor nullázni kell a számlálót
					ctr = 0;
					if (i >= 0) {                           //ha most vizsgált mezõ nem saját, akkor 
						return false;                       //már csak max 4 foglalt lehet, nem érdemes tovább számolni
					}
				}
			}
		}
		return false;  
	}
	
	// '/' átló ellenõrzése
	private boolean downUpFive(int x, int y) {
		int ctr = 0;
		for (int i = -4; i <= 4; i++) {
			if ( ((x + i) >= 0) && ((x + i) <= BMAX) &&((y - i) >= 0) && ((y - i) <= BMAX) ) { //ha a pályán van a vizsgált mezõ 
				if (steps[x + i][y - i] == OWNSTEP) { //ha saját lépés
					ctr += 1;                               //akkor eggyel több saját mezõ van abban a sorban
					if (ctr == 5) {
						return true;                        //ha talált 5-öt, akkor leáll
					}
				}	
				else {                                      //ha nem saját lépés (vagy üres), akkor nullázni kell a számlálót
					ctr = 0;
					if (i >= 0) {                           //ha most vizsgált mezõ nem saját, akkor 
						return false;                       //már csak max 4 foglalt lehet, nem érdemes tovább számolni
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

	// belépés a szerverre a megadot névvel (a kapcsolat már megvan)
	public void join(String name, String password) {
		sendToServer(JOIN + ";" + name + ";" + password);
		username = name;
	}
	
	//meghívás játékra: name = akit meghívtak
	public void playWith(String name) {
		sendToServer(PLAY + ";" + name);
	}
	
	//meghívás elfogadása
	public void acceptPlay(String name) {
		sendToServer(ACCEPT + ";" + name);
	}
	
	//meghívás elutasítása
	public void declinePlay(String name) {
		sendToServer(DECLINE + ";" + name);
	}
	
	//lépés
	public void stepTo(Integer x, Integer y) {
		if ((meNext) && (steps[x][y] == FREECELL )) {  //ha én lépek és üres a mezõ, csak akkor küldi el a lépést
			sendToServer(STEP + ";" + username + ";" +  x.toString() + ";" + y.toString());
		}
	}
	
	public void sendChat(String msg) {
		sendToServer(CHAT + ";" + username + ";" + msg);
	}
	
	//játék abbahagyása
	public void breakGame() {
		sendToServer(BREAK);
	}
	
	public void register(String name, String password) {
		sendToServer(REGISTER + ";" + name + ";" + password);
		username = name;
	}
	
}
