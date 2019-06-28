package ru.bootcode.smddatasheet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;
import androidx.recyclerview.widget.ListAdapter;

import java.util.Collections;
import java.util.Locale;

import ru.bartwell.exfilepicker.ExFilePicker;
import ru.bartwell.exfilepicker.data.ExFilePickerResult;

import static ru.bootcode.smddatasheet.Utils.showToast;

public class SettingsActivity extends AppCompatActivity {
    final Context context = this;

    public static final String KEY_PREF_CACHE   = "keyCache";
    public static final String KEY_PREF_SAVE    = "keySavePath";

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

            ExEditTextPreference editTextPreference = getPreferenceManager().findPreference(KEY_PREF_SAVE);
            editTextPreference.setSummary(editTextPreference.getText());
            editTextPreference.setOnPreferenceClickListener(new ExEditTextPreference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ExFilePicker exFilePicker = new ExFilePicker();
                    exFilePicker.setChoiceType(ExFilePicker.ChoiceType.DIRECTORIES);
                    if (SettingsFragment.this.getActivity() != null)
                        exFilePicker.start(SettingsFragment.this.getActivity(), EX_FILE_PICKER_RESULT);
                    return false;
                }
            });

            Uri uri = Uri.parse("help.pdf");
            Intent intentUrl = new Intent(Intent.ACTION_VIEW);
            intentUrl.setDataAndType(uri, "application/pdf");
            intentUrl.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            if (intentUrl.resolveActivity(getActivity().getPackageManager()) == null) {
                SwitchPreference prefCache = getPreferenceManager().findPreference(KEY_PREF_CACHE);
                prefCache.setEnabled(false);
                prefCache.setChecked(false);
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
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            // Обработка нажатия Switch Кеширование, если кеширование разрешаем,
            // то просто делаем активным поле Путь сохранения, чтобы пользователь мог его изменять
            //if (key.equals(KEY_PREF_CACHE)) {
            //    SwitchPreference prefCache = findPreference(KEY_PREF_CACHE);
            //    if (prefCache == null) { return;}
            //}
        }

       @Override
       public void onActivityResult(int requestCode, int resultCode, Intent data) {
           if (requestCode == EX_FILE_PICKER_RESULT) {
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

}