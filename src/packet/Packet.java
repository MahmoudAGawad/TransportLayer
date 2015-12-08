package packet;

import java.io.*;

import utils.CheckSumCalculator;

/**
 * Created by Mahmoud A.Gawad on 06/12/2015.
 */
public class Packet implements Serializable {

    private short cksum; /* bonus part */
    private short len;
    private int seqno;

    private byte [] data;

    public Packet(short len, int seqno, byte[] data){
    	
        this.len = len;
        this.seqno = seqno;
        this.data = data;
        cksum = CheckSumCalculator.calculateCheckSumWithParam(len,seqno,data);
        
    }

    public Packet(short len, int seqno, short cksum, byte[] data){
        
    	this.len = len;
        this.seqno = seqno;
        this.data = data;
        this.cksum = CheckSumCalculator.calculateCheckSumWithParam(len,seqno,data);
        
    }

   
    
    

    public void setCksum(short cksum) {
        this.cksum = cksum;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setLen(short len) {
        this.len = len;
    }

    public void setSeqno(int seqno) {
        this.seqno = seqno;
    }

    public byte[] getData() {
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
