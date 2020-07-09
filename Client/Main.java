import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Main extends Application {

    // Port for connection
    final static int PORT = 5000;
    // input output area
    static TextArea outputTextArea = new TextArea();
    static TextArea inputTextArea = new TextArea();
    // button
    static Button sendButton = new Button("Send/Receive");
    // VBox lays out its children in a single vertical column
    static VBox holderVBox = new VBox(outputTextArea, inputTextArea, sendButton);
    // StackPane lays out its children in a back-to-front stack.
    static StackPane stackPane = new StackPane(holderVBox);

    // socket for communication
    static Socket socket;
    // object for reading from socket
    static BufferedReader bufferedReader;
    // object for writing to socket
    static PrintWriter printWriter;
    // thread allow to connecting to server till we connect
    // this thread needs because we don't want block gui thread
    static Thread connectThread;

    //while this variable false we try connect to server
    static boolean isConnected = false;


    @Override
    public void start(Stage primaryStage) {
        // alignment for children of this VBox
        holderVBox.setAlignment(Pos.CENTER);
        // set space between children
        holderVBox.setSpacing(5);
        // set height and weight and turn off ability to write in outputArea
        inputTextArea.setMaxHeight(300);
        inputTextArea.setMaxWidth(400);
        outputTextArea.setEditable(false);
        outputTextArea.setMaxHeight(300);
        outputTextArea.setMaxWidth(400);
        //using lambda expression set method which will be calls when we click on button
        sendButton.setOnMouseClicked(Main::onClicked);
        //this construction create new Scene(every application should have at least one Scene) with parameter
        //of stackPane; primaryStage its main window
        primaryStage.setScene(new Scene(stackPane));
        // set size window to content
        primaryStage.sizeToScene();
        // show window
        primaryStage.show();
        // run method which connect us to server
        connect();
    }

    //method for connection to server
    static void connect() {
        // create new thread; that need to not freeze gui
        connectThread = new Thread(() -> {
            while (!isConnected) {
                try {
                    // ip address of server and Port to which we try connect
                    socket = new Socket("127.0.0.1", PORT);
                    bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    printWriter = new PrintWriter(socket.getOutputStream());
                    isConnected = true;
                } catch (IOException e) {
                    System.out.println("Cant connect to server");
                }
            }
        });
        //if we not connected but gui closed
        //this thread will interrupt
        connectThread.setDaemon(true);
        //start thread
        connectThread.start();

    }

    //method for click event
    static void onClicked(MouseEvent me) {
        if (me.getButton() == MouseButton.PRIMARY) {
            //send to server message from inputArea
            printWriter.println(inputTextArea.getText());
            printWriter.flush();
            // delete our input
            inputTextArea.setText("");
            String in;
            try {
                //if answer from server not null
                if ((in = bufferedReader.readLine()) != null) {
                    if (in.equals("")) {
                        // in case opposite client not send message yet we print
                        outputTextArea.appendText("No messages from Client" + (PORT == 5000 ? "B" : "A") + "\n");
                    } else if (in.equals("NOT_CONNECTED")) {
                        // in case opposite client not connected yet we print
                        outputTextArea.appendText("Opposite client not connected\n");
                    } else {
                        // in case we have message from opposite client
                        outputTextArea.appendText("Client" + (PORT == 5000 ? "B" : "A") + ": " + in + "\n");
                    }
                }
            } catch (IOException e) {
                System.out.println("Connection interrupted");
                //closing streams in case we lost connection
                try {
                    socket.close();
                    bufferedReader.close();
                    printWriter.close();
                } catch (IOException x) {
                    System.out.println("Cant close streams");
                }
            }

        }
    }

    //launch javaFx window
    public static void main(String[] args) {
        launch(args);
    }
}
