package com.salesforce.androidsyncengine.utils;

import android.util.Log;

import java.util.Scanner;

/**
 * Created by Jakub Stefanowski on 24.05.2017.
 */

public class LongLog {

    public static void e(String tag, String longMessage) {
        if (longMessage == null) {
            Log.e(tag, longMessage);
        }
        else {
            Scanner scanner = new Scanner(longMessage);
            while (scanner.hasNextLine()) {
                Log.e(tag, "|" + scanner.nextLine());
            }
        }
    }

    public static void w(String tag, String longMessage) {
        if (longMessage == null) {
            Log.w(tag, longMessage);
        }
        else {
            Scanner scanner = new Scanner(longMessage);
            while (scanner.hasNextLine()) {
                Log.w(tag, "|" + scanner.nextLine());
            }
        }
    }

    public static void i(String tag, String longMessage) {
        if (longMessage == null) {
            Log.i(tag, longMessage);
        }
        else {
            Scanner scanner = new Scanner(longMessage);
            while (scanner.hasNextLine()) {
                Log.i(tag, "|" + scanner.nextLine());
            }
        }
    }

    public static void d(String tag, String longMessage) {
        if (longMessage == null) {
            Log.d(tag, longMessage);
        }
        else {
            Scanner scanner = new Scanner(longMessage);
            while (scanner.hasNextLine()) {
                Log.d(tag, "|" + scanner.nextLine());
            }
        }
    }

    public static void v(String tag, String longMessage) {
        if (longMessage == null) {
            Log.v(tag, longMessage);
        }
        else {
            Scanner scanner = new Scanner(longMessage);
            while (scanner.hasNextLine()) {
                Log.v(tag, "|" + scanner.nextLine());
            }
        }
    }
}
