package ru.bootcode.smddatasheet;

import android.content.Intent;
import android.content.SharedPreferences;

import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.data.ExFilePickerResult;

public class SettingsActivity extends AppCompatActivity {

    public static final String KEY_PREF_CACHE   = "keyCache";   // Ключ к настройке кеша
    public static final String KEY_PREF_SAVE    = "keySavePath";// Ключ к настройке пути сохранения

    private static final int EX_FILE_PICKER_RESULT = 0;         // Обраточка от диалога выбора файлов

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

    public static class SettingsFragment extends PreferenceFragmentCompat
                                        implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            // Обрабатываем наш класс (по сути тот же editText но отличается тем, что при
            // событии нажатия не показывает диалог ввода
            ExEditTextPreference editTextPreference = getPreferenceManager().findPreference(KEY_PREF_SAVE);
            if (editTextPreference != null) {
                editTextPreference.setSummary(getPreferenceManager()
                        .getSharedPreferences()
                        .getString("keySavePath", Utils.getDefaultCacheDir()));
                editTextPreference.setOnPreferenceClickListener(
                        new ExEditTextPreference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            ExFilePicker exFilePicker = new ExFilePicker();
                            exFilePicker.setChoiceType(ExFilePicker.ChoiceType.DIRECTORIES);
                            if (SettingsFragment.this.getActivity() != null)
                                exFilePicker.start(SettingsFragment.this.getActivity(),
                                        EX_FILE_PICKER_RESULT);
                            return false;
                        }
                });
            }

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

       @Override
       public void onActivityResult(int requestCode, int resultCode, Intent data) {
           if (requestCode == EX_FILE_PICKER_RESULT) {
               // Обработка результата от выбокра каталога кеша, т.к. чудом может вернуться
               // множественный выбор то берем 1 в массиве каталог
               // Добавил тестирование каталога на возможность записи, и если гуд то используем
               ExFilePickerResult result = ExFilePickerResult.getFromIntent(data);
               if (result != null && result.getCount() > 0) {
                   Preference prefSaveValue = findPreference(KEY_PREF_SAVE);
                   String filePath = result.getPath()+result.getNames().get(0);
                   if (Utils.testDirOnWrite(filePath)) {
                       ExEditTextPreference editPref = (ExEditTextPreference) prefSaveValue;
                       if (editPref != null) {
                           editPref.setText(filePath);
                           editPref.setSummary(result.getPath() + result.getNames().get(0));
                       }
                   }
               }
           }
       }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

            if (key.equals(KEY_PREF_CACHE)) {
                // Тут быстренько проверим доступен ли нам PDF Reader и если его нет то нефиг ковырять
                // настройки с кешем (т.е. с локальными файлами), делаем их недоступными
                Uri uri = Uri.parse("help.pdf");
                Intent intentUrl = new Intent(Intent.ACTION_VIEW);
                intentUrl.setDataAndType(uri, "application/pdf");
                intentUrl.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                if (getActivity() ==null) { return;}
                if (intentUrl.resolveActivity(getActivity().getPackageManager()) == null) {
                    SwitchPreference prefCache = getPreferenceManager().findPreference(KEY_PREF_CACHE);
                    if (prefCache != null) {
                        Utils.showToast(getActivity(),R.string.toast_pdf_not_install);
                        prefCache.setEnabled(false);
                        prefCache.setChecked(false);
                    }
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Тут ловим обраточку от диалога выбора каталога кеша, и так как мы работаем с фрагментами
        // нужно послать onActivityResult всем фрагментам (доступ кним через Менеджер фрагментов)
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

}