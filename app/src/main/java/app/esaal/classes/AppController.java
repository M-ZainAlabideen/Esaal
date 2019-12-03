package app.esaal.classes;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import java.util.Locale;
import android.support.multidex.MultiDex;

import app.esaal.R;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class AppController extends Application {

    private Locale locale = null;

    @Override
    public void onCreate() {
        super.onCreate();
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("droid_arabic_kufi.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

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


}
