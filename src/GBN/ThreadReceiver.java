package GBN;

import packet.AckPacket;
import packet.Packet;
import packet.WindowNode;
import utils.Serializer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Mahmoud A.Gawad on 07/12/2015.
 */
public class ThreadReceiver extends Thread {

	private ConcurrentLinkedQueue<WindowNode> window;
	private int maxWindowSize;
	private int portno;
	private DatagramPacket packet;

	private Test done;
	private DatagramSocket childReceiverServerSocket;

	public ThreadReceiver(DatagramPacket receivePacket, ConcurrentLinkedQueue<WindowNode> window, int windowSize,
			DatagramSocket socket, Test stop) {
		this.window = window;
		this.maxWindowSize = windowSize;
		this.childReceiverServerSocket = socket;
		this.packet = receivePacket;
		this.done = stop;
	}

	@Override
	public void run() {

		byte[] receiveAck = new byte[3048];
		try {

			while (!done.flag || !window.isEmpty()) {
				DatagramPacket receivePacket = new DatagramPacket(receiveAck, receiveAck.length);
				childReceiverServerSocket.receive(receivePacket);

				AckPacket ackPacket = (AckPacket) Serializer.deserialize(receivePacket.getData());
				// get the Ack no from the data Packet
				int ackno = ackPacket.getAckno();
				// System.out.println(ackno);

				// get the Seqno of the Latest packet in the window. which is at
				// index Zero
				int base = window.peek().getSeqno();
				int index = (ackno - base) / 500;
				// System.out.println("++" + base + "__" + ackno);
				// Ack must be greater than the base
				if (base <= ackno) {
					while (!window.isEmpty() && window.peek().getSeqno() < ackno)
						window.poll().getTimer().cancel();

					if (!window.isEmpty()) {
						window.peek().getTimer().scheduleAtFixedRate(new TimerTask() {
							@Override
							public void run() {
								doAction();
							}

						}, 0, 2000);
					}

					if (window.isEmpty()) {
						System.out.println("empty????????????????????????");
						break;
					}
				}

			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void doAction() {
		// send all packages
		for (WindowNode w : window) {
			try {
				byte[] toSendBytes = Serializer.serialize(w.getPacket());
//				if (((int) (Math.random() * 100)) % 10 != 1)
					childReceiverServerSocket.send(
							new DatagramPacket(toSendBytes, toSendBytes.length, w.getIpAddress(), w.getPortNum()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
