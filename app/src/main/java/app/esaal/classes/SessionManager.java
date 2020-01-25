package app.esaal.classes;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class SessionManager {

    Context context;
    public static SharedPreferences sharedPref;
    public static SharedPreferences.Editor editor;
    public static final String USER_PREF = "user_pref";
    private static final String IS_LOGGED = "is_logged";
    private static final String IS_GUEST = "isGuest";
    private static final String ACCOUNT_TYPE = "account_type";
    private static final String USER_TOKEN = "token";
    private static final String USER_ID = "user_id";
    private static final String LANGUAGE_CODE = "language_code";
    private static final String HAS_PACKAGE = "has_package";
    private static final String BALANCE_REQUEST = "balance_request";
    private static String IS_NOTIFICATION_ON = "is_Notification_on";
    private static final String REG_ID = "reg_id";

    public SessionManager(Context context) {
        this.context = context;
        sharedPref = context.getSharedPreferences(USER_PREF, MODE_PRIVATE);
        editor = sharedPref.edit();
    }

    public void guestSession() {
        editor.putBoolean(IS_GUEST, true);
        editor.commit();
    }

    public boolean isGuest() {
        return sharedPref.getBoolean(IS_GUEST, false);
    }

    public void guestLogout() {
        editor.putBoolean(IS_GUEST, false);
        editor.commit();
    }

    public void LoginSession() {
        editor.putBoolean(IS_LOGGED, true);
        editor.commit();
    }

    public boolean isLoggedIn() {
        return sharedPref.getBoolean(IS_LOGGED, false);
    }

    public void logout() {
        editor.putBoolean(IS_LOGGED, false);
        setUserToken(null);
        setTeacher(false);
       setPackage(false);
        setUserId(0);
        setRegId(null);
        setBalanceRequest(false);
        editor.commit();
    }

    public void setTeacher(boolean isTeacher) {
        editor.putBoolean(ACCOUNT_TYPE, isTeacher);
        editor.apply();
    }

    public boolean isTeacher() {
        return sharedPref.getBoolean(ACCOUNT_TYPE, false);
    }


    public void setUserToken(String token) {
        editor.putString(USER_TOKEN,"bearer"+" "+token);
        editor.apply();
    }

    public String getUserToken() {
        return sharedPref.getString(USER_TOKEN, "");
    }

    public void setUserId(int id) {
        editor.putInt(USER_ID, id);
        editor.apply();
    }

    public int getUserId() {
        return sharedPref.getInt(USER_ID, 0);
    }


    public void setUserLanguage(String languageCode) {
        editor.putString(LANGUAGE_CODE, languageCode);
        editor.apply();

    }

    public String getUserLanguage() {
        return sharedPref.getString(LANGUAGE_CODE, "ar");
    }

    public void setPackage(boolean hasPackage){
        editor.putBoolean(HAS_PACKAGE, hasPackage);
        editor.apply();
    }

    public boolean hasPackage(){
        return sharedPref.getBoolean(HAS_PACKAGE, false);
    }


    public void setBalanceRequest(boolean isRequest) {
        editor.putBoolean(BALANCE_REQUEST, isRequest);
        editor.apply();
    }

    public boolean isBalanceRequest() {
        return sharedPref.getBoolean(BALANCE_REQUEST, false);
    }

    public void setNotification(boolean status){
        editor.putBoolean(IS_NOTIFICATION_ON,status);
        editor.commit();
    }
    public boolean isNotificationOn(){
        return  sharedPref.getBoolean(IS_NOTIFICATION_ON,true);
    }

    public String getRegId() {
        return sharedPref.getString(REG_ID, "");
    }

    public void setRegId(String id) {
        editor.putString(REG_ID, id);
        editor.commit();
    }

}
