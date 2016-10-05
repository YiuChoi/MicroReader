package name.caiyao.microreader.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.lang.reflect.InvocationTargetException;

import butterknife.BindView;
import butterknife.ButterKnife;
import name.caiyao.microreader.R;

/**
 * Created by 蔡小木 on 2016/4/12 0012.
 */
public class VideoWebViewActivity extends BaseActivity {

    @BindView(R.id.wv_video)
    WebView wvVideo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_webview);
        ButterKnife.bind(this);
        String url = getIntent().getStringExtra("url");

        WebSettings webSettings = wvVideo.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webSettings.setAllowFileAccess(true);
        webSettings.setLoadWithOverviewMode(true);
        wvVideo.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String urlTo) {
                //处理自动跳转到浏览器的问题
                view.loadUrl(urlTo);
                return true;
            }
        });
        wvVideo.setWebChromeClient(new WebChromeClient() {

            //显示全屏按钮
            private View myView = null;
            private CustomViewCallback myCallback = null;

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (myCallback != null) {
                    myCallback.onCustomViewHidden();
                    myCallback = null;
                    return;
                }
                ViewGroup parent = (ViewGroup) wvVideo.getParent();
                parent.removeView(wvVideo);
                parent.addView(view);
                myView = view;
                myCallback = callback;
            }

            @Override
            public void onHideCustomView() {
                if (myView != null) {
                    if (myCallback != null) {
                        myCallback.onCustomViewHidden();
                        myCallback = null;
                    }
                    ViewGroup parent = (ViewGroup) myView.getParent();
                    parent.removeView(myView);
                    parent.addView(wvVideo);
                    myView = null;
                }
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                result.confirm();
                return super.onJsAlert(view, url, message, result);
            }
        });
        wvVideo.loadUrl(url);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            wvVideo.getClass().getMethod("onResume").invoke(wvVideo, (Object[]) null);
        } catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            wvVideo.getClass().getMethod("onPause").invoke(wvVideo, (Object[]) null);
        } catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        if (wvVideo != null) {
            ((ViewGroup) wvVideo.getParent()).removeView(wvVideo);
            wvVideo.destroy();
            wvVideo = null;
        }
        super.onDestroy();
    }
}
