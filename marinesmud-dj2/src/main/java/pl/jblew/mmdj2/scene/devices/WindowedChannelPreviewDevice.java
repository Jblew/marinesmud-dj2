/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.mmdj2.scene.devices;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JFrame;
import javax.swing.JPanel;
import pl.jblew.mmdj2.scene.Device;

/**
 *
 * @author teofil
 */
public class WindowedChannelPreviewDevice extends Device {

    private final AtomicReference<MyPanel> panelRef = new AtomicReference<>(null);

    @Override
    public void setValues(double[] values) {
        MyPanel panel;
        synchronized (panelRef) {
            panel = panelRef.get();
            if (panel == null) {
                panel = new MyPanel();
                panelRef.set(panel);
            }
        }
        
        int numOfColors = (int)Math.ceil((float)values.length/3f);
        Color [] colors = new Color[numOfColors];
        int colorIndex = 0;
        for(int i = 0;i < values.length;i+=3) {
            if(i + 2 < values.length) {
                colors[colorIndex++] = new Color((float)values[1], (float)values[i+1], (float)values[i+2]);
            }
            else {
                colors[colorIndex++] = new Color((float)values[1], (float)values[i], (float)values[i]);
            }
        }
        panel.setColors(colors);
    }

    @Override
    public void shutdown() {
        synchronized (panelRef) {
            MyPanel panel = panelRef.get();
            if (panel != null) {
                panel.shutdown();
                panelRef.set(null);
            }
        }
    }

    private static class MyPanel extends JPanel {
        private final JFrame frame;
        private final List<Color> colors = new ArrayList<>();
        
        public MyPanel() {
            frame = new JFrame();
            frame.setSize(400, 150);
            frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            frame.setContentPane(this);
            frame.setVisible(true);
        }
        
        public void setColors(Color [] c) {
            synchronized(colors) {
                colors.clear();
                colors.addAll(Arrays.asList(c));
            }
            this.repaint();
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            synchronized(colors) {
                int rectWidth = getWidth()/colors.size();
                int rectX = 0;
                for(Color c : colors) {
                    g.setColor(c);
                    g.fillRect(rectX, 0, rectWidth, getHeight());
                }
            }
        }

        private void shutdown() {
            frame.setVisible(false);
        }
        
        
    }

}
