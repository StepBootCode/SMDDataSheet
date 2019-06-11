package ru.bootcode.smddatasheet;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class FileDialogActivity extends AppCompatActivity {
    ListView list_dir;
    TextView textPath;
    Context _context;
    int select_id_list = -1;
    String path = "/";

    ArrayList<String> ArrayDir = new ArrayList<String>();
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filedialog);

        list_dir = (ListView) findViewById(R.id.list_dir);
        textPath = (TextView) findViewById(R.id.textPath);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ArrayDir);
        list_dir.setAdapter(adapter);

        update_list_dir();

        list_dir.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                select_id_list = (int)id;
                update_list_dir();
            }
        });
    }

    public void onClickBack(View view)
    {
        path = (new File(path)).getParent();
        update_list_dir();
    }

    public void onClickGo(View view)
    {
        Intent intent = new Intent();
        intent.putExtra("url", path);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void update_list_dir(){
        if(select_id_list != -1) path = path + ArrayDir.get(select_id_list) + "/";
        select_id_list = -1;
        ArrayDir.clear();
        File[] files = new File(path).listFiles();
        for ( File aFile : files ){
            if ( aFile.isDirectory() ) {
                if(dir_opened(aFile.getPath())){
                    ArrayDir.add(aFile.getName());
                }
            }
        }

        adapter.notifyDataSetChanged();
        textPath.setText(path);
    }

    private boolean dir_opened(String url){
        try{
            File[] files = new File(url).listFiles();
            for ( @SuppressWarnings("unused") File aFile : files ){
            }
            return true;
        } catch(Exception e){
            return false;
        }
    }
}
