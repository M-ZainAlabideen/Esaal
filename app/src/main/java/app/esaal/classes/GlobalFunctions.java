package app.esaal.classes;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import app.esaal.MainActivity;
import app.esaal.R;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedInput;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class GlobalFunctions {
    public static SessionManager sessionManager;

    public static void DisableLayout(ViewGroup layout) {
        layout.setEnabled(false);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof ViewGroup) {
                DisableLayout((ViewGroup) child);
            } else {
                child.setEnabled(false);
            }
        }
    }

    public static void EnableLayout(ViewGroup layout) {
        layout.setEnabled(true);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof ViewGroup) {
                EnableLayout((ViewGroup) child);
            } else {
                child.setEnabled(true);
            }
        }
    }
    public static String formatDate(String date){
        String dateResult = "";
        Locale locale = new Locale("en");


        SimpleDateFormat dateFormatter1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",locale);
        SimpleDateFormat dateFormatter2 = new SimpleDateFormat("MM/dd/yyyy",locale);

        //SimpleDateFormat dateFormatter2 = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aaa", locale);

        int index = date.lastIndexOf('/');

        try {

            dateResult = dateFormatter2.format(dateFormatter1.parse(date.substring(index + 1)));

        } catch (ParseException e) {

            e.printStackTrace();

        }

        return dateResult;
    }

    public static String formatDateAndtime(String dateAndTime){
        String dateResult = "";
        Locale locale = new Locale("en");


        SimpleDateFormat dateFormatter1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",locale);
        SimpleDateFormat dateFormatter2 = new SimpleDateFormat("MM/dd/yyyy hh:mm aaa", locale);
        int index = dateAndTime.lastIndexOf('/');

        try {

            dateResult = dateFormatter2.format(dateFormatter1.parse(dateAndTime.substring(index + 1)));

        } catch (ParseException e) {

            e.printStackTrace();

        }

        dateFormatter2.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = null;
        try {
            date = dateFormatter2.parse(dateResult);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        dateFormatter2.setTimeZone(TimeZone.getDefault());
        String formattedDate = dateFormatter2.format(date);
        return formattedDate;
    }
    public static void generalErrorMessage(ProgressBar loading,Context context){
        loading.setVisibility(View.GONE);
        Snackbar.make(loading,context.getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
    }
    public static void setDefaultLanguage(Context context){
        SessionManager sessionManager = new SessionManager(context);
        String language = sessionManager.getUserLanguage();
        if (language.equals("en")) {
            MainActivity.isEnglish = true;
            LocaleHelper.setLocale(context,"en");
        } else {
            MainActivity.isEnglish = false;
            LocaleHelper.setLocale(context,"ar");
        }
    }
    public static void setUpFont(Context context) {
        sessionManager = new SessionManager(context);
        if (sessionManager.getUserLanguage().equals("en")) {
            CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                    .setDefaultFontPath("montserrat_regular.ttf")
                    .setFontAttrId(R.attr.fontPath)
                    .build());
        } else {
            CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                    .setDefaultFontPath("droid_arabic_kufi.ttf")
                    .setFontAttrId(R.attr.fontPath)
                    .build());
        }
    }
    public static void clearAllStack(FragmentActivity activity) {
        FragmentManager fm = activity.getSupportFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
    }
    public static void clearLastStack(FragmentActivity activity) {
        FragmentManager fm = activity.getSupportFragmentManager();
            fm.popBackStack();
    }

}
