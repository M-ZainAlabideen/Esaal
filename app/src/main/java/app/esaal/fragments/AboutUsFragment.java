package app.esaal.fragments;

import app.esaal.MainActivity;
import app.esaal.R;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;

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

    public static AboutUsFragment newInstance(FragmentActivity activity, boolean isTerms) {
        fragment = new AboutUsFragment();
        AboutUsFragment.activity = activity;
        Bundle b = new Bundle();
        b.putBoolean("isTerms", isTerms);
        fragment.setArguments(b);
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
        if (activity == null) {
            activity = getActivity();
        }
        String title = "";
        if (fragment.getArguments().getBoolean("isTerms")) {
            title = getString(R.string.terms);
        } else {
            title = getString(R.string.aboutUs);
        }
        MainActivity.setupAppbar(true, true, false, false, "more", title);
        content.setVisibility(View.GONE);
        sessionManager = new SessionManager(activity);
        GlobalFunctions.hasNewNotificationsApi(activity);
        aboutUs();
    }

    private void aboutUs() {
        EsaalApiConfig.getCallingAPIInterface().aboutUs(
                new Callback<AboutUsResponse>() {
                    @Override
                    public void success(AboutUsResponse aboutUsResponse, Response response) {
                        loading.setVisibility(View.GONE);
                        content.setVisibility(View.VISIBLE);
                        int status = response.getStatus();
                        if (status == 200) {
                            if (fragment.getArguments().getBoolean("isTerms")) {
                                if (sessionManager.isTeacher()) {
                                    setupWebView(aboutUsResponse.getTeacherTermsCondition());
                                } else {
                                    setupWebView(aboutUsResponse.getStudentTermsCondition());
                                }
                            } else {
                                setupWebView(aboutUsResponse.getAboutUs());
                            }
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        loading.setVisibility(View.GONE);
                        Snackbar.make(content, getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupWebView(String contentStr) {
        if (contentStr != null) {
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

}
