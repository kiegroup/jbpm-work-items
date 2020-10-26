package org.jbpm.contrib;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;

public class ActiveTasks {

    private final Logger logger = LoggerFactory.getLogger(ActiveTasks.class);

    private Semaphore semaphore = new Semaphore(Integer.MAX_VALUE);

    public void started() {
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void completed() {
        semaphore.release();
    }

    public void waitAllCompleted() throws InterruptedException {
        logger.debug("Waiting all completed...");
        semaphore.acquire(Integer.MAX_VALUE);
        logger.debug("All completed.");
    }
}
