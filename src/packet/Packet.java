package packet;

import java.io.*;

/**
 * Created by Mahmoud A.Gawad on 06/12/2015.
 */
public class Packet implements Serializable {

    private short cksum; /* bonus part */
    private short len;
    private int seqno;

    private char [] data;

    public Packet(short len, int seqno, char[] data){
        this.len = len;
        this.seqno = seqno;
        this.data = data;
    }

    public Packet(short len, int seqno, short cksum, char[] data){
        this.cksum = cksum;
        this.len = len;
        this.seqno = seqno;
        this.data = data;
    }


    public void setCksum(short cksum) {
        this.cksum = cksum;
    }

    public void setData(char[] data) {
        this.data = data;
    }

    public void setLen(short len) {
        this.len = len;
    }

    public void setSeqno(int seqno) {
        this.seqno = seqno;
    }

    public char[] getData() {
        return data;
    }

    public int getSeqno() {
        return seqno;
    }

    public short getCksum() {
        return cksum;
    }

    public short getLen() {
        return len;
    }
}
