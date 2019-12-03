package app.esaal.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.classes.GlobalFunctions;
import app.esaal.classes.Navigator;
import app.esaal.classes.SessionManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MyAccountFragment extends Fragment {
    public static FragmentActivity activity;
    public static MyAccountFragment fragment;
    private SessionManager sessionManager;

    @BindView(R.id.fragment_my_account_tv_paymentsOrBalance)
    TextView paymentsOrBalance;

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
        GlobalFunctions.clearAllStack(activity);
        sessionManager.logout();
        Navigator.loadFragment(activity, LoginFragment.newInstance(activity), R.id.activity_main_fl_container, false);
    }
}

