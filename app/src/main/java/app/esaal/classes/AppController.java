package app.esaal.classes;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.provider.Settings;

import androidx.multidex.MultiDex;

import java.util.Locale;
import app.esaal.MainActivity;


public class AppController extends Application {
    private Locale locale = null;
    private String lang;
    private static AppController mInstance;

    public static Context getContext() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        Configuration config = getBaseContext().getResources().getConfiguration();
        GlobalFunctions.setDefaultLanguage(this);
        GlobalFunctions.setUpFont(this);
        if (MainActivity.isEnglish) {
            lang = "en";
        } else {
            lang = "ar";
        }
        if (!"".equals(lang) && !config.locale.getLanguage().equals(lang)) {
            locale = new Locale(lang);
            Locale.setDefault(locale);
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "ar"));
        MultiDex.install(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (locale != null) {
            newConfig.locale = locale;
            Locale.setDefault(locale);
            getBaseContext().getResources().updateConfiguration(newConfig, getBaseContext().getResources().getDisplayMetrics());
        }
    }

    public static synchronized AppController getInstance() {
        if (mInstance == null) {
            try {
                mInstance = AppController.class.newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return mInstance;
    }

    public String getDeviceID() {
        return Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);

    }
}
