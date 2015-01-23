package main.java.com.milev.app;
import java.util.LinkedList;

public class Gateway {

	// it's just for testing purposes
	// it keeps track of the order in which the messages are sent
	public LinkedList<Long> testList;
	
	public Gateway() {
		testList = new LinkedList<Long>();
	}

	public void send(Message msg) {

		testList.add(msg.getId());

		msg.completed();
	};
}
