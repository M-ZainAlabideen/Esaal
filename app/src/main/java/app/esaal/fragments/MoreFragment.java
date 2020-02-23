package app.esaal.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.SplashActivity;
import app.esaal.classes.GlobalFunctions;
import app.esaal.classes.LocaleHelper;
import app.esaal.classes.Navigator;
import app.esaal.classes.SessionManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MoreFragment extends Fragment {
    public static FragmentActivity activity;
    public static MoreFragment fragment;
    private SessionManager sessionManager;

    @BindView(R.id.fragment_more_iv_aboutUsArrow)
    ImageView aboutUsArrow;
    @BindView(R.id.fragment_more_iv_contactUsArrow)
    ImageView contactUsArrow;
    @BindView(R.id.fragment_more_iv_changePasswordArrow)
    ImageView changePasswordArrow;
    @BindView(R.id.fragment_more_iv_termsArrow)
    ImageView termsArrow;
    @BindView(R.id.fragment_more_iv_langArrow)
    ImageView langArrow;
    @BindView(R.id.fragment_more_tv_changePasswordTxt)
    TextView changePasswordTxt;

    public static MoreFragment newInstance(FragmentActivity activity) {
        fragment = new MoreFragment();
        MoreFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View childView = inflater.inflate(R.layout.fragment_more, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (activity == null) {
            activity = getActivity();
        }
        MainActivity.setupAppbar(true, true, false, false, "more", getString(R.string.more));
        sessionManager = new SessionManager(activity);
        GlobalFunctions.hasNewNotificationsApi(activity);

        if (sessionManager.getUserLanguage().equals("en")) {
            aboutUsArrow.setRotation(180);
            contactUsArrow.setRotation(180);
            changePasswordArrow.setRotation(180);
            termsArrow.setRotation(180);
            langArrow.setRotation(180);
        }
        if(sessionManager.isGuest()){
            changePasswordTxt.setText(getString(R.string.login2));
        }
        else{
            changePasswordTxt.setText(getString(R.string.changePassword));
        }
    }

    @OnClick(R.id.fragment_more_v_aboutUs)
    public void aboutUsClick() {
        Navigator.loadFragment(activity, AboutUsFragment.newInstance(activity,false), R.id.activity_main_fl_container, true);
    }

    @OnClick(R.id.fragment_more_v_contactUs)
    public void contactUsClick() {
        Navigator.loadFragment(activity, ContactUsFragment.newInstance(activity), R.id.activity_main_fl_container, true);
    }

    @OnClick(R.id.fragment_more_v_changePassword)
    public void changePasswordClick() {
        if (sessionManager.isGuest()) {
            Navigator.loadFragment(activity, LoginFragment.newInstance(activity), R.id.activity_main_fl_container, false);
        } else {
            Navigator.loadFragment(activity, ChangePasswordFragment.newInstance(activity), R.id.activity_main_fl_container, true);
        }
    }

    @OnClick(R.id.fragment_more_v_terms)
    public void termsClick() {
        Navigator.loadFragment(activity, AboutUsFragment.newInstance(activity,true), R.id.activity_main_fl_container, true);
    }

    @OnClick(R.id.fragment_more_v_changeLanguage)
    public void changeLangClick() {
        changeLanguage();
    }


    private void changeLanguage() {
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
        startActivity(new Intent(activity, SplashActivity.class));
        GlobalFunctions.setUpFont(activity);
    }

}
