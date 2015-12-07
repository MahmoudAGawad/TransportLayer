package com.alexu;

import packet.AckPacket;
import packet.Packet;
import utils.Serializer;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;

/**
 * Created by Mahmoud A.Gawad on 06/12/2015.
 */
public class ThreadSender extends Thread {

    private DatagramPacket requestPacket;

    public ThreadSender(DatagramPacket packet) {
        this.requestPacket = packet;
    }

    @Override
    public void run() {

        InetAddress IPAddress = requestPacket.getAddress();
        int port = requestPacket.getPort();

        String fileName = "";
        try {
            Packet dataPacket = (Packet) Serializer.deserialize(requestPacket.getData());
            fileName = new String(dataPacket.getData());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


        DatagramSocket childServerSocket = null;
        try {
            childServerSocket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }

        InputStream reader = null;
        try {
            System.out.println(fileName);
            reader = new FileInputStream(fileName);
            int len = 1024;
            int actuallyRead;
            int seqno = 0;

            byte[] chunk = new byte[len];
            byte[] receiveAck = new byte[1024];

            while ((actuallyRead = reader.read(chunk, 0, len)) != -1) {
                System.out.println(actuallyRead + " " + new String(chunk));

                byte [] actualData = Arrays.copyOf(chunk, actuallyRead);

                Packet toSendPacket = new Packet((short) actuallyRead, seqno, actualData);
                byte[] toSendBytes = Serializer.serialize(toSendPacket);

                DatagramPacket sendPacket = new DatagramPacket(toSendBytes, toSendBytes.length, IPAddress, port);
                childServerSocket.send(sendPacket);


                // 1. start timer and wait for the ack!
                int ackno = -1;
                while (ackno != seqno) {
                    DatagramPacket receivePacket = new DatagramPacket(receiveAck, receiveAck.length);
                    childServerSocket.receive(receivePacket);

                    AckPacket ackPacket = (AckPacket) Serializer.deserialize(receivePacket.getData());
                    ackno = ackPacket.getAckno();
                }

                seqno = 1 - seqno;
            }

            // file sent; notify the client
            Packet toSendPacket = new Packet((short) 6, 0, "ok 200".getBytes());
            byte[] toSendBytes = Serializer.serialize(toSendPacket);
            DatagramPacket sendPacket = new DatagramPacket(toSendBytes, toSendBytes.length, IPAddress, port);
            childServerSocket.send(sendPacket);

        } catch (FileNotFoundException e) {
            Packet toSendPacket = new Packet((short) 6, 0, "no 404".getBytes());
            byte[] toSendBytes = new byte[0];
            try {
                toSendBytes = Serializer.serialize(toSendPacket);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            DatagramPacket sendPacket = new DatagramPacket(toSendBytes, toSendBytes.length, IPAddress, port);
            try {
                childServerSocket.send(sendPacket);
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {

                }
            }
            if (childServerSocket != null) {
                childServerSocket.close();
            }
        }

    }
}
