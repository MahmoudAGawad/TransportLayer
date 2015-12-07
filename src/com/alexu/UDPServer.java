package com.alexu;

import packet.Packet;
import utils.Serializer;

import java.io.*;
import java.net.*;

public class UDPServer {
    public static void main(String args[]) throws Exception {
        DatagramSocket serverSocket = new DatagramSocket(9875);
        byte[] receiveData = new byte[1024];
        while (true) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);

            new ThreadSender(receivePacket).start();
        }
    }
}