package application;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;


public class Main extends Application {
	
	private Stage stage;
	private AnchorPane pMain;
	
	private GameNET gnet;
	
	@Override
	public void start(Stage stage) {
		
		try {
			
			this.stage = stage;
			this.stage.setTitle("Gomoku");
			FXMLLoader loader = new FXMLLoader(Main.class.getResource("GameWindow.fxml"));
			pMain = (AnchorPane) loader.load();
			GameController gcont = loader.getController();
			gnet = new GameNET();
			gcont.setGnet(gnet);
			gcont.go();
			Scene scene = new Scene(pMain);
			stage.setScene(scene);
			stage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
