package ru.bootcode.smddatasheet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.data.ExFilePickerResult;
import rx.Observable;
import rx.Observer;
import rx.Single;
import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AddSMDActivity extends AppCompatActivity {
    final Context context = this;
    private static final int EX_FILE_PICKER_RESULT = 1;
    private EditText etName;
    private EditText etLabel;
    private EditText etBody;
    private EditText etFunc;
    private EditText etProd;
    private TextView tvPDF;
    private File pdfFile = null;
    int iIDComp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_smd);
        etName = (EditText) findViewById(R.id.etName);
        etLabel = (EditText) findViewById(R.id.etLabel);
        etBody = (EditText) findViewById(R.id.etBody);
        etFunc = (EditText) findViewById(R.id.etFunc);
        etProd = (EditText) findViewById(R.id.etProd);
        tvPDF = (TextView) findViewById(R.id.tvPDF);

        Intent intent = getIntent();
        iIDComp     = intent.getIntExtra("id",0);
        if (iIDComp > 0) {
            final DatabaseHelper dbHelper = new DatabaseHelper(AddSMDActivity.this);
            dbHelper.getReadableDatabase();

            Single.just( dbHelper.getComponent(String.valueOf(iIDComp)))//.fromCallable(callable)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleSubscriber<Component>() {
                        @Override
                        public void onSuccess(Component value) {
                            etName.setText(value.getName());
                            etBody.setText(value.getBody());
                            etLabel.setText(value.getLabel());
                            etFunc.setText(value.getFunc());
                            etProd.setText(value.getProd());
                            String sPDF = value.getDatasheet();
                            tvPDF.setText(sPDF);
                            pdfFile = new File(sPDF);
                            if (!pdfFile.isFile()) {
                                pdfFile = null;
                            }
                        }
                        @Override
                        public void onError(Throwable error) { }
                    });
        }

        Button btSelectPDF = (Button) findViewById(R.id.btSelectPDF);
        btSelectPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ExFilePicker exFilePicker = new ExFilePicker();
                exFilePicker.setChoiceType(ExFilePicker.ChoiceType.FILES);
                exFilePicker.start(AddSMDActivity.this, EX_FILE_PICKER_RESULT);
            }
        });

        Button btSave = (Button) findViewById(R.id.btSave);
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (testText())
                {
                    Intent result = new Intent("ru.bootcode.smddatasheet");//
                    Bundle extras = new Bundle();
                    extras.putInt("id", iIDComp);
                    extras.putString("name", etName.getText().toString());
                    extras.putString("label", etLabel.getText().toString());
                    extras.putString("body", etBody.getText().toString());
                    extras.putString("func", etFunc.getText().toString());
                    extras.putString("prod", etProd.getText().toString());
                    extras.putString("pdf", pdfFile.getAbsolutePath());
                    result.putExtras(extras);
                    setResult(RESULT_OK, result);
                    finish();
                }
            }
        });
    }

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
        if (requestCode == EX_FILE_PICKER_RESULT) {
            ExFilePickerResult result = ExFilePickerResult.getFromIntent(data);
            if (result != null && result.getCount() > 0) {
                String str = result.getPath() + result.getNames().get(0);
                if (str.contains(".pdf")) {
                    pdfFile = new File(str);
                    if (!pdfFile.isFile()) {
                        pdfFile = null;
                    } else {
                        tvPDF.setText(str);// = (TextView) findViewById(R.id.tvPDF);
                    }
                } else {
                    Utils.showToast(AddSMDActivity.this,R.string.toast_not_pdf_file);
                }
            }
        }
    }
}
