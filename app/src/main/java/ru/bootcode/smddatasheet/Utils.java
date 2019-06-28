package ru.bootcode.smddatasheet;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;
import androidx.annotation.StringRes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class Utils {
    // Провера каталога на запись
    static boolean testDirOnWrite(String sDir) {
        String str = "";
        File testfile = null;
        try {
            File root = new File(sDir);
            if (!root.exists()) {
                return false;
            }
            testfile = new File(root, "datasheets.cache");
            FileWriter writer = new FileWriter(testfile);
            writer.append(str);
            writer.flush();
            writer.close();
        } catch (IOException ignored) {
            return false;
        }
        if (testfile == null) return false;
        boolean res = testfile.exists();
        testfile.delete();
        return res;
    }

    // Получение каталога для хранения кеша
    static String getDefaultCacheDir(){
        String fl = "/bootcode";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            fl = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
        }  else {
            fl  = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
        }
        fl = fl + "/DataSheets";

        File f = new File(fl);
        if(!f.isDirectory()) {
            f.mkdirs();
        }
        return fl;
    }

    // Копирование базы в рабочий каталог
    static boolean copyDatabase(Context context) {
        try {
            // Открываем поток - Откуда копируем (из каталога assets)
            InputStream inputStream = context.getAssets().open(DatabaseHelper.getDBNAME());
            // Открываем поток - Куда копируем (каталог программы)
            String outFileName      = DatabaseHelper.getDBLOCATION() + DatabaseHelper.getDBNAME();
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

    // Тост, на пряую из строки
    static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    // Тост, на пряую из ресурса
    static void showToast(Context context, @StringRes int msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
