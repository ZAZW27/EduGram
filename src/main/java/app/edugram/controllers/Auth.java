package app.edugram.controllers;

import app.edugram.utils.PageAction;
import app.edugram.utils.Notices;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import app.edugram.models.UserModel;

import java.io.IOException;


public class Auth {
    public Notices noticeModel = new Notices();

    @FXML
    private Button cancelButton;
    public TextField usernameInp;
    public TextField passInp;
    public TextField emailInp;
    public TextField namaInp;

    @FXML
    public void loginButtonAction(ActionEvent event) throws IOException {
        String username = usernameInp.getText();
        String password = passInp.getText();

        if(username.isEmpty() || password.isEmpty()){Notices.formNotFilled();return;}

        boolean isValid = UserModel.ValidateUser(username, password, true);
        if(isValid){
            System.out.println("Login successful");
        }
        else{
            System.out.println("Login failed");
        }
    }

    @FXML
    public void RegisterButtonAction(ActionEvent event) throws IOException {
        String username = usernameInp.getText();
        String password = passInp.getText();
        String nama = namaInp.getText();
        String email = emailInp.getText();

        if(username.isEmpty() || password.isEmpty() || nama.isEmpty() || email.isEmpty()){Notices.formNotFilled();return;}

        boolean isValid = UserModel.ValidateRegistration(username, password, nama, email);
        if(isValid){
            System.out.println("Seleamat anda telah terregister ahahhahahahahhahahahah");
            PageAction.switchPage(event, "login.fxml");
        }else{
            System.out.println("wow ga berhasil");
        }
    }

    @FXML
    public void gotoRegis(ActionEvent event){
        PageAction.switchPage(event, "signup.fxml");
    }

    @FXML
    public void gotoLogin(ActionEvent event){
        PageAction.switchPage(event, "login.fxml");
    }
}