package app.esaal;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;

import app.esaal.classes.GlobalFunctions;
import app.esaal.classes.LocaleHelper;
import app.esaal.classes.SessionManager;
import app.esaal.webservices.EsaalApiConfig;
import app.esaal.webservices.responses.notifications.Notification;
import me.leolin.shortcutbadger.ShortcutBadger;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SplashActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private static final int SPLASH_DISPLAY_LENGTH = 3000;
    String regId = "";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(LocaleHelper.onAttach(newBase)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        GlobalFunctions.setDefaultLanguage(this);
        GlobalFunctions.setUpFont(this);
        sessionManager = new SessionManager(this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
        }, SPLASH_DISPLAY_LENGTH);

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("splash", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        regId = task.getResult().getToken();

                        Log.e("registerationid Splash ", "regid -> " + regId);

                        insertTokenApi(regId);


                    }
                });
    }

    private void insertTokenApi(final String regId) {
        EsaalApiConfig.getCallingAPIInterface().addDeviceToken(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                regId, 2,
                new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        if (response.getStatus() == 200) {
                            sessionManager.setRegId(regId);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                }
        );
    }

}
