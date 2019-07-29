package application;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class GameController implements Initializable{
	
	private GameNET gnet;
	
	@FXML
	Button btnGomb;
	
	@FXML
	TextArea taSzoveg;
	
	@FXML
	TextField tfUzenet;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// ez akkor fut, amikor megnyomj�k a gombot
		btnGomb.setOnAction(e -> {
			sendChatMessage();
		});
		
		// ez akkor fut, amikor ENTER-t �tnek a beviteli mez�n
		tfUzenet.setOnAction(e -> {
			sendChatMessage();
		});
		
	}
	
	/**********************************************
	 *  Ez az elj�r�s k�ldi el a chat �zenetet 
	 *  *******************************************/
	private void sendChatMessage() {
		synchronized (gnet) {
			gnet.setMessage(tfUzenet.getText());
		}
		tfUzenet.clear();
	}
	
	public void setGnet(GameNET gnet) {
		this.gnet = gnet;
	}
	
	public void go() {
		Thread th = new Thread(new Runnable() {
			
			@Override
			public void run() {
				synchronized (gnet) {
					while (gnet.isDoit()) {
						try {
								gnet.wait();
							}
						catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						Platform.runLater(new Runnable() {
							
							@Override
							public void run() {
								/***********************************************************
								 * itt kell lekezelni a szervert�l kapott �zenetet
								 * �s kirajzolni a k�r�ket �s X-eket
								 * meg ki�rni a chat sz�vegeket
								 * *************************************************************/
								taSzoveg.appendText(gnet.getMessage() + "\n"); // ehelyett kell majd az, hogy
								/*************************************************************************
								String[] valami = gnet.getMessage().split(";");
								switch (valami[0]) {
								case "C":
									ez egy chat �zenet �s ki kell �rni
									break;
									... stb. stb .stb.
								default:
									break;
								}
								*****************************************************************************/
							}
						});
					}  //while (gnet.isDoit())
					System.out.println("v�ge a j�t�knak!!!");
				}  //synchronized
			}  //run()
		}); //Thread-Runnable
		th.setDaemon(true);
		th.start();

	}

}
