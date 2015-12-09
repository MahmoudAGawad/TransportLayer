package GBN;

import packet.Packet;
import packet.WindowNode;
import utils.Serializer;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by Mahmoud A.Gawad on 07/12/2015.
 */
public class ThreadSender extends Thread {

	private DatagramPacket packet;
	private ConcurrentLinkedQueue<WindowNode> window;
	private int maxWindowSize;
	private DatagramSocket childServerSocket;

	private Test done;

	public ThreadSender(DatagramPacket packet, ConcurrentLinkedQueue<WindowNode> window, int windowSize,
			DatagramSocket socket, Test stop) {
		this.packet = packet;
		this.window = window;
		this.maxWindowSize = windowSize;
		this.childServerSocket = socket;
		this.done = stop;
	}

	@Override
	public void run() {

		// get IPaddress and port from the packet
		InetAddress IPAddress = packet.getAddress();
		int port = packet.getPort();
		// get file name from the packet
		String fileName = "";
		try {
			Packet dataPacket = (Packet) Serializer.deserialize(packet.getData());
			fileName = new String(dataPacket.getData());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		InputStream reader = null;

		try {

			System.out.println(fileName);
			reader = new FileInputStream(fileName);
			int len = 500;
			int actuallyRead;
			int seqno = 0;

			byte[] chunk = new byte[len];

			while (true) {
				if (window.size() < maxWindowSize) {
					chunk = new byte[len];
					actuallyRead = reader.read(chunk, 0, len);

					if (actuallyRead == -1) {
						break;
					}

					Packet packet = new Packet((short) actuallyRead, seqno, chunk);
					WindowNode node = new WindowNode(packet, false);
					node.setIpAddress(IPAddress);
					node.setPortNum(port);
					// syncronized
					window.add(node);
					// synchronized
					if (window.peek() == node)
						node.getTimer().scheduleAtFixedRate(new TimerTask() {
							@Override
							public void run() {
								doAction();
							}
						}, 0, 2000);

					seqno += len;
				} else {
					// System.out.println("Fulllllllllllllllllllllll");
				}
			}

			done.flag = true;

			System.out.println("Hell yo");
			// file sent; notify the client
			Packet toSendPacket = new Packet((short) 6, seqno, "ok 200".getBytes());
			byte[] toSendBytes = Serializer.serialize(toSendPacket);
			DatagramPacket sendPacket = new DatagramPacket(toSendBytes, toSendBytes.length, packet.getAddress(),
					packet.getPort());
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
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {

				}
			}
			// if (childServerSocket != null) {
			// childServerSocket.close();
			// }
		}

	}

	protected void doAction() {
		// send all packages
		for (WindowNode w : window) {
			try {
				byte[] toSendBytes = Serializer.serialize(w.getPacket());
//				if (((int) (Math.random() * 100)) % 10 != 1)
					childServerSocket.send(
							new DatagramPacket(toSendBytes, toSendBytes.length, w.getIpAddress(), w.getPortNum()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
