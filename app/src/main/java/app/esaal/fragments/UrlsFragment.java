package app.esaal.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import app.esaal.MainActivity;
import app.esaal.R;
import butterknife.BindView;
import butterknife.ButterKnife;

public class UrlsFragment extends Fragment {
    static FragmentActivity activity;
    static UrlsFragment fragment;
    String Url;
    @BindView(R.id.loading)
    ProgressBar loading;
    //webView for loading the content of Url
    @BindView(R.id.urls_webView)
    WebView urls_webView;

    //pass the Url as parameter when fragment is loaded for loading Url content
    public static UrlsFragment newInstance(FragmentActivity activity, String Url) {
        fragment = new UrlsFragment();
        UrlsFragment.activity = activity;
        if (Url.contains("http"))
            fragment.Url = Url;
        else
            fragment.Url = "https://" + Url;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        MainActivity.appbar.setVisibility(View.VISIBLE);
        MainActivity.bottomAppbar.setVisibility(View.VISIBLE);
        //WebView should execute JavaScript
        urls_webView.getSettings().setJavaScriptEnabled(true);
        /*when enabling this Property is that it would then allow ANY website
        that takes advantage of DOM storage to use said storage options on the device*/
        urls_webView.getSettings().setDomStorageEnabled(true);
        urls_webView.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        //load the Url content inside the webView
        urls_webView.loadUrl(Url);
        urls_webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("vnd.youtube")) {

                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));

                    return true;
                } else {
                    return false;
                }
            }

            public void onPageFinished(WebView view, String url) {
                // hide progress of Loading after finishing
                loading.setVisibility(View.GONE);
            }
        });

    }

}
