/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jul.client;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import static jul.client.JUL.*;

import static jul.client.ConstantCodes.*;
import static jul.client.CommonUtils.*;


/**
 *
 * @author Henry
 */
public class KeyListener implements NativeKeyListener {

    
    OutputStream stream;

    public KeyListener(OutputStream os) {
        stream = os;
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nke) {

        try {
            sWrite(WriteData.SINGLE_BYTE, KEY_PRESSED_CODE, EMPTYBARR, null);
            sWrite(WriteData.BYTES, EMPTYB, ByteBuffer.allocate(4).putInt((int) nke.getKeyCode()).array(), null);

        } catch (IOException ex) {
            Logger.getLogger(KeyListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nke) {

        try {
            sWrite(WriteData.SINGLE_BYTE, KEY_RELEASED_CODE, EMPTYBARR, null);
            sWrite(WriteData.BYTES, EMPTYB, ByteBuffer.allocate(4).putInt((int) nke.getKeyCode()).array(), null);

        } catch (IOException ex) {
            Logger.getLogger(KeyListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nke) {

        try {
            sWrite(JUL.WriteData.SINGLE_BYTE, KEY_TYPED_CODE, EMPTYBARR, null);
            sWrite(WriteData.BYTES, EMPTYB, ByteBuffer.allocate(4).putInt((int) nke.getKeyChar()).array(), null);

        } catch (IOException ex) {
            Logger.getLogger(KeyListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
