/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.mmdj2.scene;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import pl.jblew.mmdj2.signals.AbstractChannel;
import pl.jblew.mmdj2.signals.Channel;

/**
 *
 * @author teofil
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.CLASS, include=JsonTypeInfo.As.PROPERTY, property="@class")
public abstract class Device implements Channel {

    @Override
    public final boolean isTerminator() {
        return true;
    }

    @Override
    public final void setEndpoint(Channel c) {
        throw new RuntimeException("Device is a terminator channel");
    }
    
    
}
