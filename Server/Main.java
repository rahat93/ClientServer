import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    // array for clients sockets
    static Socket[] sockets;
    // array for serverSockets
    static ServerSocket[] serverSockets;
    // array for input stream
    static BufferedReader[] bufferedReaders;
    // array for output stream
    static PrintWriter[] printWriters;
    // here we hold message for clients
    static StringBuilder[] stringBuilders;


    // this method creates object for interacting with client
    static void createClient(char a) {
        // if ab=='a' that mean its first(0) client otherwise its second(1)
        int i = (a == 'a' ? 0 : 1);
        new Thread(() -> {
            try {
                // method accept() return Socket object when client was connected  to server
                sockets[i] = serverSockets[i].accept();
                try {
                    //bufferedRead for reads inputs and printWriter for send messages
                    bufferedReaders[i] = new BufferedReader(new InputStreamReader(sockets[i].getInputStream()));
                    printWriters[i] = new PrintWriter(sockets[i].getOutputStream());
                    //here we hold messages for opposite client
                    stringBuilders[i ^ 1] = new StringBuilder();
                    //which client was connected
                } catch (IOException e) {
                    System.out.println("Error while creating streams for client" + (i == 0 ? "A" : "B"));
                }
                System.out.println((i == 0 ? "A" : "B") + " connected");
                String in;
                // read message from client till null or exception
                try {


                    while ((in = bufferedReaders[i].readLine()) != null) {
                        // append message in stringBuilder for opposite client
                        stringBuilders[i ^ 1].append(in);
                        // if holder exist its mean opposite client had been connected to server other wise
                        // we send message "NOT_CONNECTED"
                        // if have message for this client we sent it to him
                        // and create new holder
                        if (stringBuilders[i] != null) {
                            printWriters[i].println(stringBuilders[i]);
                            printWriters[i].flush();
                            stringBuilders[i] = new StringBuilder();
                        } else {
                            //
                            printWriters[i].println("NOT_CONNECTED");
                            printWriters[i].flush();
                        }
                    }
                } catch (IOException e) {
                    //set null to know that opposite client was disconnected
                    stringBuilders[i]=null;
                    System.out.println("Connection interrupted with "+ (i == 0 ? "A" : "B"));
                }
            } catch (IOException e) {
                System.out.println("Error while creating socket for client" + (i == 0 ? "A" : "B"));
            }
            // try close all streams at the end
            try {
                bufferedReaders[i].close();
                printWriters[i].close();
                sockets[i].close();
            } catch (IOException e) {
                System.out.println("");
            }
        }).start();
    }


    public static void main(String[] args) {
        //create new arrays
        serverSockets = new ServerSocket[2];
        sockets = new Socket[2];
        bufferedReaders = new BufferedReader[2];
        printWriters = new PrintWriter[2];
        stringBuilders = new StringBuilder[2];
        // create new ServerSocket and clients
        // before choose any port you should check its free or not
        // https://en.wikipedia.org/wiki/List_of_TCP_and_UDP_port_numbers
        try {
            serverSockets[0] = new ServerSocket(5000);
            serverSockets[1] = new ServerSocket(6000);
            createClient('a');
            createClient('b');
        } catch (IOException e) {
            System.out.println("Error while creating server sockets.");
        }


    }
}
