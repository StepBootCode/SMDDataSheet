package ru.bootcode.smddatasheet;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.Single;

// ВНИМАНИЕ! Нужно перенести запросы в стринговые ресурсы
public class DatabaseHelper extends SQLiteOpenHelper {
    // Верися базы данных, при изменении следует увеличить (произойдет замешение уже имеющийся базы)
    // В базе данных есть таблица с полем "version" указывающая на версию базы данных, при увилечении
    // устанавливаем тоже значение, что и VERSION
    private static final int VERSION = 4;

    // Константы указывающие на базу данных в локальном каталоге приложения
    private static final String DBNAME = "smd.db";
    //private static final String DBLOCATION = /data/data/ru.bootcode.smddatasheet/databases/smd.db

    private Context mContext;
    private SQLiteDatabase mDatabase;

    // Возвращает имя базы данных
    static String getDBNAME() {
        return DBNAME;
    }

    // Возвращает стандартный каталог с базой данных
    static String getDBLOCATION(Context context) {
        return context.getDatabasePath(DBNAME).getPath();
    }

    DatabaseHelper(Context context) {
        super(context, DBNAME, null, VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void openDatabase() {
        String dbPath = mContext.getDatabasePath(DBNAME).getPath();
        if(mDatabase != null && mDatabase.isOpen()) return;

        try {
        mDatabase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        }catch (Exception ignored) { }
    }

    private void closeDatabase() {
        if(mDatabase!=null) mDatabase.close();
    }

    // Функция проверяет изменилась ли версия базы данных
    Boolean getIsActualVersion() {
        int v = 0;
        Boolean actual = false;
        try {
            // Откроем базу данных и загрузим таблицу настроек и из нее получим версию БД ----------
            openDatabase();
            Cursor cursor = mDatabase.rawQuery("SELECT version FROM pref", null);
            cursor.moveToFirst();
            v = cursor.getInt(0);
            cursor.close();
            closeDatabase();
        }catch (Exception e) {
           System.out.println(e.getMessage());
        }
        // Проверяем актуальность версии базы данных и выплюнем результат -------------------------
        // если мы получили 0 значит где-то косяк (по идеи надо вызвать исключение)
        if (v == 0) return false;
        return (v >= VERSION);
    }

    // Возващает полный несортикрованный список компонентов
    List<Component> getListComponent() {
        Component component;
        List<Component> componentList = new ArrayList<>();
        openDatabase();
        Cursor cursor = null;

        try {
            cursor = mDatabase.rawQuery("SELECT _id, body, label, func, prod, name " +
                                                "FROM COMPONENTS", null);
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                component = new Component(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
                component.setProd(cursor.getString(4));
                component.setName(cursor.getString(5));
                componentList.add(component);
                cursor.moveToNext();
            }
            cursor.close();
        }
        closeDatabase();
        return componentList;
    }

    // Возващает полный несортикрованный список избранных компонентов
    List<Component> getListFavorites() {
        Component component;
        List<Component> componentList = new ArrayList<>();
        openDatabase();
        Cursor cursor = null;
        try {
            cursor = mDatabase.rawQuery(
                    "SELECT _id, body, label, func, prod, name " +
                            "FROM COMPONENTS WHERE favorite=1",
                    null);
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                component = new Component(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
                component.setProd(cursor.getString(4));
                component.setName(cursor.getString(5));
                componentList.add(component);
                cursor.moveToNext();
            }
            cursor.close();
        }
        closeDatabase();
        return componentList;
    }

    // Возващает полный несортикрованный список компонентов
    List<Component> getListLocals() {
        Component component;
        List<Component> componentList = new ArrayList<>();
        openDatabase();
        Cursor cursor = null;
        try {
            cursor = mDatabase.rawQuery(
                    "SELECT _id, body, label, func, prod, name " +
                            "FROM COMPONENTS WHERE islocal=1",
                    null);
    }catch (Exception e) {
        System.out.println(e.getMessage());
    }
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                component = new Component(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
                component.setProd(cursor.getString(4));
                component.setName(cursor.getString(5));
                componentList.add(component);
                cursor.moveToNext();
            }
            cursor.close();
        }
        closeDatabase();
        return componentList;
    }

    // Возвращает список компонентов после поиска по ключевому слову
    List<Component> getFindComponent(String sLabel, Boolean sw_name, Boolean sw_function) {
        Component component;
        List<Component> componentList = new ArrayList<>();
        openDatabase();
        Cursor cursor = null;
        try {
            cursor = mDatabase.rawQuery(
                    "SELECT _id, body, label, func, prod, name " +
                         "FROM COMPONENTS " +
                         "WHERE label LIKE '%" + sLabel + "%' " +
                            (sw_name ?      "OR name LIKE '%" + sLabel + "%' " : " ") +
                            (sw_function ?  "OR func LIKE '%" + sLabel + "%'"  : " ") ,
                    null);
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                component = new Component(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
                component.setProd(cursor.getString(4));
                component.setName(cursor.getString(5));
                componentList.add(component);
                cursor.moveToNext();
            }
            cursor.close();
        }
        closeDatabase();
        return componentList;
    }

    // Возвращает Компонент по его ID в базе
    Component getComponent(String ID) {
        Component component = null;
        openDatabase();
        Cursor cursor = null;
        try {
            cursor = mDatabase.rawQuery(
                    "SELECT _id, name, body, label,  prod, func, datasheet, " +
                            "favorite, islocal " +
                         "FROM COMPONENTS " +
                         "WHERE _ID = "+ID,
                    null);
    }catch (Exception e) {
        System.out.println(e.getMessage());
    }
        if (cursor != null)

    {
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            component = new Component(cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6));
            component.set_forvarite(cursor.getInt(7));
            component.set_islcal(cursor.getInt(8));
        }
        cursor.close();
    }
        closeDatabase();
        return component;
    }

    // Возвращает являеться ли SMD избранным (1 - избр., 0... - неизбр. -1 - если воникла ошибка)
    int getIsFavoriteCmp(long ID) {
        Component component = null;
        openDatabase();
        Cursor cursor = null;
        int res = -1;
        try {
            cursor = mDatabase.rawQuery(
                    String.format("SELECT favorite FROM COMPONENTS WHERE _ID = %d", ID), null);
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                res = cursor.getInt(0);
            }
            cursor.close();
        }
        closeDatabase();
        return res;
    }
}
