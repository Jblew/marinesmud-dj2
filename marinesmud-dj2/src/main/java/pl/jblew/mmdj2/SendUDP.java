/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.mmdj2;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author teofil
 */
public class SendUDP {

    public static void main(String[] args) throws SocketException, InterruptedException, IOException {
        DatagramSocket sendSocket = new DatagramSocket(5760);

        while (true) {

            byte[] data = "hello".getBytes();
            DatagramPacket p = new DatagramPacket(data, data.length, InetAddress.getByName("192.168.1.2"), 5761);
            sendSocket.send(p);

            TimeUnit.MILLISECONDS.sleep(800);

        }
    }
}
