package selective.repeat;

/**
 * Created by Mahmoud A.Gawad on 07/12/2015.
 */

import packet.AckPacket;
import packet.Packet;
import utils.CheckSumCalculator;
import utils.Serializer;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.LinkedList;

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
    public static void main(String args[]) throws Exception,  IOException {
        writer = new FileOutputStream("receivedd.txt");
        winSize = 20;
        window = new LinkedList<>();
        for (int i = 0; i < winSize; i++) {
            window.add(null);

        }
        recvBase = 0;
        DatagramSocket clientSocket = new DatagramSocket();

        ipAddress = InetAddress.getByName("localhost");
        byte[] sendData = new byte[1024];
        byte[] receiveData = new byte[3024];
        /* Sending FileName to the server */
        String sentence = "actxml.txt";
        byte[] fileName = sentence.getBytes();
        // create new dataPacket and serialize it
        Packet dataPacket = new Packet((short) fileName.length, 0, fileName);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        ObjectOutputStream os = new ObjectOutputStream(outputStream);

        os.writeObject(dataPacket);
        os.close();
        sendData = outputStream.toByteArray();

        DatagramPacket sendPacket = new DatagramPacket(sendData,
                sendData.length, ipAddress, 9875);

        clientSocket.send(sendPacket);
		/* Finished sending filename */

        // start receiving the file

        // determine the exit condition

        while (true) {
			/* receiving packets from the sender */
            DatagramPacket receivePacket = new DatagramPacket(receiveData,
                    receiveData.length);
            clientSocket.receive(receivePacket);



			/*------------------------------------------------------*/
			/* Deserializing the received packet data to data packet */
            ByteArrayInputStream in = new ByteArrayInputStream(
                    receivePacket.getData());
            ObjectInputStream is = new ObjectInputStream(in);
            Packet recvData = (Packet) is.readObject();

            is.close();
            
            
            // checking CheckSum
            short calculatedCheckSum=CheckSumCalculator.calculateCheckSumWithParam(recvData.getLen(), recvData.getSeqno(), recvData.getData());
            short arrivedCheckSum=recvData.getCksum();
            boolean checkSumIsEqual= calculatedCheckSum==arrivedCheckSum;                     
          
             if(checkSumIsEqual){  
			/*------------------------------------------------------*/

			/* The main functionality of selective repeat(extracting info.) */
            int seqNum = recvData.getSeqno();
            int portNum = receivePacket.getPort();
			/*----------------------------------------*/
            int index = (seqNum - recvBase) / dataSize;
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
            
            sendACK(seqNum, portNum);
            
            if (index == 0) {
                window.set(index, recvData);
                /* Writing to file and updating recvBase */
                recvBase = writeToFile();

                if(recvBase == -1){
                    break;
                }

            } else if (index > 0) {
                window.set(index, recvData);
            }

        }
             else{
            	            	            	 
            	 System.out.println("check sum is wrong");
             }
        }
        writer.close();
    }
    private static int writeToFile() throws IOException {

        int curSeqNum = 0;
        while (window.get(0) != null) {
            byte[]oldData = window.get(0).getData();
            byte[]newData = Arrays.copyOfRange(oldData , 0 , window.get(0).getLen());
            System.out.println(oldData.length+" is  "+newData.length+"  is "+window.get(0).getSeqno());
            String data = new String(newData);
//            System.out.println(data);

            if(data.equals("ok 200")){
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

        ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();

        ObjectOutputStream os2 = new ObjectOutputStream(outputStream2);

        os2.writeObject(ackPacket);
        os2.close();

        byte[] sendData2 = outputStream2.toByteArray();
//        System.out.println("portno: "+port);
        DatagramPacket sendPacket = new DatagramPacket(sendData2,
                sendData2.length, ipAddress, port);
        clientSocket2.send(sendPacket);

    }

}