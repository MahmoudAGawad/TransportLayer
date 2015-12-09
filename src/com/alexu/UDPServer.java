package com.alexu;

import packet.Packet;
import utils.Serializer;

import java.io.*;
import java.net.*;

public class UDPServer {
    public static void main(String args[]) throws Exception {

        /**
         * Well-known port number for server.
         Random generator seed value.
         Probability p of datagram loss
         (real number in the range [ 0.0 , 1.
         */
        BufferedReader k = new BufferedReader(new FileReader("server.in"));

        int serverPortNum = Integer.parseInt(k.readLine());
        k.readLine(); // sliding window
        int seed = Integer.parseInt(k.readLine());
        double probability = Double.parseDouble(k.readLine());

        k.close();

        DatagramSocket serverSocket = new DatagramSocket(serverPortNum);
        byte[] receiveData = new byte[1024];
        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);

            new ThreadSender(receivePacket, seed, probability).start();
        }
    }
}
