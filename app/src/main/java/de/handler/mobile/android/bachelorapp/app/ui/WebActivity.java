package de.handler.mobile.android.bachelorapp.app.ui;

import android.support.v7.app.ActionBar;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import de.handler.mobile.android.bachelorapp.app.R;

/**
 * Displays WebView with help
 */
@EActivity(R.layout.activity_web)
public class WebActivity extends BaseActivity {

    public static final String URI = "activity_web_uri";


    @ViewById(R.id.activity_web_web_view)
    WebView webView;


    @AfterViews
    void init() {
        ActionBar actionBar = this.setupActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        String uri = getIntent().getStringExtra(URI);

        if (uri != null && !uri.equals("")) {
            webView.getSettings().setUserAgentString("Android");
            webView.setWebViewClient(new WebViewClient());
            webView.loadUrl(uri);
        }
    }
}
