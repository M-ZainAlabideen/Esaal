package app.esaal.fragments;

import app.esaal.MainActivity;
import app.esaal.R;

import android.graphics.Typeface;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import app.esaal.classes.GlobalFunctions;
import app.esaal.classes.SessionManager;
import app.esaal.webservices.EsaalApiConfig;
import app.esaal.webservices.responses.aboutUs.AboutUsResponse;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AboutUsFragment extends Fragment {
    public static FragmentActivity activity;
    public static AboutUsFragment fragment;
    private SessionManager sessionManager;

    @BindView(R.id.fragment_about_us_wv_content)
    WebView content;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static AboutUsFragment newInstance(FragmentActivity activity) {
        fragment = new AboutUsFragment();
        AboutUsFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View childView = inflater.inflate(R.layout.fragment_about_us, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.setupAppbar(true, true, false, false, "more", getString(R.string.aboutUs));
        content.setVisibility(View.GONE);
        sessionManager = new SessionManager(activity);
        aboutUs();
    }

    private void aboutUs() {
        EsaalApiConfig.getCallingAPIInterface().aboutUs(
                sessionManager.getUserToken(),
                new Callback<AboutUsResponse>() {
                    @Override
                    public void success(AboutUsResponse aboutUsResponse, Response response) {
                        loading.setVisibility(View.GONE);
                        content.setVisibility(View.VISIBLE);
                        int status = response.getStatus();
                        if (status == 200) {
                            setupWebView(aboutUsResponse.getAboutUs());
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        GlobalFunctions.generalErrorMessage(loading,activity);
                    }
                }
        );
    }

    private void setupWebView(String contentStr) {
        contentStr = contentStr.replace("font", "f");
        contentStr = contentStr.replace("color", "c");
        contentStr = contentStr.replace("size", "s");
        String fontName;
        if (MainActivity.isEnglish)
            fontName = "montserrat_regular.ttf";
        else
            fontName = "cairo_regular.ttf";

        String head = "<head><style>@font-face {font-family: 'verdana';src: url('file:///android_asset/" + fontName + "');}body {font-family: 'verdana';}</style></head>";
        String htmlData = "<html>" + head + (MainActivity.isEnglish ? "<body dir=\"ltr\"" : "<body dir=\"rtl\"") + " style=\"font-family: verdana\">" +
                contentStr + "</body></html>";
        content.loadDataWithBaseURL("", htmlData, "text/html; charset=utf-8", "utf-8", "");

    }

}
