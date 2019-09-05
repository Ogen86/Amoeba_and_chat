package server;

import static application.Constants.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class ServerMain {
	
	public static final String GAMERSFILENAME = "gamers.json";
	private static ServerSocket server;
	private static GamerList gamerlist;
	private static GameAdmin gamerstate;
	

	public static void main(String[] args) {
		gamerstate = new GameAdmin();
		readGamerData();
		try {
			server = new ServerSocket(SOCKET);
		} catch (IOException e) {
			System.out.print(" new ServerSocket: ");
			System.err.println(e.getMessage());
			System.exit(1);
		}
		
		Thread th = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (true) {
					try {
						Socket clientsocket = server.accept();
						new SocketThread(clientsocket, gamerlist, gamerstate);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				
			}
		});
		th.setDaemon(true);
		th.start();
		
		
		Scanner sc = new Scanner(System.in);
		String tmp = "";
		while (!tmp.equals("shutdown")) {
			if (sc.hasNext()) {
				tmp = sc.nextLine();
			}
		}
		sc.close();
	}
	
	private static void readGamerData() {
		try {
			Gson gs = new Gson();
			gamerlist = gs.fromJson(new FileReader(new File(GAMERSFILENAME)), GamerList.class);
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (JsonIOException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			if (gamerlist == null) {
				gamerlist = new GamerList();
			}
		}
		
	}
	
	public static void saveGamerData() {
		try {
			Gson gs = new Gson();
			FileWriter fw = new FileWriter(GAMERSFILENAME);
			gs.toJson(gamerlist, fw);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	


}
