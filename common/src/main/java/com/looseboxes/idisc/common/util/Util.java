package com.looseboxes.idisc.common.util;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.TypedValue;

import com.bc.util.SecurityTool;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.asynctasks.BaseReadTask;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class Util {

    /* renamed from: com.looseboxes.idisc.common.util.Util.1 */
    static class AnonymousClass1 extends Thread {
        final /* synthetic */ Handler val$handler;
        final /* synthetic */ Runnable val$runnable;

        AnonymousClass1(Handler handler, Runnable runnable) {
            this.val$handler = handler;
            this.val$runnable = runnable;
        }

        public void run() {
            this.val$handler.post(this.val$runnable);
        }
    }

    /* renamed from: com.looseboxes.idisc.common.util.Util.2 */
    static class AnonymousClass2 extends Thread {
        final /* synthetic */ long val$delay;
        final /* synthetic */ Handler val$handler;
        final /* synthetic */ Runnable val$runnable;

        AnonymousClass2(Handler handler, Runnable runnable, long j) {
            this.val$handler = handler;
            this.val$runnable = runnable;
            this.val$delay = j;
        }

        public void run() {
            this.val$handler.postDelayed(this.val$runnable, this.val$delay);
        }
    }

    public static void runAsync(Runnable runnable) {
        new AnonymousClass1(new Handler(), runnable).start();
    }

    public static void runAsync(Runnable runnable, long delay) {
        new AnonymousClass2(new Handler(), runnable, delay).start();
    }

    public static boolean cancel(BaseReadTask task) {
        try {
            if (task.isStarted() && !task.isCompleted()) {
                task.cancel(true);
                RemoteSession remoteSession = task.getSession();
                if (remoteSession == null) {
                    return true;
                }
                remoteSession.disconnect();
                InputStream in = remoteSession.getInputStream();
                if (in == null) {
                    return true;
                }
                in.close();
                return true;
            }
        } catch (Exception e) {
            Logx.log(Util.class, e);
        }
        return false;
    }

    public static float dipToPixels(Context context, float dipValue) {
        return toPixels(context, 1, dipValue);
    }

    public static float spToPixels(Context context, float spValue) {
        return toPixels(context, 2, spValue);
    }

    public static float toPixels(Context context, int complexUnit, float value) {
        return TypedValue.applyDimension(complexUnit, value, context.getResources().getDisplayMetrics());
    }

    public static Intent createShareIntent(Context context, CharSequence content, String contentIntentType) {
        return createShareIntent(context, getDefaultSubjectForMessages(context), content, contentIntentType, true);
    }

    public static Intent createShareIntent(Context context, CharSequence subject, CharSequence content, String contentIntentType, boolean addDefaultMessageSuffix) {
        Intent shareIntent = new Intent("android.intent.action.SEND");
        shareIntent.setType(getContentTypeForIntentType(contentIntentType));
        if (subject != null) {
            shareIntent.putExtra("android.intent.extra.SUBJECT", subject);
        }
        if (addDefaultMessageSuffix) {
            content = content + getMessageSuffix(context);
        }
        shareIntent.putExtra(contentIntentType, content);
        return shareIntent;
    }

    public static String getContentTypeForIntentType(String intentType) {
        String output;
        switch(intentType) {
            case Intent.EXTRA_HTML_TEXT:
                output = "text/html"; break;
            default:
                output = "text/plain";
        }
        return output;
    }

    public static String getMessageSuffix(Context context) {
        String appName = context.getString(R.string.app_label);
        return context.getString(R.string.fmt_message_suffix, new Object[]{appName});
    }

    public static String getDefaultSubjectForMessages(Context context) {
        String username = User.getInstance().getUsername(context);
        if (username == null) {
            return context.getString(R.string.msg_share_subject_guest);
        }
        return context.getString(R.string.msg_share_subject_user, new Object[]{username});
    }

    public static String encrpyt(String toEncrypt, String key, int maxOutputLength) {
        try {
            String output = new SecurityTool("AES", key).encrypt(toEncrypt);
            if (output.length() > maxOutputLength) {
                return output.substring(0, maxOutputLength);
            }
            if (output.length() >= maxOutputLength) {
                return output;
            }
            StringBuilder b = new StringBuilder(output);
            int toAdd = maxOutputLength - output.length();
            for (int i = 0; i < toAdd; i++) {
                b.append('0');
            }
            return b.toString();
        } catch (GeneralSecurityException e) {
            return "1a2b3c";
        }
    }

    public static String generateSHA_1Hash(String stringToHash, String encoding) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");

        byte[] result = digest.digest(stringToHash.getBytes(encoding));

        StringBuilder sb = new StringBuilder();
        for (byte b : result){
            sb.append(String.format("%02X", b));
        }

        String messageDigest = sb.toString();

        return messageDigest;
    }

    public static boolean isNetworkConnectedOrConnecting(Context context) {
        NetworkInfo networkInfo = getNetworkInfo(context);
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    public static NetworkInfo getNetworkInfo(Context context) {
        try {
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connMgr != null) {
                return connMgr.getActiveNetworkInfo();
            }
            return null;
        } catch (Exception e) {
            Logx.log(Util.class, e);
            return null;
        }
    }

    public static StringBuilder getStackTrace(Exception e) {
        StringBuilder builder = new StringBuilder();
        appendStackTrace(e, "\n", builder);
        return builder;
    }

    public static void appendStackTrace(Exception e, String separator, StringBuilder builder) {
        for (StackTraceElement ste : e.getStackTrace()) {
            builder.append(ste).append(separator);
        }
    }

    public static String truncateSides(String toTruncate, String ref, int amount, boolean ellipsize) {
        if (ref == null || toTruncate.contains(ref)) {
            if (amount >= toTruncate.length() - (ref == null ? 0 : ref.length())) {
                return ref;
            }
            if (amount == 0 || amount < 2) {
                return toTruncate;
            }
            int refEnd;
            int refStart;
            int sourceLen = toTruncate.length();
            if (ref == null) {
                refEnd = sourceLen / 2;
                refStart = refEnd;
            } else {
                refStart = toTruncate.indexOf(ref);
                refEnd = refStart + ref.length();
            }
            int aSide = amount / 2;
            int a = toTruncate.indexOf(" ", aSide);
            if (a > refStart) {
                a = toTruncate.substring(0, aSide).lastIndexOf(" ");
                if (a == -1) {
                    a = 0;
                }
            }
            int b = toTruncate.substring(refEnd).lastIndexOf(" ");
            if (b == -1) {
                b = toTruncate.length();
            } else {
                b += refEnd;
            }
            boolean elipsizeStart = ellipsize && a != 0;
            boolean elipsizeEnd = ellipsize && b != toTruncate.length();
            StringBuilder output = new StringBuilder();
            if (elipsizeStart) {
                output.append("..");
            }
            boolean started = false;
            for (int i = a; i < b; i++) {
                char ch = toTruncate.charAt(i);
                if (started) {
                    output.append(ch);
                } else if (!Character.isSpaceChar(ch)) {
                    started = true;
                    output.append(ch);
                }
            }
            if (elipsizeEnd) {
                output.append("..");
            }
            return output.toString();
        }
        throw new UnsupportedOperationException();
    }

    public static boolean equals(Map a, Object target, Object[] keys) {
        if (a == target) {
            return true;
        }
        if (!(target instanceof Map)) {
            return false;
        }
        Map b = (Map) target;
        try {
            Set<Entry> entry_set = a.entrySet();
            for (Entry entry_a : entry_set) {
                Object key_a = entry_a.getKey();
                if (contains(keys, key_a)) {
                    Object value_a = entry_a.getValue();
                    Object value_b = b.get(key_a);
                    if (value_a == null) {
                        if (value_b != null || !b.containsKey(key_a)) {
                            return false;
                        }
                    } else if (!value_a.equals(value_b)) {
                        return false;
                    }
                }
            }
            return true;
        } catch (NullPointerException e) {
            return false;
        } catch (ClassCastException e2) {
            return false;
        }
    }

    private static boolean contains(Object[] arr, Object o) {
        for (Object e : arr) {
            if (e == null && o == null) {
                return true;
            }
            if (e != null && e.equals(o)) {
                return true;
            }
            if (o != null && o.equals(e)) {
                return true;
            }
        }
        return false;
    }

    public static boolean shutdownAndAwaitTermination(ExecutorService pool, long timeout, TimeUnit unit, List<Runnable> addInterruptedHere) {
        pool.shutdown();
        if (timeout <= 0) {
            timeout = 1;
        }
        List interrupted;
        try {
            if (!pool.awaitTermination(timeout, unit)) {
                interrupted = pool.shutdownNow();
                if (!(addInterruptedHere == null || interrupted == null || interrupted.isEmpty())) {
                    addInterruptedHere.addAll(interrupted);
                }
                if (!pool.awaitTermination(timeout, unit)) {
                    return false;
                }
            }
        } catch (InterruptedException e) {
            interrupted = pool.shutdownNow();
            if (!(addInterruptedHere == null || interrupted == null || interrupted.isEmpty())) {
                addInterruptedHere.addAll(interrupted);
            }
            Thread.currentThread().interrupt();
        }
        return true;
    }

    public static Location getLocation(String country) {
        Location loc = new Location("");
        loc.setLongitude(getLongitude(country));
        loc.setLatitude(getLatitude(country));
        return loc;
    }

    public static double getLatitude(String country) {
        return 9.081999d;
    }

    public static double getLongitude(String country) {
        return 8.675277d;
    }
}
