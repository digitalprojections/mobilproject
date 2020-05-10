package furqon.io.github.mobilproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;

public class PrivacyPolicyActivity extends AppCompatActivity {
    WebView webview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        webview = (WebView) findViewById(R.id.webView);
        webview.loadUrl("file:///android_asset/privacy_policy.html");
    }
}
