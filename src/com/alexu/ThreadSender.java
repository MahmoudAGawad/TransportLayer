package com.alexu;

import packet.AckPacket;
import packet.Packet;
import utils.CheckSumCalculator;
import utils.Serializer;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Mahmoud A.Gawad on 06/12/2015.
 */
public class ThreadSender extends Thread {

    private DatagramPacket requestPacket;

    private DatagramSocket childServerSocket = null;

    private int seed;
    private double plp;

    public ThreadSender(DatagramPacket packet, int seed, double probability) {
        this.requestPacket = packet;
        this.seed = seed;
        this.plp = probability;
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



            while ((actuallyRead = reader.read(chunk, 0, len)) != -1) {

                System.out.println("Reading from file...\n"+actuallyRead+" bytes was read");

                byte [] actualData = Arrays.copyOf(chunk, actuallyRead);

                Packet toSendPacket = new Packet((short) actuallyRead, seqno, actualData);
                
                // to test if we sent wrong check sum, we can send any short random number
              //  toSendPacket.setCksum((short)34);
                
                byte[] toSendBytes = Serializer.serialize(toSendPacket);

                DatagramPacket sendPacket = new DatagramPacket(toSendBytes, toSendBytes.length, IPAddress, port);

                // 1. start timer and wait for the ack!

                Timer timer = new Timer();

                scheduleTimer(timer, sendPacket);

                System.out.println("Receiving Acknowledgment...");
                receiveAcknowledge(seqno); // blocking call

                System.out.println("Acknowledgment received...stopping timer...");
                timer.cancel();
                seqno = 1 - seqno;
            }

            // file sent; notify the client
            Packet toSendPacket = new Packet((short) 6, seqno, "ok 200".getBytes());
            byte[] toSendBytes = Serializer.serialize(toSendPacket);
            DatagramPacket sendPacket = new DatagramPacket(toSendBytes, toSendBytes.length, IPAddress, port);

            Timer timer = new Timer();
            scheduleTimer(timer, sendPacket);

            receiveAcknowledge(seqno); // blocking call
            timer.cancel();

        } catch (FileNotFoundException e) {
            Packet toSendPacket = new Packet((short) 6, 0, "no 404".getBytes());
            byte[] toSendBytes = new byte[0];
            try {
                toSendBytes = Serializer.serialize(toSendPacket);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            DatagramPacket sendPacket = new DatagramPacket(toSendBytes, toSendBytes.length, IPAddress, port);

            Timer timer = new Timer();
            scheduleTimer(timer, sendPacket);

            receiveAcknowledge(0); // blocking call
            timer.cancel();

        } catch (IOException e) {
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

    private void doAction(final DatagramPacket datagramPacket, int cnt){
        try {
            if(cnt==0) {
                System.out.println("Sending next packet...");
            }else{
                System.out.println("Timeout!...Resending packet...");
            }
            childServerSocket.send(datagramPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void scheduleTimer(Timer timer, DatagramPacket sendPacket){
        timer.scheduleAtFixedRate(new TimerTask() {
            Random rand = new Random();
            int cnt = 0;
            @Override
            public void run() {

                int randNum = rand.nextInt(seed);
//                System.out.println(new String(sendPacket.getData()));

                if (randNum >= (int) (plp * seed)) {
                    doAction(sendPacket, cnt);
                } // else -> loss occur
                else{
                    System.out.println("Loss!!");
                }
                cnt++;

                //checking checkSum last
                // return the packet to the correct value
                //   sendPacket.setData(toSendBytes1);
                //  sendPacket.setLength(toSendBytes1.length);

            }

        }, 0 , 1000);
    }


    private void receiveAcknowledge(int seqno){

        byte[] receiveAck = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveAck, receiveAck.length);

        int ackno = -1;
        while (ackno != seqno) {
            try {
                childServerSocket.receive(receivePacket);
                AckPacket ackPacket = (AckPacket) Serializer.deserialize(receivePacket.getData());
                ackno = ackPacket.getAckno();
            }catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
