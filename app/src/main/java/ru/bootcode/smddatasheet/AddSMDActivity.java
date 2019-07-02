package ru.bootcode.smddatasheet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;

import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.data.ExFilePickerResult;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AddSMDActivity extends AppCompatActivity {
    final Context context = this;
    private static final int EX_FILE_PICKER_RESULT = 1;     // Обраточка для диалога выбора PDF

    int iIDComp;                                            // идентиф. текущего компонента
    private File pdfFile = null;                            // ссылка на PDF файл

    private EditText etName;
    private EditText etLabel;
    private EditText etBody;
    private EditText etFunc;
    private EditText etProd;
    private TextView tvPDF;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_smd);
        etName  = findViewById(R.id.etName);
        etLabel = findViewById(R.id.etLabel);
        etBody  = findViewById(R.id.etBody);
        etFunc  = findViewById(R.id.etFunc);
        etProd  = findViewById(R.id.etProd);
        tvPDF   = findViewById(R.id.tvPDF);

        // Получаем SharedPreferences (Сохраненнве настройки приложения) ---------------------------
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        final String keySavePath  = sp.getString("keySavePath", Utils.getDefaultCacheDir());

        // Запроим интент, и если нам пришел ид компонента, то значит мы не добавляем, а -----------
        // редактируем компонент, и значит нужно заполнить наши поля экране
        Intent intent = getIntent();
        iIDComp     = intent.getIntExtra("id",0);
        if (iIDComp > 0) {
            final DatabaseHelper dbHelper = new DatabaseHelper(AddSMDActivity.this);
            dbHelper.getReadableDatabase();

            Observable.just( dbHelper.getComponent(String.valueOf(iIDComp)))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Component>() {
                        @Override
                        public void onNext(Component value) {
                            etName.setText(value.getName());
                            etBody.setText(value.getBody());
                            etLabel.setText(value.getLabel());
                            etFunc.setText(value.getFunc());
                            etProd.setText(value.getProd());
                            String sPDF = keySavePath +"/"+value.getDatasheet();
                            tvPDF.setText(sPDF);
                            pdfFile = new File(sPDF);
                        }
                        @Override
                        public void onCompleted() {
                            if (!pdfFile.isFile()) {
                                pdfFile = null;
                                tvPDF.setText(R.string.toast_not_pdf_file);
                            }
                        }
                        @Override
                        public void onError(Throwable error) {
                            pdfFile = null;
                            tvPDF.setText(R.string.toast_not_pdf_file);
                        }

                    });
        }

        // Диалог выбора файла ---------------------------------------------------------------------
        Button btSelectPDF = findViewById(R.id.btSelectPDF);
        btSelectPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExFilePicker exFilePicker = new ExFilePicker();
                exFilePicker.setChoiceType(ExFilePicker.ChoiceType.FILES);
                exFilePicker.start(AddSMDActivity.this, EX_FILE_PICKER_RESULT);
            }
        });

        // Сохраняем данные в интент и возвращаем их -----------------------------------------------
        Button btSave = findViewById(R.id.btSave);
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (testText())
                {
                    String sName = pdfFile.getName();
                    sName = sName.replace("~","0").replace("@","1").replace("#","2");

                    // !!! Bundle extras нужен, не сокращать до intent.putExtras(id)...
                    Intent result = new Intent("ru.bootcode.smddatasheet");//
                    Bundle extras = new Bundle();
                    extras.putInt("id", iIDComp);
                    extras.putString("name", etName.getText().toString());
                    extras.putString("label", etLabel.getText().toString());
                    extras.putString("body", etBody.getText().toString());
                    extras.putString("func", etFunc.getText().toString());
                    extras.putString("prod", etProd.getText().toString());
                    extras.putString("pdf", pdfFile.getAbsolutePath());
                    extras.putString("pdfname", sName);
                    result.putExtras(extras);
                    setResult(RESULT_OK, result);
                    finish();
                }
            }
        });
    }

    // Приметивный тест на заполнение полей
    boolean testText(){
        if (etName.getText().length() < 1) {
            Utils.showToast(context,R.string.toast_not_filled_name);
            return false;
        }
        if (etLabel.getText().length() < 1) {
            Utils.showToast(context,R.string.toast_not_filled_label);
            return false;
        }
        if (etBody.getText().length() < 1) {
            Utils.showToast(context,R.string.toast_not_filled_body);
            return false;
        }
        if (etFunc.getText().length() < 1) {
            Utils.showToast(context,R.string.toast_not_filled_func);
            return false;
        }
        if (pdfFile == null) {
            Utils.showToast(context,R.string.toast_not_pdf_file);
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Обрабатываем диалог выбора PDF файла
        if (requestCode == EX_FILE_PICKER_RESULT) {
            ExFilePickerResult result = ExFilePickerResult.getFromIntent(data);
            if (result != null && result.getCount() > 0) {
                String str = result.getPath() + result.getNames().get(0);
                if (str.contains(".pdf")) {
                    pdfFile = new File(str);
                    if (!pdfFile.isFile()) {
                        pdfFile = null;
                        tvPDF.setText(R.string.toast_not_pdf_file);
                    } else {
                        tvPDF.setText(str);
                    }
                } else {
                    Utils.showToast(AddSMDActivity.this,R.string.toast_not_pdf_file);
                }
            }
        }
    }
}
