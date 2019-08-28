package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import static application.Constants.*;

public class Server extends Thread {
	
	@Override
	public void run() {
		try {
			ServerSocket server = new ServerSocket(SOCKET);
			Scanner sc = new Scanner(System.in);
			Scanner scv = new Scanner(new File("src/application/message.txt"));
			
			Socket client = server.accept();
			PrintWriter pw = new PrintWriter(client.getOutputStream());
			
			BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
			
			String tmp = "";
			while (true) {
				tmp = br.readLine();
				System.out.println(tmp);
				if (tmp.substring(1,1).equals("B")) {
					break;
				}
				else {
					tmp = sc.nextLine();
					pw.println(tmp);
					pw.flush();
				}
			}
			pw.close();
			client.close();
			server.close();
			sc.close();
			scv.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static void main(String[] args) {
		Server s = new Server();
		s.start();

	}
	
	

}
