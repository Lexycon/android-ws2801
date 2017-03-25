package de.lexycon.ledcontrol;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;


/**
 * The Settings activity.
 *
 * @author D. Berchtenbreiter
 * created 22.12.2016
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    private PrefsFragment prefsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        switch (settings.getString(getString(R.string.pref_key_theme), "")) {
            case "DARK" :
                setTheme(R.style.AppTheme_Dark);
                break;
            case "LIGHT": default:
                setTheme(R.style.AppTheme_Light);
                break;
        }
        super.onCreate(savedInstanceState);
        setupActionBar();

        prefsFragment = new PrefsFragment();

        getFragmentManager().beginTransaction().replace(android.R.id.content, prefsFragment).commit();
        getFragmentManager().executePendingTransactions();


        Preference.OnPreferenceChangeListener preferenceChangeListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                if (preference.getKey().equals(getString(R.string.pref_key_wheelstyle))) {
                    String[] aStrValuesWheelStyle = getResources().getStringArray(R.array.pref_values_wheelstyle);
                    String[] aStrValuesTextWheelStyle = getResources().getStringArray(R.array.pref_values_text_wheelstyle);
                    for (int i=0; i<aStrValuesWheelStyle.length; i++)
                        if (aStrValuesWheelStyle[i].equals(value.toString())) preference.setSummary(aStrValuesTextWheelStyle[i]);
                } else if (preference.getKey().equals(getString(R.string.pref_key_theme))) {
                    recreate();
//                    String[] aStrValuesTheme = getResources().getStringArray(R.array.pref_values_theme);
//                    String[] aStrValuesTextTheme = getResources().getStringArray(R.array.pref_values_text_theme);
//                    for (int i=0; i<aStrValuesTheme.length; i++)
//                        if (aStrValuesTheme[i].equals(value.toString())) preference.setSummary(aStrValuesTextTheme[i]);
                } else {
                    String stringValue = value.toString();
                    preference.setSummary(stringValue);
                }
                return true;
            }
        };

        Preference prefIpAddress = prefsFragment.findPreference(getString(R.string.pref_key_ipaddress));
        Preference prefPort = prefsFragment.findPreference(getString(R.string.pref_key_port));
        Preference prefTheme = prefsFragment.findPreference(getString(R.string.pref_key_theme));
        Preference prefWheelStyle = prefsFragment.findPreference(getString(R.string.pref_key_wheelstyle));

        prefIpAddress.setOnPreferenceChangeListener(preferenceChangeListener);
        prefPort.setOnPreferenceChangeListener(preferenceChangeListener);
        prefTheme.setOnPreferenceChangeListener(preferenceChangeListener);
        prefWheelStyle.setOnPreferenceChangeListener(preferenceChangeListener);

        prefIpAddress.setSummary(PreferenceManager.getDefaultSharedPreferences(prefIpAddress.getContext()).getString(prefIpAddress.getKey(), ""));
        prefPort.setSummary(PreferenceManager.getDefaultSharedPreferences(prefPort.getContext()).getString(prefPort.getKey(), ""));

        String prefWheelStyleKey = PreferenceManager.getDefaultSharedPreferences(prefWheelStyle.getContext()).getString(prefWheelStyle.getKey(), "");
        preferenceChangeListener.onPreferenceChange(prefWheelStyle, prefWheelStyleKey);

        String prefThemeKey = PreferenceManager.getDefaultSharedPreferences(prefTheme.getContext()).getString(prefTheme.getKey(), "");

        String[] aStrValuesTheme = getResources().getStringArray(R.array.pref_values_theme);
        String[] aStrValuesTextTheme = getResources().getStringArray(R.array.pref_values_text_theme);
        for (int i=0; i<aStrValuesTheme.length; i++)
        if (aStrValuesTheme[i].equals(prefThemeKey)) prefTheme.setSummary(aStrValuesTextTheme[i]);
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * The Prefs fragment.
     */
    public static class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}