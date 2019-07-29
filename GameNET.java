package application;

public class GameNET implements Runnable {
	
	private String message = null;
	private boolean doit = true;
	
	public GameNET() {
		Thread th = new Thread(this);
		th.setDaemon(true);
		th.start();
	}

	@Override
	public void run() {
		Integer counter = 0;
		while (true) {
			try {
				// itt fogja olvasgatni a szerver �zeneteit �s �rtes�t, ha tal�lt
				Thread.sleep(2000);
				counter++;
				message = counter.toString() + ". szerver�zenet";
				if (counter == 10) {
					doit = false;
				}
				synchronized (this) {
					this.notifyAll();
				}
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String str) {
		synchronized (this) {
			message = str;
			System.out.println(message);
			message = null;
		}
	}

	public boolean isDoit() {
		return doit;
	}

	
}
