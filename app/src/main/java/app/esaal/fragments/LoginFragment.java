package app.esaal.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.SplashActivity;
import app.esaal.classes.AppController;
import app.esaal.classes.FixControl;
import app.esaal.classes.GlobalFunctions;
import app.esaal.classes.LocaleHelper;
import app.esaal.classes.Navigator;
import app.esaal.classes.SessionManager;
import app.esaal.webservices.EsaalApiConfig;
import app.esaal.webservices.requests.LoginRequest;
import app.esaal.webservices.responses.authorization.UserResponse;
import app.esaal.webservices.responses.notifications.Notification;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.leolin.shortcutbadger.ShortcutBadger;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginFragment extends Fragment {
    public static FragmentActivity activity;
    public static LoginFragment fragment;
    public SessionManager sessionManager;
    String regId = "";

    @BindView(R.id.fragment_login_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_login_et_userName)
    TextView userName;
    @BindView(R.id.fragment_login_et_password)
    TextView password;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static LoginFragment newInstance(FragmentActivity activity) {
        fragment = new LoginFragment();
        LoginFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View childView = inflater.inflate(R.layout.fragment_login, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (activity == null) {
            activity = getActivity();
        }
        MainActivity.setupAppbar(false, false, false, false, "", "");
        loading.setVisibility(View.GONE);
        activity = getActivity();
        FixControl.setupUI(container, activity);
        FixControl.closeKeyboardWhenFragmentStart(activity);
        sessionManager = new SessionManager(activity);

    }

    @OnClick(R.id.fragment_login_tv_language)
    public void languageClick() {
        changeLanguage();
    }

    @OnClick(R.id.fragment_login_tv_createAccount)
    public void createAccountClick() {
        Navigator.loadFragment(activity, AccountTypesFragment.newInstance(activity), R.id.activity_main_fl_container, true);
    }

    @OnClick(R.id.fragment_login_tv_forgetPass)
    public void forgetPassClick() {
        Navigator.loadFragment(activity, ForgetPasswordFragment.newInstance(activity), R.id.activity_main_fl_container, false);
    }

    @OnClick(R.id.fragment_login_tv_login)
    public void loginClick() {
        String userNameStr = userName.getText().toString();
        String passwordStr = password.getText().toString();

        if (userNameStr == null || userNameStr.isEmpty()) {
            Snackbar.make(activity.findViewById(R.id.fragment_login_cl_outerContainer), getString(R.string.enterUserName), Snackbar.LENGTH_SHORT).show();
        } else if (passwordStr == null || passwordStr.isEmpty()) {
            Snackbar.make(activity.findViewById(R.id.fragment_login_cl_outerContainer), getString(R.string.enterPassword), Snackbar.LENGTH_SHORT).show();
        } else {
            loginApi(userNameStr, passwordStr);
        }
    }

    @OnClick(R.id.fragment_login_tv_continueAsGuest)
    public void continueAsGuestClick() {
        sessionManager.guestSession();
        Navigator.loadFragment(activity, HomeFragment.newInstance(activity), R.id.activity_main_fl_container, false);
    }


    private void changeLanguage() {
         /*for changing the language of App
        1- check the value of currentLanguage and Reflects it
         2- set the new value of language in local and change the value of language sharedPreference to new value
         3- restart the mainActivity with noAnimation
        * */

        if (sessionManager.getUserLanguage().equals("ar")) {
            sessionManager.setUserLanguage("en");
            MainActivity.isEnglish = true;
        } else if (sessionManager.getUserLanguage().equals("en")) {
            sessionManager.setUserLanguage("ar");
            MainActivity.isEnglish = false;
        }

        LocaleHelper.setLocale(activity, sessionManager.getUserLanguage());
        sessionManager.setUserLanguage(sessionManager.getUserLanguage());

        activity.finish();
        activity.overridePendingTransition(0, 0);
        startActivity(new Intent(activity, SplashActivity.class));
        GlobalFunctions.setUpFont(activity);
    }


    private void loginApi(String userName, String password) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        final LoginRequest loginRequest = new LoginRequest();
        loginRequest.userName = userName;
        loginRequest.password = password;
        EsaalApiConfig.getCallingAPIInterface().login(loginRequest,
                new Callback<UserResponse>() {
                    @Override
                    public void success(UserResponse userResponse, Response response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int status = response.getStatus();
                        if (status == 200) {
                            setupSession(userResponse);
                            registrationFirebase();

                            clearStack();
                            if (userResponse.user.isTeacher) {
                                if (userResponse.user.isActive) {
                                    Navigator.loadFragment(activity, HomeFragment.newInstance(activity), R.id.activity_main_fl_container, true);
                                } else {
                                    Snackbar.make(activity.findViewById(R.id.fragment_login_cl_outerContainer), getString(R.string.notActive), Snackbar.LENGTH_SHORT).show();
                                }
                            } else {
                                if (userResponse.hasPackages) {
                                    sessionManager.setPackage(true);
                                    Navigator.loadFragment(activity, HomeFragment.newInstance(activity), R.id.activity_main_fl_container, false);
                                } else {
                                    Navigator.loadFragmentPackages(activity, PackagesFragment.newInstance(activity, "addPackage"), R.id.activity_main_fl_container, "backPackage");
                                }
                                setupSession(userResponse);
                            }
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        GlobalFunctions.EnableLayout(container);
                        if (error.getResponse() != null && error.getResponse().getStatus() == 400) {
                            loading.setVisibility(View.GONE);
                            Snackbar.make(activity.findViewById(R.id.fragment_login_cl_outerContainer), getString(R.string.invalidLogin), Snackbar.LENGTH_SHORT).show();
                        } else {
                            loading.setVisibility(View.GONE);
                            Snackbar.make(activity.findViewById(R.id.fragment_login_cl_outerContainer),getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setupSession(UserResponse userResponse) {
        sessionManager.guestLogout();
        sessionManager.setUserToken(userResponse.token);
        sessionManager.setUserId(userResponse.user.id);
        sessionManager.setTeacher(userResponse.user.isTeacher);
        sessionManager.setPackage(userResponse.hasPackages);
        sessionManager.setBalanceRequest(userResponse.user.isRequest);
        sessionManager.LoginSession();
    }


    private void clearStack() {
        FragmentManager fm = activity.getSupportFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
    }

    private void insertTokenApi(final String regId) {
        EsaalApiConfig.getCallingAPIInterface().addDeviceToken(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                regId, 2,
                AppController.getInstance().getDeviceID(),
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

    private void registrationFirebase() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("login", "getInstanceId failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        regId = task.getResult().getToken();

                        Log.e("registrationId", "regId -> " + regId);

                        insertTokenApi(regId);
                    }
                });
    }

}
