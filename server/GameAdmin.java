package server;

import static application.Constants.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class GameAdmin {
	private static Map<String, State> states = new HashMap<String, State>();
	
	private class State {
		public State(Socket sock) {
			super();
			this.socket = sock;
		}
		private Socket socket;
		private Socket partnersocket;
		private String partnername;
		
		public void delPartner() {
			partnersocket = null;
			partnername = null;
		}
		public void setPartnername(String partnername) {
			this.partnername = partnername;
		}
		
		public String getPartnerName() {
			return partnername;
		}
		public Socket getSocket() {
			return socket;
		}
		public boolean notPlaying(String name) {
			return states.get(name).getPartnerSocket() == null; //ha van partner, akkor játszk
					
		}
		public Socket getPartnerSocket() {
			return partnersocket;
		}
		public void setPartnerSocket(Socket socket) {
			partnersocket = socket;
		}
		
	}
	
	public void delPartner(String name) {
		states.get(name).delPartner();
	}
	
	public String getPartnerName(String name) {
		return states.get(name).getPartnerName();
	}
	
	public void setPartner(String name, String partner) {
		State othergamer = states.get(name);
		othergamer.setPartnername(partner);
		othergamer.setPartnerSocket(states.get(partner).getSocket());
	}
	
	public void addGamer(String name, Socket socket) {
		State st = new State(socket);
		states.put(name, st);
	}
	
	public void deleteGamer(String name) {
		states.remove(name);
	}
	
	public Socket getSocket(String name) {
		return states.get(name).getSocket();
	}
	
	public Socket getPartnerSocket(String name) {
		return states.get(name).getPartnerSocket();
	}
	
	public void setPartnerSocket(String name, Socket socket) {
		states.get(name).setPartnerSocket(socket);
	}
	
	public boolean notPlaying(String name) {
		return states.get(name).notPlaying(name);
	}
	
	public String getGamerList() {
		String tmp = "";
		for (Map.Entry<String, State> gamer : states.entrySet()) {
			if (!tmp.equals("")) {
				tmp += ";";
			}
			tmp += gamer.getKey();
		}
		return tmp;
	}
	
	public void sendGamerList(){
		PrintWriter pw;
		String list = getGamerList();
		for (Map.Entry<String, State> state : states.entrySet()) {
			try {
				pw = new PrintWriter(state.getValue().getSocket().getOutputStream());
				pw.println(LIST + ";" + list);
				pw.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
