package app.edugram.controllers.Components;
import app.edugram.Main;
import app.edugram.controllers.ExploreController;
import app.edugram.controllers.ProfileController;
import app.edugram.models.*;
import app.edugram.utils.PageAction;
import app.edugram.utils.Sessions;
import app.edugram.utils.PostClickHandler;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;

import java.io.IOException;
import java.util.List;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;

public class PostFrameController {
    @FXML private ImageView postContent;
    @FXML private Label postDesc;
    @FXML private Label postLike;
    @FXML private Label postTitle;
    @FXML private Button dislikeBtn;
    @FXML private Button likeBtn;
    @FXML private Button profileBtn;
    @FXML private ImageView postProfile;
    @FXML private Button saveBtn;
//    @FXML private Button tag;
    @FXML private HBox tagBox;
    @FXML private VBox rootContainer;
    @FXML private ImageView iconLike;
    @FXML private ImageView iconDislike;
    @FXML private ImageView iconBookmark;
    @FXML private ImageView iconMore;
    @FXML private ImageView iconComment;
    @FXML private Button backtoexplore;
    @FXML private Button moreBtn;

    private PostModel currentPost;
    private Popup currentOptionsPopup;
    private Runnable returnToExploreCallBack;
    private PostClickHandler postClickHandler;
    private ReportController reportPageController;

    private final LikeModel likeModel = new LikeModel();
    private final DislikeModel dislikeModel = new DislikeModel();
    private final SaveModel saveModel = new SaveModel();

    public void initialize() {
        Platform.runLater(() -> {
            Scene scene = rootContainer.getScene();
            if (scene != null) {
                postContent.fitWidthProperty().bind(rootContainer.widthProperty().multiply(0.46));
                double radius = Math.min(postProfile.getFitWidth(), postProfile.getFitHeight()) / 2;
                Circle clip = new Circle(radius, radius, radius);
                postProfile.setClip(clip);
            }
        });
//      //////////////// More option button ////////////////////////////////////////

            if (moreBtn != null) {moreBtn.setOnAction(event -> TombolMoreMunculHilang(event));}
            else {System.err.println("moreBtn is null in PostFrameController.initialize()");}

//      /////////////////More Option Button//////////////////////////////////////////////////////////

//      //////////////// back to explore button ///////////////////////////////////
            if (backtoexplore != null) {
            showBackButton(false); }
    }

    public void setReportPageController(ReportController controller) {
        this.reportPageController = controller;
    }

    private static BorderPane mainBorderPane;

    public static void setMainBorderPane(BorderPane pane) {
        mainBorderPane = pane;
    }

    public void setData(PostModel postModel){
        this.currentPost = postModel;

        String mainPath = "src/main/resources/app/edugram/userData/Images/";
        Path projectRoot = Paths.get("").toAbsolutePath();
        Path postImagePath = projectRoot.resolve(mainPath + "posts/" + postModel.getPostContent());
        Path profileImagePath = projectRoot.resolve(mainPath + "profile_pictures/" + postModel.getProfile());

        File postImageFile = postImagePath.toFile();
        File profileImageFile = profileImagePath.toFile();

        if (postImageFile.exists()) {
            postContent.setImage(new Image(postImageFile.toURI().toString()));
        }else {
            System.err.println("Post image not found: " + postImageFile.getAbsolutePath());
        }

        if(profileImageFile.exists()){
            postProfile.setImage(new Image(profileImageFile.toURI().toString()));
        }else {
            System.err.println("Profile image not found: " + profileImageFile.getAbsolutePath());
        }

        postTitle.setText(postModel.getTitle());
        postDesc.setText(postModel.getDescription());
        countAvgPostLike(String.valueOf(postModel.getId()));
        profileBtn.setText(postModel.getPostUsername());

        boolean isLiked = likeModel.exists(postModel.getId());
        likeBtn.setUserData(isLiked ? "hasLiked" : "like");
        setPostIconButton(iconLike, isLiked ? "like-active.png" : "like.png");

        boolean isDisliked = dislikeModel.exists(postModel.getId());
        dislikeBtn.setUserData( isDisliked ? "hasDisliked" : "dislike");
        setPostIconButton(iconDislike, isDisliked ? "dislike-active.png" : "dislike.png");

        boolean isSaved = saveModel.exists(postModel.getId());
        saveBtn.setUserData( isSaved ? "hasSaved" : "save");
        setPostIconButton(iconBookmark, isSaved ? "bookmark-active.png" : "bookmark.png");

        tagBox.getChildren().clear();

        List<String> tags = currentPost.getTags();
        if(tags != null && !tags.isEmpty()){
            for(String tagName : tags){
                String[] tag = tagName.split("-");
                Button tagButton = new Button("#"+tag[0]);
                tagButton.getStyleClass().addAll(tag[1].equals("a") ? "built_in_tag" : "user_made_tag", "tagButton");

                tagButton.setOnAction(event -> {
                    try {
                        FXMLLoader loader = new FXMLLoader(PageAction.class.getResource("/app/edugram/pages/explore.fxml"));
                        Parent root = loader.load();

                        ExploreController explore = loader.getController();
                        explore.setWhatPage("explore-" + tag[0]);

                        // Set the content to the center of BorderPane
                        mainBorderPane.setCenter(root);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                tagBox.getChildren().add(tagButton);
            }
        }
    }

    @FXML
    void onLikeClicked(ActionEvent event) {
        if("hasDisliked".equals(dislikeBtn.getUserData())){
            dislikeAction();
        }
        likeAction();
        countAvgPostLike(String.valueOf(currentPost.getId()));
    }

    void likeAction(){
        if("hasLiked".equals(likeBtn.getUserData())){
            System.out.println("like: unsetting");
            likeModel.unset(currentPost.getId());
            likeBtn.setUserData("like");
            setPostIconButton(iconLike, "like.png");
            return;
        }
        System.out.println("like: setting");
        List<String> value = List.of(String.valueOf(Sessions.getUserId()), String.valueOf(currentPost.getId()));
        likeModel.set(value);
        likeBtn.setUserData("hasLiked");
        setPostIconButton(iconLike,"like-active.png");
    }

    @FXML
    void onDislikeClicked(ActionEvent event) {
        if("hasLiked".equals(likeBtn.getUserData())){
            likeAction();
        }
        dislikeAction();
        countAvgPostLike(String.valueOf(currentPost.getId()));
    }

    void dislikeAction(){
        if("hasDisliked".equals(dislikeBtn.getUserData())){
            System.out.println("dislike: unsetting");
            dislikeModel.unset(currentPost.getId());
            dislikeBtn.setUserData("dislike");
            setPostIconButton(iconDislike, "dislike.png");
            return;
        }
        System.out.println("dislike: setting");
        List<String> value = List.of(String.valueOf(Sessions.getUserId()), String.valueOf(currentPost.getId()));
        dislikeModel.set(value);
        dislikeBtn.setUserData("hasDisliked");
        setPostIconButton(iconDislike,"dislike-active.png");
    }

    void countAvgPostLike(String idPost) {
        String sumCount = String.valueOf(currentPost.getAvgLike(idPost));
        postLike.setText(sumCount);
    }

    @FXML
    void onSaveClicked(ActionEvent event) {
        if("hasSaved".equals(saveBtn.getUserData())){
            System.out.println("Save: Unsetting");
            saveModel.unset(currentPost.getId());
            saveBtn.setUserData("save");
            setPostIconButton(iconBookmark, "bookmark.png");
            return;
        }
        System.out.println("save: setting");
        List<String> value = List.of(String.valueOf(Sessions.getUserId()), String.valueOf(currentPost.getId()));
        saveModel.set(value);
        saveBtn.setUserData("hasSaved");
        setPostIconButton(iconBookmark, "bookmark-active.png");
    }

    private VBox contentContainer;
    public void setContentContainer(VBox contentContainer){
        this.contentContainer = contentContainer;
    }
    @FXML
    void onProfileClicked(ActionEvent event) {
        System.out.println("Profile button clicked for user " + currentPost.getPostUsername());
        try {
            FXMLLoader loader = new FXMLLoader(PageAction.class.getResource("/app/edugram/pages/profile.fxml"));
            VBox userProfile = loader.load();

            ProfileController profileFrameController = loader.getController();
            profileFrameController.setUserData(Integer.parseInt(currentPost.getUserId()));

            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(userProfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onCommentClicked(ActionEvent event) {
        if (currentPost == null) {
            System.err.println("No current post selected to show comments.");
            return;
        }
        System.out.println("Showing comment popup for post " + currentPost.getId());
        showCommentPopup();
    }

    @FXML
    void onMoreActionClicked(ActionEvent event){
        System.out.println("more action button clicked for post  " + currentPost.getId());
    }

    void setPostIconButton(ImageView iconPost, String iconImage) {
        String imagePath = "/app/edugram/assets/Image/Icons/post/"+iconImage;
        Image image = new Image(getClass().getResource(imagePath).toExternalForm());
        iconPost.setImage(image);
    }

    private void TombolMoreMunculHilang(ActionEvent event) {
        if (currentOptionsPopup != null && currentOptionsPopup.isShowing()) {
            currentOptionsPopup.hide(); // Jika pop-up sedang tampil, sembunyikan
            currentOptionsPopup = null;} // Reset referensi
        else {showPostOptionsPopup();}
    }

    private void showPostOptionsPopup() {
        try {
            Popup popup = new Popup();

            FXMLLoader loader = new FXMLLoader(Main.class.getResource("pages/components/post_option_popup.fxml"));
            VBox popupcontent = loader.load();
            PostOptionPopUpController popupController = loader.getController();
            popupController.setPopup(popup);
            popupController.setPostData(this.currentPost);
            popup.getContent().add(popupcontent);
            popupController.setReportPageController(this.reportPageController);

            if (moreBtn.getScene() != null && moreBtn.getScene().getWindow() != null) {
                // Konversi koordinat lokal tombol ke koordinat layar
                double x = moreBtn.localToScreen(moreBtn.getBoundsInLocal()).getMinX();
                double y = moreBtn.localToScreen(moreBtn.getBoundsInLocal()).getMaxY();

                popup.show(moreBtn.getScene().getWindow(), x, y);
                System.out.println("Popup shown at X: " + x + ", Y: " + y); // Debugging

                currentOptionsPopup = popup;
                popup.setAutoHide(true);
                popup.setHideOnEscape(true);

            } else {
                System.err.println("moreBtn is not attached to a scene/window. Cannot show popup."); // Debugging
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load post options popup.");
        }
    }

    private void showCommentPopup() {
        try {
            Popup popup = new Popup();

            FXMLLoader loader = new FXMLLoader(Main.class.getResource("pages/components/comment.fxml"));
            BorderPane popupRoot = loader.load();
            CommentController commentController = loader.getController();

            commentController.setPopup(popup);
            commentController.setPostData(currentPost.getId(), currentPost.getTitle());

            popup.getContent().add(popupRoot);
            Window ownerWindow = Main.getPrimaryStage();

            if (ownerWindow != null) {
                // Posisi popup di tengah layar owner
                double x = ownerWindow.getX() + ownerWindow.getWidth() / 2 - popupRoot.prefWidth(-1) / 2; // gunakan popupRoot
                double y = ownerWindow.getY() + ownerWindow.getHeight() / 2 - popupRoot.prefHeight(-1) / 2; // gunakan popupRoot

                popup.show(ownerWindow, x, y);
                popup.setAutoHide(true);
                popup.setHideOnEscape(true);
            } else {
                System.err.println("Main Stage not found. Cannot show comment popup.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load comment .");
        }
    }

    public void setPostClickHandler(PostClickHandler handler) {
        this.postClickHandler = handler;

        if(backtoexplore != null) {
            backtoexplore.setOnAction(this::handlePostBack);
        } else {
            System.out.println("buttonpost is null");
        }
    }

    public void handlePostBack(ActionEvent event) {
        if(currentPost == null) {
            System.err.println("ERROR: current post is null");
            return;
        } if(postClickHandler != null) {
            postClickHandler.onPostBackClicked(currentPost);
        }
    }


    //  /////////////// back to explore function ///////////////////////////////////////
//    public void setReturnToExploreCallBack (Runnable CallBack) {
//        this.returnToExploreCallBack = CallBack;
//    }
    public void showBackButton(boolean show) {
        if(backtoexplore != null) {
            backtoexplore.setVisible(show);
            backtoexplore.setManaged(show);
        }
    }

    //  /////////////// back to explore function ///////////////////////////////////////

}

