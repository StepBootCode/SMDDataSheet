package ru.bootcode.smddatasheet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.List;
import java.util.concurrent.Callable;

import ru.bartwell.exfilepicker.ExFilePicker;
import rx.Observable;
import rx.Observer;
import rx.Single;
import rx.SingleSubscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class NewSMDActivity extends AppCompatActivity  {
    private static final int EX_FILE_PICKER_RESULT = 1;
    final Context context = this;
    ListView lvDB;
    long selectedID=0;
    private DatabaseHelper mDBHelper;
    private ListComponentAdapter adapter;
    private List<Component> mComponentList;
    private Component SelectedComponent;

    Button btnAdd;
    Button btnEdit;
    Button btnDel;
    Button btnFavorite;
    Button btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_smd);

        mDBHelper = new DatabaseHelper(this);

        btnEdit = (Button) findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        btnFavorite = (Button) findViewById(R.id.btnFav);
        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedID>0) {
                    final int[] iFavorite = new int[1];
                    String[] str = {String.valueOf(selectedID)};
                    Observable.from(str)
                            .subscribe(new Observer<String>() {
                                @Override
                                public void onNext(String s) {
                                    DatabaseHelper dbHelper = new DatabaseHelper(NewSMDActivity.this);
                                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                                    int fvr = dbHelper.getIsFavoriteCmp(s);

                                    ContentValues updatedValues = new ContentValues();
                                    if (fvr == 0) {
                                        updatedValues.put("favorite", 1);
                                        iFavorite[0] = 1;
                                    } else {
                                        updatedValues.put("favorite", 0);
                                        iFavorite[0] = 0;
                                    }
                                    String where = "_id=?";
                                    String[] whereArgs = {s};

                                    db.update("COMPONENTS", updatedValues, where, whereArgs);
                                }

                                @Override
                                public void onCompleted() {
                                    if (iFavorite[0] > 0) {
                                        btnFavorite.setBackgroundResource(R.drawable.ic_favorite_on);
                                        Utils.showToast(NewSMDActivity.this, R.string.toast_success_add_favorites);
                                    } else {
                                        btnFavorite.setBackgroundResource(R.drawable.ic_favorite_off);
                                        Utils.showToast(NewSMDActivity.this, R.string.toast_success_remove_favorites);
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {

                                }
                            });
                }
            }
        });


        lvDB = (ListView) findViewById(R.id.lvDB);
        //mComponentList = mDBHelper.getListLocals();
        Callable<List<Component>> callable = new Callable<List<Component>>() {
            @Override
            public List<Component> call() throws Exception {
                return mDBHelper.getListLocals();
            }
        };
        Single.fromCallable(callable)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleSubscriber<List<Component>>() {
                    @Override
                    public void onSuccess(List<Component> value) {

                        adapter = new ListComponentAdapter(NewSMDActivity.this, value);
                        lvDB.setAdapter(adapter);
                    }

                    @Override
                    public void onError(Throwable error) {

                    }
                });

        // Обработка нажатия элемента списка -------------------------------------------------------
        lvDB.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedID = id;
                String[] str = {String.valueOf(id)};
                Observable.from(str)
                        .subscribe(new Observer<String>() {
                            @Override
                            public void onNext(String s) {
                                DatabaseHelper dbHelper = new DatabaseHelper(NewSMDActivity.this);
                                SQLiteDatabase db = dbHelper.getWritableDatabase();

                                int fvr = dbHelper.getIsFavoriteCmp(s);

                                if (fvr == 0) {
                                    btnFavorite.setBackgroundResource(R.drawable.ic_favorite_on);
                                } else {
                                    btnFavorite.setBackgroundResource(R.drawable.ic_favorite_off);
                                }
                            }
                            @Override
                            public void onCompleted() {

                            }
                            @Override
                            public void onError(Throwable e) {

                            }
                        });
            }
        });
        btnDel = (Button) findViewById(R.id.btnDel);
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedID>0) {
                    AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(context);
                    //Настраиваем сообщение в диалоговом окне:
                    Component selectedComponent = mDBHelper.getComponent(String.valueOf(selectedID));
                    //intent.putExtra("id", String.valueOf(id));
                    mDialogBuilder.setTitle("Удалить "+selectedComponent.getLabel());
                    mDialogBuilder.setCancelable(false);
                    mDialogBuilder.setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    clickDelete(new String[]{String.valueOf(selectedID)});
                                }
                            });
                    mDialogBuilder.setNegativeButton("Отмена",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    //Создаем AlertDialog и отображаем его:
                    AlertDialog alertDialog = mDialogBuilder.create();
                    alertDialog.show();
                }
            }
        });
    }
    public void clickDelete(String[] str){
        Observable.from(str)
                .subscribe(new Observer<String>() {
                                @Override
                                public void onNext(String s) {
                                    String where = "ID=?";
                                    String whereArgs[] = {s};
                                    SQLiteDatabase db = mDBHelper.getWritableDatabase();
                                    db.delete("COMPONENTS", where, whereArgs);
                                }
                               @Override
                               public void onCompleted() {
                                   List<Component> value =  mDBHelper.getListLocals();
                                   adapter = new ListComponentAdapter(NewSMDActivity.this, value);
                                   lvDB.setAdapter(adapter);
                               }
                               @Override
                               public void onError(Throwable e) {

                               }
                           });
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
