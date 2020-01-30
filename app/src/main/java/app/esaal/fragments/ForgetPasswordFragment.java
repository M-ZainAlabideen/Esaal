package app.esaal.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;

import app.esaal.R;
import app.esaal.classes.FixControl;
import app.esaal.classes.GlobalFunctions;
import app.esaal.classes.Navigator;
import app.esaal.webservices.EsaalApiConfig;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ForgetPasswordFragment extends Fragment {
    public static FragmentActivity activity;
    public static ForgetPasswordFragment fragment;

    @BindView(R.id.fragment_forget_password_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_forget_password_et_email)
    EditText email;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static ForgetPasswordFragment newInstance(FragmentActivity activity) {
        fragment = new ForgetPasswordFragment();
        ForgetPasswordFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View childView = inflater.inflate(R.layout.fragment_forget_password, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (activity == null) {
            activity = getActivity();
        }
        loading.setVisibility(View.GONE);
        FixControl.setupUI(container, activity);

    }


    @OnClick(R.id.fragment_forget_password_tv_send)
    public void sendClick() {
        String emailStr = email.getText().toString();
        if(emailStr == null || emailStr.isEmpty()){
            Snackbar.make(container,getString(R.string.enterEmail),Snackbar.LENGTH_SHORT).show();
        }
        else if(!FixControl.isValidEmail(emailStr)){
            Snackbar.make(container,getString(R.string.invalidEmail),Snackbar.LENGTH_SHORT).show();
        }
        else{
            forgetPasswordApi(emailStr);
        }
    }

    private void forgetPasswordApi(String email) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        EsaalApiConfig.getCallingAPIInterface().forgetPassword(email,
                new Callback<String>() {
                    @Override
                    public void success(String s, Response response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int status = response.getStatus();
                        if (status == 200) {
                            Snackbar.make(container,getString(R.string.checkYourMail),Snackbar.LENGTH_SHORT).show();
                            Navigator.loadFragment(activity,LoginFragment.newInstance(activity),R.id.activity_main_fl_container,false);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        GlobalFunctions.EnableLayout(container);
                        if (error.getResponse() != null && error.getResponse().getStatus() == 203) {
                            loading.setVisibility(View.GONE);
                            Snackbar.make(container,getString(R.string.notFoundEmail),Snackbar.LENGTH_SHORT).show();
                        }
                        else{
                            loading.setVisibility(View.GONE);
                            Snackbar.make(container,getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}