package packet;

import java.net.DatagramSocket;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Mahmoud A.Gawad on 07/12/2015.
 */
public class WindowNode {
    private Packet dataPacket;
    private boolean isAck;
    private Timer timer;

    public WindowNode(Packet packet, boolean isAck) {
        this.dataPacket = packet;
        this.isAck = isAck;
        this.timer = new Timer();
    }

    public int getSeqno() {
        return dataPacket == null ? -1 : dataPacket.getSeqno();
    }

    public void ackReceived(){
        isAck = true;
        timer.cancel();
    }
    public boolean isAck(){
        return  isAck;
    }

    public Timer getTimer() {
        return timer;
    }

    public void scheduleTime(final DatagramSocket socket) {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // logic toh handle time out

            }
        }, 2000, 2000);
    }


}
