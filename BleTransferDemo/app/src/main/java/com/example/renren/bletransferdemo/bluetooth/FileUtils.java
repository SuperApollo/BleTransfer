package com.example.renren.bletransferdemo.bluetooth;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import org.apache.http.util.EncodingUtils;
import org.apache.http.util.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class FileUtils {
    private static final String TAG = "FileUtils";

    /**
     * 写数据
     *
     * @param mContext 上下文
     * @param fileName 文件名
     * @param writestr 写入文件的字符串
     * @throws IOException
     */
    public static void writeFile(Context mContext, String fileName, String writestr) {
        try {
            //创建流文件写出类
//            FileOutputStream fout = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            File file = new File(fileName);
            FileOutputStream fout = new FileOutputStream(file);
            //获取流的字符数
            byte[] bytes = writestr.getBytes();
            //写出流,保存在文件fileName中
            fout.write(bytes);
            //关闭流
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 写数据
     *
     * @param fileName
     * @param bytes
     */
    public static void writeFile(String fileName, byte[] bytes) {
        try {
            //创建流文件写出类
//            FileOutputStream fout = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
            File file = new File(fileName);
            FileOutputStream fout = new FileOutputStream(file);
            //写出流,保存在文件fileName中
            fout.write(bytes);
            //关闭流
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 读数据
     *
     * @param fileName 文件名
     * @return
     * @throws IOException
     */
    public static String readFile(String fileName) {
        String res = "";
        try {
            //创建流文件读入类
//            FileInputStream fin = mContext.openFileInput(fileName);
            File file = new File(fileName);
            FileInputStream fin = new FileInputStream(file);
            //通过available方法取得流的最大字符数
            byte[] buffer = new byte[fin.available()];
            int len = -1;
            StringBuilder sb = new StringBuilder();
            //读入流,保存在byte数组
            while ((len = fin.read(buffer)) != -1) {
                sb.append(new String(buffer, 0, len, "UTF-8"));
            }
            res = sb.toString();
            //关闭流
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    /**
     * 判断sd卡是否可用
     *
     * @return
     */
    public static boolean isSdCardExist() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SD卡根目录路径
     *
     * @return
     */
    public static String getSdCardPath() {
        boolean exist = isSdCardExist();
        String sdpath = null;
        if (exist) {
            sdpath = Environment.getExternalStorageDirectory()
                    .getAbsolutePath();
        }

        return sdpath;
    }

    /**
     * 获取文件路径
     *
     * @return
     */
    public static String getFilePath(String fileName) {
        String filepath = null;
        File file = new File(Environment.getExternalStorageDirectory(),
                fileName);
        if (file.exists()) {
            filepath = file.getAbsolutePath();
        }
        return filepath;
    }

    /**
     * 根据流生成文件
     *
     * @param ins  输入流
     * @param file 文件
     */
    public static long inputstreamtofile(InputStream ins, File file) throws IOException {
        if (file.exists()) {
            file.delete();
        }
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = ins.read(buffer, 0, 1024)) != -1) {
                os.write(buffer, 0, bytesRead);
                os.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                os.close();
//                ins.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "file saved");
            return file.length();
        }


    }

    /**
     * Mapped File way MappedByteBuffer 可以在处理大文件时，提升性能
     *
     * @param filename
     * @return
     * @throws IOException
     */
    public static byte[] fileToByteArray(String filename) {
        if (TextUtils.isEmpty(filename)) {
            return null;
        }
        FileChannel fc = null;
        try {
            fc = new RandomAccessFile(filename, "r").getChannel();
            MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0,
                    fc.size()).load();
            System.out.println(byteBuffer.isLoaded());
            byte[] result = new byte[(int) fc.size()];
            if (byteBuffer.remaining() > 0) {
                // System.out.println("remain");
                byteBuffer.get(result, 0, byteBuffer.remaining());
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (fc != null)
                    fc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
