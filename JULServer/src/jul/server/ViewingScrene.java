/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jul.server;

import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Henry
 */
public class ViewingScrene extends JApplet{
    private static ViewingScrene viewingScrene = null;
    private static JPanel controllPanel = null;
    private static JFrame frame;
    
    
    
    private static JTextField scaleField;
    private static JCheckBox editable;
    
    public static ViewingScrene initViewingScrene() {
        frame = new JFrame("Server Viewer");
        viewingScrene = new ViewingScrene();
        scaleField = new JTextField("100");
        scaleField.setSize(200, 30);
        
        editable = new JCheckBox("Editable");
        editable.setSelected(false);
        
        controllPanel = new JPanel();
        controllPanel.add(scaleField);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.PAGE_AXIS));
        frame.add(controllPanel);
        frame.add(viewingScrene);
        frame.pack();
        frame.setVisible(true);
        return viewingScrene;
    
    }
    
    public static ViewingScrene getViewingScrene() {
        return viewingScrene;
    }
    
    public ViewingScrene() throws HeadlessException {
        setSize(1000, 562);
    }
    private double oldScale = 1.0;
    private double scale = 1.0;
    public void update() {
        scale = Double.parseDouble(scaleField.getText()) / 100.0;
        capUpdated = false;
        repaint();
    }

    private Image scaledSreneCap = null;
    private boolean capUpdated = false;
    @Override
    public void paint(Graphics g) {
        if (scale != oldScale) {
            oldScale = scale;
            g.clearRect(0, 0, getWidth(), getHeight());
        }
        if (!capUpdated) {
            capUpdated = true;
            scaledSreneCap = JULServer.lastRenderedScreneCap.getScaledInstance((int) (JULServer.lastRenderedScreneCap.getWidth() * scale), (int) (JULServer.lastRenderedScreneCap.getHeight() * scale), Image.SCALE_SMOOTH);
        }
        g.drawImage(scaledSreneCap, 0, 0, this);
    }
    
    
    
    
}
