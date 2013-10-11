import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class Filter implements Runnable {
    protected BlockingQueue in;
    protected BlockingQueue out;
    protected volatile boolean done;
    
    public Filter(BlockingQueue in, BlockingQueue out) {
        this.in = in;
        this.out = out;
        this.done = false;
    }
    
    public void run() {
        Object o;
        while(! this.done) {
        	if(this.in != null) {
        		o = in.take();    // read from input queue, may block
        	}
            o = transform(o); // allow filter to change message
            out.put(o);       // forward to output queue
        }
    }
    
    protected abstract Object transform(Object o);
}