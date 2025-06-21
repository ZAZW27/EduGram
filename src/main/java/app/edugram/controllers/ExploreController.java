package app.edugram.controllers;
import app.edugram.Main;
import app.edugram.controllers.Components.PostFrameController;
import app.edugram.controllers.Components.SmallPostFrameController;
import app.edugram.models.PostModel;
import app.edugram.models.UserPrefTagModel;
import app.edugram.utils.PostClickHandler;
import app.edugram.utils.Sessions;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ExploreController extends BaseController implements Initializable, PostClickHandler {
    @FXML private GridPane postContentGrid;
    @FXML private ScrollPane contentScrollPane;
    @FXML private VBox contentContainer;
    @FXML private HBox hbox_explore;
    @FXML private Button tagFollow;

    private Node exploreGridView;
    private Node tulisanExplore;

    // Lazy loading variables (fixed syntax errors)
    private int currentPage = 0;
    private final int postsPerPage = 9; // 3x3 grid per batch
    private boolean isLoading = false;
    private boolean hasMorePosts = true;
    private List<PostModel> allPosts = new ArrayList<>(); // Fixed syntax
    private VBox loadingContainer;

    public String whatPage = "explore";

    public void setWhatPage(String whatPage) {
        this.whatPage = whatPage;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("ExploreController initialized");
        System.out.println("postContentGrid: " + postContentGrid);
        System.out.println("contentScrollPane: " + contentScrollPane);

        Platform.runLater(() -> {
            postContentGrid.requestFocus();
        });

        exploreGridView = contentScrollPane;
        tulisanExplore = hbox_explore;

        createLoadingIndicator();
        setupScrollListener();
        loadAllPostsData(); // This will call loadNextBatch when done
    }

    private void createLoadingIndicator() {
        loadingContainer = new VBox();
        loadingContainer.setAlignment(javafx.geometry.Pos.CENTER);
        loadingContainer.setPadding(new Insets(20));
        loadingContainer.setVisible(false);

        Label loadingLabel = new Label("Loading more posts...");
        loadingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");

        loadingContainer.getChildren().add(loadingLabel);

        // Add to the bottom of the grid content, spanning all 3 columns
        postContentGrid.add(loadingContainer, 0, 999);
        GridPane.setColumnSpan(loadingContainer, 3);
    }

    private void setupScrollListener() {
        if (contentScrollPane != null) {
            contentScrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
                // Check if user scrolled to bottom (within 10% of the bottom)
                if (newValue.doubleValue() >= 0.9 && !isLoading && hasMorePosts) {
                    System.out.println("Scroll triggered - loading next batch");
                    loadNextBatch();
                }
            });
        }
    }

    private void loadAllPostsData() {
        System.out.println("Loading all posts data...");

        Task<List<PostModel>> loadAllPostsTask = new Task<List<PostModel>>() {
            @Override
            protected List<PostModel> call() throws Exception {
                List<PostModel> posts = new ArrayList<>(initData());
                System.out.println("Total posts loaded: " + posts.size());
                return posts;
            }
        };

        loadAllPostsTask.setOnSucceeded(e -> {
            allPosts = loadAllPostsTask.getValue();
            hasMorePosts = !allPosts.isEmpty();
            System.out.println("Posts loaded successfully. Count: " + allPosts.size());
            System.out.println("Has more posts: " + hasMorePosts);

            // Now that data is loaded, load the first batch
            Platform.runLater(() -> {
                loadNextBatch();
            });
        });

        loadAllPostsTask.setOnFailed(e -> {
            System.err.println("Failed to load posts data: " + loadAllPostsTask.getException().getMessage());
//            e.printStackTrace();
            hasMorePosts = false;
        });

        Thread thread = new Thread(loadAllPostsTask);
        thread.setDaemon(true);
        thread.start();
    }

    public void loadNextBatch() {
        System.out.println("loadNextBatch called - isLoading: " + isLoading + ", hasMorePosts: " + hasMorePosts);

        if (isLoading || !hasMorePosts) {
            System.out.println("Skipping batch load - isLoading: " + isLoading + ", hasMorePosts: " + hasMorePosts);
            return;
        }

        isLoading = true;
        showLoadingIndicator();
        System.out.println("Starting to load batch " + currentPage);

        Task<List<PostModel>> loadBatchTask = new Task<List<PostModel>>() {
            @Override
            protected List<PostModel> call() throws Exception {
                System.out.println("In background task - allPosts size: " + allPosts.size());

                int startIndex = currentPage * postsPerPage;
                int endIndex = Math.min(startIndex + postsPerPage, allPosts.size());

                System.out.println("Batch indices - start: " + startIndex + ", end: " + endIndex);

                if (startIndex >= allPosts.size()) {
                    System.out.println("No more posts to load");
                    return new ArrayList<>();
                }

                // Simulate network delay (remove this in production) <- lmao
                Thread.sleep(500);

                List<PostModel> batch = allPosts.subList(startIndex, endIndex);
                System.out.println("Returning batch of size: " + batch.size());
                return batch;
            }
        };

        loadBatchTask.setOnSucceeded(e -> {
            List<PostModel> batch = loadBatchTask.getValue();
            System.out.println("Batch loaded successfully. Size: " + batch.size());

            if (!batch.isEmpty()) {
                displayPostsBatch(batch);
                currentPage++;
                System.out.println("Current page updated to: " + currentPage);

                // Check if we've loaded all posts
                if (currentPage * postsPerPage >= allPosts.size()) {
                    hasMorePosts = false;
                    System.out.println("All posts loaded. No more posts available.");
                }
            } else {
                hasMorePosts = false;
                System.out.println("Empty batch received. No more posts.");
            }

            hideLoadingIndicator();
            isLoading = false;
        });

        loadBatchTask.setOnFailed(e -> {
            System.err.println("Failed to load batch: " + loadBatchTask.getException().getMessage());
            loadBatchTask.getException().printStackTrace();
            hideLoadingIndicator();
            isLoading = false;
            hasMorePosts = false;
        });

        Thread thread = new Thread(loadBatchTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void displayPostsBatch(List<PostModel> posts) {
        System.out.println("Displaying batch of " + posts.size() + " posts");

        try {
            for (int i = 0; i < posts.size(); i++) {
                PostModel post = posts.get(i);

                // Calculate position in 3-column grid (excluding loading indicator)
                int currentChildren = postContentGrid.getChildren().size() - 1; // -1 for loading indicator
                int columns = currentChildren % 3;
                int row = currentChildren / 3;

                System.out.println("Adding post " + i + " to column " + columns + ", row " + row);
                addPostToGrid(post, columns, row);
            }
            System.out.println("Batch display completed");
        } catch (IOException e) {
            System.err.println("Error displaying posts batch: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addPostToGrid(PostModel post, int columns, int row) throws IOException {
        System.out.println("Adding post to grid at column " + columns + ", row " + row);

        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(Main.class.getResource("pages/components/smallpost.fxml"));
        VBox box = fxmlLoader.load();

        SmallPostFrameController smallPostFrameController = fxmlLoader.getController();
        smallPostFrameController.setData(post);
//        smallPostFrameController.setExploreController(this);
        smallPostFrameController.setPostClickHandler(this);

        postContentGrid.add(box, columns, row);
        GridPane.setMargin(box, new Insets(2, 2, 1, 2));

        System.out.println("Post added successfully to grid");
    }

    private void showLoadingIndicator() {
        if (loadingContainer != null) {
            Platform.runLater(() -> loadingContainer.setVisible(true));
        }
    }

    private void hideLoadingIndicator() {
        if (loadingContainer != null) {
            Platform.runLater(() -> loadingContainer.setVisible(false));
        }
    }

    // Method to refresh posts (clears current posts and reloads from beginning)
    public void refreshPosts() {
        // Clear only post items, keep loading indicator
        postContentGrid.getChildren().removeIf(node -> node != loadingContainer);
        currentPage = 0;
        hasMorePosts = true;
        loadAllPostsData();
    }

    @Override
    public List<PostModel> initData() {
        System.out.println("initData() called");
        PostModel post = new PostModel();
        List<PostModel> posts = post.listAll(whatPage);
        tagFollow.setVisible(whatPage.startsWith("explore-"));

        setFollowButton();

        System.out.println("initData() returning " + posts.size() + " posts");
        return posts;
    }

    public void setFollowButton(){
        String tagName = whatPage.split("-")[1];
        int tagId = Integer.parseInt(UserPrefTagModel.getTagId(tagName));
        UserPrefTagModel upt = new UserPrefTagModel();

        tagFollow.setText(
                (upt.exists(tagId) ? "Followed" : "Follow") +
                        " #" +
                        tagName);
    }

    @FXML
    public void onFollowTag(ActionEvent event) {
        String tagName = whatPage.split("-")[1];
        String tagId = String.valueOf(UserPrefTagModel.getTagId(tagName));
        List<String> value = List.of(
                String.valueOf(Sessions.getUserId()),
                tagId
        );
        UserPrefTagModel upt = new UserPrefTagModel();

        if(upt.exists(Integer.parseInt(tagId))){
            System.out.println("delete folow");
            upt.unset(Integer.parseInt(tagId));
        }else{
            System.out.println("add folow");
            upt.set(value);
        }
        setFollowButton();
    }

    public void onPostClicked(PostModel post) {
        ShowPostDetail(post);
    }


    public void onPostBackClicked(PostModel post) {
        showExploreGrid();
    }

    public void ShowPostDetail(PostModel post){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("pages/components/post.fxml"));
            VBox postDetailView = fxmlLoader.load();

            PostFrameController postFrameController = fxmlLoader.getController();
            postFrameController.setData(post);
            postFrameController.setPostClickHandler(this);
            postFrameController.showBackButton(true);


            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(postDetailView);

            Platform.runLater(() -> {
                postDetailView.requestFocus();
            });

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to load post details: " + post.getTitle());
        }
    }

    public void showExploreGrid(){
        contentContainer.getChildren().clear();
        contentContainer.getChildren().add(tulisanExplore);
        contentContainer.getChildren().add(exploreGridView);
    }
}