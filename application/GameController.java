package application;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseButton;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import static application.Constants.*;

public class GameController implements Initializable{
	
	private GameNET gnet;
	private AnchorPane pLogin;
	private Canvas cvs;
	private GraphicsContext gc;
	
	private Image imgRedDot   = new Image(getClass().getResourceAsStream("red.png"));
	private Image imgGreenDot = new Image(getClass().getResourceAsStream("green.png"));
	
	@FXML
	AnchorPane acpMain;
		@FXML
		ScrollPane scpGame;
			@FXML
			Label lblOther;
			@FXML
			Pane pnlBoard;
			@FXML
			TextField tfUzenet;
			@FXML
			TextArea taSzoveg;
			@FXML
			Button btnStopGame;
	
	@FXML
	AnchorPane acpPlayers;
		@FXML
		ListView<String> lvPlayers;
	
	@FXML
	MenuItem itmLogin;
	@FXML
	MenuItem itmExit;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		cvs = new Canvas(400, 400);
		gc = cvs.getGraphicsContext2D();
		pnlBoard.getChildren().add(cvs);
		
		gnet = new GameNET();
		showGameArea(false);
		acpPlayers.setVisible(false);  //itt még a playerlist sem látszik
		
		lvPlayers.setOnMouseClicked(e -> {
			if ((e.getButton() == MouseButton.PRIMARY) && (e.getClickCount() == 2)) {
				String name = lvPlayers.getSelectionModel().getSelectedItem();
				if (name != null) {
					selectPlayer(name);
				}
			} 
		});
		
		itmLogin.setOnAction(e -> {
			showLogin();
		});
		
		itmExit.setOnAction(e -> {
			showExit();
		});
		
		// ez akkor fut, amikor ENTER-t ütnek a beviteli mezõn
		tfUzenet.setOnAction(e -> {
			gnet.sendChat(tfUzenet.getText());
		});
		
		btnStopGame.setOnAction(e -> {
			breakGame();
		});
		
		pnlBoard.setOnMouseClicked(e -> {
			int x, y;
			x = (int) (e.getX() / CSIZE);
			y = (int) (e.getY() / CSIZE);		
			gnet.stepTo(x, y);
		});
	
	}
	
	private void showGameArea(boolean visible) {
		scpGame.setVisible(visible);
		acpPlayers.setVisible(!visible);
	}
	
	private void selectPlayer(String name) {
		gnet.playWith(name);
	}
	
	private void showLogin() {
		try {
			Stage s = new Stage();
			FXMLLoader loader = new FXMLLoader(GameController.class.getResource("Login.fxml")); 
			pLogin = (AnchorPane) loader.load();
			LoginController lcont = loader.getController();
			lcont.setGameNET(gnet);
			s.setScene(new Scene(pLogin));
			s.setTitle("Gomoku login");
			s.initModality(Modality.APPLICATION_MODAL);
			s.showAndWait();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	private void showExit() {
		Alert a = new Alert(AlertType.CONFIRMATION);
		a.setTitle("Gomoku");
		a.setHeaderText("Kilépés a programból");
		a.setContentText("Biztos, hogy kilép?");
		Optional<ButtonType> res = a.showAndWait();
		if (res.get() == ButtonType.OK) {
			System.exit(0);
		}
	}
	
	private void resetBoard() {
		gc.setFill(Color.WHITE);
		gc.fillRect(0, 0, 400, 400);
		gc.setStroke(Color.GRAY);
		gc.setLineWidth(0.5);
		for (int i = 0; i <= BSIZE; i++) { //azért <= mert N mezõhöz N + 1 vonal kell
			gc.strokeLine(0, i * CSIZE, CSIZE * CSIZE, i * CSIZE);  //CSIZE a cellaméret
			gc.strokeLine(i * CSIZE, 0, i * CSIZE, CSIZE * CSIZE);
		}
		taSzoveg.clear();
		tfUzenet.clear();
		gnet.startGame();
	}
	
	private void updatePlayerList(String[] msg) {
		lvPlayers.getItems().clear();
		//a 0. nem kell, az csak a kód
		for (int i = 1; i < msg.length; i++) {
			lvPlayers.getItems().add(msg[i]);
		}
	}
	
	private void showPlayers() {
		Stage s = (Stage) acpMain.getScene().getWindow();
		s.setTitle("Gomoku - belépve: " + gnet.getUsername());
		acpPlayers.setVisible(true);
		itmLogin.setDisable(true);
	}
	
	private void showRefuseName(String name) {
		showError(name, "A név nem engedélyezett");
	}

	private void startGame(String name) {
		resetBoard();
		lblOther.setText("");
		showGameArea(true);
		gnet.setInPlay(true);
	}
	
	private void declineGame(String name) {
		showError(name, "Most nem akar játszani");
	}
	
	private void showError(String title, String text) {
		Alert a = new Alert(AlertType.ERROR);
		a.setTitle("Gomoku");
		a.setHeaderText(title);
		a.setContentText(text);
		a.showAndWait();
	}
	
	private void wantPlay(String name) {
		Alert a = new Alert(AlertType.CONFIRMATION);
		a.setTitle("Gomoku");
		a.setHeaderText(name + " meghívott játszani.");
		a.setContentText("Elfogadod?");
		Optional<ButtonType> res = a.showAndWait();
		if (res.get() == ButtonType.OK) {
			startGame(name);
		}
		if (res.get() == ButtonType.CANCEL) {
			gnet.declinePlay(name);
		}
	}
	
	private void stopGame() {
		gnet.stopGame();
		showGameArea(false);
	}
	
	private void breakGame() {
		Alert a = new Alert(AlertType.CONFIRMATION);
		a.setTitle("Gomoku");
		a.setHeaderText("Játék befejezése");
		a.setContentText("Biztos, hogy befejezed?");
		Optional<ButtonType> res = a.showAndWait();
		if (res.get() == ButtonType.OK) {
			stopGame();
		}
	}
	
	private void showBreakAndStop(String name){
		Alert a = new Alert(AlertType.INFORMATION);
		a.setTitle("Gomoku");
		a.setHeaderText(name + " megszakította a játékot");
		a.showAndWait();
		stopGame();
	}
	
	private void showStep(String name, int x, int y) {
		Image img;
		if (name.equals(gnet.getUsername())) {     //ha én léptem
			img = imgRedDot;
			lblOther.setTextFill(Color.GREEN);
			lblOther.setText(gnet.getOtherName()); //az ellenfelet írja ki zöldben
		}
		else {
			lblOther.setTextFill(Color.RED);
			lblOther.setText(gnet.getUsername());  //a másik lépett -> én következem
			img = imgGreenDot;
		}
		gc.drawImage(img, x * CSIZE, y * CSIZE, CSIZE, CSIZE);
	}
	
	public void showWinner(String name) {
		Alert a = new Alert(AlertType.INFORMATION);
		a.setTitle("Gomoku");
		a.setHeaderText(name + " gyõzött");
		a.setContentText("Vége a játéknak");
		a.showAndWait();
		showGameArea(false);
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
									String message = gnet.getMessage();
									String[] valami = message.split(";");
									switch (valami[0]) {
									case CHAT:
										taSzoveg.appendText(valami[1] + ": " + valami[2] + "\n");
										break;
									case LIST:
										updatePlayerList(valami);
										break;
									case OK:
										showPlayers();
										gnet.sendChat("semmi...");
										break;
									case NOTOK:
										showRefuseName(valami[1]);
										break;
									case ACCEPT:
										startGame(valami[1]);
										break;
									case DECLINE:
										declineGame(valami[1]);
										break;
									case PLAY:
										wantPlay(valami[1]);
										break;
									case STEP:
										showStep(valami[1], Integer.parseInt(valami[2]), Integer.parseInt(valami[3]));
										break;
									case WIN:
										showWinner(valami[1]);
										break;
									case BREAK:
										showBreakAndStop(valami[1]);
										break;
									default:
										showError("Ismeretlen üzenet", message);
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
