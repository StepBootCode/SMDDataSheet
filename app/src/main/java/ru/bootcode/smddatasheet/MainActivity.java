package ru.bootcode.smddatasheet;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.MenuItem;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.ActionBarDrawerToggle;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;


import com.google.android.gms.ads.MobileAds;

import android.os.Bundle;

import java.io.File;
import java.sql.Array;
import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.Observer;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    final Context context = this;
    static final int NEW_SMD_REQUEST = 2;

    // SharedPreferences переменные (хранение настроек)
    SharedPreferences sp;
    Boolean do_cash;            // true - включено кеширование на внутренний носитель
    Boolean sw_search_name, sw_search_function; // Указывают на поля для поиска
    String save_path;

    // Переменные для работы с базой данных
    private ListView lvComponents;
    private ListComponentAdapter adapter;
    private List<Component> componentList;
    private Component selectedComponent;
    private DatabaseHelper dbHelper;

    // Переменные для работы с рекламмой
    static final private String ADMOB_APP_ID="ca-app-pub-4325894448754236~8052800321";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lvComponents = findViewById(R.id.lvMain);
        // запросим кеш по умолчанию
        String sSavePathDef = Utils.getDefaultCacheDir();

        // Получаем SharedPreferences (Сохраненнве настройки приложения) ---------------------------
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        sw_search_name      = sp.getBoolean("keySwitchSearchName", false);
        sw_search_function  = sp.getBoolean("keySwitchSearchFunction", false);
        //do_cash           = sp.getBoolean("keyCache", false);
        save_path           = sp.getString("keySavePath", sSavePathDef);

        // Тут бы проверить savePath на доступность и др. хрень. (в утилитах конечно)

        // Стандартная колбаса создающая тулбар ----------------------------------------------------
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        
        // Инициализируем AdMob,  Для теста можно взять ID: ca-app-pub-3940256099942544/6300978111
        MobileAds.initialize(this, ADMOB_APP_ID);

        // Создаем Helper для базы данных Компонентов и определяемся с локализацией ----------------
        dbHelper = new DatabaseHelper(this);

        // Проверка на существование базы данных ---------------------------------------------------
        File database = getApplicationContext().getDatabasePath(DatabaseHelper.getDBNAME());
        if(!database.exists()) {
            dbHelper.getReadableDatabase();
            if(Utils.copyDatabase(this)) {
                Utils.showToast(this, R.string.toast_copy_succes);
            } else {
                Utils.showToast(this, R.string.toast_copy_error);
                return;
            }
        }

        // Проверка на актуальность версии базы данных ---------------------------------------------
        Boolean isActualVersion =  dbHelper.getIsActualVersion();
        if(!isActualVersion) {
            dbHelper.getReadableDatabase();
            if(Utils.copyDatabase(this)) {
                Utils.showToast(this, R.string.toast_copy_succes);
            } else {
                Utils.showToast(this, R.string.toast_copy_error);
                return;
            }
        }

        // Запрос комонентов в из базы и передаем их адаптер (Все это под оболочкой RXJava)---------
        //сomponentList = dbHelper.getListComponent();                  - отдал под RXJava
        // Single это упрощенный Observable, он может быть уместным, если вы ожидаете одно значение,
        // например, запрос сети, который выполняется один раз и возвращает значение или ошибку,
        // сетевой вызов работает один раз и он уже не вернет дополнительные значения со временем.
        // Другим примером является работа с данными из базы данных.
        Observable.just(dbHelper.getListComponent())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Component>>() {
                    @Override
                    public void onNext(List<Component> components) {
                        adapter = new ListComponentAdapter(context, components);
                        lvComponents.setAdapter(adapter);
                    }
                    @Override
                    public void onCompleted() {

                    }
                    @Override
                    public void onError(Throwable e) {

                    }
                });

        // Создаем список и установим обработчик нажатия элемента списка ---------------------------
        //adapter = new ListComponentAdapter(this, сomponentList);      - отдал под RXJava
        //lvComponents.setAdapter(adapter);                             - отдал под RXJava
        lvComponents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Intent intent = new Intent(MainActivity.this, ComponentActivity.class);
                adapter.switchSelection(position);
                //selectedComponent = dbHelper.getComponent(String.valueOf(id));
                String[] str = {String.valueOf(id)};
                Observable.from(str)
                        .subscribe(new Observer<String>() {
                            @Override
                            public void onNext(String s) {
                                   selectedComponent = dbHelper.getComponent(s);
                            }
                            @Override
                            public void onCompleted() {
                                Intent intent = new Intent(MainActivity.this, ComponentActivity.class);
                                intent.putExtra("id",       selectedComponent.getID());
                                intent.putExtra("name",     selectedComponent.getName());
                                intent.putExtra("body",     selectedComponent.getBody());
                                intent.putExtra("label",    selectedComponent.getLabel());
                                intent.putExtra("func",     selectedComponent.getFunc());
                                intent.putExtra("prod",     selectedComponent.getProd());
                                intent.putExtra("datasheet",selectedComponent.getDatasheet());
                                intent.putExtra("favorite", selectedComponent.get_forvarite());
                                intent.putExtra("islocal",  selectedComponent.get_islcal());
                                startActivity(intent);
                            }
                            @Override
                            public void onError(Throwable e) { }
                        });
            }
        });

        // Обработка нажатия плавающей кнопки ------------------------------------------------------
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Получаем вид с файла dialog_find.xml, который применим для диалогового окна
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.dialog_find, null);

                //Создаем AlertDialog и настраиваем вид
                AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context);
                mDialogBuilder.setView(promptsView);

                //Настраиваем отображение поля для ввода текста в открытом диалоге
                final EditText userInput = promptsView.findViewById(R.id.etMarker);

                //Настраиваем сообщение в диалоговом окне
                mDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //Получаем текст из отображаем в строки ввода
                                        String txt = userInput.getText().toString();
                                        Observable.just(dbHelper.getFindComponent(txt,
                                                sw_search_name, sw_search_function))
                                                .subscribeOn(Schedulers.io())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Subscriber<List<Component>>() {
                                                    @Override
                                                    public void onNext(List<Component> components) {
                                                        adapter = new ListComponentAdapter(context, components);
                                                        lvComponents.setAdapter(adapter);
                                                    }
                                                    @Override
                                                    public void onCompleted() {

                                                    }
                                                    @Override
                                                    public void onError(Throwable e) {

                                                    }
                                                });
                                    }
                                })
                        .setNegativeButton("Отмена",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });
                //Создаем AlertDialog и отображаем его
                AlertDialog alertDialog = mDialogBuilder.create();
                alertDialog.show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Обработчик нажатия на элемент списка меню, элемент определяем по id ---------------------
        int id = item.getItemId();
        Intent intent;
        switch(item.getItemId()) {
            case R.id.nav_home:
                componentList = dbHelper.getListComponent();
                adapter = new ListComponentAdapter(MainActivity.this, componentList);
                lvComponents.setAdapter(adapter);
                break;
            case R.id.nav_favorites:
                componentList = dbHelper.getListFavorites();
                adapter = new ListComponentAdapter(MainActivity.this, componentList);
                lvComponents.setAdapter(adapter);
                break;
            case R.id.nav_add:
                intent = new Intent(MainActivity.this, AddSMDActivity.class);
                intent.putExtra("id",       0);
                startActivityForResult(intent,NEW_SMD_REQUEST);
                break;
            case R.id.nav_edit:
                intent = new Intent(MainActivity.this, EditSMDActivity.class);
                startActivityForResult(intent,NEW_SMD_REQUEST);
                break;
            case R.id.nav_settings:
                intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_update:

                break;
            default:
                break;
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NEW_SMD_REQUEST) {
            if (resultCode == RESULT_OK) {
                ContentValues newValues = new ContentValues();
                newValues.put("name",       data.getStringExtra("name"));
                newValues.put("label",      data.getStringExtra("label"));
                newValues.put("body",       data.getStringExtra("body"));
                newValues.put("func",       data.getStringExtra("func"));
                newValues.put("datasheet",  data.getStringExtra("pdfname"));
                newValues.put("prod",       data.getStringExtra("prod"));
                newValues.put("favorite",   1);
                newValues.put("islocal",    1);

                String dst = save_path+"/"+data.getStringExtra("pdfname");
                if (!data.getStringExtra("pdf").equals(dst)) {
                    Observable.just(Utils.copyFile(data.getStringExtra("pdf"), dst))
                            .subscribe(new Observer<Boolean>() {
                                @Override
                                public void onCompleted() {
                                    Utils.showToast(context, "File is cached");
                                }

                                @Override
                                public void onError(Throwable e) {

                                }

                                @Override
                                public void onNext(Boolean aBoolean) {

                                }
                            });
                }

                Observable.from(new ContentValues[]{newValues})
                        .subscribe(new Observer<ContentValues>() {
                            @Override
                            public void onNext(ContentValues s) {
                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                db.insert("COMPONENTS", null, s);
                            }
                            @Override
                            public void onCompleted() {
                                List<Component> value =  dbHelper.getListComponent();
                                adapter = new ListComponentAdapter(MainActivity.this, value);
                                lvComponents.setAdapter(adapter);
                            }
                            @Override
                            public void onError(Throwable e) { }
                        });
            }
        }

    }

}
