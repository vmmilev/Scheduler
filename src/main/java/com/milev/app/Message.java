package main.java.com.milev.app;

public class Message implements Comparable<Message> {

	long msgId; // keep track of message
	long groupId;
	// status which determinate if this message terminates the group or the scheduler
	// 0 - normal msg, 1 - terminate group, 2 - terminate the scheduler
	int termMsg;
	MsgObserver msgObserver = null;

	@SuppressWarnings("unused")
	private Message() {}; // forbid using default constructor

	// Initialize
	public Message(long msgId, long groupId, int termMsg) { 
		this.groupId = groupId;
		this.msgId   = msgId; 
		this.termMsg = termMsg;
	}

	public Message(long msgId, long groupId, int termMsg, MsgObserver msgObserver) { 
		this.groupId = groupId;
		this.msgId   = msgId; 
		this.termMsg = termMsg;
		setMsgObserver(msgObserver);
	}

	// getters
	public long getGroup() { return groupId; }
	public long getId() { return msgId; } 
	public int isTerminating() { return termMsg; }

	// set the observer which will receive the notification when the message is consumed
	// in our case it is the scheduler
	public void setMsgObserver(MsgObserver msgObserver) { this.msgObserver = msgObserver; }

	// the resource is calling this when it's done with the message
	// we notify the scheduler here
	public void completed() {

		if(msgObserver != null)
			msgObserver.msgCompleted(this);
	}

	// we sort messages by their id(timestamp) if there is no priority for the group
	@Override
	public int compareTo(Message msg) {

		// unfortunately int Long.compare(long,long) comes with java 1.7	
		Long msg1Id = new Long(this.getId());
		Long msg2Id = new Long(msg.getId());

		return msg1Id.compareTo(msg2Id); 
	}
}
