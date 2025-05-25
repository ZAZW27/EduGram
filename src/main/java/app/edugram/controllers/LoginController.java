package app.edugram.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

public class LoginController {
    @FXML
    private Label welcomeText;

    @FXML
    private ImageView image_login_bg;

    @FXML
    public void initialize() {
        Region parent = (Region) image_login_bg.getParent();

        image_login_bg.fitWidthProperty().bind(parent.widthProperty());
        image_login_bg.fitHeightProperty().bind(parent.heightProperty());
        image_login_bg.setPreserveRatio(false); // Biar selalu isi seluruh area
    }

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}