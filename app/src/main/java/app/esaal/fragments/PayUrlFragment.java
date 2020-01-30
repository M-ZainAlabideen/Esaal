package app.esaal.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.classes.Constants;
import app.esaal.classes.GlobalFunctions;
import app.esaal.classes.Navigator;
import app.esaal.classes.SessionManager;
import butterknife.BindView;
import butterknife.ButterKnife;

public class PayUrlFragment extends Fragment {
    public static FragmentActivity activity;
    public static PayUrlFragment fragment;
    private SessionManager sessionManager;
    private int dataSetBefore;
    private String payUrl,comingFrom;

    @BindView(R.id.urls_webView)
    WebView webView;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static PayUrlFragment newInstance(FragmentActivity activity,String Url,String comingFrom) {
        fragment = new PayUrlFragment();
        PayUrlFragment.activity = activity;
        Bundle b = new Bundle();
        b.putString("Url",Url);
        b.putString("comingFrom",comingFrom);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View childView = inflater.inflate(R.layout.fragment_urls, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (activity == null) {
            activity = getActivity();
        }
        MainActivity.setupAppbar(true,false,false,false,"",getString(R.string.pay));
        sessionManager = new SessionManager(activity);
        if (getArguments() != null) {
            payUrl = getArguments().getString("Url");
            comingFrom = getArguments().getString("comingFrom");
        }
        if (dataSetBefore == 0) {
            dataSetBefore = 1;

            webView.setWebViewClient(new WebViewClient());

            webView.getSettings().setJavaScriptEnabled(true);

            webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

            webView.getSettings().setPluginState(WebSettings.PluginState.ON);

            webView.setWebChromeClient(new WebChromeClient());

            webView.loadUrl(payUrl);

            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.contains(Constants.SUCCESS_PAGE)) {
                        //fix  it
                        getFragmentManager().popBackStack();
                       // GlobalFunctions.clearStack(activity);
                        if (comingFrom.equals("addPackage")) {
                           // GlobalFunctions.clearStack(activity);
                                Snackbar.make(webView, getString(R.string.subscriptionSuccessfully), Snackbar.LENGTH_SHORT).show();
                                Navigator.loadFragment(activity, MyAccountFragment.newInstance(activity), R.id.activity_main_fl_container, false);
                            } else {
                                sessionManager.setPackage(true);
                                Snackbar.make(webView, getString(R.string.accountCreatedSuccessfully), Snackbar.LENGTH_SHORT).show();
                                Navigator.loadFragment(activity, HomeFragment.newInstance(activity), R.id.activity_main_fl_container, false);
                            }

                        return false;

                    }

                    else if (url.contains(Constants.ERROR_PAGE)) {

                        Snackbar.make(webView, activity.getString(R.string.OperationFailed), Snackbar.LENGTH_LONG).show();

                        getFragmentManager().popBackStackImmediate();

                        return false;

                    }

                    webView.loadUrl(url);

                    return true;

                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    // hide progress of Loading after finishing
                    loading.setVisibility(View.GONE);
                }


            });

        }
    }
}

