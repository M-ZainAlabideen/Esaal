package app.esaal.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.adapters.PaymentsAdapter;
import app.esaal.classes.GlobalFunctions;
import app.esaal.classes.Navigator;
import app.esaal.classes.SessionManager;
import app.esaal.webservices.EsaalApiConfig;
import app.esaal.webservices.responses.packages.Package;
import app.esaal.webservices.responses.payments.Payment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PaymentsFragment extends Fragment {
    public static FragmentActivity activity;
    public static PaymentsFragment fragment;
    SessionManager sessionManager;
    private ArrayList<Payment> paymentsList = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private PaymentsAdapter paymentsAdapter;

    @BindView(R.id.fragment_payments_rv_payments)
    RecyclerView payments;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static PaymentsFragment newInstance(FragmentActivity activity) {
        fragment = new PaymentsFragment();
        PaymentsFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View childView = inflater.inflate(R.layout.fragment_payments, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (activity == null) {
            activity = getActivity();
        }
        MainActivity.setupAppbar(true, true, false, false, "account", getString(R.string.payments));
        sessionManager = new SessionManager(activity);
        GlobalFunctions.hasNewNotificationsApi(activity);

        layoutManager = new LinearLayoutManager(activity);
        paymentsAdapter = new PaymentsAdapter(activity, paymentsList);
        payments.setLayoutManager(layoutManager);
        payments.setAdapter(paymentsAdapter);

        if (paymentsList.size() > 0) {
            loading.setVisibility(View.GONE);
        } else {
            paymentsApi();
        }
    }

    @OnClick(R.id.fragment_payments_ll_addPackage)
    public void addPackageClick() {
        Navigator.loadFragment(activity, PackagesFragment.newInstance(activity, "addPackage"), R.id.activity_main_fl_container, true);
    }

    private void paymentsApi() {
        EsaalApiConfig.getCallingAPIInterface().payments(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                new Callback<ArrayList<Payment>>() {
                    @Override
                    public void success(ArrayList<Payment> payments, Response response) {
                        loading.setVisibility(View.GONE);
                        int status = response.getStatus();
                        if (status == 200) {
                            if (payments.size() > 0) {
                                paymentsList.addAll(payments);
                                paymentsAdapter.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (error.getResponse() != null && error.getResponse().getStatus() == 204) {
                            loading.setVisibility(View.GONE);
                            Snackbar.make(payments, getString(R.string.noPayments), Snackbar.LENGTH_SHORT).show();
                        } else {
                            loading.setVisibility(View.GONE);
                            Snackbar.make(payments,getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

}
