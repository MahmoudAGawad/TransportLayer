package com.alexu;

import packet.AckPacket;
import packet.Packet;
import utils.CheckSumCalculator;
import utils.Serializer;

import java.io.*;
import java.net.*;

public class UDPClient {
    public static void main(String args[]) throws Exception {

        BufferedReader k = new BufferedReader(new FileReader("client.in"));

        String serverIp = k.readLine();
        int serverPortNum = Integer.parseInt(k.readLine());
        int clientPortNum = Integer.parseInt(k.readLine());
        String requestFile = k.readLine();
        String outputFile = k.readLine();

        k.close();

        DatagramSocket clientSocket = new DatagramSocket(clientPortNum);
        InetAddress IPAddress = InetAddress.getByName(serverIp);
        byte[] sendData = new byte[3024];
        byte[] receiveData = new byte[3024];

        // 1. send the file request.

        String fileName = requestFile;
        Packet dataPacket = new Packet((short) fileName.length(), 0, fileName.getBytes());
        sendData = Serializer.serialize(dataPacket);



        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, serverPortNum);
        // start timer

        long startTime   = System.currentTimeMillis();
        clientSocket.send(sendPacket);

        // 2. begin receiving the file

        OutputStream writer = new FileOutputStream(outputFile);
        String data = "";
        do {
            // 1. receive the data
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            System.out.println("Receiving next packet...");
            clientSocket.receive(receivePacket);
            System.out.println("Waiting for Acknowledgment...");

            dataPacket = (Packet) Serializer.deserialize(receivePacket.getData());
            data = new String(dataPacket.getData());

            // checking CheckSum
            short calculatedCheckSum = CheckSumCalculator.calculateCheckSumWithParam(dataPacket.getLen(), dataPacket.getSeqno(), dataPacket.getData());
            short arrivedCheckSum = dataPacket.getCksum();
            boolean checkSumIsEqual = calculatedCheckSum == arrivedCheckSum;


            // checking CheckSum
            if (checkSumIsEqual) {
                writer.write(dataPacket.getData());

                // 2. send the ack
                AckPacket ackPacket = new AckPacket((short) 0, dataPacket.getSeqno());
                byte[] toSendBytes = Serializer.serialize(ackPacket);
                sendPacket = new DatagramPacket(toSendBytes, toSendBytes.length, receivePacket.getAddress(), receivePacket.getPort());


                clientSocket.send(sendPacket);
                System.out.println("Acknowledgment has been sent");
            } else {
                System.out.println("check sum is wrong");
            }

            if (data.equals("ok 200") || data.equals("no 404")) {
                System.out.println("Done Receiving File...exit");
                break;
            }


        } while (true);

        writer.close();

        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("TOTAL TIME : "+ totalTime);

        clientSocket.close();
    }
}