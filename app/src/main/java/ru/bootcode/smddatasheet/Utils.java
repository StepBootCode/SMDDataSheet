package ru.bootcode.smddatasheet;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.StringRes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import static androidx.core.content.res.TypedArrayUtils.getString;

class Utils {
    static String getCacheSavePath() {
        File dir = null;
        String path = "/";
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                path = dir.getAbsolutePath();
            } else {
                dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                path = dir.getAbsolutePath();
            }
        } catch (Exception e){
            path = "/";
        }
        return path;
    }

    // Копирование базы в рабочий каталог
    static boolean copyDatabase(Context context) {
        try {
            // Открываем поток - Откуда копируем (из каталога assets)
            InputStream inputStream = context.getAssets().open(DatabaseHelper.DBNAME);
            // Открываем поток - Куда копируем (каталог программы)
            String outFileName      = DatabaseHelper.DBLOCATION + DatabaseHelper.DBNAME;
            OutputStream outputStream = new FileOutputStream(outFileName);
            // Стандартное копирование потоков
            byte[]buff = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(buff)) > 0) {
                outputStream.write(buff, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            return true;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    static boolean dirIsGood(String path){
       // File p = File.get(path);
//
//        if (Files.exists(p)) {
//            // действия, если папка существует
 //       }
        return true;
    }

    static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    static void showToast(Context context, @StringRes int msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }


}
