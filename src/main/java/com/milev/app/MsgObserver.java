package main.java.com.milev.app;
// Observer pattern, we want to be able to notify the scheduler
// when the resource is done with the message
public interface MsgObserver {
	public void msgCompleted(Message msg);
}
