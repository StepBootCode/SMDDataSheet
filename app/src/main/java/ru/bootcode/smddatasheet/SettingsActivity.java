package ru.bootcode.smddatasheet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import java.util.Collections;

import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.data.ExFilePickerResult;

public class SettingsActivity extends AppCompatActivity {
    final Context context = this;
    private static final int EX_FILE_PICKER_RESULT = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }

        @Override
        public void onResume() {
            super.onResume();

            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();

            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        public static final String KEY_PREF_CACHE = "do_cash";
        public static final String KEY_PREF_SAVE = "savepath";

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            // Обработка нажатия Switch Кеширование, если кеширование разрешаем,
            // то просто делаем активным поле Путь сохранения, чтобы пользователь мог его изменять
            if (key.equals(KEY_PREF_CACHE)) {
                Preference prefCache = findPreference(KEY_PREF_CACHE);
                Boolean prefCacheValue = ((SwitchPreference) prefCache).isChecked();
                Preference prefSaveValue = findPreference(KEY_PREF_SAVE);
                prefSaveValue.setEnabled(prefCacheValue);

                if (prefSaveValue.isEnabled()) {
                    // сохраним старый путь


                    // диалог выбора файла

                    //Intent intent = new Intent(getContext(), FileDialogActivity.class);
                    //startActivityForResult(intent, 1);

                    FragmentActivity fra = this.getActivity();
                    ExFilePicker exFilePicker = new ExFilePicker();
                    exFilePicker.setChoiceType(ExFilePicker.ChoiceType.DIRECTORIES);
                    exFilePicker.start(this.getActivity(), EX_FILE_PICKER_RESULT);

                    // проверяем изменения



                    // копируем старый кеш в новое место

                }
            }
            // Если нажали путь сохранения и этот пункт активен, тогда предложим пользователю
            // Выбрать папку сохранения и если путь изменился и есть старые файлы, то перенесем
            // их в новый кеш
            if (key.equals(KEY_PREF_SAVE)) {
                Preference prefSaveValue = findPreference(KEY_PREF_SAVE);
                if (prefSaveValue.isEnabled()) {
                    // сохраним старый путь


                    // диалог выбора файла

                    Intent intent = new Intent(getContext(), FileDialogActivity.class);
                    startActivityForResult(intent, 1);

                    // проверяем изменения

                    CharSequence mCurrentValue = new String("/eeeee");
                    prefSaveValue.setSummary(mCurrentValue);
                    prefSaveValue.setEnabled(true);

                    // копируем старый кеш в новое место

                }
            }
        }

       @Override
       public void onActivityResult(int requestCode, int resultCode, Intent data) {
           if (requestCode == EX_FILE_PICKER_RESULT) {
               ExFilePickerResult result = ExFilePickerResult.getFromIntent(data);
               if (result != null && result.getCount() > 0) {
                   Preference prefSaveValue = findPreference(KEY_PREF_SAVE);
                   String str = result.getPath()+result.getNames().get(0);
                   CharSequence mCurrentValue = new String(str);
                   prefSaveValue.setSummary(mCurrentValue);
                   prefSaveValue.setEnabled(true);
               }
           }
       }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

}