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
import android.widget.ProgressBar;
import android.widget.TextView;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.classes.GlobalFunctions;
import app.esaal.classes.Navigator;
import app.esaal.classes.SessionManager;
import app.esaal.webservices.EsaalApiConfig;
import app.esaal.webservices.responses.authorization.User;
import app.esaal.webservices.responses.balance.Balance;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class BalanceFragment extends Fragment {
    public static FragmentActivity activity;
    public static BalanceFragment fragment;
    private SessionManager sessionManager;

    @BindView(R.id.fragment_balance_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_balance_tv_balance)
    TextView balance;
    @BindView(R.id.fragment_balance_tv_questionsNum)
    TextView questionNum;
    @BindView(R.id.fragment_balance_tv_withdrawBalance)
    TextView withdrawBalance;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static BalanceFragment newInstance(FragmentActivity activity) {
        fragment = new BalanceFragment();
        BalanceFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View childView = inflater.inflate(R.layout.fragment_balance, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.setupAppbar(true, true, false, false, "account", getString(R.string.myBalance));
        sessionManager = new SessionManager(activity);
        container.setVisibility(View.GONE);
        if(sessionManager.isBalanceRequest()){
            withdrawBalance.setText(getString(R.string.waitingResponse));
            withdrawBalance.setClickable(false);
        }
        teacherBalanceApi();
    }

    @OnClick(R.id.fragment_balance_tv_withdrawBalance)
    public void withdrawBalanceClick() {
        withdrawBalanceApi();

    }

    private void teacherBalanceApi() {
        EsaalApiConfig.getCallingAPIInterface().teacherBalance(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                new Callback<Balance>() {
                    @Override
                    public void success(Balance balanceResponse, Response response) {
                        loading.setVisibility(View.GONE);
                        int status = response.getStatus();
                        if (status == 200) {
                            if (balanceResponse.totalAmount == 0) {
                                balance.setText("00.00" + " " + getString(R.string.currency));
                            } else {
                                balance.setText(balanceResponse.totalAmount + " " + getString(R.string.currency));
                            }
                            questionNum.setText(balanceResponse.totalQuestions + " " + getString(R.string.questionWord));
                            container.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                }
        );
    }

    private void withdrawBalanceApi() {
        loading.setVisibility(View.VISIBLE);
        EsaalApiConfig.getCallingAPIInterface().withdrawBalance(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                new Callback<User>() {
                    @Override
                    public void success(User user, Response response) {
                        loading.setVisibility(View.GONE);
                        int status = response.getStatus();
                        if (status == 200) {
                                Snackbar.make(loading, getString(R.string.withdrawBalanceSuccess), Snackbar.LENGTH_SHORT).show();
                                sessionManager.setBalanceRequest(user.isRequest);
                                if (sessionManager.isBalanceRequest()) {
                                    withdrawBalance.setText(getString(R.string.waitingResponse));
                                    withdrawBalance.setClickable(false);
                                }

                                withdrawBalance.setText(getString(R.string.waitingResponse));
                                withdrawBalance.setClickable(false);
                                balance.setText("00.00" + getString(R.string.currency));
                                questionNum.setText("0" + " " + getString(R.string.questionWord));
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                            int failureStatus = error.getResponse().getStatus();
                            if (failureStatus == 202) {
                                loading.setVisibility(View.GONE);
                                Snackbar.make(loading, getString(R.string.balanceLessThanRequired), Snackbar.LENGTH_SHORT).show();
                            }
                            else{
                                GlobalFunctions.generalErrorMessage(loading,activity);
                            }
                    }
                }
        );
    }
}

