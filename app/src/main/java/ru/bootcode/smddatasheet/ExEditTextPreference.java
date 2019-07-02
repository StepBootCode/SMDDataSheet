package ru.bootcode.smddatasheet;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.TypedArrayUtils;
import androidx.preference.DialogPreference;

public class ExEditTextPreference extends DialogPreference {
    private String mText;

    @Nullable
    private ExEditTextPreference.OnBindEditTextListener mOnBindEditTextListener;

    public ExEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr,
                                 int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.EditTextPreference, defStyleAttr, defStyleRes);

        if (TypedArrayUtils.getBoolean(a, R.styleable.ExEditTextPreference_useExSimpleSummaryProvider,
                R.styleable.ExEditTextPreference_useExSimpleSummaryProvider, false)) {
            setSummaryProvider(ExEditTextPreference.SimpleSummaryProvider.getInstance());
        }
        a.recycle();
    }

    public ExEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    //!!! Тут должен быть по любому PUBLIC иначе будет крашиться при доступе в настройках
    public ExEditTextPreference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, R.attr.editTextPreferenceStyle,
                android.R.attr.editTextPreferenceStyle));
    }

    public ExEditTextPreference(Context context) {

        this(context, null);
    }

    /**
     * Saves the text to the current data storage.
     *
     * @param text The text to save
     */
    void setText(String text) {
        final boolean wasBlocking = shouldDisableDependents();

        mText = text;

        persistString(text);

        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }

        notifyChanged();
    }

    /**
     * Gets the text from the current data storage.
     *
     * @return The current preference value
     */
    String getText() {
        return mText;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        setText(getPersistedString((String) defaultValue));
    }

    @Override
    public boolean shouldDisableDependents() {
        return TextUtils.isEmpty(mText) || super.shouldDisableDependents();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        final ExEditTextPreference.SavedState myState = new ExEditTextPreference.SavedState(superState);
        myState.mText = getText();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(ExEditTextPreference.SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        ExEditTextPreference.SavedState myState = (ExEditTextPreference.SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setText(myState.mText);
    }

    public void setOnBindEditTextListener(@Nullable ExEditTextPreference.OnBindEditTextListener onBindEditTextListener) {
        mOnBindEditTextListener = onBindEditTextListener;
    }


    @Nullable
    ExEditTextPreference.OnBindEditTextListener getOnBindEditTextListener() {
        return mOnBindEditTextListener;
    }

    /**
     * Interface definition for a callback to be invoked when the corresponding dialog view for
     * this preference is bound. This allows you to customize the {@link EditText} displayed
     * in the dialog, such as setting a max length or a specific input type.
     */
    public interface OnBindEditTextListener {
        /**
         * Called when the dialog view for this preference has been bound, allowing you to
         * customize the {@link EditText} displayed in the dialog.
         *
         * @param editText The {@link EditText} displayed in the dialog
         */
        void onBindEditText(@NonNull EditText editText);
    }

    private static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<ExEditTextPreference.SavedState> CREATOR =
                new Parcelable.Creator<ExEditTextPreference.SavedState>() {
                    @Override
                    public ExEditTextPreference.SavedState createFromParcel(Parcel in) {
                        return new ExEditTextPreference.SavedState(in);
                    }

                    @Override
                    public ExEditTextPreference.SavedState[] newArray(int size) {
                        return new ExEditTextPreference.SavedState[size];
                    }
                };

        String mText;

        SavedState(Parcel source) {
            super(source);
            mText = source.readString();
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(mText);
        }
    }

    public static final class SimpleSummaryProvider implements SummaryProvider<ExEditTextPreference> {

        private static ExEditTextPreference.SimpleSummaryProvider sSimpleSummaryProvider;

        private SimpleSummaryProvider() {}

        private static ExEditTextPreference.SimpleSummaryProvider getInstance() {
            if (sSimpleSummaryProvider == null) {
                sSimpleSummaryProvider = new ExEditTextPreference.SimpleSummaryProvider();
            }
            return sSimpleSummaryProvider;
        }


        @Override
        public CharSequence provideSummary(ExEditTextPreference preference) {
            if (TextUtils.isEmpty(preference.getText())) {
                return (preference.getContext().getString(R.string.text_default));
            } else {
                return preference.getText();
            }
        }
    }

    @Override
    protected void onClick() {
       // getPreferenceManager().showDialog(this);
    }

}
