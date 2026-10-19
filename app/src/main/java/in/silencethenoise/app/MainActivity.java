package in.silencethenoise.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private WebView myWebView;
    private ProgressBar progressBar;
    private ValueCallback<Uri[]> mUploadMessage;
    private final static int FILECHOOSER_RESULTCODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkAndRequestPermissions();

        myWebView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.progressBar);
        ImageView btnRefresh = findViewById(R.id.btnRefresh);
        ImageView btnAbout = findViewById(R.id.btnAbout);

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myWebView != null) {
                    myWebView.reload();
                }
            }
        });

        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAboutDialog();
            }
        });

        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    return false;
                } else {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    } catch (Exception e) {}
                    return true;
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (progressBar != null) progressBar.setVisibility(View.GONE);
            }
        });

        myWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (progressBar != null) {
                    if (newProgress == 100) {
                        progressBar.setVisibility(View.GONE);
                    } else {
                        progressBar.setVisibility(View.VISIBLE);
                        progressBar.setProgress(newProgress);
                    }
                }
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                request.grant(request.getResources());
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (mUploadMessage != null) mUploadMessage.onReceiveValue(null);
                mUploadMessage = filePathCallback;
                try {
                    startActivityForResult(fileChooserParams.createIntent(), FILECHOOSER_RESULTCODE);
                } catch (Exception e) {
                    mUploadMessage = null;
                    return false;
                }
                return true;
            }
        });

        myWebView.loadUrl("https://silencethenoise.in/");
    }

    private void showAboutDialog() {
        String aboutText = "SILENCEtheNOISE.in features noise pollution awareness, complaint support, legal resources, a dB(A) meter, campaign updates, a media gallery, and citizen participation tools to promote a quieter and healthier Kerala.\n\n" +
                "In a democracy, sovereignty belongs to the people.\n\n" +
                "A violation continues only as long as it goes unquestioned. Change begins the moment someone asks.\n\n" +
                "Don't ask, 'Why complain?'\nAsk, 'Is the law being followed?' Is there permission? Are the legal limits met? Was action taken? If not, ask why, and ask again.\n\n" +
                "One complaint may not change everything, but persistent questions and documented complaints ensure accountability. When the finger points at the complainant, learn to point it back at the authorities responsible for enforcing the law.\n\n" +
                "A complaint is not a nuisance.\nIt is a citizen's democratic right.\n\n" +
                "Are you ready to question noise pollution, lawfully?\n\n" +
                "— Manu A. S.\n" +
                "— Campaign Coordinator\n" +
                "— SILENCEtheNOISE.in\n\n" +
                "More details: https://silencethenoise.in/";

        new AlertDialog.Builder(this)
                .setTitle("About - SILENCEtheNOISE.in")
                .setMessage(aboutText)
                .setPositiveButton("OK", null)
                .setNeutralButton("Visit Website", (dialog, which) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://silencethenoise.in/"));
                    startActivity(intent);
                })
                .show();
    }

    private void checkAndRequestPermissions() {
        List<String> permissionsNeeded = new ArrayList<>();
        permissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsNeeded.add(Manifest.permission.CAMERA);
        permissionsNeeded.add(Manifest.permission.RECORD_AUDIO);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsNeeded.add(Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            permissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        List<String> permissionsToRequest = new ArrayList<>();
        for (String perm : permissionsNeeded) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(perm);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), 100);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == FILECHOOSER_RESULTCODE && mUploadMessage != null) {
            mUploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
            mUploadMessage = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
