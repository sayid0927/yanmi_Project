package com.zxly.o2o.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.zxly.o2o.account.Account;
import com.zxly.o2o.application.Config;
import com.zxly.o2o.shop.R;
import com.zxly.o2o.util.PreferUtil;
import com.zxly.o2o.util.ViewUtils;
import com.zxly.o2o.view.LoadingView;

/**
 * Author fengrongjian
 * Date 2015-08-24
 * Description 流量充值常见问题
 */
public class CommonQuestionAct extends BasicAct implements View.OnClickListener {

    private View back;
    private WebView webView;
    private String browserUrl;
    private LoadingView loadingView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.win_recharge_record);
        back = findViewById(R.id.btn_back);
        back.setOnClickListener(this);
        loadingView = (LoadingView) findViewById(R.id.view_loading);
        initViews();
    }

    public static void start(Activity curAct) {
        Intent intent;
        intent = new Intent(curAct, CommonQuestionAct.class);
        ViewUtils.startActivity(intent, curAct);
    }

    private void initViews() {
        browserUrl = Config.dataBaseUrl + "/static/flow/commonQuestion.html";
        ViewUtils.setText(findViewById(R.id.txt_title), "常见问题");
        webView = (WebView) findViewById(R.id.web_view);
        // 支持JavaScript
        webView.getSettings().setJavaScriptEnabled(true);
        // 支持缩放
        webView.getSettings().setBuiltInZoomControls(false);
        // 支持保存数据
        webView.getSettings().setSaveFormData(false);
        // 清除缓存
        webView.clearCache(true);
        // 清除历史记录
        webView.clearHistory();
        webView.loadUrl(browserUrl);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            /** 开始载入页面 */
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                loadingView.startLoading();
            }

            /** 捕获点击事件 */
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("tel:")){
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                return false;
            }

            /** 错误返回 */
            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            /** 页面载入完毕 */
            @Override
            public void onPageFinished(WebView view, String url) {
                setProgressBarIndeterminateVisibility(false);// 设置标题栏的滚动条停止
                super.onPageFinished(view, url);
                String token = PreferUtil.getInstance().getLoginToken();
                StringBuilder builder = new StringBuilder();
                builder.append(token).append(",");
                builder.append(Config.imei).append(",");
                builder.append(System.currentTimeMillis()).append(",");
                builder.append(1).append(",");//设备类型1代表android
                builder.append(Account.user.getShopId());
                webView.loadUrl("javascript:setUserInfo('" + builder.toString() + "')");
                loadingView.onLoadingComplete();
            }
        });

    }

    @Override
    public void onClick(View v) {
        if (v == back) {
            finish();
        }
    }

}
