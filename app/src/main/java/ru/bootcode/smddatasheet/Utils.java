package ru.bootcode.smddatasheet;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;
import androidx.annotation.StringRes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class Utils {
    // Провера каталога на запись
    static boolean testDirOnWrite(String sDir) {
        String str = "";
        File testfile;
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
        // Если он удаляеться значит все ОК
        return testfile.delete();
    }

    // Получение каталога для хранения кеша
    static String getDefaultCacheDir(){
        String fl;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            fl = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).toString();
        }  else {
            fl  = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
        }
        // Добавим к этому всему нашу директорию и рекурсивно создадим директории
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
            String outFileName = DatabaseHelper.getDBLOCATION(context);
            OutputStream outputStream = new FileOutputStream(outFileName);
            // Стандартное копирование потоков
            byte[]buff = new byte[1024];
            int length;
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

    // Копирование файлов
    static boolean copyFile(String src, String dst) {
        try {
            // Открываем потоки - Откуда и куда копируем
            InputStream inputStream = new FileInputStream(src);
            OutputStream outputStream = new FileOutputStream(dst);
            // Стандартное копирование потоков
            byte[]buff = new byte[1024];
            int length;
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
