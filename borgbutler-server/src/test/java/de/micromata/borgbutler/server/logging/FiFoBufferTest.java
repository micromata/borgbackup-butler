package de.micromata.borgbutler.server.logging;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class FiFoBufferTest {
    private FiFoBuffer<Long> fiFoBuffer;
    private Long counter = 0L;
    private ArrayList<Thread> threads = new ArrayList<>();

    @Test
    void test() {
        fiFoBuffer = new FiFoBuffer<>(1000);
        for (int i = 0; i < 10; i++) {
            startProducerThread();
            startConsumerThread(i % 2 == 0);
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ex) {

            }
        }
        Assert.assertEquals(100000, counter.longValue());
    }

    private void startProducerThread() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 10000; i++) {
                    long value;
                    synchronized (threads) {
                        value = ++counter;
                    }
                    fiFoBuffer.add(value);
                }
            }
        };
        thread.start();
        threads.add(thread);
    }

    private void startConsumerThread(boolean ascending) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {
                    if (ascending) {
                        for (int j = 0; j < fiFoBuffer.getSize(); j++) {
                            fiFoBuffer.get(j);
                        }
                    } else {
                        for (int j = fiFoBuffer.getSize(); j >= 0; j--) {
                            fiFoBuffer.get(j);
                        }
                    }
                }
            }
        };
        thread.start();
        threads.add(thread);
    }
}
