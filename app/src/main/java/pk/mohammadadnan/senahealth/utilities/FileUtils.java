package pk.mohammadadnan.senahealth.utilities;

import android.content.Context;
import android.os.Environment;

import java.io.File;

import pk.mohammadadnan.senahealth.R;

public class FileUtils {

    public static String getAppDir(Context context){
        return context.getExternalFilesDir(null) + "/" + context.getString(R.string.app_name);
    }

    public static File createDirIfNotExist(String path){
        File dir = new File(path);
        if( !dir.exists() ){
            dir.mkdirs();
        }
        return dir;
    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }
}
