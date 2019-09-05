package server;

import java.util.HashMap;
import java.util.Map;

public class GamerList {

	private Map<String, Gamer> gamerlist;
	
	public GamerList() {
		super();
		gamerlist = new HashMap<String, Gamer>();
	}

	public void addWin(String name) {
		gamerlist.get(name).addWins();
		ServerMain.saveGamerData();
	}
	
	public int getWins(String name) {
		return gamerlist.get(name).getWins();
	}
	
	public void addGamer(String name, Gamer gamer) {
		gamerlist.put(name, gamer);
	}
	
	public boolean checkGamer(String name, String password) {
		Gamer gr = gamerlist.get(name);
		if (gr == null) {
			return false;
		}
		else {
			if (gr.getPassword().equals(password)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean noGamerName(String name) {  //REGISZTRÁLÁSHOZ KELL
		if (gamerlist.get(name) == null) {
			return true;
		}
		return false;    //ez csak azt ellenõrzi, hogy van-e ilyen név
	}
	
	public void registerGamer(Gamer gr) {
		gamerlist.put(gr.getName(), gr);
		ServerMain.saveGamerData();
	}

	@Override
	public String toString() {
		String names = "";
		for (Map.Entry<String, Gamer> entry: gamerlist.entrySet()) {
			names += entry.getKey() + " : " + entry.getValue().getPassword() + "\n";
		}
		return names;
	}
	
	

}
