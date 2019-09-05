package application;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class LoginController implements Initializable {
	private GameNET gnet;
	
	@FXML
	AnchorPane apLogin;
	
	@FXML
	Button btnLogin;
	
	@FXML
	Button btnCancel;
	
	@FXML
	TextField tfUsername;
	
	@FXML
	TextField pfPassword;
	
	@FXML
	Button btnRegister;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		btnLogin.setOnAction(e -> {
			sendAndClose();
		});
		
		btnCancel.setOnAction(e -> {
			closeMe();
		});
		
		pfPassword.setOnAction(e -> {
			sendAndClose();
		});
		
		btnRegister.setOnAction(e -> {
			register();
		});
	}
	
	private void register() {
		try {
			gnet.register(tfUsername.getText(), pfPassword.getText());
		} finally {
			closeMe();
		}
	}
	
	private void sendAndClose() {
		try {
			gnet.join(tfUsername.getText(), pfPassword.getText());
		} finally {
			closeMe();
		}
	}
	
	private void closeMe() {
		Stage s = (Stage) apLogin.getScene().getWindow();
		s.close();
	}
	
	public void setGameNET(GameNET gnet) {
		this.gnet = gnet;
	}

}
