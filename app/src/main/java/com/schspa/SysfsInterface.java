package com.schspa;

/**
 * Created by schspa on 11/11/15.
 */
public class SysfsInterface {
    static {
        System.loadLibrary("sysfsinterface");
    }
    public native String read(String file);
    public native String write(String file, String buf);
    public native void test();
    public native String[] getfile(String dir);
}
