package server;

public class Gamer {
	private String name;
	private String password;
	private int wins;
	
	public int getWins() {
		return wins;
	}

	public void addWins() {
		wins += 1;
	}

	public Gamer(String name, String password) {
		super();
		this.name = name;
		this.password = password;
		this.wins = 0;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "name: " + name + ", password: " + password;
	}
	
	
	
	
}
