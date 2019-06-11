package ru.bootcode.smddatasheet;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.List;

public class NewSMDActivity extends AppCompatActivity  {
    ListView lvDB;
    private DatabaseHelper mDBHelper;
    private ListComponentAdapter adapter;
    private List<Component> mComponentList;
    private Component SelectedComponent;

    Button btnAdd;
    Button btnEdit;
    Button btnDel;
    Button btnFav;
    Button btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newsmd);

        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickAdd();
            }
        });

        btnEdit = (Button) findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        btnDel = (Button) findViewById(R.id.btnDel);
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        btnFav = (Button) findViewById(R.id.btnFav);
        btnFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        btnClose = (Button) findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean is_ru_local = sp.getBoolean("ru_switch", false);
        mDBHelper = new DatabaseHelper(this);
        mDBHelper.setRuLocal(is_ru_local);

        lvDB = (ListView) findViewById(R.id.lvDB);
        mComponentList = mDBHelper.getListLocals();
        adapter = new ListComponentAdapter(this, mComponentList);

        lvDB.setAdapter(adapter);
        // Обработка нажатия элемента списка -------------------------------------------------------
        lvDB.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(NewSMDActivity.this, ComponentActivity.class);
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
    }

    public void clickAdd(){
        showFileChooser();

        //Получаем вид с файла dialog_find.xml, который применим для диалогового окна:
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.dialog_add_smd, null);

        //Создаем AlertDialog
        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(this);

        //Настраиваем prompt.xml для нашего AlertDialog:
        mDialogBuilder.setView(promptsView);

        //Настраиваем отображение поля для ввода текста в открытом диалоге:
        //final EditText userInput = (EditText) promptsView.findViewById(R.id.etMarker);

        //Настраиваем сообщение в диалоговом окне:
        mDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //Вводим текст и отображаем в строке ввода на основном экране:
                                //String txt = userInput.getText().toString();

                            }
                        })
                .setNegativeButton("Отмена",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });
        //Создаем AlertDialog и отображаем его:
        AlertDialog alertDialog = mDialogBuilder.create();
        alertDialog.show();
    }

    private static final int FILE_SELECT_CODE = 0;

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select PDF File"), FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            // Нет файлового менеджера, откроем ввод вручную
        }
    }

    public void clickEdit(){

    }

    public void clickDel(){

    }

    public void clickFav(){

    }

    public void clickClose(){

    }
}
