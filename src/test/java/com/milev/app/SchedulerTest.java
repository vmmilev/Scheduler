package test.java.com.milev.app;
import static org.junit.Assert.assertTrue;
import main.java.com.milev.app.Gateway;
import main.java.com.milev.app.Message;
import main.java.com.milev.app.MsgAlreadyTerminated;
import main.java.com.milev.app.Prioritizer;
import main.java.com.milev.app.Scheduler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class SchedulerTest {

	@Spy 
	Gateway gateway;
	Prioritizer prioritizer = null;
	Scheduler scheduler = null;
	
    @Before
    public void setUp() {
    	prioritizer	= new Prioritizer();
    	scheduler	= new Scheduler(2, gateway, prioritizer);
    }

    @After
	public void tearDown() {
    	gateway.testList.clear();
	}

    @Test
    public void testProcessWithPriority() throws MsgAlreadyTerminated {

    	Message msg1 = new Message(1, 2, 0, scheduler);
    	Message msg2 = new Message(2, 1, 0, scheduler);
    	Message msg3 = new Message(3, 2, 0, scheduler);
    	Message msg4 = new Message(4, 3, 0, scheduler);
    	Message msg5 = new Message(5, 9, 2, scheduler);    	

    	// fill the queue
    	scheduler.addMsg(msg1);
    	scheduler.addMsg(msg2);
    	scheduler.addMsg(msg3);
    	scheduler.addMsg(msg4);
    	scheduler.addMsg(msg5);

    	// process the queue
        scheduler.process();

        // check that those messages were sent
        Mockito.verify(gateway).send(msg1);
    	Mockito.verify(gateway).send(msg2);
    	Mockito.verify(gateway).send(msg3);
    	Mockito.verify(gateway).send(msg4);

    	// check the order in which the messages were sent
        assertTrue(gateway.testList.poll() == 1);
        assertTrue(gateway.testList.poll() == 3);
        assertTrue(gateway.testList.poll() == 2);
        assertTrue(gateway.testList.poll() == 4);
        assertTrue(gateway.testList.isEmpty());
    }
    
    @Test
    public void testCanceledGroups() throws MsgAlreadyTerminated {

    	Message msg1 = new Message(1, 2, 0, scheduler);
    	Message msg2 = new Message(2, 1, 0, scheduler);
    	Message msg3 = new Message(3, 2, 0, scheduler);
    	Message msg4 = new Message(4, 9, 2, scheduler);

    	// fill the queue
    	scheduler.addMsg(msg1);
    	scheduler.addMsg(msg2);
    	scheduler.addMsg(msg3);
    	scheduler.addMsg(msg4);

    	// cancel all messages from group 2
    	scheduler.cancelGroup(2);

    	// process the queue
        scheduler.process();

        // check that those messages were sent
    	Mockito.verify(gateway).send(msg2);

    	// check the order in which the messages were sent
        assertTrue(gateway.testList.poll() == 2);
        assertTrue(gateway.testList.isEmpty());
    }

    @Test (expected = MsgAlreadyTerminated.class)
    public void testTerminateGroup() throws MsgAlreadyTerminated {

    	Message msg1 = new Message(1, 2, 1, scheduler);
    	Message msg2 = new Message(2, 1, 0, scheduler);
    	Message msg3 = new Message(3, 2, 1, scheduler);

    	// fill the queue
    	scheduler.addMsg(msg1);
    	scheduler.addMsg(msg2);
    	scheduler.addMsg(msg3);

    	// process the queue
        scheduler.process();

        // check that those messages were sent
    	Mockito.verify(gateway).send(msg2);
    }

    @Test
    public void testResourceLimit() throws MsgAlreadyTerminated, InterruptedException {

    	Thread thread = new Thread() {
    		@Override
    		public void run() {

    			// create messages without passing the scheduler
    			// so it won't be notified
    			// the expected behavior is that process() will block
    			// so we know that we can't use more than 2 resources
    			Message msg1 = new Message(1, 2, 0, null);
    	    	Message msg2 = new Message(2, 1, 0, null);
    	    	Message msg3 = new Message(3, 2, 0, scheduler);
    	
    	    	// fill the queue
    	    	scheduler.addMsg(msg1);
    	    	scheduler.addMsg(msg2);
    	    	scheduler.addMsg(msg3);
    	
    	    	// process the queue
    	        try {
					scheduler.process();
				} catch (MsgAlreadyTerminated e) {
					assertTrue(false);
				}
    		}
    	};

    	thread.start();

		// Let the current thread sleep
		Thread.sleep(500);

    	assertTrue(thread.isAlive());
    	thread.interrupt();
    }
}
