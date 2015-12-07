package packet;

import java.io.Serializable;

/**
 * Created by Mahmoud A.Gawad on 06/12/2015.
 */
public class AckPacket implements Serializable{

    private short cksum; /* bonus part */
    private short len;
    private int ackno;

    public AckPacket(short len, int ackno){
        this.len = len;
        this.ackno = ackno;
    }

    public AckPacket(short len, int ackno, short cksum){
        this.cksum = cksum;
        this.len = len;
        this.ackno = ackno;
    }

    public int getAckno() {
        return ackno;
    }
}
