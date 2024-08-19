package acsse.csc2b.client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;
import java.io.File;

/**
 * This class creates handles the creation of the javafx interface
 */
public class InterfaceGui
{
    // All gui nodes needed
    Button Download;
    Button Upload;
    Button List;
    Button Send;
    File UploadedFile;
    FileChooser fileChooser;
    ImageView ViewImage;
    ListView<String> ListImages;
    Stage stage;
    TextField textField;
    UserHandler userHandler;
    ObservableList<String> files;

    /**
     * This constructor initializes the nodes
     * @param stage is the javafx stage where the scene with the nodes will be placed
     */
    public InterfaceGui(Stage stage)
    {
        // creating an observable list
        files = FXCollections.observableArrayList();
        // create instance of User handler
        userHandler = new UserHandler(files);
        // initialize stage
        this.stage = stage;
        // initialize nodes
        initializeNodes();
        // customize nodes
        customizeNodes();
        // configure buttons
        setButtonFunctionalities();
    }

    /**
     * This function creates the scene that will contain all the nodes that the user will interact with
     * @return returns the scene
     */
     public Scene ClientInterface()
     {
         // Root pane
         BorderPane root = new BorderPane();
         // creating label
         Label label = new Label("List of Files:");
         label.setFont(new Font("Arial", 20));
         label.setUnderline(true);
         // adding the nodes on the root node for layout
         root.setLeft(stackV(label, ListImages, stackV(List, Upload, stackH(Download, textField))));
         root.setRight(ImageViewerBox(ViewImage, Send));
         // returns the scene with the layout and nodes
         return new Scene(root, 1300, 750);
     }

    /**
     * This function handles the functionality to layer nodes vertically using a javafx Vbox
     * @param node a node to be layered vertically. Can accept multiple nodes
     * @return returns the Vbox with the  nodes layered
     */
     private VBox stackV(Node... node)
     {
         VBox v = new VBox();
         v.getChildren().addAll(node);
         v.setSpacing(20);
         v.setPadding(new Insets(20, 20, 20, 20));
         return v;
     }

    /**
     * This function handles the functionality to layer nodes horizontally using a javafx Hbox
     * @param node a node to be layered horizontally. Can accept multiple nodes
     * @return returns the Hbox with the nodes
     */
     private HBox stackH(Node... node)
     {
         HBox h = new HBox();
         h.getChildren().addAll(node);
         h.setSpacing(20);
         return h;
     }

    /**
     * This function serves as a custom layer node for the ImageView Node
     * @param node all the nodes to be layered with the ImageView
     * @param button is a button to send the Image shown on the ImageView
     * @return returns a Vbox with all the nodes and ImageView all layered vertically
     */
     private VBox ImageViewerBox(Node node, Button button)
     {
         VBox v = new VBox();
         // create a label
         Label label = new Label("ImageViewer:");
         label.setFont(new Font("Arial", 20));
         label.setUnderline(true);
         // customize button layout
         HBox buttonBox = new HBox(button);
         buttonBox.setAlignment(Pos.CENTER);
         // add all nodes to the vbox
         v.getChildren().addAll(label, node, buttonBox);
         v.setSpacing(20);
         v.setPadding(new Insets(20, 20, 20, 20));
         v.setPrefHeight(700);
         return v;
     }

    /**
     * This function serves as an eventHandler for the upload button
     */
    private void OpenFileDialog()
     {
         // get the file from the user
         UploadedFile = fileChooser.showOpenDialog(stage);
         // check if file a file was selected
         if(UploadedFile != null)
         {
             // if a valid file was selected render the image on the ImageView
             userHandler.showImage(ViewImage, new File("data/client/" + UploadedFile.getName()));
         }
         else
         {
             // if not then issue a warning to the user
             Alert alert = new Alert(Alert.AlertType.ERROR);
             alert.setTitle("Error");
             alert.setHeaderText(null);
             alert.setContentText("File not found");
             alert.showAndWait();
         }
     }

    /**
     * This function will handle set an eventHandler for each Button on the GUI
     */
    private void setButtonFunctionalities()
     {
         // set functionality for Upload Files button
         Upload.setOnAction(e -> {
             OpenFileDialog();
             Send.setVisible(true);
         });
         // set functionality for List Files button
         List.setOnAction(e -> {
             userHandler.ListFilesOnView();
             Alert alert = new Alert(Alert.AlertType.INFORMATION);
             alert.setTitle("List request");
             alert.setHeaderText(null);
             alert.setContentText("List request sent. Please wait for few seconds");
             alert.showAndWait();
         });
         // set functionality for Download button
         Download.setOnAction(e -> {
             String id = textField.getText().trim();
             if(id.isEmpty())
             {
                 Alert alert = new Alert(Alert.AlertType.WARNING);
                 alert.setTitle("Warning");
                 alert.setHeaderText(null);
                 alert.setContentText("Please enter a file ID");
                 alert.showAndWait();
             }
             else
             {
                 userHandler.DownloadFile(id, ViewImage);
             }
             // now clear text field
             textField.clear();
         });
         // set functionality for Send Button
         Send.setOnAction(e -> {
             userHandler.SendFileToServer(UploadedFile);
             Alert alert = new Alert(Alert.AlertType.INFORMATION);
             alert.setTitle("Information");
             alert.setHeaderText(null);
             alert.setContentText("File Sent");
             alert.showAndWait();
         });
     }

    /**
     * THis function will customize each node on the GUI such as setting dimensions of a node
     */
    private void customizeNodes()
     {
         // set initial path for file chooser
         fileChooser.setInitialDirectory(new File("data/client"));
         // set dimensions for listView
         ListImages.setMinWidth(450);
         ListImages.setMinHeight(300);
         // set  dimension for ImageView
         ViewImage.setFitWidth(600);
         ViewImage.setFitHeight(500);
         // set text field dimensions
         textField.setMinWidth(400);
         textField.setMinHeight(40);
         textField.setPromptText("Enter Image ID");
     }

    /**
     * This function initializes each node on the GUI
     */
    private void initializeNodes()
     {
         // initialize variables
         fileChooser = new FileChooser();
         Upload = new Button("Upload File");
         Download = new Button("Download File");
         List = new Button("List Files");
         Send = new Button("Send File");
         Send.setVisible(false); // hide button for now
         ViewImage = new ImageView();
         ListImages = new ListView<>(files);
         textField = new TextField();
     }
}
