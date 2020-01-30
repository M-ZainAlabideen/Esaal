package app.esaal.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.classes.Navigator;
import app.esaal.classes.SessionManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AccountTypesFragment extends Fragment {
    public static FragmentActivity activity;
    public static AccountTypesFragment fragment;
    private SessionManager sessionManager;

    @BindView(R.id.fragment_account_types_tv_teacherWord)
    TextView teacherWord;
    @BindView(R.id.fragment_account_types_tv_studentWord)
    TextView studentWord;

    public static AccountTypesFragment newInstance(FragmentActivity activity) {
        fragment = new AccountTypesFragment();
        AccountTypesFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View childView = inflater.inflate(R.layout.fragment_account_types, container, false);
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
        sessionManager = new SessionManager(activity);
        if (MainActivity.isEnglish) {
            Typeface enBold = Typeface.createFromAsset(activity.getAssets(), "montserrat_medium.ttf");
            studentWord.setTypeface(enBold);
            teacherWord.setTypeface(enBold);
        } else {
            Typeface arBold = Typeface.createFromAsset(activity.getAssets(), "cairo_bold.ttf");
            studentWord.setTypeface(arBold);
            teacherWord.setTypeface(arBold);
        }
    }

    @OnClick(R.id.fragment_account_types_iv_student)
    public void studentClick() {
        sessionManager.setTeacher(false);
        Navigator.loadFragment(activity, RegistrationFragment.newInstance(activity), R.id.activity_main_fl_container, true);

    }

    @OnClick(R.id.fragment_account_types_iv_teacher)
    public void teacherClick() {
        sessionManager.setTeacher(true);
        Navigator.loadFragment(activity, RegistrationFragment.newInstance(activity), R.id.activity_main_fl_container, true);

    }

    @OnClick(R.id.fragment_account_types_iv_close)
    public void closeCLick(){
        activity.onBackPressed();
    }
}
