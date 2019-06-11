package ru.bootcode.smddatasheet;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import static ru.bootcode.smddatasheet.Utils.showToast;


public class ComponentActivity extends Activity {

    final Context context = this;

    Boolean do_cash;                    // Тру - нужно кешировать PDF файлы на устройстве
    int iFavorite;                      //

    private TextView tvCode;
    private TextView tvName;
    private TextView tvNote;
    private TextView tvMarker;
    private String tvDatasheet;
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
        //is_ru_local = sp.getBoolean("ru_switch", false);
        do_cash = sp.getBoolean("do_cash", false);
        String dir_cash = sp.getString("dir_cash","");

        // Получаем переданные из MainActivity параметры о SMD компоненте --------------------------
        Intent intent = getIntent();
        //Boolean useCash = intent.getBooleanExtra("cash",false);
        String sName = intent.getStringExtra("name");
        String sCode = intent.getStringExtra("code");
        String sMarker = intent.getStringExtra("marker");
        String sNote = intent.getStringExtra("note");
        String sProd = intent.getStringExtra("prod");
        String sDatasheet = intent.getStringExtra("datasheet");
        int iFavorite = intent.getIntExtra("favorite",0);
        int iIsLocal = intent.getIntExtra("islocal",0);

        // Выводим информацию о компоненте ---------------------------------------------------------
        tvNote = (TextView) findViewById(R.id.tvNote);
        tvNote.setText(sNote);

        tvCode = (TextView) findViewById(R.id.tvCode);
        tvCode.setText(sCode);

        tvName = (TextView) findViewById(R.id.tvName);
        tvName.setText(sName);

        tvMarker = (TextView) findViewById(R.id.tvMarker);
        tvMarker.setText(sMarker);

        sCode =sCode.replace("-","_").toLowerCase();
        ImageView mImageView = (ImageView) findViewById(R.id.ivCode);
        //Из ресурсов приложения (файл из res/drawable/img3.jpg)
        int id = getResources().getIdentifier("ru.bootcode.smddatasheet:drawable/big_" + sCode, null, null);
        if (id != 0) {
            mImageView.setImageResource(id);
        } else {
            id = getResources().getIdentifier("ru.bootcode.smddatasheet:drawable/big_def", null, null);
            mImageView.setImageResource(id);
        }

        tvDatasheet = LINK+sDatasheet.replace("~","0").replace("@","1").replace("#","2")+".pdf";

        // создаем обработчик нажатия кнопки загрузки PDF ------------------------------------------
        btnDataSheet = (Button) findViewById(R.id.btnDataSheet);
        btnDataSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean nowLoad = true;
                if (do_cash){
                    // Проверим наш кеш если в нем есть файл, то открывать будем его
                    // Иначе загрузим его и потом уже откроем
                    try
                    {
                        Uri uri = Uri.parse(tvDatasheet);
                        Intent intentUrl = new Intent(Intent.ACTION_VIEW);
                        intentUrl.setDataAndType(uri, "application/pdf");
                        intentUrl.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        if (intentUrl.resolveActivity(getPackageManager()) != null) {
                            startActivity(intentUrl);
                        } else {
                            showToast(context, R.string.toast_pdf_not_install);

                        }
                        //ViewerActivity.startActivity(intentUrl);
                    }
                    catch (ActivityNotFoundException e)
                    {
                        //Toast.makeText(mActivity, "No PDF Viewer Installed", Toast.LENGTH_LONG).show();
                    }
                }
                if (nowLoad) {
                    String format = "https://drive.google.com/viewerng/viewer?embedded=true&url=%s";
                    String fullPath = String.format(Locale.ENGLISH, format, tvDatasheet);
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fullPath));
                    startActivity(browserIntent);
                }
            }
        });

        // Обработка кнопки Избранное --------------------------------------------------------------
        btnFavorite = (Button) findViewById(R.id.btnFavorite);
        if (iFavorite > 0) {
            // btnFavorite.setBackground(getResources().getDrawable());
        }
        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });



        // Рекламма, без нее ни как ) --------------------------------------------------------------
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int errorCode) {
                //ERROR_CODE_INTERNAL_ERROR - Something happened internally; for instance, an invalid response was received from the ad server.
                //ERROR_CODE_INVALID_REQUEST - The ad request was invalid; for instance, the ad unit ID was incorrect.
                //ERROR_CODE_NETWORK_ERROR - The ad request was unsuccessful due to network connectivity.
                //ERROR_CODE_NO_FILL - The ad request was successful, but no ad was returned due to lack of ad inventory.
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