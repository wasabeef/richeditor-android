package jp.wasabeef.richeditor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Copyright (C) 2015 Wasabeef
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class RichEditor extends WebView {

    public interface OnTextChangeListener {

        void onTextChange(String text);
    }

    private static final String SETUP_HTML = "file:///android_res/raw/editor.html";
    private static final String CALLBACK_SCHEME = "re-callback://";
    private boolean isReady = false;
    private String mContents;
    private OnTextChangeListener mListener;

    public RichEditor(Context context) {
        this(context, null);
    }

    public RichEditor(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.webViewStyle);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public RichEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);
        getSettings().setJavaScriptEnabled(true);
        setWebChromeClient(new WebChromeClient());
        setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                isReady = url.equalsIgnoreCase(SETUP_HTML);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (TextUtils.indexOf(url, CALLBACK_SCHEME) == 0) {
                    callback(url);
                    return true;
                }

                return super.shouldOverrideUrlLoading(view, url);
            }
        });
        loadUrl(SETUP_HTML);
    }

    public void setOnTextChangeListener(OnTextChangeListener listener) {
        mListener = listener;
    }

    public void callback(String text) {
        try {
            String decode = URLDecoder.decode(text, "UTF-8");
            mContents = decode.replaceFirst(CALLBACK_SCHEME, "");
            if (mListener != null) {
                mListener.onTextChange(mContents);
            }
        } catch (UnsupportedEncodingException e) {
            // No handling
        }
    }

    public void setEditorFontSize(int px) {
        exec("javascript:RE.setFontSize('" + px + "px');");
    }

    public void setHtml(String contents) {
        if (contents == null) {
            contents = "";
        }
        exec("javascript:RE.setHtml('" + contents + "');");
        mContents = contents;
    }

    public String getHtml() {
        return mContents;
    }

    public void setEditorBackgroundColor(int color) {
        String hex = convertHexColorString(color);
        exec("javascript:RE.setBackgroundColor('" + hex + "');");
    }

    public void setEditorWidth(int px) {
        exec("javascript:RE.setWidth('" + px + "px');");
    }

    public void setEditorHeight(int px) {
        exec("javascript:RE.setHeight('" + px + "px');");
    }

    public void setPlaceholder(String placeholder) {
        exec("javascript:RE.setPlaceholder('" + placeholder + "');");
    }

    public void undo() {
        exec("javascript:RE.undo();");
    }

    public void redo() {
        exec("javascript:RE.redo();");
    }

    public void setBold() {
        exec("javascript:RE.setBold();");
    }

    public void setItalic() {
        exec("javascript:RE.setItalic();");
    }

    public void setSubscript() {
        exec("javascript:RE.setSubscript();");
    }

    public void setSuperscript() {
        exec("javascript:RE.setSuperscript();");
    }

    public void setStrikeThrough() {
        exec("javascript:RE.setStrikeThrough();");
    }

    public void setUnderline() {
        exec("javascript:RE.setUnderline();");
    }

    public void setTextColor(int color) {
        exec("javascript:RE.prepareInsert();");

        String hex = convertHexColorString(color);
        exec("javascript:RE.setTextColor('" + hex + "');");
    }

    public void setTextBackgroundColor(int color) {
        exec("javascript:RE.prepareInsert();");

        String hex = convertHexColorString(color);
        exec("javascript:RE.setTextBackgroundColor('" + hex + "');");
    }

    public void setHeading(int heading) {
        exec("javascript:RE.setHeading('" + heading + "');");
    }

    public void setIndent() {
        exec("javascript:RE.setIndent();");
    }

    public void setOutdent() {
        exec("javascript:RE.setOutdent();");
    }

    public void setAlignLeft() {
        exec("javascript:RE.setJustifyLeft();");
    }

    public void setAlignCenter() {
        exec("javascript:RE.setJustifyCenter();");
    }

    public void setAlignRight() {
        exec("javascript:RE.setJustifyRight();");
    }

    public void setBlockquote() {
        exec("javascript:RE.setBlockquote();");
    }

    public void insertImage(String url, String alt) {
        exec("javascript:RE.prepareInsert();");
        exec("javascript:RE.insertImage('" + url + "', '" + alt + "');");
    }

    public void insertLink(String href, String title) {
        exec("javascript:RE.prepareInsert();");
        exec("javascript:RE.insertLink('" + href + "', '" + title + "');");
    }

    public void focusEditor() {
        requestFocus();
        exec("javascript:RE.focus();");
    }

    public void clearFocusEditor() {
        exec("javascript:RE.blurFocus();");
    }

    private String convertHexColorString(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }

    private void exec(String trigger) {
        if (isReady) {
            load(trigger);
        } else {
            new waitLoad(trigger).execute();
        }
    }

    private void load(String trigger) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            evaluateJavascript(trigger, null);
        } else {
            loadUrl(trigger);
        }
    }

    private class waitLoad extends AsyncTask<Void, Void, Void> {

        private String mTrigger;

        public waitLoad(String trigger) {
            super();
            mTrigger = trigger;
        }

        @Override
        protected Void doInBackground(Void... params) {
            while (!RichEditor.this.isReady) {
                sleep(100);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            load(mTrigger);
        }

        private synchronized void sleep(long ms) {
            try {
                wait(ms);
            } catch (InterruptedException ignore) {
            }
        }
    }
}