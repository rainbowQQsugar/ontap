package com.abinbev.dsa.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateUtils;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static com.abinbev.dsa.utils.DateUtils.UTCnow;

/**
 * Created by mewa on 6/15/17.
 */

/**
 * Helper class for dealing with SharedPreferences holding forced sync settings
 */
public class ForcedSyncPreferences {
    private static final String TAG = ForcedSyncPreferences.class.getSimpleName();
    private static final String SNOOZE_SYNC_UNTIL_DATE = "snooze_sync";
    private static final String LAST_SYNC = "last_sync";
    private static final String PREFS_NAME = "forced_sync_prefs";

    private final SharedPreferences prefs;
    private final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);


    public ForcedSyncPreferences(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        fmt.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Checks if current time is past the point at which sync should be performed
     *
     * @return true if sync is needed
     */
    public boolean needsUpdate() {
        String snoozePref = prefs.getString(SNOOZE_SYNC_UNTIL_DATE, null);
        String lastSyncPref = prefs.getString(LAST_SYNC, null);

        // First use
        if (snoozePref == null)
            return false;

        Calendar now = UTCnow();

        Calendar snoozed = null;
        Calendar last = null;
        Date tmp;
        try {
            tmp = fmt.parse(snoozePref);
            snoozed = UTCnow();
            snoozed.setTime(tmp);

            if (lastSyncPref != null) {
                tmp = fmt.parse(lastSyncPref);
                last = UTCnow();
                last.setTime(tmp);
            }
        } catch (ParseException e) {
            Log.w(TAG, "Saved nextSync date is invalid");
            return false;
        }

        if (last != null) {
            // should be next day
            last.add(Calendar.HOUR, 18);

            return now.after(snoozed) && now.after(last);
        } else {
            return now.after(snoozed);
        }
    }

    /**
     * Save next sync date
     *
     * @param hours   amount of hours to postpone sync
     * @param synced true if sync was performed and should update last sync date
     */
    public void nextSync(int hours, boolean synced) {
        Calendar snoozed = UTCnow();
        snoozed.add(Calendar.HOUR, hours);
        String snoozeTill = fmt.format(snoozed.getTime());

        prefs.edit()
                .putString(SNOOZE_SYNC_UNTIL_DATE, snoozeTill)
                .apply();

        if (synced) {
            Calendar updated = UTCnow();

            String lastUpdate = fmt.format(updated.getTime());
            prefs.edit()
                    .putString(LAST_SYNC, lastUpdate)
                    .apply();
        }
    }
}
