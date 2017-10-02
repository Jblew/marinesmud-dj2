/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.mmdj2.signals;

/**
 *
 * @author teofil
 */
public interface Channel {
    public int getChannelCount();
    public void setValues(double [] values);
    public boolean isTerminator();
    public void setEndpoint(Channel c);
}
