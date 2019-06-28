package ru.bootcode.smddatasheet;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
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

    Boolean keyCache;                    // Тру - нужно кешировать PDF файлы на устройстве
    String keySavePath;                    // Путь к кешу
    int iFavorite;
    int iIDComp;

    private TextView tvCode;
    private TextView tvName;
    private TextView tvNote;
    private TextView tvMarker;
    private String sDatasheet;
    private String sLinkDatasheet;
    private String sCacheDatasheet;

    private Button btnDataSheet;
    private Button btnFavorite;
    private Button btnSave;
    private AdView mAdView;

    private final static String LINK = "http://bootcode.ru/datasheet/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_component);

        // Получим настройки и в частности нужно ли кешировать PDF файлы ---------------------------
        SharedPreferences sp;
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        keyCache    = sp.getBoolean("keyCache", false);
        keySavePath = sp.getString("keySavePath", Utils.getDefaultCacheDir());

        // Определяем ссылки на PDF файлы на сервере и локально
        sLinkDatasheet = LINK+sDatasheet.replace("~","0")
                .replace("@","1")
                .replace("#","2")+".pdf";
        sCacheDatasheet = keySavePath +"/"+ sDatasheet.replace("~","0")
                .replace("@","1")
                .replace("#","2")+".pdf";

        // Получаем переданные из MainActivity параметры о SMD компоненте --------------------------
        Intent intent = getIntent();
        iIDComp = intent.getIntExtra("id",0);
        String sName    = intent.getStringExtra("name");
        String sBody    = intent.getStringExtra("body");
        String sLabel  = intent.getStringExtra("label");
        String sFunc    = intent.getStringExtra("func");
        String sDatasheet = intent.getStringExtra("datasheet");
        iFavorite   = intent.getIntExtra("favorite",0);
        //String sProd = intent.getStringExtra("prod");
        //int iIsLocal    = intent.getIntExtra("islocal",0);

        // Выводим информацию о компоненте ---------------------------------------------------------
        ((TextView) findViewById(R.id.tvNote)).setText(sFunc);
        ((TextView) findViewById(R.id.tvCode)).setText(sBody);
        ((TextView) findViewById(R.id.tvName)).setText(sName);
        ((TextView) findViewById(R.id.tvMarker)).setText(sLabel);

        // Вытаскиваем и показываем картинку из ресурсов
        sBody =sBody.replace("-","_").toLowerCase();
        int id = getResources().getIdentifier(
                "ru.bootcode.smddatasheet:drawable/big_" + sBody,
                null, null);
        if (id == 0) id = getResources().getIdentifier(
                "ru.bootcode.smddatasheet:drawable/big_def",
                null, null);
        ((ImageView) findViewById(R.id.ivCode)).setImageResource(id);

        // создаем обработчик нажатия кнопки загрузки PDF ------------------------------------------
        btnDataSheet = (Button) findViewById(R.id.btnDataSheet);
        btnDataSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (keyCache){
                    // Проверим наш кеш если в нем есть файл, то открывать будем его
                    // Иначе загрузим его и потом уже откроем

                    File f = new File(sCacheDatasheet);
                    if (f.exists()) // файл есть
                    {
                        try {
                            Uri uri = Uri.parse(sCacheDatasheet);
                            Intent intentUrl = new Intent(Intent.ACTION_VIEW);
                            intentUrl.setDataAndType(uri, "application/pdf");
                            intentUrl.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            if (intentUrl.resolveActivity(getPackageManager()) != null) {
                                startActivity(intentUrl);
                            } else {
                                showToast(context, R.string.toast_pdf_not_install);
                                String format = "https://drive.google.com/viewerng/viewer?embedded=true&url=%s";
                                String fullPath = String.format(Locale.ENGLISH, format, sLinkDatasheet);
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fullPath));
                                startActivity(browserIntent);
                            }
                        } catch (ActivityNotFoundException e) {
                            showToast(context, R.string.toast_pdf_not_install);
                        }
                    }else{
                        String format = "https://drive.google.com/viewerng/viewer?embedded=true&url=%s";
                        String fullPath = String.format(Locale.ENGLISH, format, sLinkDatasheet);
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fullPath));
                        startActivity(browserIntent);

                        // Загружаем файл в кеш
                        downloadPDFFile(sLinkDatasheet, sCacheDatasheet);
                    }
                } else {
                    String format = "https://drive.google.com/viewerng/viewer?embedded=true&url=%s";
                    String fullPath = String.format(Locale.ENGLISH, format, sLinkDatasheet);
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fullPath));
                    startActivity(browserIntent);
                }
            }
        });

        // Обработка кнопки Избранное --------------------------------------------------------------
        btnFavorite = (Button) findViewById(R.id.btnFavorite);
        if (iFavorite > 0) {
            btnFavorite.setBackgroundResource(R.drawable.ic_favorite_on);
        } else {
            btnFavorite.setBackgroundResource(R.drawable.ic_favorite_off);
        }
        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] str = {String.valueOf(iIDComp)};
                Observable.from(str)
                        .subscribe(new Observer<String>() {
                            @Override
                            public void onNext(String s) {
                                DatabaseHelper dbHelper = new DatabaseHelper(ComponentActivity.this);
                                SQLiteDatabase db = dbHelper.getWritableDatabase();

                                int fvr = dbHelper.getIsFavoriteCmp(s);

                                ContentValues updatedValues = new ContentValues();
                                if (fvr == 0) {
                                    updatedValues.put("favorite", 1);
                                    iFavorite = 1;
                                } else {
                                    updatedValues.put("favorite", 0);
                                    iFavorite = 0;
                                }
                                String where = "_id=?";
                                String[] whereArgs = {s};

                                db.update("COMPONENTS", updatedValues, where, whereArgs);
                            }
                            @Override
                            public void onCompleted() {
                                if (iFavorite > 0) {
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
                        });
            }
        });

        // Рекламма, без нее ни как ) --------------------------------------------------------------
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
            }
        });
    }

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

    class MyTask extends AsyncTask<String, Integer, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... urls) {
            try {
                int cnt = 0;
                for (String url : urls) {
                    // загружаем файл
                    downloadFile(url);
                    // выводим промежуточные результаты
                    publishProgress(++cnt);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

        private void downloadFile(String str_url) throws InterruptedException {
            URL url;
            HttpURLConnection urlConnection;
            InputStream inputStream;
            int totalSize;
            int downloadedSize;
            byte[] buffer;
            int bufferLength;

            File file = null;
            FileOutputStream fos = null;

            try {
                url = new URL(str_url);
                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(true);
                urlConnection.connect();

                file = File.createTempFile("Mustachify", "download");
                fos = new FileOutputStream(file);
                inputStream = urlConnection.getInputStream();

                totalSize = urlConnection.getContentLength();
                downloadedSize = 0;

                buffer = new byte[1024];
                bufferLength = 0;

                // читаем со входа и пишем в выход,
                // с каждой итерацией публикуем прогресс
                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    fos.write(buffer, 0, bufferLength);
                    downloadedSize += bufferLength;
                    publishProgress(downloadedSize, totalSize);
                }

                fos.close();
                inputStream.close();

                openPDF(file.toURI().toString());

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void openPDF(String doc){
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("path-to-document"));
            intent.setDataAndType(Uri.parse(doc), "application/pdf");
            PackageManager pm = getPackageManager();
            List<ResolveInfo> activities = pm.queryIntentActivities(intent, 0);
            if (activities.size() > 0) {
                startActivity(intent);
            } else {
                // Do something else here. Maybe pop up a Dialog or Toast
            }
        }
    }
}