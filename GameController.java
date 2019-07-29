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
		// ez akkor fut, amikor megnyomják a gombot
		btnGomb.setOnAction(e -> {
			sendChatMessage();
		});
		
		// ez akkor fut, amikor ENTER-t ütnek a beviteli mezõn
		tfUzenet.setOnAction(e -> {
			sendChatMessage();
		});
		
	}
	
	/**********************************************
	 *  Ez az eljárás küldi el a chat üzenetet 
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
								 * itt kell lekezelni a szervertõl kapott üzenetet
								 * és kirajzolni a köröket és X-eket
								 * meg kiírni a chat szövegeket
								 * *************************************************************/
								taSzoveg.appendText(gnet.getMessage() + "\n"); // ehelyett kell majd az, hogy
								/*************************************************************************
								String[] valami = gnet.getMessage().split(";");
								switch (valami[0]) {
								case "C":
									ez egy chat üzenet és ki kell írni
									break;
									... stb. stb .stb.
								default:
									break;
								}
								*****************************************************************************/
							}
						});
					}  //while (gnet.isDoit())
					System.out.println("vége a játéknak!!!");
				}  //synchronized
			}  //run()
		}); //Thread-Runnable
		th.setDaemon(true);
		th.start();

	}

}
