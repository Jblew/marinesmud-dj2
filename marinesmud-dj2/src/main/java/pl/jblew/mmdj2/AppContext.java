/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.mmdj2;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.jblew.marinesmud.framework.services.Service;
import pl.jblew.marinesmud.framework.services.ServiceInjector;
import pl.jblew.marinesmud.framework.services.ServiceManager;
import pl.jblew.marinesmud.framework.util.TrueUID;
import pl.jblew.mmdj2.scene.Scene;

/**
 *
 * @author teofil
 */
public final class AppContext implements Service {
    private final long startupTime = System.currentTimeMillis();
    private final String threadName = "app-context-thread" + TrueUID.obtainUniqueId();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, threadName));
    private final EventBus eventBus = new AsyncEventBus(executor);
    private final ServiceManager serviceManager;
    private final Scene scene;
    private final State state = new State();

    public AppContext(Scene scene, ServiceManager serviceManager) {
        this.scene = scene;
        this.serviceManager = serviceManager;
    }

    public boolean isInContextThread() {
        return Thread.currentThread().getName().equals(threadName);
    }

    public ExecutorService getCommonExecutor() {
        return executor;
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }
    
    
    public void executeInContextThread(Runnable r) {
        executor.execute(r);
    }

    public void executeInContextThreadAndWait(Runnable r) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        executor.execute(() -> {
            try {
                r.run();
            } catch (Exception e) {
                Logger.getLogger(AppContext.class.getName()).log(Level.SEVERE, null, e);
            }

            countDownLatch.countDown();
        });
        countDownLatch.await();
    }

    public ScheduledFuture<?> scheduleInContext(Runnable r, long rate, TimeUnit unit) {
        return executor.scheduleAtFixedRate(() -> {
            try {
                r.run();
            } catch (Exception e) {
                Logger.getLogger(AppContext.class.getName()).log(Level.SEVERE, null, e);
            }
        }, rate, rate, unit);
    }

    public ScheduledFuture<?> scheduleSingleInContext(Runnable r, long rate, TimeUnit unit) {
        return executor.schedule(() -> {
            try {
                r.run();
            } catch (Exception e) {
                Logger.getLogger(AppContext.class.getName()).log(Level.SEVERE, null, e);
            }
        }, rate, unit);
    }

    public EventBus getEventBus() {
        //if(!this.isInContextThread()) throw new NotInContextThreadException(); //no longer needed because we're using AsyncEventBus with our thread
        return eventBus;
    }

    public Scene getScene() {
        if (!this.isInContextThread()) {
            throw new NotInContextThreadException();
        }
        return scene;
    }

    public State getState() {
        if (!this.isInContextThread()) {
            throw new NotInContextThreadException();
        }
        return state;
    }
    
    public long getStartupMillis() {
        return this.startupTime;
    }

    void stop() {
        executor.shutdown();
        try {
            executor.awaitTermination(250, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Logger.getLogger(AppContext.class.getName()).log(Level.SEVERE, null, ex);
        }
        executor.shutdownNow();
    }

    public static class NotInContextThreadException extends RuntimeException {

    }
}
