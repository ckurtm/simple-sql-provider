package example.kurt.todo.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by kurt on 2015/02/19.
 */
public class TodoUtils {

    static String TAG = TodoUtils.class.getSimpleName();

    public static void copyDbAsset(Context context, String file, String toFolder) throws IOException {
        AssetManager assetManager = context.getAssets();
        InputStream in = assetManager.open(file);
        String[] parts = file.split("/");
        File outFile = new File(toFolder, parts[parts.length - 1]);
        if (outFile.exists()) {
            return;
        } else {
            outFile.mkdirs();
        }
        if (context.getFilesDir() != null) {
            if (!context.getFilesDir().exists()) {
                context.getFilesDir().mkdirs();
            }
        }
        if (outFile.exists()) {
            outFile.delete();
        }
        OutputStream out = new FileOutputStream(outFile);
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        in.close();
        out.flush();
        outFile.setWritable(true, false);
        outFile.setReadable(true, false);
        out.close();
    }


    @TargetApi(Build.VERSION_CODES.FROYO)
    public static File getDataPath(Context context, boolean internal) {
        if (internal)
            return context.getFilesDir();
        return context.getExternalFilesDir(null);
    }

    public static String getDataBasePath(Context context) {
        return "/data/data/" + context.getPackageName() + "/databases/";
    }

    public static String[] getAssetFiles(Context context, String path) {
        try {
            return context.getAssets().list(path);
        } catch (IOException e) {
            Log.e(TAG, "exception reading assets: ", e);
        }
        return null;
    }
}
