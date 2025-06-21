package app.edugram.controllers.Components;

import app.edugram.Main;
import app.edugram.controllers.ExploreController;
import app.edugram.models.PostModel;
import app.edugram.models.UserModel;
import app.edugram.utils.PageAction;
import app.edugram.utils.Sessions;
import app.edugram.utils.cookies.CookieUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.stage.Window;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.stage.Modality;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class SidebarController implements Initializable {

    @FXML private Button berandaButton;
    @FXML private Button exploreButton;
    @FXML private Button profileButton;
    @FXML private Button reportButton;
    @FXML private Button profileBtn2;

    @FXML private Line berandaLine;
    @FXML private Line exploreLine;
    @FXML private Line profileLine;
    @FXML private Line reportLine;

    // ... sisa field @FXML Anda ...
    @FXML private ImageView navProfilePicture;
    @FXML private ImageView navCreatePost;
    @FXML private Button navLogout;
    @FXML private ImageView navLike;
    @FXML private ImageView navBookmarks;

    @FXML private Label navFollowing;
    @FXML private Label navFollowers;
    @FXML private Label navPosts;

    @FXML private Label navUsername;

    // Interface untuk berkomunikasi dengan BaseViewController
    public interface NavigationHandler {
        void onNavigateToPage(String pageName);
    }

    private NavigationHandler navigationHandler;

    // Method ini akan dipanggil oleh BaseViewController
    public void setNavigationHandler(NavigationHandler handler) {
        this.navigationHandler = handler;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Set data pengguna awal
        setNavData();
        if (navCreatePost != null) {
            navCreatePost.setOnMouseClicked(this::onCreatePostClick); // Mengikat event klik ke metode
            System.out.println("Create Post ImageView action set in SidebarController.");
        } else {
            System.err.println("createPostImageView is null in SidebarController.initialize(). Check sidebar.fxml fx:id.");
        }
    }

    // Method ini dipanggil oleh BaseViewController untuk mengubah style tombol
    public void updateSelectionState(String selectedPage) {
        // Reset semua style
        berandaButton.getStyleClass().remove("activated_button");
        exploreButton.getStyleClass().remove("activated_button");
        profileButton.getStyleClass().remove("activated_button");
        reportButton.getStyleClass().remove("activated_button");

        berandaLine.setVisible(false);
        exploreLine.setVisible(false);
        profileLine.setVisible(false);
        reportLine.setVisible(false);

        // Terapkan style ke tombol yang benar
        switch (selectedPage.toLowerCase()) {
            case "explore":
                exploreButton.getStyleClass().add("activated_button");
                exploreLine.setVisible(true);
                break;
            case "profile":
                profileButton.getStyleClass().add("activated_button");
                profileLine.setVisible(true);
                break;
            case "report":
                reportButton.getStyleClass().add("activated_button");
                reportLine.setVisible(true);
                break;
            case "beranda":
            default:
                berandaButton.getStyleClass().add("activated_button");
                berandaLine.setVisible(true);
                break;
        }
    }

    // --- Event Handler ---
    @FXML
    public void onBerandaClick(ActionEvent event) {
        if (navigationHandler != null) {
            navigationHandler.onNavigateToPage("beranda");
        }
    }

    @FXML
    public void onExploreClick(ActionEvent event) {
        if (navigationHandler != null) {
            navigationHandler.onNavigateToPage("explore");
        }
    }

    @FXML
    public void onProfileClick(ActionEvent event) {
        if (navigationHandler != null) {
            navigationHandler.onNavigateToPage("profile");
        }
    }

    @FXML
    public void onReportClick(ActionEvent event) {
        if (navigationHandler != null) {
            navigationHandler.onNavigateToPage("report");
        }
    }



    @FXML
    public void onCreatePostClick(MouseEvent mouseEvent) {
        showCreatePostDialog();
    }

    @FXML
    public void onSettingsClick(MouseEvent mouseEvent) {
        if (navigationHandler != null) {
            navigationHandler.onNavigateToPage("setting");
        } }

    @FXML
    public void onProfileClick2(MouseEvent mouseEvent) {
        if (navigationHandler != null) {
            navigationHandler.onNavigateToPage("profile");
    } }

    @FXML
    public void onBookmarksClick(MouseEvent event) {
        if (navigationHandler != null) {
            switchExplorePage(event, "bookmark");
            System.out.println("Bookmark: clicked");
        }
    }

    @FXML
    public void onLikeClick(MouseEvent event) {
        if (navigationHandler != null) {
            switchExplorePage(event, "like");
            System.out.println("save: clicked");
        }
    }
    private BorderPane mainBorderPane;

    public void setMainBorderPane(BorderPane mainBorderPane) {
        this.mainBorderPane = mainBorderPane;
    }

    private void switchExplorePage(MouseEvent event, String pageName) {
        try {
            FXMLLoader loader = new FXMLLoader(PageAction.class.getResource("/app/edugram/pages/explore.fxml"));
            Parent root = loader.load();

            ExploreController explore = loader.getController();
            explore.setWhatPage(pageName);

            // Set the content to the center of BorderPane
            mainBorderPane.setCenter(root);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onLogoutClick(ActionEvent event) {
        if (navigationHandler != null) {
            CookieUtil.clearCookie();
            Sessions.clear();
            PageAction.switchPage(event, "login.fxml");
        }
    }


    private void showCreatePostDialog() {
        System.out.println("Attempting to show create post dialog from SidebarController...");
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("pages/components/popup_option/tambahpostpopup.fxml"));
            VBox createPostLayout = loader.load(); // Root dari FXML pop-up Anda

            TambahPostController controller = loader.getController();

            Stage dialogStage = new Stage(); // <--- Buat Stage baru untuk dialog
            dialogStage.setTitle("Buat Postingan Baru");
            dialogStage.initModality(Modality.APPLICATION_MODAL); // <--- Set modality agar dialog memblokir aplikasi
            dialogStage.setResizable(false); // Opsional: nonaktifkan resize

            dialogStage.setScene(new Scene(createPostLayout)); // Set Scene untuk Stage dialog

            // Dapatkan pemilik dialog (Stage utama aplikasi)
            Window ownerWindow = null;
            if (navCreatePost != null && navCreatePost.getScene() != null && navCreatePost.getScene().getWindow() != null) {
                ownerWindow = navCreatePost.getScene().getWindow();
            }
            // Karena Anda tidak ingin mengubah BaseViewController, kita tetap menggunakan cara ini.

            if (ownerWindow != null) {
                dialogStage.initOwner(ownerWindow); // <--- Set Stage utama sebagai pemilik dialog ini
                System.out.println("Dialog owner set to main application window.");
            } else {
                System.err.println("Owner window for dialog not found. Dialog might appear independently.");
            }

            controller.setDialogStage(dialogStage); // <--- Teruskan Stage dialog ini ke controllernya

            // Opsional: Posisikan dialog di tengah pemiliknya atau di tengah layar
            if (dialogStage.getOwner() != null) {
                // Untuk memposisikan di tengah owner, Anda mungkin perlu perhitungan X/Y yang lebih detail
                // Untuk kemudahan, kita bisa centerOnScreen() atau manual X/Y.
                dialogStage.centerOnScreen();
            }


            dialogStage.showAndWait(); // <--- Tampilkan dialog dan tunggu hingga ditutup

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load create post dialog: " + e.getMessage());
        }
    }

    private List<PostModel> allPostsToDisplay;
    // --- Method Helper ---
    private void setNavData() {
        if (navProfilePicture != null && Sessions.getProfilePicture() != null) {
            Map<String, String> user = UserModel.findUser(String.valueOf(Sessions.getUserId()));
            navFollowers.setText(user.get("follower"));
            navFollowing.setText(user.get("following"));
            try {
                Boolean please = Sessions.getLevel().equals("admin") ? true : false;
//                System.out.println(please);
                reportButton.setVisible(please);
                System.out.println("============Sessions level: " + Sessions.getLevel());
                String profileImagePath = "/app/edugram/userData/Images/profile_pictures/" + Sessions.getProfilePicture();
                URL resource = getClass().getResource(profileImagePath);
                if (resource != null) {
                    navProfilePicture.setImage(new Image(resource.toExternalForm()));
                } else {
                    System.err.println("Profile picture resource not found: " + profileImagePath);
                }
            } catch (Exception e) {
                System.err.println("Could not load profile picture: " + e.getMessage());
            }
        }
        if (navUsername != null) {
            navUsername.setText("@" + Sessions.getUsername());
        }
    }
}
