package org.jbpm.contrib;

import java.util.concurrent.Semaphore;

public class ActiveTasks {

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
        semaphore.acquire(Integer.MAX_VALUE);
    }
}
