/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.mmdj2.signals;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 *
 * @author teofil
 */
public abstract class AbstractChannel implements Channel {

    private final AtomicReference<Channel> endpointRef = new AtomicReference<>(null);
    //private final int channelCount;
    private final AtomicBoolean enabled = new AtomicBoolean(true);

    public AbstractChannel(Channel endpoint) {
        //this.channelCount = channelCount;
    }

    public abstract double[] performCalc(double[] values);

    void setEnabled(boolean v) {
        enabled.set(v);
    }

    boolean isEnabled() {
        return enabled.get();
    }

    @Override
    public final void setEndpoint(Channel endpoint) {
        /*if (endpoint.getChannelCount() < channelCount) {
            throw new IllegalArgumentException("endpoint.channelCount < this.channelCount");
        }*/
        this.endpointRef.set(endpoint);
    }

    @Override
    public boolean isTerminator() {
        return false;
    }
    
    @Override
    public final void setValues(double[] values) {
        /*if (values.length > channelCount) {
            throw new IllegalArgumentException("values.length > this.channelCount");
        }*/

        Channel endpoint = endpointRef.get();
        if (endpoint != null) {
            if (enabled.get()) {
                double[] newValues = performCalc(values);
                endpoint.setValues(newValues);
            } else {
                endpoint.setValues(values);
            }
        }
    }
}
