package main.java.com.milev.app;
import java.util.Comparator;
import java.util.concurrent.ConcurrentSkipListMap;


public class Prioritizer implements Comparator<Message> {

	// Map to hold message groups
	// msgId is the order of received messages (can be timestamp)
	// if the group is in the map then we have received message from this group
	// and we have it's msgId (timestamp) so we can prioritise it
	ConcurrentSkipListMap<Long,Long> groupOrder = null;

	// Initialize
	public Prioritizer() {
		groupOrder = new ConcurrentSkipListMap<Long,Long>();
	}

	// check that this if the first message from this group and if so - remember it's id (timestamp) 
	public void addGroupPriority(Message msg) {
		if(groupOrder.get(msg.getGroup()) == null)
			groupOrder.put(msg.getGroup(), msg.getId());
	}

	// remove the group from the priority map
	// if the message group was terminated or canceled we dont need priority
	public void removeGroupPriority(Message msg) {
		groupOrder.remove(msg.getGroup());
	}

	// here is the actual work of setting the message priority in the scheduler
	// groupOrder is a map which holds the msgId of the first message from each group
	// we use this information to choose the next message which will be sent to the resource
	@Override
	public int compare(Message msg1, Message msg2) {
		
		// unfortunately int Long.compare(long,long) comes with java 1.7
		Long msg1pos = groupOrder.get(msg1.getGroup());
		Long msg2pos = groupOrder.get(msg2.getGroup());

		int result = msg1pos.compareTo(msg2pos);
		if (result == 0) {
			msg1pos = msg1.getId();
			msg2pos = msg2.getId();
			return msg1pos.compareTo(msg2pos);
			
		} else
			return result;
	}
}
