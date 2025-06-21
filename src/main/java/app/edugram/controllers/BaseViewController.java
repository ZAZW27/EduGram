package app.edugram.controllers;

import app.edugram.controllers.Components.PostFrameController;
import app.edugram.controllers.Components.SidebarController;
import app.edugram.controllers.Components.TopbarController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class BaseViewController implements Initializable{

    // Pastikan di baseView.fxml, BorderPane root memiliki fx:id="mainBorderPane"
    @FXML
    private BorderPane mainBorderPane;

    // Pastikan di baseView.fxml, <fx:include> untuk sidebar memiliki fx:id="sidebar"
    // Nama field ini SANGAT PENTING: harus [fx:id] + "Controller".
    @FXML
    private SidebarController sidebarController;
    @FXML private TopbarController topbarController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        sidebarController.setNavigationHandler(pageName -> loadPage(pageName));
        if (sidebarController != null) {
            sidebarController.setMainBorderPane(mainBorderPane);
        }
        topbarController.setMainBorderPane(mainBorderPane);
        PostFrameController.setMainBorderPane(mainBorderPane);
        loadPage("beranda");
    }

    // Method untuk memuat dan mengganti konten di tengah BorderPane
    public void loadPage(String fxmlName) {
        try {
            sidebarController.updateSelectionState(fxmlName);

            URL fxmlUrl = getClass().getResource("/app/edugram/pages/" + fxmlName + ".fxml");
            if (fxmlUrl == null) {
                System.err.println("FATAL: Tidak bisa menemukan file FXML: " + fxmlName + ".fxml");
                return;
            }

            // Muat node FXML baru
            Node pageNode = FXMLLoader.load(fxmlUrl);

            // Set node baru ini sebagai konten di tengah BorderPane
            mainBorderPane.setCenter(pageNode);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
