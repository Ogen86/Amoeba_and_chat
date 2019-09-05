package server;

import static application.Constants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketThread {
	private PrintWriter pw;
	private BufferedReader br;
	private Socket mysocket;
	private GamerList gamerlist;
	private GameAdmin gameadmin;
	private String gamername;
	
	

	public SocketThread(Socket sock, GamerList gamers, GameAdmin gameadmin) {
		super();
		this.mysocket = sock;
		this.gamerlist = gamers;
		this.gameadmin = gameadmin;
		try {
			pw = new PrintWriter(sock.getOutputStream());
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Thread th = new Thread(new Runnable() {
			
			@Override
			public void run() {
				String tmp;
				String[] message;
				while (true) {
					try {
						tmp = br.readLine();
						message = tmp.split(";");
						switch (message[0]) {
						case REGISTER:
							registerGamer(message[1], message[2]);
							break;
						case JOIN:
							joinGamer(message[1], message[2]);
							break;
						case PLAY:
							sendInvitation(message [1]);
							break;
						case ACCEPT:
							acceptGame(message[1]);
							break;
						case CHAT:
							sendChat(message[1], message[2]);
							break;
						case STEP:
							sendStep(tmp);
							break;
						case WIN:
							sendWinAndClose(message[1]);
							break;
						case BREAK:
							breakGame(message[1]);
							break;
						default:
							break;
						}
					} catch (IOException e) {
						gameadmin.deleteGamer(gamername);
						gameadmin.sendGamerList();
						break;
					}
				}
				try {
					sock.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		th.setDaemon(true);
		th.start();
	}
	
	private void breakGame(String name) {
		sendMessage(BREAK + ";" + name, gameadmin.getPartnerSocket(gamername));
		gameadmin.delPartner(name);
		gameadmin.delPartner(gamername);
	}
	
	private void sendWinAndClose(String name) {
		gamerlist.addWin(name);
		sendMessage(WIN + ";" + name);
		sendMessage(WIN + ";" + name, gameadmin.getPartnerSocket(name));
		gameadmin.delPartner(gameadmin.getPartnerName(gamername));  //a partner elenfelét törli
		gameadmin.delPartner(gamername);                             //a saját ellenfelét törli
	}
	
	private void sendStep(String message) {
		sendMessage(message);                                         //ugyanazt küldi mind a kettõnek
		sendMessage(message, gameadmin.getPartnerSocket(gamername));
	}
	
	private void sendChat(String name, String msg) {
		String tmp = CHAT + ";" + name + ";" + msg; 
		sendMessage(tmp);                                         //a thread userének küldi
		sendMessage(tmp, gameadmin.getPartnerSocket(gamername)); //a thread partnerének küldi
	}
	
	private void acceptGame(String name) {
		synchronized (gameadmin) {
			if (gameadmin.notPlaying(name)) {          //ha az ellenfél nem játszik, akkor elfogadható a kérése
				gameadmin.setPartner(gamername, name);  //beállítja egymásnak a játékosokat
				gameadmin.setPartner(name, gamername);
				sendMessage(ACCEPT + ";" + name);                                   //az aktuális gamernek az ellenfél nevét küldi
				sendMessage(ACCEPT + ";" + gamername, gameadmin.getSocket(name));  //az ellenfélnek az aktuális gamer nevét
			}
		}
	}
	
	private void sendInvitation(String name) {
		sendMessage(PLAY + ";" + gamername, gameadmin.getSocket(name));
	}

	private void registerGamer(String name, String password) {
		synchronized (gamerlist) {
			if (gamerlist.noGamerName(name)) {                 //ha nincs ilyen nevû felhasználó, akkor lehet regisztrálni
				gamerlist.registerGamer(new Gamer(name, password));
				gameadmin.addGamer(name, mysocket);
				gamername = name;
				sendMessage(OK);
				gameadmin.sendGamerList();
			}
			else {                                             //ha van ilyen felhasználó, akkor NOTOK-visszaküldés
				sendMessage(NOTOK + ";A felhasználónév foglalt!");
			}
		}
	}
	
	private void joinGamer(String name, String password) {
		synchronized (gamerlist) {
			if (gamerlist.checkGamer(name, password)) {
				gameadmin.addGamer(name, mysocket);
				gamername = name;
				sendMessage(OK);
				gameadmin.sendGamerList();
			}
			else {
				sendMessage(NOTOK + ";Hibás név vagy jelszó!");
			}
		}
	}
	
	private void sendMessage(String msg) {
		pw.println(msg);
		pw.flush();
	}
	
	private void sendMessage(String msg, Socket socket) {
		PrintWriter pwr;
		try {
			pwr = new PrintWriter(socket.getOutputStream());
			pwr.println(msg);
			pwr.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Socket getSock() {
		return mysocket;
	}
}
