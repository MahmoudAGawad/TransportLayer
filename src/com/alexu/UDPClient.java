package com.alexu;

import packet.AckPacket;
import packet.Packet;
import utils.Serializer;

import java.io.*;
import java.net.*;

public class UDPClient
{
    public static void main(String args[]) throws Exception
    {
        BufferedReader inFromUser =
                new BufferedReader(new InputStreamReader(System.in));
        DatagramSocket clientSocket = new DatagramSocket();
        InetAddress IPAddress = InetAddress.getByName("localhost");
        byte[] sendData = new byte[3024];
        byte[] receiveData = new byte[3024];

        // 1. send the file request.

        String fileName = inFromUser.readLine();
        Packet dataPacket = new Packet((short)fileName.length(), 0, fileName.getBytes());
        sendData = Serializer.serialize(dataPacket);

        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9875);
        // start timer
        clientSocket.send(sendPacket);

        // 2. begin receiving the file

        OutputStream writer = new FileOutputStream("receive.txt");
        String data = "";
        do{
            // 1. receive the data
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);
            dataPacket = (Packet) Serializer.deserialize(receivePacket.getData());
            data = new String(dataPacket.getData());

            if(data.equals("ok 200") || data.equals("no 404")){
                break;
            }
            writer.write(dataPacket.getData());

            // 2. send the ack
            AckPacket ackPacket = new AckPacket((short)0, dataPacket.getSeqno());
            byte[] toSendBytes = Serializer.serialize(ackPacket);
            sendPacket = new DatagramPacket(toSendBytes, toSendBytes.length, receivePacket.getAddress(), receivePacket.getPort());
            clientSocket.send(sendPacket);

        }while(true);

        writer.close();
        clientSocket.close();
    }
}