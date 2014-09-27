/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jul.server;

/**
 *
 * @author Henry
 */
public class ConstantCodes {
    public static final byte TERMINATE_SESSION = 0x01;
    public static final byte CLIPBOARD_DATA = 0x02;
    public static final byte KEY_PRESSED_CODE = 0x03;
    public static final byte KEY_RELEASED_CODE = 0x04;
    public static final byte KEY_TYPED_CODE = 0x05;
    public static final byte SEND_VERSION = 0x6;
    public static final byte EXECUTING_NATIVE_COMMAND = 0x7;
    public static final byte EXECUTING_NATIVE_COMMAND_AND_BLOCKING = 0x8;
    public static final byte EXECUTING_NATIVE_COMMAND_AND_BLOCKING_END = 0x9;
    
    
    public static final byte SCRENECAP_STREAM_INIT = 0x10;
    public static final byte SCRENECAP_DATA = 0x11;
    public static final byte UPDATE_PACKAGE = 0x12;
    public static final byte REQUEST_VERSION = 0x13;
    public static final byte EXEC_NATIVE_COMMAND = 0x14;
    public static final byte EXEC_NATIVE_COMMAND_AND_BLOCK = 0x15;
    public static final byte SET_SCRENECAP_RATE = 0x16;

    public static final byte EMPTYB = 0;
    public static final byte[] EMPTYBARR = {};
    
    public static final byte[] encryptionKey = "fds7^%E$%ETJ^%765rvgfd\t54HGFDnjhj".getBytes();
}
