package main.java.com.milev.app;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Semaphore;

public class Scheduler implements MsgObserver {
	
	int resources = 0;
	ConcurrentSkipListSet<Message> msgQueue= null;
	ConcurrentSkipListSet<Long> canceledGroups = null;
	ConcurrentSkipListSet<Long> terminatedGroups = null;
	private final Semaphore available;
	Gateway gw = null;
	Prioritizer prioritizer = null;

	// Initialize the scheduler
	public Scheduler(int resources, Gateway gw, Prioritizer prioritizer) {
		// create semaphore to guard the resources
		available = new Semaphore(resources, true);
		// set Comparator
		this.prioritizer = prioritizer;
		// set the gateway
		this.gw = gw;
		// create the queue which will hold the messages
		msgQueue = new ConcurrentSkipListSet<Message>(prioritizer); // set comparator
		// create the queue which will hold canceled groups		
		canceledGroups = new ConcurrentSkipListSet<Long>();
		// create the queue which will hold terminated groups
		terminatedGroups = new ConcurrentSkipListSet<Long>();
	}

	// release resource
	public void msgCompleted(Message msg) {
		// the scheduler was notified that msg was completed so we can release the resource
		available.release();
	}

	// cancel group
	public void cancelGroup(long groupId) {
		canceledGroups.add(groupId);
	}

	// add message to the queue till there is resource to process it
	public boolean addMsg(Message msg) {
		// if this message is from canceled group do not add it 
		if(canceledGroups.contains(msg.getGroup()))
				return false;
		prioritizer.addGroupPriority(msg);

		return msgQueue.add(msg);
	}
	
	public void process() throws MsgAlreadyTerminated {
		
		try {
			while(true) {			

				// wait for receiving message in the queue
				// it's ok to loop here waiting for it
				// after all we don't have anything else to do
				while(msgQueue.isEmpty()) {}
				
				// get next correct message from the queue
				Message msg = msgQueue.pollFirst();

				// if this group was canceled - skip it!
				if(canceledGroups.contains(msg.getGroup())) {
					// also remove the priority for this group
					// we won't need it anymore
					prioritizer.removeGroupPriority(msg);
					continue;
				}
					
				if(terminatedGroups.contains(msg.getGroup())) {
					// if this group was terminated - raise error!
					prioritizer.removeGroupPriority(msg);
					available.release(resources);
					throw new MsgAlreadyTerminated();
				}

				// if this message terminates the group - add it to the queue for terminated messages
				if(msg.isTerminating() == 1)
					terminatedGroups.add(msg.getGroup());

				// terminate the scheduler
				if(msg.isTerminating() == 2) {
					available.release(resources);
					break;
				}

				// acquire resource
				available.acquire();

				// finally send the message to available resource
				gw.send(msg);
			}		
		} catch (InterruptedException e) {
		}
	}

	public static void main(String[] args) {

	}

}
