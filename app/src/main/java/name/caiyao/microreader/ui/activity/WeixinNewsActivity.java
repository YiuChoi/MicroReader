package name.caiyao.microreader.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import java.lang.reflect.InvocationTargetException;

import butterknife.BindView;
import butterknife.ButterKnife;
import name.caiyao.microreader.R;
import name.caiyao.microreader.utils.NetWorkUtil;

public class WeixinNewsActivity extends BaseActivity {

    @BindView(R.id.wv_weixin)
    WebView wvWeixin;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.pb_web)
    ProgressBar pbWeb;
    @BindView(R.id.fabButton)
    FloatingActionButton fabButton;
    @BindView(R.id.nest)
    NestedScrollView nest;

    String url;
    String title;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weixin_news);
        ButterKnife.bind(this);

        url = getIntent().getStringExtra("url");
        title = getIntent().getStringExtra("title");


        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WeixinNewsActivity.this.onBackPressed();
            }
        });
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nest.smoothScrollTo(0, 0);
            }
        });

        setToolBar(fabButton, toolbar, true, true, null);
        overridePendingTransition(R.anim.zoomin, R.anim.zoomout);

        WebSettings webSettings = wvWeixin.getSettings();
        if (!NetWorkUtil.isNetWorkAvailable(this))
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        else
            webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setBuiltInZoomControls(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        webSettings.setAppCachePath(getCacheDir().getAbsolutePath() + "/webViewCache");
        webSettings.setAllowFileAccess(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        wvWeixin.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String urlTo) {
                //处理自动跳转到浏览器的问题
                view.loadUrl(urlTo);
                return true;
            }
        });
        wvWeixin.setWebChromeClient(new WebChromeClient() {


            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (pbWeb != null) {
                    if (newProgress == 100) {
                        pbWeb.setVisibility(View.GONE);
                    } else {
                        if (pbWeb.getVisibility() == View.GONE) {
                            pbWeb.setVisibility(View.VISIBLE);
                        }
                        pbWeb.setProgress(newProgress);
                    }
                }
                super.onProgressChanged(view, newProgress);
            }

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
                ViewGroup parent = (ViewGroup) wvWeixin.getParent();
                parent.removeView(wvWeixin);
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
                    parent.addView(wvWeixin);
                    myView = null;
                }
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                result.confirm();
                return super.onJsAlert(view, url, message, result);
            }
        });
        wvWeixin.loadUrl(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_share, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && wvWeixin.canGoBack()) {
            wvWeixin.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, title + " " + url + getString(R.string.share_tail));
                shareIntent.setType("text/plain");
                //设置分享列表的标题，并且每次都显示分享列表
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
                break;
            case R.id.action_use_browser:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            wvWeixin.getClass().getMethod("onResume").invoke(wvWeixin, (Object[]) null);
        } catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            wvWeixin.getClass().getMethod("onPause").invoke(wvWeixin, (Object[]) null);
        } catch (IllegalAccessException|InvocationTargetException|NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wvWeixin != null) {
            ((ViewGroup) wvWeixin.getParent()).removeView(wvWeixin);
            wvWeixin.destroy();
            wvWeixin = null;
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.zoomin, R.anim.zoomout);
    }
}
