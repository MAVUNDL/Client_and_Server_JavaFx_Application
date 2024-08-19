package acsse.csc2b.client;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * This class handles the client
 */
public class ImgClient extends Application
{
    /**
     * This function runs the client and runs te javafx Application
     * @param args all th args needed by the program
     */
   public static void main(String[]  args)
    {
        launch(args); // launch the javafx Application
    }

    /**
     *This function handles the creation of the stage where the scenes will be displayed
     * @param stage stage the hosts the scenes
     * @throws Exception the application can throw any exception
     */
    @Override
    public void start(Stage stage) throws Exception
    {
        // creating the interface
       InterfaceGui interfaceGui = new InterfaceGui(stage);
       // adding the scene to the stage
       stage.setScene(interfaceGui.ClientInterface());
       stage.setTitle("Network System");
       stage.show();
    }

}




