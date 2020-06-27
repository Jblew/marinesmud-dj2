/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.mmdj2.serial;

import pl.jblew.marinesmud.framework.services.Service;
import gnu.io.RXTXPort;
import gnu.io.CommPortIdentifier;
import gnu.io.RXTXVersion;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.jblew.marinesmud.framework.services.ServiceManager;
import pl.jblew.marinesmud.framework.services.ServiceSingletonProvider;
import pl.jblew.mmdj2.AppContext;
import pl.jblew.mmdj2.ui.UI;

/**
 *
 * @author teofil
 */
public class SerialService implements Service {
    public static final int FPS = 20;
    private final Object valuesSync = new Object();
    private final byte[] values = new byte[100];
    private final AtomicReference<SerialPort> deviceRef = new AtomicReference<>(null);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Lock outputLock = new ReentrantLock();
    private final Condition outputBufferEmptyCondition = outputLock.newCondition();
    private final AtomicBoolean outputBufferEmpty = new AtomicBoolean(true);

    public SerialService() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            SerialPort port = deviceRef.get();
            if (port != null) {
                try {
                    port.close();
                } catch (Exception e) {
                }
            }
        }));

        System.out.println(RXTXVersion.getVersion());
        System.out.println("DRIVER_AVAILABLE=" + isDriverAvailable());

        executor.execute(() -> {
            long sTime = System.currentTimeMillis();
            SerialPort port = deviceRef.get();
            if (port != null) {
                sendValues(port);
            } else {
                System.out.println("Not connected... Waiting 5 seconds...");
                try {
                    TimeUnit.MILLISECONDS.sleep(5000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(SerialService.class.getName()).log(Level.SEVERE, null, ex);
                }
                tryConnect();
            }

            try {
                TimeUnit.MICROSECONDS.sleep(1500);
                long timeToSleepMs = 1000 / FPS - (System.currentTimeMillis() - sTime);
                if (timeToSleepMs > 0) {
                    TimeUnit.MILLISECONDS.sleep(timeToSleepMs);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(SerialService.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    private void tryConnect() {
        SerialPort port = deviceRef.get();
        if (port != null) {
            return;
        }

        Enumeration ports = CommPortIdentifier.getPortIdentifiers();
        while (ports.hasMoreElements()) {
            CommPortIdentifier portId = (CommPortIdentifier) ports.nextElement();
            if (!portId.getName().contains("Bluetooth")) {
                if (!portId.isCurrentlyOwned()) {
                        System.out.println("isOwned=" + portId.isCurrentlyOwned()
                                + "; owner=" + portId.getCurrentOwner());
                        initPort(portId);
                        return;

                    } else {
                        System.out.println("Port "+portId.getName()+" is currently owned");
                    }
            }
        }
        System.out.println("No ports found...");
        
    }

    private void initPort(CommPortIdentifier portId) {
        try {
            SerialPort prevPort = deviceRef.get();
            if (prevPort != null) {
                prevPort.close();
            }
        } catch (Exception e) {
            System.err.println(e.toString());
        }

        try {
            // open serial port, and use class name for the appName.
            SerialPort serialPort = (SerialPort) portId.open(this.getClass().getName(), 200);

            // set port parameters
            serialPort.setSerialPortParams(57600,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_NONE);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_OUT);
            System.out.println("Port set pairity, baud=" + serialPort.getBaudRate());

            serialPort.notifyOnOutputEmpty(true);
            serialPort.addEventListener(new SerialPortEventListener() {
                @Override
                public void serialEvent(SerialPortEvent spe) {
                    if (spe.getEventType() == SerialPortEvent.OUTPUT_BUFFER_EMPTY) {
                        outputBufferEmpty.set(true);
                        outputLock.lock();
                        try {
                            outputBufferEmptyCondition.signalAll();
                        } finally {
                            outputLock.unlock();
                        }
                    }
                }
            });

            // open the streams
            InputStream input = serialPort.getInputStream();
            OutputStream output = serialPort.getOutputStream();

            System.out.println("Checking transmission...");

            int v = 0;
            write(serialPort, new byte[]{(byte) v, (byte) v, (byte) v, (byte) v, (byte) v, (byte) v, (byte) v, (byte) v, (byte) v, (byte) v, (byte) v, (byte) v, (byte) v, (byte) v, (byte) v});

            System.out.println("Connection estabilished");

            //this.configDMX(serialPort);
            deviceRef.set(serialPort);
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        System.out.println("Finished init");
    }
    
    private void write(SerialPort port, byte[] channelValues) throws InterruptedException, IOException {

        //long sTime = System.currentTimeMillis();
        DataOutputStream os = new DataOutputStream(port.getOutputStream());
        
        os.write(channelValues);
        outputBufferEmpty.set(false);
        os.flush();
        
        TimeUnit.MILLISECONDS.sleep(1);

        if(!outputBufferEmpty.get()) {
        outputLock.lock();
        try {
            outputBufferEmptyCondition.await();
        } finally {
            outputLock.unlock();
        }
        }

        //while (!port.isCTS()) {
        //    TimeUnit.MICROSECONDS.sleep(1);
        //}
        //long eTime = System.currentTimeMillis();
        //long intervalMs = (eTime-sTime);
        //System.out.println("DMX serial send time: "+intervalMs+" ms");
        
        //send time = 1/57600*1000*10 * bytes.length ms
    }


    private void sendValues(SerialPort port) {
            byte [] valuesCopy = new byte [values.length];
            synchronized(valuesSync) {
                System.arraycopy(values, 0, valuesCopy, 0, values.length);
            }
            
            try {
                write(port, valuesCopy);
            } catch (IOException ex) {
                Logger.getLogger(SerialService.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InterruptedException ex) {
                Logger.getLogger(SerialService.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
            }
    }
    
    public void setValue(int i, byte v) {
        if(i < 0 || i > values.length) throw new RuntimeException("i outside of bounds");
        synchronized(valuesSync) {
            values[i] = v;
        }
    }
    
    public void setValues(int i, byte r, byte g, byte b) {
        if(i < 0 || i+2 > values.length) throw new RuntimeException("i outside of bounds");
        synchronized(valuesSync) {
            values[i] = r;
            values[i] = g;
            values[i] = b;
        }
    }
    
    private static boolean isDriverAvailable() {
        try {
            Class.forName("gnu.io.CommPortIdentifier");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    public static class Provider extends ServiceSingletonProvider<SerialService> {     
        @Override
        protected SerialService provideService(ServiceManager sm) {
            return new SerialService();
        }

        @Override
        public Class<? extends SerialService> getServiceClass() {
            return SerialService.class;
        }
    }
}
