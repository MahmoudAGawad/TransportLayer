package selective.repeat;

import com.alexu.*;
import packet.WindowNode;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Mahmoud A.Gawad on 07/12/2015.
 */
public class Server {



    public static void main(String args[]) throws Exception {


        BufferedReader k = new BufferedReader(new FileReader("server.in"));

        int serverPortNum = Integer.parseInt(k.readLine());
        int windowSize = Integer.parseInt(k.readLine());
        int seed = Integer.parseInt(k.readLine());

        double probability = Double.parseDouble(k.readLine());

        k.close();

        DatagramSocket serverSocket = new DatagramSocket(serverPortNum);

        byte[] receiveData = new byte[3024];

        while (true) {

            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);



            CongestionControl congestionControl = new CongestionControl();

            ConcurrentLinkedQueue<WindowNode> window = new ConcurrentLinkedQueue<>();
            DatagramSocket socket = new DatagramSocket();
            Test stop = new Test();
            stop.flag = false;
            new ThreadSender(receivePacket, window, windowSize, socket, stop, congestionControl, seed, probability).start();
            new ThreadReceiver(receivePacket, window, windowSize, socket, stop, congestionControl).start();
        }
    }
}
