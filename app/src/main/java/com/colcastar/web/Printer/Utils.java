package com.colcastar.web.Printer;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {
    private static final int MAX_THREADS = 16;

    public static void threadedWorkflow(String[] array, Bitmap bitmap, String zeroStr) {
        long time = System.currentTimeMillis();
        final int length = array.length;
        boolean exact = length % MAX_THREADS == 0;
        int maxlim = exact ? length / MAX_THREADS : length / (MAX_THREADS - 1);
        maxlim = Math.max(maxlim, MAX_THREADS);
        final ArrayList<SortThreads> threads = new ArrayList<>();
        for (int i = 0; i < length; i += maxlim) {
            int remain = (length) - i;
            int end = remain < maxlim ? i + (remain - 1) : i + (maxlim - 1);
            final SortThreads t = new SortThreads(array, i, end, bitmap, zeroStr);
            threads.add(t);
        }
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException ignored) {
            }
        }
        time = System.currentTimeMillis() - time;
        System.out.println("Time spent for custom multi-threaded recursive merge_sort(): " + time + "ms");
    }

    private static class SortThreads extends Thread {
        SortThreads(String[] array, int begin, int end, Bitmap bitmap, String zeroStr) {
            super(() -> ProcessBitmap(array, begin, end, bitmap, zeroStr));
            this.start();
        }
    }

    private static void ProcessBitmap(String[] array, int begin, int end, Bitmap bitmap, String zeroStr) {
        int zeroCount = bitmap.getWidth() % 8;
        for (int i = begin; i <= end; i++) {
//            int x = i % 100 == 0 ? Log.e("TAG", "decodeBitmap: i" + i) : 1;
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < bitmap.getWidth(); j++) {
                int color = bitmap.getPixel(j, i);
                int r = (color >> 16) & 0xff;
                int g = (color >> 8) & 0xff;
                int b = color & 0xff;

                // if color close to white，bit='0', else bit='1'
                if (color == 0) {
                    sb.append("0");
                } else if (r > 160 && g > 160 && b > 160)
                    sb.append("0");
                else
                    sb.append("1");
            }
            if (zeroCount > 0) {
                sb.append(zeroStr);
            }
            array[i] = sb.toString();
        }
    }

    // UNICODE 0x23 = #
    public static final byte[] UNICODE_TEXT = new byte[]{0x23, 0x23, 0x23,
            0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23,
            0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23,
            0x23, 0x23, 0x23};

    private static String hexStr = "0123456789ABCDEF";
    private static String[] binaryArray = {"0000", "0001", "0010", "0011",
            "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011",
            "1100", "1101", "1110", "1111"};

    public static byte[] decodeBitmap(Bitmap bmp) {
        int bmpWidth = bmp.getWidth();
        int bmpHeight = bmp.getHeight();

        //binaryString list
        String[] list2 = new String[bmpHeight]; //binaryString list


        int bitLen = bmpWidth / 8;
        int zeroCount = bmpWidth % 8;

        String zeroStr = "";
        if (zeroCount > 0) {
            bitLen = bmpWidth / 8 + 1;
            for (int i = 0; i < (8 - zeroCount); i++) {
                zeroStr = zeroStr + "0";
            }
        }
        threadedWorkflow(list2, bmp, zeroStr);

        String[] bmpHexList = binaryListToHexStringList(list2);
//        Log.e("TAG", "binaryListToHexStringList: took"+(System.currentTimeMillis()-time)/1000  );
        String commandHexString = "1D763000";
        String widthHexString = Integer
                .toHexString(bmpWidth % 8 == 0 ? bmpWidth / 8
                        : (bmpWidth / 8 + 1));
        if (widthHexString.length() > 2) {
            Log.e("decodeBitmap error", " width is too large");
            return null;
        } else if (widthHexString.length() == 1) {
            widthHexString = "0" + widthHexString;
        }
        widthHexString = widthHexString + "00";

        String heightHexString = Integer.toHexString(bmpHeight);
        while (heightHexString.length() <= 4) {
            heightHexString += "0";
        }
        heightHexString = "6230";
        Log.e("heightHexString", "decodeBitmap: heightHexString" + heightHexString);
        List<String> commandList = new ArrayList<>();
        commandList.add(commandHexString + widthHexString + heightHexString);
        commandList.addAll(Arrays.asList(bmpHexList));
        byte[] result = hexList2Byte(commandList);

        return result;
    }

    public static String[] binaryListToHexStringList(String[] list) {
        String[] hexList = new String[list.length];
        binaryListToHexStringListThreaded(list, hexList);
        return hexList;
    }

    public static void binaryListToHexStringListThreaded(String[] origin, String[] dst) {
        long time = System.currentTimeMillis();
        final int length = origin.length;
        boolean exact = length % MAX_THREADS == 0;
        int maxlim = exact ? length / MAX_THREADS : length / (MAX_THREADS - 1);
        maxlim = Math.max(maxlim, MAX_THREADS);
        final ArrayList<ConverterThread> threads = new ArrayList<>();
        for (int i = 0; i < length; i += maxlim) {
            int remain = (length) - i;
            int end = remain < maxlim ? i + (remain - 1) : i + (maxlim - 1);
            final ConverterThread t = new ConverterThread(origin, dst, i, end);
            threads.add(t);
        }
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException ignored) {
            }
        }
        time = System.currentTimeMillis() - time;
        System.out.println("Time spent for custom multi-threaded recursive merge_sort(): " + time + "ms");
    }

    private static class ConverterThread extends Thread {
        ConverterThread(String[] origin, String[] dst, int begin, int end) {
            super(() -> binaryListToHexStringListV2(origin, dst, begin, end));
            this.start();
        }
    }

    public static void binaryListToHexStringListV2(String[] origin, String[] dst, int begin, int end) {
        for (int j = begin; j <= end; j++) {
            String binaryStr = origin[j];
//            int x = j % 100 == 0 ? Log.e("TAG", "decodeBitmap: i" + j) : 1;
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < binaryStr.length(); i += 8) {
                String str = binaryStr.substring(i, i + 8);
                String hexString = myBinaryStrToHexString(str);
                sb.append(hexString);
            }
            dst[j] = sb.toString();
        }
    }

    public static String myBinaryStrToHexString(String binaryStr) {
        String hex = "";
        String f4 = binaryStr.substring(0, 4);
        String b4 = binaryStr.substring(4, 8);
        for (int i = 0; i < binaryArray.length; i++) {
            if (f4.equals(binaryArray[i]))
                hex += hexStr.substring(i, i + 1);
        }
        for (int i = 0; i < binaryArray.length; i++) {
            if (b4.equals(binaryArray[i]))
                hex += hexStr.substring(i, i + 1);
        }

        return hex;
    }

    public static byte[] hexList2Byte(List<String> list) {
        List<byte[]> commandList = new ArrayList<byte[]>();

        for (String hexStr : list) {
            commandList.add(hexStringToBytes(hexStr));
        }
        byte[] bytes = sysCopy(commandList);
        return bytes;
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    public static byte[] sysCopy(List<byte[]> srcArrays) {
        int len = 0;
        for (byte[] srcArray : srcArrays) {
            len += srcArray.length;
        }
        byte[] destArray = new byte[len];
        int destLen = 0;
        for (byte[] srcArray : srcArrays) {
            System.arraycopy(srcArray, 0, destArray, destLen, srcArray.length);
            destLen += srcArray.length;
        }
        return destArray;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

}
