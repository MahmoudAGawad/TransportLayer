package selective.repeat;

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

	private CongestionControl congestionControl;

	public ThreadReceiver(DatagramPacket receivePacket,
						  ConcurrentLinkedQueue<WindowNode> window, int windowSize,
						  DatagramSocket socket, Test stop, CongestionControl congestionControl) {
		this.window = window;
		this.maxWindowSize = windowSize;
		this.childReceiverServerSocket = socket;
		this.packet = receivePacket;
		this.done = stop;

        this.congestionControl = congestionControl;
	}

	@Override
	public void run() {

		byte[] receiveAck = new byte[3048];
		try {

			while (!done.flag || !window.isEmpty()) {

				DatagramPacket receivePacket = new DatagramPacket(receiveAck,
						receiveAck.length);

                System.out.println("Receiving Acknowledgement...");

                childReceiverServerSocket.receive(receivePacket);

				AckPacket ackPacket = (AckPacket) Serializer
						.deserialize(receivePacket.getData());
				// get the Ack no from the data Packet
				int ackno = ackPacket.getAckno();
				 System.out.println("Ack. number = " + ackno);

				synchronized(this) {
					// get the Seqno of the Latest packet in the window. which
					// is at index Zero
					int base = window.peek().getSeqno();
					int index = (ackno - base) / 500;

					// Ack must be greater than the base

					if (base <= ackno) {
						// stop the timer of the required packet
						Iterator<WindowNode> it = window.iterator();
						for (int i = 0; i < index; i++) {
							it.next();
						}

                        WindowNode curNode = it.next();

                        if (index > 0) { // duplicate ack!

                            if (congestionControl.getState() == CongestionControl.FAST_RECOVERY) {
                                congestionControl.setCwnd(congestionControl
                                        .getCwnd() + 1);
                            } else {
                                congestionControl.updateAckCount();
                                if (congestionControl.getAckCount() == 3) {
                                    congestionControl.moveToFastRecovery();
                                }
                            }

                        } else { // new ack. for BASE!

                            if (congestionControl.getState() == CongestionControl.SLOW_START) {

                                congestionControl.setCwnd(congestionControl
                                        .getCwnd() + 1);
                                congestionControl.setAckCount(0);

                            } else if (congestionControl.getState() == CongestionControl.CONGESTION_AVOIDANCE) {
                                congestionControl.updateAvoidanceNewAck();
                            } else {
                                congestionControl.moveToCongestionAvoidance();
                            }

                        }

                        if (congestionControl.getState() == CongestionControl.SLOW_START
                                && congestionControl.getCwnd() >= congestionControl
                                .getSsthreshold()) {
                            congestionControl.moveToCongestionAvoidance();
                        }

						curNode.ackReceived();

						// loop over the window, and remove all the Ack packet
						while (!window.isEmpty() && window.peek().isAck()) {
							// watch out here some error might occur
							window.poll();
						}

						System.out.println("Current window size = " + window.size());
						// if(window.isEmpty()){
						// System.out.println("empty????????????????????????");
						// break;
						// }
					}
				}

			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
