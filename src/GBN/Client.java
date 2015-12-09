package GBN;

/**
 * Created by Mahmoud A.Gawad on 07/12/2015.
 */

import packet.AckPacket;
import packet.Packet;
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
	private static int lastAcked = -1 * dataSize;

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
		writer = new FileOutputStream("ans.txt");
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
		String sentence = "send50.txt";
		byte[] fileName = sentence.getBytes();
		// create new dataPacket and serialize it
		Packet dataPacket = new Packet((short) fileName.length, 0, fileName);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		ObjectOutputStream os = new ObjectOutputStream(outputStream);

		os.writeObject(dataPacket);
		os.close();
		sendData = outputStream.toByteArray();

		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ipAddress, 9875);

		clientSocket.send(sendPacket);
		/* Finished sending filename */

		// start receiving the file

		// determine the exit condition

		while (true) {
			/* receiving packets from the sender */
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			clientSocket.receive(receivePacket);
			/*------------------------------------------------------*/
			/* Deserializing the received packet data to data packet */
			ByteArrayInputStream in = new ByteArrayInputStream(receivePacket.getData());
			ObjectInputStream is = new ObjectInputStream(in);
			Packet recvData = (Packet) is.readObject();

			is.close();
			/*------------------------------------------------------*/

			/* The main functionality of selective repeat(extracting info.) */
			int seqNum = recvData.getSeqno();
			int portNum = receivePacket.getPort();
			/*----------------------------------------*/

			// System.out.println(index + "\t\t" + seqNum);
			int cur = lastAcked;
			sendACK(seqNum, portNum);
			System.out.println(new String(receiveData));
			if (seqNum == lastAcked && cur != lastAcked) {
				recvBase = writeToFile(recvData);
				if (recvBase == -1) {
					break;
				}
			}

		}
		System.out.println("la");

		writer.close();
	}

	private static int writeToFile(Packet recvData) throws IOException {

		int curSeqNum = 0;
		if (recvData != null) {
			byte[] oldData = recvData.getData();
			byte[] newData = Arrays.copyOfRange(oldData, 0, recvData.getLen());
			// System.out.println(oldData.length + " is " + newData.length + "
			// is " + recvData.getSeqno());
			String data = new String(newData);
			// System.out.println(data);

			if (data.equals("ok 200")) {
				System.out.println(data);
				return -1;
			}
			// writer.append(new String(window.get(0).getData()));
			writer.write(newData);
			curSeqNum = recvData.getSeqno();

		}
		return curSeqNum + dataSize;

	}

	private static void sendACK(int seqNum, int port) throws IOException {

		AckPacket ackPacket = new AckPacket((short) 0,
				(seqNum == lastAcked + dataSize) ? (lastAcked = seqNum) : lastAcked);
		DatagramSocket clientSocket2 = new DatagramSocket();

		ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();

		ObjectOutputStream os2 = new ObjectOutputStream(outputStream2);

		os2.writeObject(ackPacket);
		os2.close();

		byte[] sendData2 = outputStream2.toByteArray();
		// System.out.println("portno: "+port);
		DatagramPacket sendPacket = new DatagramPacket(sendData2, sendData2.length, ipAddress, port);
//		if (((int) (Math.random() * 100)) % 10 != 1) {
			clientSocket2.send(sendPacket);
//			System.out.println(" -----====== " + lastAcked);
//		}
	}

}