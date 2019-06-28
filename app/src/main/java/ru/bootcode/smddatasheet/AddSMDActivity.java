package ru.bootcode.smddatasheet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.Preference;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.File;

import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.data.ExFilePickerResult;

public class AddSMDActivity extends AppCompatActivity {
    final Context context = this;
    private static final int EX_FILE_PICKER_RESULT = 1;
    private EditText etName;
    private EditText etLabel;
    private EditText etBody;
    private EditText etFunc;
    private File pdfFile = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_smd);
        etName = (EditText) findViewById(R.id.etName);
        etLabel = (EditText) findViewById(R.id.etLabel);
        etBody = (EditText) findViewById(R.id.etBody);
        etFunc = (EditText) findViewById(R.id.etFunc);


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
              //  if (testText())
                {
                    Intent result = new Intent("ru.bootcode.smddatasheet");
                    result.putExtra("name", etName.getText());
                    result.putExtra("label", etLabel.getText());
                    result.putExtra("body", etBody.getText());
                    result.putExtra("func", etFunc.getText());
                    result.putExtra("pdf", pdfFile.getAbsolutePath());
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
                    }
                } else {
                    Utils.showToast(AddSMDActivity.this,R.string.toast_not_pdf_file);
                }
            }
        }
    }
}
