package com.salesforce.dsa.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Method;

public class DisplayUtils {

    private DisplayUtils() {
    }

    ;

    private static final String TAG = "DisplayUtils";

    // values coming from the server seem to relate to non-retina display
    // we are doing centerCrop on the image which pertains to the below
    // calculations for landscape

    // landscape values that need to be calculated not hard-coded

    private static double ipadWidth;
    private static double ipadHeight;

    private static double deviceWidth;
    private static double deviceHeight;

    // Values for Nexus 7 1st generation
    // public static double deviceWidth = 1280;
    // public static double deviceHeight = 736;

    private static double xMultiplier;
    private static double yMultiplier;

    private static double xOffset;
    private static double yOffset;

    private static double multiplier;

    private static int defaultPadding = 2;


    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @SuppressWarnings("deprecation")
    public static void updateDeviceDisplayValues(Context context) {
        WindowManager wm = ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE));

        Display mDisplay = wm.getDefaultDisplay();

        // Usable Screen dimensions
        try {
            Method getSizeMethod = mDisplay.getClass().getMethod("getSize", Point.class);
            Point pt = new Point();
            getSizeMethod.invoke(mDisplay, pt);
            deviceWidth = pt.x;
            deviceHeight = pt.y;
        } catch (Exception ignore) {
            // Use older APIs
            deviceWidth = mDisplay.getWidth();
            deviceHeight = mDisplay.getHeight();
        }

        Log.i(TAG, "device width: " + deviceWidth + " height: " + deviceHeight);

        // either landscape or portrait are the only values you get here
        int currentOrientation = context.getResources().getConfiguration().orientation;

        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            ipadWidth = 1024;
            ipadHeight = 768;

            xMultiplier = deviceWidth / ipadWidth;
            yMultiplier = deviceHeight / ipadHeight;
        } else {
            ipadWidth = 768;
            ipadHeight = 1024;

            xMultiplier = deviceWidth / ipadWidth;
            yMultiplier = deviceHeight / ipadHeight;

        }

        if (xMultiplier > yMultiplier) {
            xOffset = 0;
            yOffset = ((ipadHeight * xMultiplier) - deviceHeight) / 2;

            multiplier = xMultiplier;
        } else {
            xOffset = ((ipadWidth * yMultiplier) - deviceWidth) / 2;
            yOffset = 0;
            multiplier = yMultiplier;
        }

        Log.i(TAG, "x pct: " + xMultiplier + " y pct: " + yMultiplier);
        Log.i(TAG, "x offset: " + xOffset + " y offset: " + yOffset);

    }

    public static int calculatedWidth(double buttonWidth, boolean isTransparentButton) {
        if (isTransparentButton) {
            return (int) (buttonWidth * multiplier);
        } else {
            return (int) (buttonWidth * xMultiplier);
        }
    }

    public static int calculatedHeight(double buttonHeight, boolean isTransparentButton) {
        if (isTransparentButton) {
            return (int) (buttonHeight * multiplier);
        } else {
            return (int) (buttonHeight * yMultiplier);
        }
    }

    public static int calculatedLeftMargin(double xValue, double buttonWidth) {
        double leftMargin;

        leftMargin = ((xValue * multiplier) - xOffset);

        if ((leftMargin + (buttonWidth * multiplier) + defaultPadding) > deviceWidth) {
            leftMargin = deviceWidth - (buttonWidth * multiplier) - defaultPadding;
        } else if (leftMargin < 0) {
            leftMargin = defaultPadding;
        }

        return (int) (leftMargin);
    }

    public static int calculatedTopMargin(double yValue, double buttonHeight) {
        double topMargin;

        topMargin = ((yValue * multiplier) - yOffset);

        if ((topMargin + (buttonHeight * multiplier) + defaultPadding) > deviceHeight) {
            topMargin = deviceHeight - (buttonHeight * multiplier) - defaultPadding;
        } else if (topMargin < defaultPadding) {
            topMargin = defaultPadding;
        }

        return (int) topMargin;
    }

}
