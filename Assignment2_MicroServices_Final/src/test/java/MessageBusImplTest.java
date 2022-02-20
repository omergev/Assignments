import bgu.spl.mics.*;
import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import static org.junit.Assert.*;

public class MessageBusImplTest {
    private MessageBusImpl messageBus;
    private MicroService microService1;
    private MicroService microService2;
    private ExampleEvent exampleEvent1;
    private ExampleEvent exampleEvent2;
    private ExampleEvent exampleEvent3;
    private Broadcast exampleBroadcast1;

    @Before
    public void setUp() throws Exception {
        messageBus = MessageBusImpl.getInstance();
        microService1 = new MicroService("ms1") {
            @Override
            protected void initialize() {

            }
        };
        microService2 = new MicroService("ms2") {
            @Override
            protected void initialize() {

            }
        };
        exampleEvent1 = new ExampleEvent("e");
        exampleEvent2 = new ExampleEvent("e");
        exampleEvent3 = new ExampleEvent("e");
        exampleBroadcast1 = new ExampleBroadcast("b");
        messageBus.register(microService1);
        messageBus.register(microService2);

    }

    @After
    public void tearDown() throws Exception {
        messageBus.unregister(microService1);
        messageBus.unregister(microService2);
    }


    @Test
    public void complete() {
        //subscribe an event.
        messageBus.subscribeEvent(exampleEvent1.getClass() ,microService1);
        //Send the event
        Future<String> f = messageBus.sendEvent(exampleEvent1);

        //Check if there is a result on Future object
        assertFalse(f.isDone());
        //Call complete method -  set a result on Future object
        messageBus.complete(exampleEvent1, "Done");
        //Check the result on Future object
        assertTrue(f.isDone());
        assertEquals(f.get(), "Done");

        //Check that micro-services is still register after the method
        assertTrue(messageBus.isRegistered(microService1));
    }

    @Test
    public void sendAndSubscribeBroadcast() {

        //Subscribe and send the broadcast to two micro-services
        messageBus.subscribeBroadcast(exampleBroadcast1.getClass(), microService1);
        messageBus.subscribeBroadcast(exampleBroadcast1.getClass(), microService2);
        messageBus.sendBroadcast(exampleBroadcast1);

        Message message1 = new ExampleBroadcast("message1");
        Message message2 = new ExampleBroadcast("message2");

        try {
            message1 = messageBus.awaitMessage(microService1);
            message2 = messageBus.awaitMessage(microService2);

        } catch (InterruptedException e) {
            System.out.println("No more messages!");
        }
        //The messages should be equals to the broadcast that the microServices subscribed to.
        assertEquals(message1,exampleBroadcast1);
        assertEquals(message2,exampleBroadcast1);

        //Check that micro-services is still register after the method
        assertTrue(messageBus.isRegistered(microService1));
        assertTrue(messageBus.isRegistered(microService2));

    }

    @Test

    public void sendAndSubscribeEvent() {

        //subscribe to event
        messageBus.subscribeEvent(exampleEvent1.getClass(), microService1);
        messageBus.subscribeEvent(exampleEvent1.getClass(), microService2);

        //Send the event (according to round-robin rotation)
        messageBus.sendEvent(exampleEvent1);
        messageBus.sendEvent(exampleEvent2);
        messageBus.sendEvent(exampleEvent3);

        Message message1 = new ExampleEvent("message1");
        Message message2 = new ExampleEvent("message2");
        Message message3 = new ExampleEvent("message3");

        try {
            //If the method is not working well - should get stuck in 'awaitMessage'
            message1 = messageBus.awaitMessage(microService1);
            message2 = messageBus.awaitMessage(microService2);
            message3 = messageBus.awaitMessage(microService1);


        } catch (InterruptedException e) {
            System.out.println("No more messages!");
        }
        //Check if it is the right message
        assertEquals(message1,exampleEvent1);
        assertEquals(message2,exampleEvent2);
        assertEquals(message3,exampleEvent3);


        //Check that micro-services is still register after the method
        assertTrue(messageBus.isRegistered(microService1));
        assertTrue(messageBus.isRegistered(microService2));
    }

    @Test
    public void register_unregister() {

        //subscribe the ms
        messageBus.subscribeEvent(exampleEvent1.getClass(), microService1);
        //Send event to ms1 message queue
        messageBus.sendEvent(exampleEvent1);

        try {
            //Check if the message is in ms1 message queue and if it is the right message
            //If the method is not working well - should get stuck in 'awaitMessage'
            Message m = messageBus.awaitMessage(microService1);
            assertEquals(m, exampleEvent1);

        } catch (InterruptedException e) {
            System.out.println("No more messages!");
        }

        //Check that micro-services is still register after the method
        assertTrue(messageBus.isRegistered(microService1));


        //Unregister
        messageBus.unregister(microService1);
        //Try to subscribe unregister ms to message queue - should do nothing.
        messageBus.subscribeEvent(exampleEvent1.getClass(), microService1);

        //Check that micro-services is not register after the method
        assertFalse(messageBus.isRegistered(microService1));

    }


    @Test
    //Checks if awaitMessage is a block method
    public void awaitMessage() {

        //subscribe event
        messageBus.subscribeEvent(exampleEvent1.getClass(), microService1);

        // Create new thread that first goes to sleep for 1000ms and then the mb send a event
        Thread t1 = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println("Interrupted");
            }
            messageBus.sendEvent(exampleEvent1);
        });

        t1.start();

        try {
            //Message Queue is empty right now! check if awaitMessage is a blocking method
            Message m = messageBus.awaitMessage(microService1);
            //If the method is block so the message should be 'e' - the message we sent after 1000ms.
            assertEquals(m, exampleEvent1);

        } catch (InterruptedException e) {
            System.out.println("No more messages!");
        }

        //Check that micro-services is still register after the method
        assertTrue(messageBus.isRegistered(microService1));
    }
}