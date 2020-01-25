package app.esaal.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import javax.microedition.khronos.egl.EGLSurface;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.classes.GlobalFunctions;
import app.esaal.classes.Navigator;
import app.esaal.classes.SessionManager;
import app.esaal.webservices.EsaalApiConfig;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MyAccountFragment extends Fragment {
    public static FragmentActivity activity;
    public static MyAccountFragment fragment;
    private SessionManager sessionManager;

    @BindView(R.id.fragment_my_account_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_my_account_tv_paymentsOrBalance)
    TextView paymentsOrBalance;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static MyAccountFragment newInstance(FragmentActivity activity) {
        fragment = new MyAccountFragment();
        MyAccountFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View childView = inflater.inflate(R.layout.fragment_my_account, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.setupAppbar(true, true, false, false, "account", getString(R.string.myAccount));
        sessionManager = new SessionManager(activity);
        GlobalFunctions.hasNewNotificationsApi(activity);

        loading.setVisibility(View.GONE);
        if (sessionManager.isTeacher()) {
            paymentsOrBalance.setText(getString(R.string.myBalance));
        }
    }


    @OnClick(R.id.fragment_my_account_v_editProfile)
    public void editProfileClick() {
        Navigator.loadFragment(activity, EditProfileFragment.newInstance(activity), R.id.activity_main_fl_container, true);
    }

    @OnClick(R.id.fragment_my_account_v_paymentsOrBalance)
    public void paymentsOrBalanceClick() {
        if (sessionManager.isTeacher()) {
            Navigator.loadFragment(activity, BalanceFragment.newInstance(activity), R.id.activity_main_fl_container, true);
        } else {
            Navigator.loadFragment(activity, PaymentsFragment.newInstance(activity), R.id.activity_main_fl_container, true);
        }
    }

    @OnClick(R.id.fragment_my_account_v_questionsAndReplies)
    public void questionsAndRepliesClick() {
        Navigator.loadFragment(activity, QuestionsFragment.newInstance(activity), R.id.activity_main_fl_container, true);
    }

    @OnClick(R.id.fragment_my_account_v_logout)
    public void logoutClick() {
        logoutApi();
    }

    private void clearStack() {
        FragmentManager fm = activity.getSupportFragmentManager();
        for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }
    }

    private void logoutApi(){
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        EsaalApiConfig.getCallingAPIInterface().logout(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);

                        int status = response.getStatus();
                        if(status == 200){
                            sessionManager.logout();
                            clearStack();
                            if (QuestionDetailsFragment.isRunning) {
                                QuestionDetailsFragment.countDownTimer.cancel();
                            }
                            Navigator.loadFragment(activity, LoginFragment.newInstance(activity), R.id.activity_main_fl_container, false);
                            MainActivity.notification.setImageResource(R.mipmap.ic_notifi_unsel);
                        }

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        GlobalFunctions.EnableLayout(container);
                        GlobalFunctions.generalErrorMessage(loading, activity);
                    }
                }
        );
    }
}

