/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jul.server;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Henry
 */
public class CommonUtils {
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
}
