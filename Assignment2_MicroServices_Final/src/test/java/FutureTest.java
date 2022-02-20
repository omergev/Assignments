import bgu.spl.mics.Future;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class FutureTest {
    private static Future<Integer> f;

    @Before
    public void setUp() throws Exception {
        f = new Future<>();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void get() {
        Thread t1 = new Thread( () ->{
            try {
                Thread.sleep(1000);
            }catch (InterruptedException e){
                System.out.println(e);
            }
            f.resolve(4);
        });
        t1.start();
        //Result = null
        Integer x = f.get();
        //Result = 4
        assertEquals(4, (int) x);
    }

    @Test
    public void resolve() {
    }

    @Test
    public void isDone() {
    }

    @Test
    public void testGet() {
        Thread t1 = new Thread( () ->{
            try {
                Thread.sleep(1000);
            }catch (InterruptedException e){
                System.out.println(e);
            }
            f.resolve(4);
        });
        t1.start();
        //Result = null
        Integer x = f.get(2000, TimeUnit.MILLISECONDS);
        //Result = 4
        assertEquals(4, (int) x);

        f.resolve(null);
        Thread t2 = new Thread( () ->{
            try {
                Thread.sleep(2000);
            }catch (InterruptedException e){
                System.out.println(e);
            }
            f.resolve(4);
        });
        t2.start();
        //Result = null
        Integer y = f.get(500, TimeUnit.MILLISECONDS);
        //Result = 4
        assertNull(y);



    }

}