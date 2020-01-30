package app.esaal.classes;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.snackbar.Snackbar;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.webservices.EsaalApiConfig;
import app.esaal.webservices.responses.notifications.Notification;
import me.leolin.shortcutbadger.ShortcutBadger;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class GlobalFunctions {
    public static SessionManager sessionManager;
    private static int WRITE_PERMISSION_CODE = 23;
    private static int READ_PERMISSION_CODE = 33;
    private static int CAMERA_CODE = 43;


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

    public static String formatDate(String date) {
        String dateResult = "";
        Locale locale = new Locale("en");


        SimpleDateFormat dateFormatter1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", locale);
        SimpleDateFormat dateFormatter2 = new SimpleDateFormat("MM/dd/yyyy", locale);

        //SimpleDateFormat dateFormatter2 = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aaa", locale);

        int index = date.lastIndexOf('/');

        try {

            dateResult = dateFormatter2.format(dateFormatter1.parse(date.substring(index + 1)));

        } catch (ParseException e) {

            e.printStackTrace();

        }

        return dateResult;
    }

    public static String formatDateAndTime(String dateAndTime) {
        String dateResult = "";
        Locale locale = new Locale("en");


        SimpleDateFormat dateFormatter1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", locale);
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

//    public static void generalErrorMessage(ProgressBar loading, Context context) {
//        loading.setVisibility(View.GONE);
//        Snackbar.make(loading, context.getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
//    }

    public static void setDefaultLanguage(Context context) {
        SessionManager sessionManager = new SessionManager(context);
        String language = sessionManager.getUserLanguage();
        if (language.equals("en")) {
            MainActivity.isEnglish = true;
            LocaleHelper.setLocale(context, "en");
        } else {
            MainActivity.isEnglish = false;
            LocaleHelper.setLocale(context, "ar");
            if (language == null || language.isEmpty()) {
                sessionManager.setUserLanguage("ar");
            }
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

    public static Bitmap loadVideoFrameFromPath(String videoPath)
            throws Throwable {
        Bitmap bitmap = null;
        MediaMetadataRetriever mediaMetadataRetriever = null;
        try {
            mediaMetadataRetriever = new MediaMetadataRetriever();
            mediaMetadataRetriever.setDataSource(videoPath);
            bitmap = mediaMetadataRetriever.getFrameAtTime();
        } catch (Exception m_e) {
            throw new Throwable(
                    "Exception in retriveVideoFrameFromVideo(String p_videoPath)"
                            + m_e.getMessage());
        } finally {
            if (mediaMetadataRetriever != null) {
                mediaMetadataRetriever.release();
            }
        }
        return bitmap;
    }

    public static void hasNewNotificationsApi(final Context context) {
        EsaalApiConfig.getCallingAPIInterface().notifications(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                0,
                new Callback<ArrayList<Notification>>() {
                    @Override
                    public void success(ArrayList<Notification> notifications, Response response) {
                        int status = response.getStatus();
                        if (status == 200) {
                            if (notifications != null && notifications.size() > 0) {
                                MainActivity.hasNewNotifications = false;
                                MainActivity.notification.setImageResource(R.mipmap.ic_notifi_unsel);
                                ShortcutBadger.removeCount(context);
                                int counter = 0;
                                for (Notification value : notifications) {
                                    if (!value.isRead) {
                                        MainActivity.hasNewNotifications = true;
                                        MainActivity.notification.setImageResource(R.mipmap.ic_notifi_new);
                                        counter += 1;
                                    }
                                }
                                if (counter > 0) {
                                    ShortcutBadger.applyCount(context, counter);
                                }
                            }
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                }
        );
    }

    public static boolean isWriteExternalStorageAllowed(FragmentActivity activity) {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }

    //Requesting permission
    public static void requestWriteExternalStoragePermission(FragmentActivity activity) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission

            //checkMyPermission();
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_CODE);
    }

    public static boolean isReadExternalStorageAllowed(FragmentActivity activity) {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }

    //Requesting permission
    public static void requestReadExternalStoragePermission(FragmentActivity activity) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission

            //checkMyPermission();
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_PERMISSION_CODE);
    }

    public static boolean isCameraPermission(FragmentActivity activity) {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }

    public static void requestCameraPermission(FragmentActivity activity) {

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.CAMERA)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission

            //checkMyPermission();
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CAMERA}, CAMERA_CODE);
    }
}
