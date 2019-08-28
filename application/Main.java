package application;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;


public class Main extends Application {
	
	private Stage stage;
	private AnchorPane pMain;
	
	@Override
	public void start(Stage stage) {
		
		try {
			
			this.stage = stage;
			this.stage.setTitle("Gomoku");
			FXMLLoader loader = new FXMLLoader(Main.class.getResource("GameWindow.fxml"));
			pMain = (AnchorPane) loader.load();
			GameController gcont = loader.getController();
			gcont.go();
			Scene scene = new Scene(pMain);
			stage.setScene(scene);
			stage.setOnCloseRequest(e -> {
				System.exit(0);
			});
			stage.show();
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
