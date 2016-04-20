package com.looseboxes.idisc.common.util;

import android.content.Context;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

import com.looseboxes.idisc.common.io.FileIO;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;

public class DeviceID {
    private static String ID;
    private static Context context;
    private static final boolean useTelephonyManager = false;

    static {
        ID = null;
    }

    public static String getID(Context ctx) {
        context = ctx.getApplicationContext();
        if (ID == null) {
            try {
                File appPrivateFile = FileIO.getFile(context, FileIO.getInstallationIdFilename());
                if (appPrivateFile != null && appPrivateFile.exists()) {
                    ID = FileIO.readFile(appPrivateFile);
                }
            } catch (IOException e) {
                Logx.log(DeviceID.class, e);
            }
        }
        if (ID == null || "0".equals(ID)) {
            ID = generateID();
            if (ID != null) {
                try {
                    FileIO.writeToFile(FileIO.getFile(context, FileIO.getInstallationIdFilename()), ID, false);
                } catch (IOException e2) {
                    Logx.log(DeviceID.class, e2);
                }
            }
        }
        return ID;
    }

    private static String generateID() {
        String deviceId = Secure.getString(context.getContentResolver(), "android_id");
        if (("9774d56d682e549c".equals(deviceId) || deviceId == null) && deviceId == null) {
            deviceId = String.valueOf(new Random().nextLong());
        }
        try {
            return Util.generateSHA_1Hash(deviceId, "UTF-8");
        } catch (Exception e) {
            Logx.log(DeviceID.class, e);
            return UUID.randomUUID().toString();
        }
    }

    public static String generateIDFromTelephonyManager() {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return new UUID((long) (Secure.getString(context.getContentResolver(), "android_id")).hashCode(), (((long) (tm.getDeviceId()).hashCode()) << 32) | ((long) (tm.getSimSerialNumber()).hashCode())).toString();
    }
}
