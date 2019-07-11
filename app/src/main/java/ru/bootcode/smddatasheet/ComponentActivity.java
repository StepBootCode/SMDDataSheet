package ru.bootcode.smddatasheet;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.Observer;
import rx.schedulers.Schedulers;

import static ru.bootcode.smddatasheet.Utils.showToast;

public class ComponentActivity extends Activity {
    final Context context = this;

    Boolean keyCache;                       // Тру - нужно кешировать PDF файлы на устройстве
    String keySavePath;                     // Путь к кешу


    int isFavorite;
    int iIDComp;

    private String sLinkDatasheet;
    private String sCacheDatasheet;

    private Button btnFavorite;

    private final static String LINK = "http://bootcode.ru/datasheet/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_component);

        // Получим настройки и в частности нужно ли кешировать PDF файлы ---------------------------
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        keyCache    = sp.getBoolean("keyCache", false);
        keySavePath = sp.getString("keySavePath", Utils.getDefaultCacheDir());

        // Получаем переданные из MainActivity (Информация о SMD компоненте) -----------------------
        Intent intent = getIntent();
        iIDComp     = intent.getIntExtra("id",0);
        isFavorite  = intent.getIntExtra("favorite",0);
        final int iisLocal  = intent.getIntExtra("islocal",0);
        String sName        = intent.getStringExtra("name");
        String sBody        = intent.getStringExtra("body");
        String sLabel       = intent.getStringExtra("label");
        String sFunc        = intent.getStringExtra("func");
        String sDatasheet   = intent.getStringExtra("datasheet");
        //String sProd        = intent.getStringExtra("prod");


        // Определяем ссылки на PDF файлы на сервере и локально (в кеше) ---------------------------
        String fileName = sDatasheet.replace("~","0").replace("@","1").replace("#","2")+".pdf";
        sLinkDatasheet = LINK+fileName;
        sCacheDatasheet = keySavePath +"/"+ fileName;

        // Выводим информацию о компоненте ---------------------------------------------------------
        ((TextView) findViewById(R.id.tvNote)).setText(sFunc);
        ((TextView) findViewById(R.id.tvCode)).setText(sBody);
        ((TextView) findViewById(R.id.tvName)).setText(sName);
        ((TextView) findViewById(R.id.tvMarker)).setText(sLabel);

        // Вытаскиваем и показываем картинку из ресурсов -------------------------------------------
        sBody =sBody.replace("-","_").toLowerCase();
        int id = getResources().getIdentifier( "ru.bootcode.smddatasheet:drawable/big_" + sBody,
                                                                                        null, null);
        if (id == 0) id = getResources().getIdentifier("ru.bootcode.smddatasheet:drawable/big_def",
                                                                                        null, null);
        ((ImageView) findViewById(R.id.ivCode)).setImageResource(id);

        // создаем обработчик нажатия кнопки загрузки PDF ------------------------------------------
        // Сначала проверяем, может это файл пользователя (значит он локально)
        // Потом проверяем кеш, если стоит в настройках тру, при всем при этом не забываем проверять
        // установлен ли PDF Reader на устройстве
        Button btnDataSheet = findViewById(R.id.btnDataSheet);
        btnDataSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(iisLocal > 0) {
                    File f = new File(sCacheDatasheet);
                    if (f.exists()) // файл есть
                        showPDFfromCache(sCacheDatasheet);
                    else
                        showToast(context, R.string.toast_not_pdf_file);

                    return;
                }
                if (keyCache){
                    // Проверим наш кеш если в нем есть файл, то открывать будем его
                    File f = new File(sCacheDatasheet);
                    if (f.exists()) // файл есть
                    {
                        if (!showPDFfromCache(sCacheDatasheet))  showPDFfromURL(sLinkDatasheet);
                    }else{
                        showPDFfromURL(sLinkDatasheet);
                        // Загружаем файл в кеш

                        downloadPDFFile(sLinkDatasheet, sCacheDatasheet);

                    }
                } else {
                    showPDFfromURL(sLinkDatasheet);
                }
            }
        });

        // Обработка кнопки Избранное --------------------------------------------------------------
        btnFavorite = findViewById(R.id.btnFavorite);
        if (isFavorite > 0)     btnFavorite.setBackgroundResource(R.drawable.ic_favorite_on);
            else                btnFavorite.setBackgroundResource(R.drawable.ic_favorite_off);

        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] str = {String.valueOf(iIDComp)};
                DatabaseHelper dbHelper = new DatabaseHelper(ComponentActivity.this);
                dbHelper.getReadableDatabase();
                Observable.just(dbHelper.getIsFavoriteCmp(iIDComp))
                        .subscribe(new Observer<Integer>() {
                            @Override
                            public void onCompleted() {
                                if (isFavorite > 0) {
                                    btnFavorite.setBackgroundResource(R.drawable.ic_favorite_on);
                                    Utils.showToast(ComponentActivity.this,R.string.toast_success_add_favorites);
                                } else {
                                    btnFavorite.setBackgroundResource(R.drawable.ic_favorite_off);
                                    Utils.showToast(ComponentActivity.this,R.string.toast_success_remove_favorites);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(Integer fvr) {
                                if (fvr == 0) isFavorite = 1;
                                else isFavorite = 0;

                                ContentValues updatedValues = new ContentValues();
                                updatedValues.put("favorite", isFavorite);

                                String where = "_id=?";
                                String[] whereArgs = {String.valueOf(iIDComp)};

                                DatabaseHelper dbHelper = new DatabaseHelper(ComponentActivity.this);
                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                db.update("COMPONENTS", updatedValues, where, whereArgs);
                            }
                        });
            }
        });

        // Рекламма, без нее ни как :) -------------------------------------------------------------
        // Надо подумать как добавить китайскую сеть Youmi
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
            }
        });
    }
    // Простое отображение PDF в браузере, через сервис google -------------------------------------
    private void showPDFfromURL(String url) {
        String format = "https://drive.google.com/viewerng/viewer?embedded=true&url=%s";
        String fullPath = String.format(Locale.ENGLISH, format, url);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fullPath));
        startActivity(browserIntent);
    }
    // Простое отображение PDF в PDF Reader, если он установлен (иначе возвращает FALSE) -----------
    private boolean showPDFfromCache(String f){
        try {
            Intent intent = new Intent(ComponentActivity.this, PDFViewer.class);
            intent.putExtra("filename", f);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            showToast(context, R.string.toast_pdf_not_install);
        }
        return true;
    }


    // Загрузка PDF с сервера в локальное хранилище ------------------------------------------------
    private void downloadPDFFile(String url, final String fl) {
        final String[] str = {url};
        Observable.from(str)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onNext(String s) {
                        final OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url(s)
                                .build();
                        Response response = null;
                        try {
                            response = client.newCall(request).execute();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (null != response && response.body() != null) {
                            if (response.isSuccessful()) {
                                try {
                                    OutputStream outputStream = new FileOutputStream(fl);
                                    // Стандартное копирование потоков
                                    byte[] buff = new byte[1024];
                                    int length = 0;
                                    while ((length = response.body().byteStream().read(buff)) > 0) {
                                        outputStream.write(buff, 0, length);
                                    }
                                    outputStream.flush();
                                    outputStream.close();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    @Override
                    public void onCompleted() {
//Utils.showToast(ComponentActivity.this,"Good");
                    }
                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    private void downloadPDFFileTEST(String url, final String fl) throws IOException {
        final OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        Observable.just(client.newCall(request).execute())
                //.subscribeOn(Schedulers.io())
                //.observeOn(Schedulers.newThread())
                .subscribe(new Observer<Response>() {
                    @Override
                    public void onNext(Response response) {
                        if (null != response && response.body() != null) {
                            if (response.isSuccessful()) {
                                try {
                                    OutputStream outputStream = new FileOutputStream(fl);
                                    // Стандартное копирование потоков
                                    byte[] buff = new byte[1024];
                                    int length;
                                    while ((length = response.body().byteStream().read(buff)) > 0) {
                                        outputStream.write(buff, 0, length);
                                    }
                                    outputStream.flush();
                                    outputStream.close();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    @Override
                    public void onCompleted() {
                        //Utils.showToast(ComponentActivity.this,"Good");
                    }
                    @Override
                    public void onError(Throwable e) {

                    }
                });
    }
}