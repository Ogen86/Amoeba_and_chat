package application;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
	
	@FXML
	Label lblStep;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// ez akkor fut, amikor megnyomják a gombot
		btnGomb.setOnAction(e -> {
			try {
				gnet.connect();
			} catch (Exception exc) {
				lblStep.setText(exc.getMessage());
			}
		});
		
		// ez akkor fut, amikor ENTER-t ütnek a beviteli mezõn
		tfUzenet.setOnAction(e -> {
			gnet.sendChat(tfUzenet.getText());
		});
		
	}
	
	public void setGnet(GameNET gnet) {
		this.gnet = gnet;
	}
	
	public void go() {
		Thread th = new Thread(new Runnable() {
			
			@Override
			public void run() {
				synchronized (gnet) {
					while (true) {
						try {
								gnet.wait();
							}
						catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						Platform.runLater(new Runnable() {
							
							@Override
							public void run() {
								while (gnet.hasMessage()) {
									String[] valami = gnet.getMessage().split(";");
									switch (valami[0]) {
									case "C":
										taSzoveg.appendText(valami[1] + ": " + valami[2] + "\n"); // ehelyett kell majd az, hogy
										break;
									default:
										String tmp = "";
										for (String string : valami) {
											tmp += string;
										}
										lblStep.setText(tmp);
										break;
									}
								}
							}
						});
					}  //while (gnet.isDoit())
				}  //synchronized
			}  //run()
		}); //Thread-Runnable
		th.setDaemon(true);
		th.start();

	}

}
