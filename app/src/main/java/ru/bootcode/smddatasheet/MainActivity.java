package ru.bootcode.smddatasheet;

import android.content.Intent;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Environment;
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
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;

import android.os.Bundle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    final Context context = this;

    // SharedPreferences переменные (хранение настроек)
    SharedPreferences sp;
    Boolean is_ru_local;        // Локализация базы данных En / Ru  (Ru = True, En = false)
    Boolean do_cash;            // true - включено кеширование на внутренний носитель
    Boolean sw_search_name, sw_search_function; // Указывают на поля для поиска
    String save_path;


    // Переменные для работы с базой данных
    private ListView lvMain;
    private ListComponentAdapter adapter;
    private List<Component> mComponentList;
    private Component SelectedComponent;
    private DatabaseHelper mDBHelper;

    // Переменные для работы с рекламмой
    static final private String ADMOB_APP_ID="ca-app-pub-4325894448754236~8052800321";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // запросим кеш по умолчанию
        String sSavePathDef = Utils.getCacheSavePath();

        // Получаем SharedPreferences (Сохраненнве настройки приложения) ---------------------------
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        is_ru_local = sp.getBoolean("ru_switch", false);
        do_cash     = sp.getBoolean("do_cash", false);
        save_path     = sp.getString("savepath", sSavePathDef);
        sw_search_name     = sp.getBoolean("switch_search_name", false);
        sw_search_function = sp.getBoolean("sw_search_function", false);


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
        mDBHelper = new DatabaseHelper(this);
        mDBHelper.setRuLocal(is_ru_local);

        // Проверка на существование базы данных ---------------------------------------------------
        File database = getApplicationContext().getDatabasePath(DatabaseHelper.DBNAME);
        if(!database.exists()) {
            mDBHelper.getReadableDatabase();
            if(Utils.copyDatabase(this)) {
                Utils.showToast(this, R.string.toast_copy_succes);
            } else {
                Utils.showToast(this, R.string.toast_copy_error);
                return;
            }
        }

        // Проверка на актуальность версии базы данных ---------------------------------------------
        Boolean isActualVersion =  mDBHelper.getIsActualVersion();
        if(!isActualVersion) {
            mDBHelper.getReadableDatabase();
            if(Utils.copyDatabase(this)) {
                Utils.showToast(this, R.string.toast_copy_succes);
            } else {
                Utils.showToast(this, R.string.toast_copy_error);
                return;
            }
        }

        // Запрос комонентов в из базы и передаем их адаптер ---------------------------------------
        mComponentList = mDBHelper.getListComponent();
        adapter = new ListComponentAdapter(this, mComponentList);

        // Создаем список и установим обработчик нажатия элемента списка ---------------------------
        lvMain = (ListView) findViewById(R.id.lvMain);
        lvMain.setAdapter(adapter);
        lvMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ComponentActivity.class);
                SelectedComponent = mDBHelper.getComponent(String.valueOf(id));
                //intent.putExtra("id", String.valueOf(id));
                intent.putExtra("name",     SelectedComponent.getName());
                intent.putExtra("code",     SelectedComponent.getCode());
                intent.putExtra("marker",   SelectedComponent.getMarker());
                intent.putExtra("note",     SelectedComponent.getNote());
                intent.putExtra("prod",     SelectedComponent.getProd());
                intent.putExtra("datasheet",SelectedComponent.getDatasheet());
                intent.putExtra("favorite",SelectedComponent.get_forvarite());
                intent.putExtra("islocal",SelectedComponent.get_islcal());
                startActivity(intent);
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
                final EditText userInput = (EditText) promptsView.findViewById(R.id.etMarker);

                //Настраиваем сообщение в диалоговом окне
                mDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //Получаем текст из отображаем в строки ввода
                                        String txt = userInput.getText().toString();
                                        mComponentList = mDBHelper.getFindComponent(txt,
                                                sw_search_name,
                                                sw_search_function);
                                        adapter = new ListComponentAdapter(context, mComponentList);
                                        lvMain.setAdapter(adapter);
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
                mComponentList = mDBHelper.getListComponent();
                adapter = new ListComponentAdapter(MainActivity.this, mComponentList);
                lvMain.setAdapter(adapter);
                break;
            case R.id.nav_favorites:
                mComponentList = mDBHelper.getListFavorites();
                adapter = new ListComponentAdapter(MainActivity.this, mComponentList);
                lvMain.setAdapter(adapter);
                break;
            case R.id.nav_add:
                intent = new Intent(MainActivity.this, NewSMDActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_edit:

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


}
