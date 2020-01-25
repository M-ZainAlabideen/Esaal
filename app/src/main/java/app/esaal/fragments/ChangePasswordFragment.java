package app.esaal.fragments;

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
import android.widget.EditText;
import android.widget.ProgressBar;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.classes.FixControl;
import app.esaal.classes.GlobalFunctions;
import app.esaal.classes.Navigator;
import app.esaal.classes.SessionManager;
import app.esaal.webservices.EsaalApiConfig;
import app.esaal.webservices.responses.authorization.ChangePasswordRequest;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ChangePasswordFragment extends Fragment {
    public static FragmentActivity activity;
    public static ChangePasswordFragment fragment;
    private SessionManager sessionManager;

    @BindView(R.id.fragment_change_password_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_change_password_et_oldPassword)
    EditText oldPassword;
    @BindView(R.id.fragment_change_password_et_newPassword)
    EditText newPassword;
    @BindView(R.id.fragment_change_password_et_confirmPassword)
    EditText confirmPassword;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static ChangePasswordFragment newInstance(FragmentActivity activity) {
        fragment = new ChangePasswordFragment();
        ChangePasswordFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View childView = inflater.inflate(R.layout.fragment_change_password, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.setupAppbar(true, false, false, false, "more", getString(R.string.changePassword));
        loading.setVisibility(View.GONE);
        FixControl.setupUI(container, activity);
        sessionManager = new SessionManager(activity);
        GlobalFunctions.hasNewNotificationsApi(activity);

    }


    @OnClick(R.id.fragment_change_password_tv_send)
    public void sendClick() {
        String oldPassStr = oldPassword.getText().toString();
        String newPassStr = newPassword.getText().toString();
        String confirmPassStr = confirmPassword.getText().toString();

        if (oldPassStr == null || oldPassStr.isEmpty()) {
            Snackbar.make(loading, getString(R.string.enterCurrentPass), Snackbar.LENGTH_SHORT).show();
        } else if (newPassStr == null || newPassStr.isEmpty()) {
            Snackbar.make(loading, getString(R.string.enterNewPass), Snackbar.LENGTH_SHORT).show();
        } else if (confirmPassStr == null || confirmPassStr.isEmpty()) {
            Snackbar.make(loading, getString(R.string.enterConfirmPass), Snackbar.LENGTH_SHORT).show();
        }else if (!newPassStr.equals(confirmPassStr)) {
            Snackbar.make(loading, getString(R.string.mismatchPass), Snackbar.LENGTH_SHORT).show();
        }
        else {
            ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
            changePasswordRequest.userId = sessionManager.getUserId();
            changePasswordRequest.oldPassword = oldPassStr;
            changePasswordRequest.password = newPassStr;
            changePasswordApi(changePasswordRequest);
        }
    }

    private void changePasswordApi(ChangePasswordRequest changePasswordRequest) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        EsaalApiConfig.getCallingAPIInterface().changePassword(
                sessionManager.getUserToken(),
                changePasswordRequest,
                new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int status = response2.getStatus();
                        if (status == 200) {
                            Snackbar.make(loading, getString(R.string.passwordChanged), Snackbar.LENGTH_SHORT).show();
                            //fix it
                            getFragmentManager().popBackStack();
                            //activity.onBackPressed();
                            //GlobalFunctions.clearStack(activity);
                            //Navigator.loadFragment(activity, MoreFragment.newInstance(activity), R.id.activity_main_fl_container, false);
                        }
                        else if (status == 202) {
                            loading.setVisibility(View.GONE);
                            Snackbar.make(loading, getString(R.string.incorrectPassword), Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        GlobalFunctions.EnableLayout(container);
                        GlobalFunctions.generalErrorMessage(loading,activity);
                    }
                });
    }
}