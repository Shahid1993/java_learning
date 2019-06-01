package com.shahid;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
 
/**
 * Created by Shahid on 01/06/19.
 */
@Component
@Scope("prototype")
public class MyThread implements Runnable {
 
    private static final Logger LOGGER = LoggerFactory.getLogger(MyThread.class);
 
    @Override
    public void run() {
         
        LOGGER.info("Called from thread");
    }
}
