package ru.bootcode.smddatasheet;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.Observer;
import rx.Single;
import rx.SingleSubscriber;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class EditSMDActivity extends AppCompatActivity  {
    private static final int EX_FILE_PICKER_RESULT = 1;
    static final int UPDATE_SMD_REQUEST = 3;
    final Context context = this;
    ListView lvDB;
    long selectedID=0;
    String keySavePath;
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

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        keySavePath  = sp.getString("keySavePath", Utils.getDefaultCacheDir());

        mDBHelper = new DatabaseHelper(this);

        btnEdit = findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedID>0) {
                    Intent intent = new Intent(EditSMDActivity.this, AddSMDActivity.class);
                    Bundle extras = new Bundle();
                    extras.putInt("id", (int) selectedID);
                    intent.putExtras(extras);
                    startActivityForResult(intent, UPDATE_SMD_REQUEST);
                }
            }
        });


        btnFavorite = findViewById(R.id.btnFav);
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
                                    DatabaseHelper dbHelper = new DatabaseHelper(EditSMDActivity.this);
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
                                        Utils.showToast(EditSMDActivity.this, R.string.toast_success_add_favorites);
                                    } else {
                                        btnFavorite.setBackgroundResource(R.drawable.ic_favorite_off);
                                        Utils.showToast(EditSMDActivity.this, R.string.toast_success_remove_favorites);
                                    }
                                }

                                @Override
                                public void onError(Throwable e) {

                                }
                            });
                }
            }
        });

        btnDel = findViewById(R.id.btnDel);
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

        // Выводим список компонентов --------------------------------------------------------------
        lvDB = findViewById(R.id.lvDB);
        Observable.just(mDBHelper.getListLocals())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Component>>() {
                    @Override
                    public void onCompleted() {

                    }
                    @Override
                    public void onError(Throwable error) {

                    }
                    @Override
                    public void onNext(List<Component> components) {
                        adapter = new ListComponentAdapter(EditSMDActivity.this, components);
                        lvDB.setAdapter(adapter);
                    }
                });

        // Обработка нажатия элемента списка -------------------------------------------------------
        lvDB.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.switchSelection(position);
                selectedID = id;

                DatabaseHelper dbHelper = new DatabaseHelper(EditSMDActivity.this);
                dbHelper.getWritableDatabase();
                Observable.just(dbHelper.getIsFavoriteCmp(String.valueOf(id)))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Integer>() {
                            @Override
                            public void onNext(Integer components) {
                                if (components == 0) {
                                    btnFavorite.setBackgroundResource(R.drawable.ic_favorite_off);
                                } else {
                                    btnFavorite.setBackgroundResource(R.drawable.ic_favorite_on);
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
    }
    public void clickDelete(String[] str){
        Observable.from(str)
                .subscribe(new Observer<String>() {
                    @Override
                    public void onNext(String s) {
                        String where = "_id=?";
                        String[] whereArgs = {s};
                        SQLiteDatabase db = mDBHelper.getWritableDatabase();
                        db.delete("COMPONENTS", where, whereArgs);
                    }
                    @Override
                    public void onCompleted() {
                        List<Component> value =  mDBHelper.getListLocals();
                        adapter = new ListComponentAdapter(EditSMDActivity.this, value);
                        lvDB.setAdapter(adapter);
                    }
                    @Override
                    public void onError(Throwable e) { }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_SMD_REQUEST) {
            if (resultCode == RESULT_OK) {
                ContentValues newValues = new ContentValues();
                newValues.put("id",         data.getIntExtra("id",0));
                newValues.put("name",       data.getStringExtra("name"));
                newValues.put("label",      data.getStringExtra("label"));
                newValues.put("body",       data.getStringExtra("body"));
                newValues.put("func",       data.getStringExtra("func"));
                newValues.put("datasheet",  data.getStringExtra("pdfname"));
                newValues.put("prod",       data.getStringExtra("prod"));
                newValues.put("favorite",   1);
                newValues.put("islocal",    1);

                String dst = keySavePath+"/"+data.getStringExtra("pdfname");
                if (!data.getStringExtra("pdf").equals(dst)) {
                    Observable.just(Utils.copyFile(data.getStringExtra("pdf"), dst))
                            .subscribe(new Observer<Boolean>() {
                                @Override
                                public void onCompleted() {
                                    Utils.showToast(context, "File is cached");
                                }

                                @Override
                                public void onError(Throwable e) {

                                }

                                @Override
                                public void onNext(Boolean aBoolean) {

                                }
                            });
                }
                Observable.from(new ContentValues[]{newValues})
                        .subscribe(new Observer<ContentValues>() {
                            @Override
                            public void onNext(ContentValues s) {
                                int _id = s.getAsInteger("id");
                                if (_id >0) {
                                    s.remove("id");
                                    String where = "_id=?";
                                    String[] whereArgs = {String.valueOf(_id)};

                                    DatabaseHelper dbHelper = new DatabaseHelper(EditSMDActivity.this);
                                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                                    db.update("COMPONENTS", s, where, whereArgs);
                                }
                            }
                            @Override
                            public void onCompleted() {
                                List<Component> value =  mDBHelper.getListLocals();
                                adapter = new ListComponentAdapter(EditSMDActivity.this, value);
                                lvDB.setAdapter(adapter);
                            }
                            @Override
                            public void onError(Throwable e) { }
                        });
            }
        }
    }
}
