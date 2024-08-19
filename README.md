# Client_and_Server_JavaFx_Application
# Network System
![image](https://github.com/user-attachments/assets/da05ae7b-5818-4c90-9d1d-fed2c7cd9578)


I am developing a network system that will allow both staff and students to upload images of women who have made a significant impact on their lives. The system will also enable users to retrieve any of the uploaded images. To achieve this, I will be developing both the server and client sides of the system.

## Server

The server will play a crucial role in this system, and here's how I plan to implement it:

- The server will run on **port 5432** and will continuously wait for incoming connections.
- To keep track of the uploaded images, I will use a text file that stores a list of image files along with their corresponding IDs.
- The server will handle the following requests from the client:

  - `LIST`: This command will return a list of all image file names along with their IDs.
  - `DOWN <ID>`: When a client sends this command, the server will return the image associated with the provided ID.
  - `UP <ID> <Name> <Size> <Image>`: This command will allow the client to upload an image to the server. The server will store the image and update the text file with its details. If the upload is successful, the server will reply with "SUCCESS"; otherwise, it will respond with "FAILURE."

## Client

The client side of the system will be user-friendly and will include the following features:

- A graphical user interface (GUI) that is intuitive and easy to use.
- The client will provide functionality for users to select and upload an image to the server.
- Users will be able to request a list of available images from the server through the client interface.
- The client will allow users to enter an ID to retrieve the corresponding image from the server, which will then be displayed to them.

With these features, the network system will provide a seamless experience for staff and students to share and view impactful images.

