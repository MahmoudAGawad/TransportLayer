package selective.repeat;

import com.alexu.*;
import packet.WindowNode;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Mahmoud A.Gawad on 07/12/2015.
 */
public class Server {

    final static int MAX_WINDOW_SIZE = 10;

    public static void main(String args[]) throws Exception {
        DatagramSocket serverSocket = new DatagramSocket(9875);
        byte[] receiveData = new byte[3024];
//        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);


            ConcurrentLinkedQueue<WindowNode> window = new ConcurrentLinkedQueue<>();
            DatagramSocket socket = new DatagramSocket(9874);
            Test stop = new Test();
            stop.flag = false;
            new ThreadSender(receivePacket, window, MAX_WINDOW_SIZE, socket, stop).start();
            new ThreadReceiver(receivePacket, window, MAX_WINDOW_SIZE, socket, stop).start();
//        }
    }
}
