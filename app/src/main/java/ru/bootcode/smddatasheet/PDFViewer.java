package ru.bootcode.smddatasheet;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.joanzapata.pdfview.PDFView;

import java.io.File;

public class PDFViewer extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdfviewer);

        PDFView pdfView = findViewById(R.id.pdfview);
        Intent intent = getIntent();
        String filename        = intent.getStringExtra("filename");


        pdfView.fromFile(new File(filename))
                .load();
    }

}
