
package selective.repeat;


public class CongestionControl {

	// defined state
	static final int SLOW_START = 1;
	static final int CONGESTION_AVOIDANCE = 2;
	static final int FAST_RECOVERY = 3;

	// defined variables
	private int cwnd;
	private int ssthreshold;
	private int AckCount;
	private int state;
	
	private int avoidanceCwndUpperBound;
	private int avoidanceNewAck;

    String[] printState;

	public CongestionControl() {
		cwnd = 1;
		AckCount = 0;
		// to be determined later
		ssthreshold = 5;
		state = 1;
		
		avoidanceCwndUpperBound = 0;
		avoidanceNewAck = 0;
        printState = new String[]{"", "SLOW_START", "CONGESTION_AVOIDANCE", "FAST_RECOVERY"};

	}


    private void log(){
        System.out.printf("[STATE]: %s\n\t-[CWND]: %d\n\t-[SSTHREDHOLD]: %d\n", printState[state], cwnd, ssthreshold);
    }

	public void moveToSlowStart() {
		ssthreshold = cwnd/2;
		cwnd = 1;
		AckCount = 0;
		
		avoidanceCwndUpperBound = 0;
		avoidanceNewAck = 0;
		
		state = SLOW_START;

        log();
	}

	public void moveToCongestionAvoidance() {
		if(state == FAST_RECOVERY){
			cwnd = ssthreshold;
			AckCount = 0;
		}
		
		avoidanceCwndUpperBound = cwnd;
		avoidanceNewAck = 0;
		
		state = CONGESTION_AVOIDANCE;

        log();
	}

	public void moveToFastRecovery() {
		ssthreshold = cwnd/2;
		cwnd = ssthreshold + 3;
		
		avoidanceCwndUpperBound = 0;
		avoidanceNewAck = 0;
		
		state = FAST_RECOVERY;

        log();
	}
	
	public void updateAvoidanceNewAck(){
		avoidanceNewAck++;
		if(avoidanceNewAck == avoidanceCwndUpperBound){
			cwnd++;
			AckCount = 0;
			avoidanceNewAck = 0;
		}
        log();
	}

	public int getCwnd() {
		// we might put the lock here instead
		return cwnd;
	}

	public void setCwnd(int cwnd) {
		this.cwnd = cwnd;
	}

	public int getSsthreshold() {
		return ssthreshold;
	}

	public void setSsthreshold(int ssthreshold) {
		this.ssthreshold = ssthreshold;
	}

	public int getAckCount() {
		return AckCount;
	}

	public void setAckCount(int ackCount) {
		AckCount = ackCount;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public void updateCwnd() {

	}

	public void updateAckCount() {
		AckCount++;

	}
}
