package acsse.csc2b.client;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;

/**
 * This class handles the backend of the client
 */
public class UserHandler
{
    Socket socket;
    BufferedReader in;
    PrintWriter out;
    DataInputStream dataIn;
    DataOutputStream dataOut;
    ObservableList<String> files;

    /**
     * This constructor will create the socket and initialize all the streams needed to communication
     * @param files an observable list used to bind the javafx ListView with the list from th server
     */
    public UserHandler(ObservableList<String> files)
    {
        // now I am creating an observable list to populate the listView on the GUI
        this.files = files;
        // Initialize Socket and the streams
        try
        {
            socket = new Socket("localhost", 5432);
            System.out.println("Connected");
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            dataIn = new DataInputStream(socket.getInputStream());
            dataOut = new DataOutputStream(socket.getOutputStream());
        }
        catch (IOException e)
        {
            System.out.println("Connection failed on client socket: " + e.getMessage());
        }
    }

    /**
     * This function handles sending the LIST request to the server and to display the list on the GUI
     */
    public void ListFilesOnView()
    {
        // creating a new thread so that it does not interfere with javafx threads
        new Thread(() -> {
            // I first send the List request to the client
            out.println("LIST");
            out.flush();
            System.out.println("sent list request");
            boolean flagEndOfResponce = false; // flag to indicate we have received full response
            long startTime = System.currentTimeMillis();
            long timeout = 5000; // in milliseconds
            // read the response from the server
            try
            {
                StringBuilder response =  new StringBuilder(); // I will use this to store each line from the response
                String line; // read each line
                /*
                    since the response carries a single string with multiple lines.
                    readLine will read a single line  from that string, so using the while loop I can read all the lines.
                    I will also account time to wait for the server to respond
                 */
                while(!flagEndOfResponce && (System.currentTimeMillis() - startTime) < timeout)
                {
                    if(in.ready())
                    {
                        if ((line = in.readLine()) != null)
                        {
                            // add each line to the string builder
                            response.append(line).append("\n");
                        }
                        else
                        {
                            flagEndOfResponce = true;
                        }
                    }
                    else
                    {
                        // sleep for a while, while we wait for response from the server
                        try
                        {
                            Thread.sleep(100);
                        }
                        catch (InterruptedException e)
                        {
                            Thread.currentThread().interrupt(); //  restore interrupted status
                        }
                    }
                }
                // in case  we waited till our timer reached end
                if(!flagEndOfResponce)
                {
                    System.out.println("Timeout waiting for response fromm server");
                }
                // now split the string by new line character and store results on array
                String[] FileOnServer = response.toString().split("\n");
                // add all the files to list
                Platform.runLater(() -> {
                    files.clear();
                    files.addAll(Arrays.asList(FileOnServer));
                });
            }
            catch (IOException e)
            {
                System.out.println("Failed to get List from server: " + e.getMessage());
            }
        }).start();
    }

    /**
     * This function handles the functionality to download a file from the server
     * @param fileID is the ID of the file
     * @param imageView is a javafx Node used to display the downloaded image
     */
    public void DownloadFile(String fileID, ImageView imageView)
    {
        // creating a new thread so that it does not interfere with javafx threads
       Thread thread = new Thread(() -> {
           // I am going to send the request to the server after retrieving the file ID on the GUI
           out.println("DOWNLOAD " + fileID);
           out.flush();
            /*
                I am going to receive the file size first with the file name from the server, then file itself
             */
           try
           {
               boolean flagEndOfResponce = false; // flag to indicate we have received full response
               long startTime = System.currentTimeMillis();
               long timeout = 5000; // in milliseconds

               // read the file size and file name
               String filesize = in.readLine();
               String filename = in.readLine();
               System.out.println(filesize + " " + filename);
               // downloading file from server and saving it on the client data folder
               createFile(filename, Integer.parseInt(filesize), imageView);
           }
           catch (IOException e)
           {
               System.out.println("Error receiving file from the server: " + e.getMessage());
           }
       });
       thread.start();
    }

    /**
     * This function handles the functionality to save the downloaded file from the server to the client folder
     * @param fileName is the name of the file
     * @param filesize is the size of the file
     * @param imageView is a javafx node used to display th image
     */
    private void createFile(String fileName, int filesize, ImageView imageView)
    {
        // save file on the client folder
        File file = new File("data/client/" + fileName);
        // write into the file
        try(FileOutputStream writer =  new FileOutputStream(file))
        {
            byte[] buffer = new byte[2034];
            int numRead = 0;
            int totalByte = 0;
            // read from sever while writing to the file also
            while (totalByte != filesize)
            {
                // read from server
                numRead = dataIn.read(buffer, 0, buffer.length);
                // write to file
                writer.write(buffer, 0, numRead);
                writer.flush();
                totalByte += numRead;
            }
        }
        catch (IOException e)
        {
            System.out.println("Error creating file on client: " + e.getMessage());
        }
        try {
            System.out.println("Feed back: " + in.readLine());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // after storing the image, display it
        showImage(imageView, file);
    }

    /**
     * This function handles the functionality to display an image on the GUI
     * @param imageView is the javafx node to display images
     * @param file is the image file to be displayed
     */
    public void showImage(ImageView imageView, File file)
    {
        // ensuring that this does not interfere with the javafx thread
        Platform.runLater(() ->{
            try
            {
                // get the image from the client folder
                Image image = new Image(new FileInputStream(file));
                // display it on the GUI
                imageView.setImage(image);
            }
            catch (FileNotFoundException e)
            {
                System.out.println("File to view not found: " + e.getMessage());
            }
        });
    }

    /**
     * This function handles the functionality to send a file to the sever
     * @param file is the file to be sent
     */
    public void SendFileToServer(File file)
    {
        System.out.println("Sending file to server");
        Platform.runLater(() -> {
            // UP <ID> <Name> <Size> <Image>.
            // First send the request with the file info
            out.println("UP " + file.getName() + " " + file.length());
            out.flush();
            // then I send the actual file
            try(FileInputStream reader = new FileInputStream(file))
            {
                byte[] buffer = new byte[2034];
                int n =0;
                // read into buffer and send the file
                while ((n = reader.read(buffer)) > 0)
                {
                    dataOut.write(buffer, 0, n);
                    out.flush();
                }
            }
            catch (IOException e)
            {
                System.out.println("Error sending file to server: " + e.getMessage());
            }
            try {
                System.out.println("FeedBack from Server: " + in.readLine());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
