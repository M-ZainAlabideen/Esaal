package app.esaal.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.classes.FixControl;
import app.esaal.classes.GlobalFunctions;
import app.esaal.classes.LocaleHelper;
import app.esaal.classes.Navigator;
import app.esaal.classes.SessionManager;
import app.esaal.webservices.EsaalApiConfig;
import app.esaal.webservices.requests.LoginRequest;
import app.esaal.webservices.responses.authorization.UserResponse;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginFragment extends Fragment {
    public static FragmentActivity activity;
    public static LoginFragment fragment;
    public SessionManager sessionManager;

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
        MainActivity.setupAppbar(false, false, false, false, "", "");
        loading.setVisibility(View.GONE);
        FixControl.setupUI(container, activity);
        FixControl.closeKeyboardWhenFragmentStart(activity);
        sessionManager = new SessionManager(activity);
    }

    @OnClick(R.id.fragment_login_tv_language)
    public void languageClick(){
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
            Snackbar.make(loading, getString(R.string.enterUserName), Snackbar.LENGTH_SHORT).show();
        } else if (passwordStr == null || passwordStr.isEmpty()) {
            Snackbar.make(loading, getString(R.string.enterPassword), Snackbar.LENGTH_SHORT).show();
        } else {
            loginApi(userNameStr, passwordStr);
        }
    }


    private void changeLanguage(){
         /*for changing the sessionManager.getUserLanguage() of App
        1- check the value of sessionManager.getUserLanguage() in sharedPreference and Reflects the sessionManager.getUserLanguage()
         2- set the new value of sessionManager.getUserLanguage() in local and change the value of sharedPreference to new value
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
        startActivity(new Intent(activity, MainActivity.class));
        GlobalFunctions.setUpFont(activity);
    }


    private void loginApi(String userName, String password) {
        loading.setVisibility(View.VISIBLE);
        final LoginRequest loginRequest = new LoginRequest();
        loginRequest.userName = userName;
        loginRequest.password = password;
        EsaalApiConfig.getCallingAPIInterface().login(loginRequest,
                new Callback<UserResponse>() {
                    @Override
                    public void success(UserResponse userResponse, Response response) {
                        loading.setVisibility(View.GONE);
                        int status = response.getStatus();
                        if (status == 200) {
                            setupSession(userResponse);
                            if (sessionManager.isTeacher()) {
                                Navigator.loadFragment(activity, HomeFragment.newInstance(activity), R.id.activity_main_fl_container, false);
                            } else {
                                if (userResponse.hasPackages) {
                                    sessionManager.setPackage(true);
                                    Navigator.loadFragment(activity, HomeFragment.newInstance(activity), R.id.activity_main_fl_container, false);
                                } else {
                                    Navigator.loadFragment(activity, PackagesFragment.newInstance(activity, "addPackage"), R.id.activity_main_fl_container, false);
                                }
                            }
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        int failureStatus = error.getResponse().getStatus();
                        if (failureStatus == 400) {
                            loading.setVisibility(View.GONE);
                            Snackbar.make(loading, getString(R.string.invalidLogin), Snackbar.LENGTH_SHORT).show();
                        } else {
                            GlobalFunctions.generalErrorMessage(loading, activity);
                        }
                    }
                });
    }

    private void setupSession(UserResponse userResponse) {
        sessionManager.setUserToken(userResponse.token);
        sessionManager.setUserId(userResponse.user.id);
        sessionManager.setTeacher(userResponse.user.isTeacher);
        sessionManager.setPackage(userResponse.hasPackages);
        sessionManager.setBalanceRequest(userResponse.user.isRequest);
        sessionManager.LoginSession();
    }

}
