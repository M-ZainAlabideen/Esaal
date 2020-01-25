package app.esaal.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.adapters.PackagesAdapter;
import app.esaal.classes.GlobalFunctions;
import app.esaal.classes.Navigator;
import app.esaal.classes.SessionManager;
import app.esaal.webservices.EsaalApiConfig;
import app.esaal.webservices.responses.packages.Package;
import app.esaal.webservices.responses.packages.SelectPackageResponse;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class PackagesFragment extends Fragment {
    public static FragmentActivity activity;
    public static PackagesFragment fragment;
    private SessionManager sessionManager;
    private LinearLayoutManager layoutManager;
    private PackagesAdapter packagesAdapter;
    private ArrayList<Package> packagesList = new ArrayList<>();
    private String comingFrom;

    @BindView(R.id.fragment_packages_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_packages_rv_packages)
    RecyclerView packages;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static PackagesFragment newInstance(FragmentActivity activity, String comingFrom) {
        fragment = new PackagesFragment();
        PackagesFragment.activity = activity;
        Bundle b = new Bundle();
        b.putString("comingFrom", comingFrom);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View childView = inflater.inflate(R.layout.fragment_packages, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        comingFrom = getArguments().getString("comingFrom");
        if (comingFrom.equals("addPackage")) {
            MainActivity.setupAppbar(true, false, false, false, "", getString(R.string.addPackage));
        } else {
            MainActivity.setupAppbar(true, false, false, false, "", getString(R.string.studentRegisterWord));
        }
        sessionManager = new SessionManager(activity);

        layoutManager = new LinearLayoutManager(activity);
        packagesAdapter = new PackagesAdapter(activity, packagesList,
                new PackagesAdapter.OnItemClickListener() {
                    @Override
                    public void packageClick(int subscriptionId) {
                        selectPackageApi(subscriptionId);
                    }
                });
        packages.setLayoutManager(layoutManager);
        packages.setAdapter(packagesAdapter);

        if (packagesList.size() == 0) {
            packagesApi();
        } else {
            loading.setVisibility(View.GONE);
        }
    }

    private void packagesApi() {
        EsaalApiConfig.getCallingAPIInterface().packages(
                sessionManager.getUserToken(),
                new Callback<ArrayList<Package>>() {
                    @Override
                    public void success(ArrayList<Package> packages, Response response) {
                        loading.setVisibility(View.GONE);
                        int status = response.getStatus();
                        if (status == 200) {
                            if (packages != null && packages.size() > 0) {
                                packagesList.addAll(packages);
                                packagesAdapter.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        GlobalFunctions.generalErrorMessage(loading, activity);
                    }
                }
        );
    }

    private void selectPackageApi(int subscriptionId) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        EsaalApiConfig.getCallingAPIInterface().selectPackage(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                subscriptionId,
                new Callback<SelectPackageResponse>() {
                    @Override
                    public void success(SelectPackageResponse selectPackageResponse, Response response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);

                        int status = response.getStatus();
                        if (status == 200) {
                            if(selectPackageResponse.returnUrl != null && !selectPackageResponse.returnUrl.isEmpty()){
                                Navigator.loadFragment(activity,PayUrlFragment.newInstance(activity,selectPackageResponse.returnUrl,comingFrom), R.id.activity_main_fl_container, false);
                            }
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

