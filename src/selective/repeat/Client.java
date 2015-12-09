package selective.repeat;

/**
 * Created by Mahmoud A.Gawad on 07/12/2015.
 */

import packet.AckPacket;
import packet.Packet;
import packet.WindowNode;
import utils.CheckSumCalculator;
import utils.Serializer;

import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
    static final int dataSize = 500;
    private static LinkedList<Packet> window;
    private static int winSize;
    private static int recvBase;// expected sequence number.

    static OutputStream writer;

    private static InetAddress ipAddress;

    public Client(int windowSize) {

        winSize = windowSize;
        window = new LinkedList<>();
        for (int i = 0; i < winSize; i++) {
            window.add(null);

        }
        recvBase = 0;
    }

    public static void main(String args[]) throws Exception, IOException {

        BufferedReader k = new BufferedReader(new FileReader("client.in"));

        String serverIp = k.readLine();
        int serverPortNum = Integer.parseInt(k.readLine());
        int clientPortNum = Integer.parseInt(k.readLine());
        String requestFile = k.readLine();
        String outputFile = k.readLine();

        winSize = Integer.parseInt(k.readLine());
        k.close();


        writer = new FileOutputStream(outputFile);

        window = new LinkedList<>();
        for (int i = 0; i < winSize; i++) {
            window.add(null);

        }
        recvBase = 0;
        DatagramSocket clientSocket = new DatagramSocket(clientPortNum);

        ipAddress = InetAddress.getByName(serverIp);
        byte[] sendData = new byte[1024];
        byte[] receiveData = new byte[3024];
        /* Sending FileName to the server */

        byte[] fileName = requestFile.getBytes();
        // create new dataPacket and serialize it
        Packet dataPacket = new Packet((short) fileName.length, 0, fileName);

        sendData = Serializer.serialize(dataPacket);

        DatagramPacket sendPacket = new DatagramPacket(sendData,
                sendData.length, ipAddress, serverPortNum);


        long startTime = System.currentTimeMillis();

        clientSocket.send(sendPacket);
//        Timer timer = new Timer();
//
//        scheduleTimer(clientSocket, timer, sendPacket);
//
//        receiveAcknowledge(clientSocket, 0);
//
//        timer.cancel();

        /* Finished sending filename */

        // start receiving the file

        // determine the exit condition

        while (true) {
            /* receiving packets from the sender */
            DatagramPacket receivePacket = new DatagramPacket(receiveData,
                    receiveData.length);
            System.out.println("Receiving next packet...");
            clientSocket.receive(receivePacket);


			/* Deserializing the received packet data to data packet */
            Packet recvData = (Packet) Serializer.deserialize(receivePacket.getData());


            // checking CheckSum
            short calculatedCheckSum = CheckSumCalculator.calculateCheckSumWithParam(recvData.getLen(), recvData.getSeqno(), recvData.getData());
            short arrivedCheckSum = recvData.getCksum();
            boolean checkSumIsEqual = calculatedCheckSum == arrivedCheckSum;

            if (checkSumIsEqual) {
            /*------------------------------------------------------*/

                int seqno = recvData.getSeqno();
                int portno = receivePacket.getPort();


                int index = (seqno - recvBase) / dataSize;
			/*approciate index for this packet in window*/

			/*
			 * SEND ACK for all cases. index >= 0 && index < n : firstly ACK
			 * index < 0 send ACK again.
			 */
//            System.out.println(index + " " + winSize);
                if (index >= winSize) {
				/*invalid sequence number.*/
                    continue;

                }
//            System.out.println(index +  "\t\t" + seqNum);

                System.out.println("Sending Acknowledgement...");
                sendACK(seqno, portno);
                if (index == 0) {
                    window.set(index, recvData);
                /* Writing to file and updating recvBase */
                    recvBase = writeToFile();

                    if (recvBase == -1) {
                        break;
                    }

                } else if (index > 0) {
                    window.set(index, recvData);
                }

            } else {
                System.out.println("check sum is wrong");
            }
        }
        writer.close();

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("TOTAL TIME : " + totalTime);

        clientSocket.close();
    }

    private static int writeToFile() throws IOException {

        int curSeqNum = 0;
        while (window.get(0) != null) {
            byte[] oldData = window.get(0).getData();
            byte[] newData = Arrays.copyOfRange(oldData, 0, window.get(0).getLen());
//            System.out.println(oldData.length + " is  " + newData.length + "  is " + window.get(0).getSeqno());
            String data = new String(newData);
//            System.out.println(data);

            if (data.equals("ok 200") || data.equals("no 404")) {
                return -1;
            }
//            writer.append(new String(window.get(0).getData()));
            writer.write(newData);
            curSeqNum = window.get(0).getSeqno();
            window.removeFirst();
            window.addLast(null);

        }
        return curSeqNum + dataSize;

    }

    private static void sendACK(int seqNum, int port) throws IOException {

        AckPacket ackPacket = new AckPacket((short) 0, seqNum);
        DatagramSocket clientSocket2 = new DatagramSocket();


        byte[] sendData2 = Serializer.serialize(ackPacket);

//        System.out.println("portno: "+port);
        DatagramPacket sendPacket = new DatagramPacket(sendData2,
                sendData2.length, ipAddress, port);
        clientSocket2.send(sendPacket);

        clientSocket2.close();
    }


    private static void scheduleTimer(DatagramSocket socket, Timer timer, DatagramPacket sendPacket) {
        timer.scheduleAtFixedRate(new TimerTask() {


            @Override
            public void run() {
                try {
                    socket.send(sendPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }, 0, 1000);
    }

    private static void receiveAcknowledge(DatagramSocket socket, int seqno) {

        byte[] receiveAck = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveAck, receiveAck.length);

        int ackno = -1;
        while (ackno != seqno) {
            try {
                socket.receive(receivePacket);
                AckPacket ackPacket = (AckPacket) Serializer.deserialize(receivePacket.getData());
                ackno = ackPacket.getAckno();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

}