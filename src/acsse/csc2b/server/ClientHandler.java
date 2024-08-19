package acsse.csc2b.server;

import java.io.*;

/**
 * This class will handle the request from the client
 */
public class ClientHandler
{
    /**
     * This constructor calls the function that handles the requests
     * @param dataOut DataOutputStream to send bytes
     * @param dataIn DataInputStream to receive bytes
     * @param out PrintWriter to send responses to the client
     * @param in BufferedReader to read in requests from the client
     */
    public ClientHandler(DataOutputStream dataOut, DataInputStream dataIn, PrintWriter out, BufferedReader in)
    {
        // handle the requests
        handle_client_queries(dataOut, dataIn, out, in);
    }

    /**
     * This function handles the request from the client
     * @param dataOut DataOutputStream to send bytes
     * @param dataIn DataInputStream to receive bytes
     * @param out PrintWriter to send responses to the client
     * @param in BufferedReader to read in requests from the client
     */
    private void handle_client_queries(DataOutputStream dataOut, DataInputStream dataIn, PrintWriter out, BufferedReader in)
    {
        String message;
        try
        {
            /*
                read the message from the client

                Structure:
                - < Request Type > < information related to request>

                I am interested on the request type. Once I know the request type I can handle it accordingly.
                I am expecting 3 types of request namely:

                1. LIST
                2. DOWN
                3. UP
             */
           while((message = in.readLine()) != null)
           {
               System.out.println("Message: " + message);
               if(message.contains("LIST"))
               {
                /*
                    The client requested the list of image files with their ID.
                    So I will send a string containing the information.
                 */
                   System.out.println("List Request Received");
                   sendList(out);
                   System.out.println("List sent");
               }
               else if(message.contains("DOWN"))
               {
                   System.out.println("Download request received");
                   retrieve_requiredFile(message, dataOut, out);
                   System.out.println("Download completed");
               }
               else if(message.contains("UP"))
               {
                   System.out.println("Upload request received");
                   recordFile(message, out, dataIn);
                   System.out.println("Upload completed");
               }
               else
               {
                   System.err.println("This should never happen");
               }
           }
        }
        catch(IOException e)
        {
            System.err.println("Error: " + e);
        }
    }

    /**
     * This function looks for the requested file from the server data folder and sends it to the client
     * @param message is the request containing the File ID
     * @param dataOut DataOutputStream to send the file to the client
     * @param out PrintWriter to communicate back to the client
     */
    private void retrieve_requiredFile(String message, DataOutputStream dataOut, PrintWriter out)
    {
        /*
            On this request I am going to extract the File ID.
        */
        String[] extracted = message.split(" ");
        String ID = extracted[1]; // This is the ID of the file

        /*
            Here I will search throughout the data folder for a file that matches that file ID
         */
        File data_directory = new File("data/server");

        if(!data_directory.exists() && !data_directory.isDirectory())
        {
            System.out.println("data directory does not exist");
        }
        /*
            If this block runs then it means the directory exist.
            I am now proceeding with the search.

            1. Get the list of files on th directory
            2. Check if the list is not empty
            3. loop through the list and loop for a file that matches the ID
            4. Send the file
         */
        File[] files = data_directory.listFiles();
        if(files != null)
        {
            for(File file : files)
            {
                String fileName = file.getName();
                if(fileName.substring(0, fileName.lastIndexOf(".")).equals(ID) || fileName.contains(ID))
                {
                    sendFile(file, out, dataOut);
                    break;
                }
                else
                {
                    System.out.println("There's no such file with this ID: " + ID);
                }
            }
        }
        else
        {
            System.out.println("No files found in data directory");
        }
        // feedback
        out.println("File sent");
        out.flush();
    }

    /**
     * This function handles the ability to send files
     * @param file is the file to be sent
     * @param out PrintWriter to communicate with client
     * @param dataOut DataOutputStream to send the file to the client
     */
    private void sendFile(File file, PrintWriter out, DataOutputStream dataOut)
    {
        // send the file size with file name
        String allInformation = file.length() + "\n" + file.getName();
        // send the information
        out.print(allInformation);
        out.println();
        out.flush();
        /*
            Read the file into memory store it into a buffer =, then send it over the network
         */
        try(FileInputStream file_in =  new FileInputStream(file))
        {
            byte[] bytes = new byte[2048];
            int n;
            while ((n = file_in.read(bytes)) > 0)
            {
                dataOut.write(bytes, 0, n);
                dataOut.flush();
            }
        }
        catch (IOException e)
        {
            System.err.println("Sending File error: " + e);
        }
    }

    /**
     * This file reads the list of files available on the client on the text file and sends it to th client
     * @param out PrintWriter to communicate with client
     */
    private void sendList(PrintWriter out)
    {
        /*
            I will first read all the lines from the txt file available on the server data folder.
            Use a String builder to just create single string to send to the client with the info.
         */
        try(BufferedReader reader =  new BufferedReader(new FileReader("data/server/ImgList.txt")))
        {
            StringBuilder builder = new StringBuilder();
            String line;
            // read each line from the file
            while ((line = reader.readLine()) != null)
            {
                // add the line to the string builder and add a new line
                builder.append(line).append("\n");
            }

            // Now I send the information to the client
            out.write(builder.toString());
            out.flush();
        }
        catch(IOException e)
        {
            System.err.println("Could not read txt file");
        }
    }

    /**
     * This function receives a file from the client and saves it on the server folder and records it on the txt file
     * @param ID file ID
     * @param FileName file name
     * @param FileSize file size
     * @param out PrintWriter to communicate with client
     * @param dataIn DataInputStream to receive the file
     */
    private void StoreFile(String ID, String FileName, int FileSize, PrintWriter out, DataInputStream dataIn)
    {
        // where to store the file
        File store_file = new File("data/server/" + FileName);
        try (FileOutputStream f_out = new FileOutputStream(store_file)) {
            byte[] buffer = new byte[2048];
            int n;
            int totalBytes = 0;

            while (totalBytes != FileSize)
            {
                // read from the byte input stream
                n = dataIn.read(buffer, 0, buffer.length);
                // write the data into the file
                f_out.write(buffer, 0, n);
                f_out.flush();
                totalBytes += n; //  update
            }
            // now record the file on the txt file
            try(BufferedWriter writer = new BufferedWriter(new FileWriter("data/server/ImgList.txt", true)))
            {
                writer.write(ID);
                writer.flush();
            }
            catch(IOException e)
            {
                System.err.println("Could not append to txt file");
            }
            out.println("SUCCESS");
            out.flush();
        } catch (IOException e) {
            out.println("FAILURE");
            out.flush();
        }
    }

    /**
     * This function record an incoming file from the client
     * @param message is the request with the necessary info
     * @param out PrintWriter to communicate with client
     * @param dataIn DataInputStream to receive the file
     */
    private void recordFile(String message, PrintWriter out, DataInputStream dataIn)
    {
        /*
            Here I am expecting this format : UP <ID> <Name> <Size> <Image>.
            I will remove UP from the string and write the info for the file I will store.
         */
        String prefix = "UP";

        if(message.startsWith(prefix))
        {
            String fileInfo = message.substring(prefix.length());
            try(BufferedWriter bw = new BufferedWriter(new FileWriter("data/server/ImgList.txt", true)))
            {
                String[] info = fileInfo.split(" ");
                bw.write("\n" + info[1]);
                bw.flush();
            }
            catch(IOException e)
            {
                System.err.println("Could not write to file");
            }
            /*
                 Extract the information I am interested in
             */
            String[] extracted = fileInfo.split(" ");
            String ID = extracted[0]; // file ID
            String FileName = extracted[1]; // File name
            String FileSize = extracted[2]; // file size
            /*
                Reading the data and storing it into the file on the server data directory
             */
            StoreFile(ID, FileName, Integer.parseInt(FileSize), out, dataIn);
        }
    }

}
