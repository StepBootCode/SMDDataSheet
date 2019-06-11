package ru.bootcode.smddatasheet;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    // Верися базы данных, при изменении следует увеличить (произойдет замешение уже имеющийся базы)
    // В базе данных есть таблица с полем "version" указывающая на версию базы данных
    public static final int VERSION = 2;

    // Константы указывающие на базу данных в локальном каталоге приложения
    public static final String DBNAME = "smd.db";
    public static final String DBLOCATION = "/data/data/ru.bootcode.smddatasheet/databases/";

    private Context mContext;
    private SQLiteDatabase mDatabase;

    // Переменная указывающая на локализацию БД устанавливаеться из настроек в MainActivity
    private Boolean is_ru = false;

    public DatabaseHelper(Context context) {
        super(context, DBNAME, null, VERSION);
        this.mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void openDatabase() {
        String dbPath = mContext.getDatabasePath(DBNAME).getPath();
        if(mDatabase != null && mDatabase.isOpen()) {
            return;
        }
        try {
        mDatabase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void closeDatabase() {
        if(mDatabase!=null) {
            mDatabase.close();
        }
    }

    // Установка (выбор) локализации БД
    public void setRuLocal(Boolean ru_localization) {
        is_ru = ru_localization;
    }

    // Функция проверяет изменилась ли версия базы данных
    public Boolean getIsActualVersion() {
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
        return ((v < VERSION) ? false : true);
    }

    // Возващает полный несортикрованный список компонентов
    public List<Component> getListComponent() {
        Component component = null;
        List<Component> componentList = new ArrayList<>();
        openDatabase();
        Cursor cursor;
        if (is_ru == true) {
            cursor = mDatabase.rawQuery(
                    "SELECT id, code, marker, note_ru AS note, prod, name " +
                        "FROM COMPONENTS",
                    null);
        } else {
            try {
            cursor = mDatabase.rawQuery(
                    "SELECT id, code, marker, note, prod, name " +
                         "FROM COMPONENTS",
                    null);
            }catch (Exception e) {
                System.out.println(e.getMessage());
            }
            cursor = mDatabase.rawQuery(
                    "SELECT id, code, marker, note, prod, name " +
                            "FROM COMPONENTS",
                    null);
        }
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            component = new Component(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
            component.setProd(cursor.getString(4));
            component.setName(cursor.getString(5));
            componentList.add(component);
            cursor.moveToNext();
        }
        cursor.close();
        closeDatabase();
        return componentList;
    }

    // Возващает полный несортикрованный список избранных компонентов
    public List<Component> getListFavorites() {
        Component component = null;
        List<Component> componentList = new ArrayList<>();
        openDatabase();
        Cursor cursor;
        if (is_ru == true) {
            cursor = mDatabase.rawQuery(
                    "SELECT id, code, marker, note_ru AS note, prod, name " +
                            "FROM COMPONENTS WHERE favorite=1",
                    null);
        } else {
            cursor = mDatabase.rawQuery(
                    "SELECT id, code, marker, note, prod, name " +
                            "FROM COMPONENTS WHERE favorite=1",
                    null);
        }
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            component = new Component(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
            component.setProd(cursor.getString(4));
            component.setName(cursor.getString(5));
            componentList.add(component);
            cursor.moveToNext();
        }
        cursor.close();
        closeDatabase();
        return componentList;
    }

    // Возващает полный несортикрованный список компонентов
    public List<Component> getListLocals() {
        Component component = null;
        List<Component> componentList = new ArrayList<>();
        openDatabase();
        Cursor cursor;
        if (is_ru == true) {
            cursor = mDatabase.rawQuery(
                    "SELECT id, code, marker, note_ru AS note, prod, name " +
                            "FROM COMPONENTS WHERE islocal=1",
                    null);
        } else {
            cursor = mDatabase.rawQuery(
                    "SELECT id, code, marker, note, prod, name " +
                            "FROM COMPONENTS WHERE islocal=1",
                    null);
        }
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            component = new Component(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
            component.setProd(cursor.getString(4));
            component.setName(cursor.getString(5));
            componentList.add(component);
            cursor.moveToNext();
        }
        cursor.close();
        closeDatabase();
        return componentList;
    }

    // Возвращает список компонентов после поиска по ключевому слову
    public List<Component> getFindComponent(String sMarker,Boolean sw_name, Boolean  sw_function) {
        Component component = null;
        List<Component> componentList = new ArrayList<>();
        openDatabase();
        Cursor cursor;
        if (is_ru == true) {
            cursor = mDatabase.rawQuery(
                    "SELECT id, code, marker, note_ru AS note, prod, name " +
                         "FROM COMPONENTS " +
                         "WHERE marker LIKE '%" + sMarker + "%' " +
                            (sw_name ?      "OR name LIKE '%" + sMarker + "%' "   : " ") +
                            (sw_function ?  "OR note_ru LIKE '%" + sMarker + "%'" : ""),
                    null);
        } else {
            cursor = mDatabase.rawQuery(
                    "SELECT id, code, marker, note, prod, name " +
                         "FROM COMPONENTS " +
                         "WHERE marker LIKE '%" + sMarker + "%' " +
                            (sw_name ?      "OR name LIKE '%" + sMarker + "%' " : " ") +
                            (sw_function ?  "OR note LIKE '%" + sMarker + "%'"  : " ") ,
                    null);
        }
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            component = new Component(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
            component.setProd(cursor.getString(4));
            component.setName(cursor.getString(5));
            componentList.add(component);
            cursor.moveToNext();
        }
        cursor.close();
        closeDatabase();
        return componentList;
    }

    // Возвращает Компонент по его ID в базе
    public Component getComponent(String ID) {
        Component component = null;
        openDatabase();
        Cursor cursor;
        if (is_ru == true) {
            cursor = mDatabase.rawQuery(
                    "SELECT id, name, code, marker, prod, note_ru AS note, datasheet, " +
                            "favorite, islocal " +
                         "FROM COMPONENTS " +
                         "WHERE ID = "+ID,
                    null);
        } else {
            cursor = mDatabase.rawQuery(
                    "SELECT id, name, code, marker, prod, note,  datasheet, " +
                            "favorite, islocal " +
                         "FROM COMPONENTS " +
                         "WHERE ID = "+ID,
                    null);
        }
        cursor.moveToFirst();
        if (cursor.getCount()>0) {
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
        closeDatabase();
        return component;
    }
}
