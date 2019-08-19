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
			steps[x][y] = tmp[1];                //az x,y -dik elem a lépõ neve
			if (tmp[1] == username) {            //ha saját lépés volt, akkor ellenõrizni kell a gyõzelmet
				checkWinner(x, y);				 //ha saját gyõzelem, akkor elküldi a szervernek
			}
			meNext = !tmp[1].equals(username);   //ha a lépõ neve nem egyenlõ a saját névvel, akkor az
		}										 //ellenfél lépett, tehát én jövök	
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
				if (steps[x + i][y].equals(username)) {   //ha saját lépés
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
				if (steps[x][y + i].equals(username)) {   //ha saját lépés
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
				if (steps[x + i][y + i].equals(username)) { //ha saját lépés
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
				if (steps[x + i][y - i].equals(username)) { //ha saját lépés
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
	
	
	// belépés a szerverre a megadot névvel (a kapcsolat már megvan)
	
	public void logIn(String name) {
		this.username = name;
		sendToServer(JOIN + ";" + username);
	}
	
	//meghívás játékra: name = akit meghívtak
	public void playWith(String name) {
		sendToServer(PLAY + ";" + username);
	}
	
	//meghívás elfogadása
	public void acceptPlay(String name) {
		sendToServer(ACCEPT + ";" + username);
	}
	
	//meghívás elutasítása
	public void declinePlay(String name) {
		sendToServer(DECLINE + ";" + username);
	}
	
	//lépés
	public void stepTo(Integer x, Integer y) {
		if ((meNext) && (steps[x][y].equals("") )) {  //ha én lépek és üres a mezõ, csak akkor küldi el a lépést
			sendToServer(STEP + ";" + username + ";" +  x.toString() + y.toString());
		}
	}
	
	public void sendChat(String msg) {
		sendToServer(CHAT + username + ";" + msg);
	}
	
	//játék abbahagyása
	public void breakGame() {
		sendToServer(BREAK);
	}
	
}
