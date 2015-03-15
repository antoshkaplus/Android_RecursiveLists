package com.antoshkaplus.recursivelists;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

/**
 * Created by antoshkaplus on 2/3/15.
 */
public class Logger {

    // will log to file name with corresponding tag
    public static void log(String tag, String message) {
        if (!BuildConfig.DEBUG) return;
        // to get it back on your computer run adb from platform-tools
        // adb pull sdcard/Documents/RecursiveLists/TAG.txt destination
        String logPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath()
                + File.separator + "RecursiveLists" + File.separator + tag + ".txt";
        File logFile = new File(logPath);
        logFile.getParentFile().mkdirs();
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            }
            catch (IOException e) {
                // what is it all about???
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(DateFormat.getDateTimeInstance().format(new Date()) + "; " + message);
            buf.newLine();
            buf.close();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}