package com.james.trixeditor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrixEditor extends WebView {

    public interface OnTextChangeListener {
        void onTextChange(String text);
    }

    public interface AfterInitialLoadListener {
        void onAfterInitialLoad(boolean isReady);
    }

    private static final String TAG = TrixEditor.class.getSimpleName();
    private static final String SETUP_HTML = "file:///android_asset/editor.html";
    private static final String CALLBACK_SCHEME = "te-callback://";


    private boolean isReady = false;
    private String mContents;
    private OnTextChangeListener mTextChangeListener;
    private AfterInitialLoadListener mLoadListener;

    public TrixEditor(Context context) {
        super(context);
    }

    public TrixEditor(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.webViewStyle);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public TrixEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);
        getSettings().setJavaScriptEnabled(true);
        setWebViewClient(createWebViewClient());
        loadUrl(SETUP_HTML);

    }

    protected void exec(final String trigger, final ICallbackContent callbackContent) {
        if (isReady) {
            load(trigger,callbackContent);
        } else {
            postDelayed(new Runnable() {
                @Override public void run() {
                    exec(trigger, callbackContent);
                }
            }, 100);
        }
    }


    private void load(String trigger, final ICallbackContent callbackContent) {
        evaluateJavascript(trigger, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                String result = removeUTFCharacters(value).toString();
                final JsonReader reader = new JsonReader(new StringReader(value));
                reader.setLenient(true);
                try {
                    if(reader.peek() == JsonToken.STRING) {
                        String domStr = reader.nextString();
                        if(domStr != null) {
                            result = domStr;
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }

                if(callbackContent!=null)
                    callbackContent.onResult(result);
            }
        });
    }

    protected EditorWebViewClient createWebViewClient() {
        return new EditorWebViewClient();
    }

    private void callback(String text) {
        mContents = text.replaceFirst(CALLBACK_SCHEME, "");
        if (mTextChangeListener != null) {
            mTextChangeListener.onTextChange(mContents);
        }
    }


    protected class EditorWebViewClient extends WebViewClient {

        @Override public void onPageFinished(WebView view, String url) {
            isReady = url.equalsIgnoreCase(SETUP_HTML);
            if (mLoadListener != null) {
                mLoadListener.onAfterInitialLoad(isReady);
            }
        }

        @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
            String decode;
            try {
                decode = URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // No handling
                return false;
            }

            if (TextUtils.indexOf(url, CALLBACK_SCHEME) == 0) {
                callback(decode);
                return true;
            }

            return super.shouldOverrideUrlLoading(view, url);
        }
    }


    public void setHtml(String contents) {
        if (contents == null) {
            contents = "";
        }
        try {
            exec("javascript:TE.setHtml('" + URLEncoder.encode(contents, "UTF-8") + "');", null);
        } catch (UnsupportedEncodingException e) {
            // No handling
        }
        mContents = contents;
    }

    public StringBuffer removeUTFCharacters(String data) {
        data = data.substring(1,data.length()-2);
        Pattern p = Pattern.compile("\\\\u(\\p{XDigit}{4})");
        Matcher m = p.matcher(data);
        StringBuffer buf = new StringBuffer(data.length());
        while (m.find()) {
            String ch = String.valueOf((char) Integer.parseInt(m.group(1), 16));
            m.appendReplacement(buf, Matcher.quoteReplacement(ch));
        }
        m.appendTail(buf);
        return buf;
    }

    public void getHtml(final ICallbackContent callbackContent) {
        exec("javascript:TE.getHtml();", callbackContent);
    }



    public interface ICallbackContent{
        void onResult(String content);
    }
}
