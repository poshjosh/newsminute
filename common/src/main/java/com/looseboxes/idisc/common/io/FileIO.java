package com.looseboxes.idisc.common.io;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.looseboxes.idisc.common.util.AliasesManager.AliasType;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.PreferenceFeedsManager;
import com.looseboxes.idisc.common.util.PreferenceFeedsManager.PreferenceType;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Arrays;

public class FileIO {

    public String load(Context context, String fname, boolean mayNotExist) {
        return load(context, fname, 0, mayNotExist);
    }

    public String load(Context context, String fname, int mode, boolean mayNotExist) {
        if (context == null || fname == null) {
            throw new NullPointerException();
        }
        InputStream in = null;
        String output = null;
        try {
            in = context.openFileInput(fname);
            output = readContents(in);
            close(in);
        } catch (FileNotFoundException e) {
            if (!mayNotExist) {
                Logx.log(5, getClass(), e.toString());
            }
            close(in);
        } catch (IOException e2) {
            Logx.log(5, getClass(), "Error reading : " + fname + ". " + e2);
            close(in);
        } catch (NullPointerException e3) {
            Logx.log(5, getClass(), "Does not exist: " + fname + ". " + e3);
            close(in);
        } catch (Throwable th) {
            close(in);
        }
        return output;
    }

    public void save(Context context, String contents, String fname) {
        save(context, contents, fname, 0);
    }

    public void save(Context context, String contents, String fname, int mode) {
        if (context == null || contents == null || fname == null) {
            throw new NullPointerException();
        }
        OutputStream outputStream = null;
        try {
            outputStream = context.openFileOutput(fname, mode);
            writeContents(outputStream, contents);
        } catch (FileNotFoundException e) {
            Logx.log(5, getClass(), e.toString());
        } catch (IOException e2) {
            Logx.log(5, getClass(), "Error saving object to: " + fname + ". " + e2);
        } finally {
            close(outputStream);
        }
    }

    public boolean saveObject(Context context, String path, Object object) {
        return saveObject(context, path, object, 0);
    }

    public boolean saveObject(Context context, String path, Object object, int mode) {
        try {
            writeObject(context, path, object, mode);
            return true;
        } catch (IOException e) {
            Logx.log(5, getClass(), "Error saving to: " + path + ". " + e);
            return false;
        }
    }

    public Object loadObject(Context context, String path, boolean mayNotExist) {
        Object result = null;
        try {
            result = readObject(context, path);
        } catch (FileNotFoundException e) {
            if (!mayNotExist) {
                Logx.log(5, getClass(), e.toString());
            }
        } catch (IOException e2) {
            Logx.log(5, getClass(), "Error loading object from: " + path + ". " + e2);
        } catch (ClassNotFoundException e3) {
            Logx.log(5, getClass(), "Error loading object from: " + path + ". " + e3);
        }
        return result;
    }

    public Object readObject(Context context, String source) throws ClassNotFoundException, IOException {
        Object result = null;

        FileInputStream     fis = null;
        BufferedInputStream bis = null;
        ObjectInputStream   ois = null;

        try {

            fis = context.openFileInput(source);
            bis = new BufferedInputStream(fis);
            ois = new ObjectInputStream(bis);

            result = ois.readObject();

        }catch(NullPointerException e) {
//@bug
//This method throws NullPointerException if the file doesn't exist
            Logx.log(Log.WARN, this.getClass(), "Does not exist: "+source+". "+e);
        }finally {
            close(ois);
            close(bis);
            close(fis);
        }

        return result;
    }

    public void writeObject(Context context, String destination, Object obj) throws FileNotFoundException, IOException {
        writeObject(context, destination, obj, 0);
    }

    public void writeObject(Context context, String destination, Object obj, int mode) throws FileNotFoundException, IOException {

        FileOutputStream     fos = null;
        BufferedOutputStream bos = null;
        ObjectOutputStream   oos = null;

        try{

            fos = context.openFileOutput(destination, mode);
            bos = new BufferedOutputStream(fos);
            oos = new ObjectOutputStream(bos);

            oos.writeObject(obj);

        }finally {

            if (oos != null) try { oos.close(); }catch(IOException e) { Logx.log(this.getClass(), e); }
            if (bos != null) try { bos.close(); }catch(IOException e) { Logx.log(this.getClass(), e); }
            if (fos != null) try { fos.close(); }catch(IOException e) { Logx.log(this.getClass(), e); }
        }
    }

    private String readContents(InputStream is) throws IOException {

        InputStreamReader isr = null;

//        BufferedReader br = null;

        StringBuilder output = null;

        try{

            isr = new InputStreamReader(is, Charset.forName("UTF-8"));

//            br = new BufferedReader(isr);

            int bufferSize = 8192;

            char[] buff = new char[bufferSize];

            output = new StringBuilder();

            int nRead;

            while ( (nRead=isr.read( buff, 0, bufferSize )) != -1 ) {

                output.append(buff, 0, nRead);
            }
        }finally{

//            close(br);

            close(isr);
        }

        return output.toString();
    }

    private void writeContents(OutputStream os, String contents) throws IOException {

        OutputStreamWriter osw = null;

        BufferedWriter bw = null;

        try{

            osw = new OutputStreamWriter(os, Charset.forName("UTF-8"));

            bw = new BufferedWriter(osw);

            bw.write(contents);

        }finally{

            close(bw);

            close(osw);
        }
    }

    public static void close(Closeable cl) {
        if (cl != null) {
            try {
                cl.close();
            } catch (IOException e) {
                Logx.log(5, FileIO.class, "Failed to close " + cl.getClass().getName() + ". Reason: " + e.toString());
            }
        }
    }

    public static String readFile(File file) throws IOException {
        RandomAccessFile f = new RandomAccessFile(file, "r");
        byte[] bytes = new byte[((int) f.length())];
        try {
            f.readFully(bytes);
            return new String(bytes);
        } finally {
            close(f);
        }
    }

    public static void writeToFile(File file, String contents, boolean append) throws IOException {
        FileOutputStream out = null;
        try{
            out = new FileOutputStream(file, append);
            out.write(contents.getBytes());
        }finally{
            close(out);
        }
    }

    public static File getExternalPublicFile(Context context, String fname) {
        return getExternalPublicFile(context, fname, true);
    }

    public static File getExternalPublicFile(Context context, String fname, boolean nomedia) {
        return new File(getExternalPublicDir(nomedia), fname);
    }

    public static File getFile(Context context, String fname) {
        return new File(context.getApplicationContext().getFilesDir(), fname);
    }

    public static File getExternalPublicDir(boolean nomedia) {
        return getExternalPublicStorageDir(getExternalPublicDirName(), nomedia);
    }

    public static File getExternalPublicStorageDir(String name, boolean nomedia) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), name);
        if (file.exists()) {
            return file;
        }
        if (!file.mkdirs()) {
            return null;
        }
        if (!nomedia) {
            return file;
        }
        File nomediaFile = new File(file, ".nomedia");
        if (nomediaFile.exists()) {
            return file;
        }
        try {
            nomediaFile.createNewFile();
            return file;
        } catch (IOException e) {
            Logx.log(FileIO.class, e);
            return file;
        }
    }

    public static boolean isExternalStorageWritable() {
        return "mounted".equals(Environment.getExternalStorageState());
    }

    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return "mounted".equals(state) || "mounted_ro".equals(state);
    }

    public static String getExternalPublicDirName() {
        return "donot_delete_newsminte";
    }

    public static String getDefaultPreferencesFilename() {
        return "com.looseboxes.idisc.common.Pref";
    }

    public static String getDisplayedFeedidsFilename() {
        return "com.looseboxes.idisc.common.FeedLoadService.displayedFeeds.intarray";
    }

    public static String getReadFeedidsFilename() {
        return "com.looseboxes.idisc.common.User.readFeedids.intarray";
    }

    public static String getDeletedFeedidsFilename() {
        return "com.looseboxes.idisc.common.User.deletedFeedids.intarray";
    }

    public static String getLastDownloadTimeFilename() {
        return "com.looseboxes.idisc.common.FeedLoadService.lastDownloadTime.long";
    }

    public static String getLastDownloadTimePreferenceName(Class aClass, AliasType aliasType) {
        if(aliasType == null) {
            throw new NullPointerException();
        }
        return aClass.getName() + "." + aliasType.name() + ".lastDownloadTime.long";
    }

    public static String getSessionCookiesFilename() {
        return "com.looseboxes.idisc.common.sessioncookies.list";
    }

    public static String getEmailsFilename() {
        return "com.looseboxes.idisc.common.User.emails.list";
    }

    public static String getUsernameFilename() {
        return "com.looseboxes.idisc.common.User.name.string";
    }

    public static String getPasswordFilename() {
        return "com.looseboxes.idisc.common.User.password.string";
    }

    public static String getUserDetailsFilename() {
        return "com.looseboxes.idisc.common.User.details.map";
    }

    public static String getInstallationIdFilename() {
        return "com.looseboxes.idisc.common.app_installationid.string";
    }

    public static String getLastInstallationTimeFilename() {
        return "com.looseboxes.idisc.common.app_last_installationtime.long";
    }

    public static String getFirstInstallationTimeFilename() {
        return "com.looseboxes.idisc.common.app_first_installationtime.long";
    }

    public static String getFeedsFilename() {
        return "com.looseboxes.idisc.common." + getFeedskey() + ".json";
    }

    public static String getCommentNotificationsFilename() {
        return "com.looseboxes.idisc.common.commentNotifications.json";
    }

    public static String getFeedsFilename(PreferenceType preferenceType) {
        switch (preferenceType) {
            case bookmarks:
                return "com.looseboxes.idisc.common.bookmark" + getFeedskey() + ".json";
            case favorites:
                return "com.looseboxes.idisc.common.favorite" + getFeedskey() + ".json";
            default:
                throw new IllegalArgumentException("Expected any of: " + Arrays.toString(PreferenceType.values()) + ", found: " + preferenceType);
        }
    }

    public static String getSearchfeedskey() {
        return "selectfeeds";
    }

    public static String getFeedskey() {
        return "feeds";
    }

    public static String getSyncFeedidskey(PreferenceType preferenceType) {
        switch (preferenceType) {
            case bookmarks:
                return "syncbookmarks";
            case favorites:
                return "syncfavorites";
            default:
                throw new IllegalArgumentException("Expected any of: " + Arrays.toString(PreferenceType.values()) + ", found: " + preferenceType);
        }
    }

    public static String getFeedskey(PreferenceType preferenceType) {
        switch (preferenceType) {
            case bookmarks:
                return "getbookmarkfeeds";
            case favorites:
                return "getfavoritefeeds";
            default:
                throw new IllegalArgumentException("Expected any of: " + Arrays.toString(PreferenceType.values()) + ", found: " + preferenceType);
        }
    }

    public static String getCommentsFilename() {
        return "com.looseboxes.idisc.common." + getCommentskey() + ".json";
    }

    public static String getCommentskey() {
        return "comments";
    }

    public static String getPreferenceFeedidsFilename(PreferenceType prefType) {
        return "com.looseboxes.idisc.common.userpreferences." + prefType.name().toLowerCase() + ".feedids.json";
    }

    public static String getUserFeedPreferenceKey(String prefix, PreferenceType prefType) {
        PreferenceFeedsManager.validatePrefix(prefix);
        switch (prefType) {
            case bookmarks:
                return prefix.toLowerCase() + "bookmarkfeedids";
            case favorites:
                return prefix.toLowerCase() + "favoritefeedids";
            default:
                throw new IllegalArgumentException("Expected any of: " + Arrays.toString(PreferenceType.values()) + ", found: " + prefType);
        }
    }

    public static String getAliasesFilename(AliasType aliasesAliasType) {
        return "com.looseboxes.idisc.common.aliases_for_" + aliasesAliasType.name().toLowerCase() + ".json";
    }

    public static String getAliasesKey(AliasType aliasesAliasType) {
        return "aliasesfor" + aliasesAliasType.name().toLowerCase();
    }

    public static String getAppPropertiesFilename() {
        return "com.looseboxes.idisc.common.appproperties.json";
    }

    public static String getAppPropertiesKey() {
        return "appproperties";
    }
}
