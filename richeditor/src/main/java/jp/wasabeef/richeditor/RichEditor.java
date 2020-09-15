package jp.wasabeef.richeditor;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Copyright (C) 2020 Wasabeef
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class RichEditor extends WebView {

  public enum Type {
    BOLD,
    ITALIC,
    SUBSCRIPT,
    SUPERSCRIPT,
    STRIKETHROUGH,
    UNDERLINE,
    H1,
    H2,
    H3,
    H4,
    H5,
    H6,
    ORDEREDLIST,
    UNORDEREDLIST,
    JUSTIFYCENTER,
    JUSTIFYFULL,
    JUSTIFYLEFT,
    JUSTIFYRIGHT
  }

  public interface OnTextChangeListener {

    void onTextChange(String text);
  }

  public interface OnDecorationStateListener {

    void onStateChangeListener(String text, List<Type> types);
  }

  public interface AfterInitialLoadListener {

    void onAfterInitialLoad(boolean isReady);
  }

  private static final String SETUP_HTML = "file:///android_asset/editor.html";
  private static final String CALLBACK_SCHEME = "re-callback://";
  private static final String STATE_SCHEME = "re-state://";
  private boolean isReady = false;
  private String mContents;
  private OnTextChangeListener mTextChangeListener;
  private OnDecorationStateListener mDecorationStateListener;
  private AfterInitialLoadListener mLoadListener;

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
    setWebViewClient(createWebviewClient());
    loadUrl(SETUP_HTML);

    applyAttributes(context, attrs);
  }

  protected EditorWebViewClient createWebviewClient() {
    return new EditorWebViewClient();
  }

  public void setOnTextChangeListener(OnTextChangeListener listener) {
    mTextChangeListener = listener;
  }

  public void setOnDecorationChangeListener(OnDecorationStateListener listener) {
    mDecorationStateListener = listener;
  }

  public void setOnInitialLoadListener(AfterInitialLoadListener listener) {
    mLoadListener = listener;
  }

  private void callback(String text) {
    mContents = text.replaceFirst(CALLBACK_SCHEME, "");
    if (mTextChangeListener != null) {
      mTextChangeListener.onTextChange(mContents);
    }
  }

  private void stateCheck(String text) {
    String state = text.replaceFirst(STATE_SCHEME, "").toUpperCase(Locale.ENGLISH);
    List<Type> types = new ArrayList<>();
    for (Type type : Type.values()) {
      if (TextUtils.indexOf(state, type.name()) != -1) {
        types.add(type);
      }
    }

    if (mDecorationStateListener != null) {
      mDecorationStateListener.onStateChangeListener(state, types);
    }
  }

  private void applyAttributes(Context context, AttributeSet attrs) {
    final int[] attrsArray = new int[]{
      android.R.attr.gravity
    };
    TypedArray ta = context.obtainStyledAttributes(attrs, attrsArray);

    int gravity = ta.getInt(0, NO_ID);
    switch (gravity) {
      case Gravity.LEFT:
        exec("javascript:RE.setTextAlign(\"left\")");
        break;
      case Gravity.RIGHT:
        exec("javascript:RE.setTextAlign(\"right\")");
        break;
      case Gravity.TOP:
        exec("javascript:RE.setVerticalAlign(\"top\")");
        break;
      case Gravity.BOTTOM:
        exec("javascript:RE.setVerticalAlign(\"bottom\")");
        break;
      case Gravity.CENTER_VERTICAL:
        exec("javascript:RE.setVerticalAlign(\"middle\")");
        break;
      case Gravity.CENTER_HORIZONTAL:
        exec("javascript:RE.setTextAlign(\"center\")");
        break;
      case Gravity.CENTER:
        exec("javascript:RE.setVerticalAlign(\"middle\")");
        exec("javascript:RE.setTextAlign(\"center\")");
        break;
    }

    ta.recycle();
  }

  public void setHtml(String contents) {
    if (contents == null) {
      contents = "";
    }
    try {
      exec("javascript:RE.setHtml('" + URLEncoder.encode(contents, "UTF-8") + "');");
    } catch (UnsupportedEncodingException e) {
      // No handling
    }
    mContents = contents;
  }

  public String getHtml() {
    return mContents;
  }

  public void setEditorFontColor(int color) {
    String hex = convertHexColorString(color);
    exec("javascript:RE.setBaseTextColor('" + hex + "');");
  }

  public void setEditorFontSize(int px) {
    exec("javascript:RE.setBaseFontSize('" + px + "px');");
  }

  @Override
  public void setPadding(int left, int top, int right, int bottom) {
    super.setPadding(left, top, right, bottom);
    exec("javascript:RE.setPadding('" + left + "px', '" + top + "px', '" + right + "px', '" + bottom
      + "px');");
  }

  @Override
  public void setPaddingRelative(int start, int top, int end, int bottom) {
    // still not support RTL.
    setPadding(start, top, end, bottom);
  }

  public void setEditorBackgroundColor(int color) {
    setBackgroundColor(color);
  }

  @Override
  public void setBackgroundColor(int color) {
    super.setBackgroundColor(color);
  }

  @Override
  public void setBackgroundResource(int resid) {
    Bitmap bitmap = Utils.decodeResource(getContext(), resid);
    String base64 = Utils.toBase64(bitmap);
    bitmap.recycle();

    exec("javascript:RE.setBackgroundImage('url(data:image/png;base64," + base64 + ")');");
  }

  @Override
  public void setBackground(Drawable background) {
    Bitmap bitmap = Utils.toBitmap(background);
    String base64 = Utils.toBase64(bitmap);
    bitmap.recycle();

    exec("javascript:RE.setBackgroundImage('url(data:image/png;base64," + base64 + ")');");
  }

  public void setBackground(String url) {
    exec("javascript:RE.setBackgroundImage('url(" + url + ")');");
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

  public void setInputEnabled(Boolean inputEnabled) {
    exec("javascript:RE.setInputEnabled(" + inputEnabled + ")");
  }

  public void loadCSS(String cssFile) {
    String jsCSSImport = "(function() {" +
      "    var head  = document.getElementsByTagName(\"head\")[0];" +
      "    var link  = document.createElement(\"link\");" +
      "    link.rel  = \"stylesheet\";" +
      "    link.type = \"text/css\";" +
      "    link.href = \"" + cssFile + "\";" +
      "    link.media = \"all\";" +
      "    head.appendChild(link);" +
      "}) ();";
    exec("javascript:" + jsCSSImport + "");
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

  public void setFontSize(int fontSize) {
    if (fontSize > 7 || fontSize < 1) {
      Log.e("RichEditor", "Font size should have a value between 1-7");
    }
    exec("javascript:RE.setFontSize('" + fontSize + "');");
  }

  public void removeFormat() {
    exec("javascript:RE.removeFormat();");
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

  public void setBullets() {
    exec("javascript:RE.setBullets();");
  }

  public void setNumbers() {
    exec("javascript:RE.setNumbers();");
  }

  public void insertImage(String url, String alt) {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.insertImage('" + url + "', '" + alt + "');");
  }

  /**
   * the image according to the specific width of the image automatically
   *
   * @param url
   * @param alt
   * @param width
   */
  public void insertImage(String url, String alt, int width) {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.insertImageW('" + url + "', '" + alt + "','" + width + "');");
  }

  /**
   * {@link RichEditor#insertImage(String, String)} will show the original size of the image.
   * So this method can manually process the image by adjusting specific width and height to fit into different mobile screens.
   *
   * @param url
   * @param alt
   * @param width
   * @param height
   */
  public void insertImage(String url, String alt, int width, int height) {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.insertImageWH('" + url + "', '" + alt + "','" + width + "', '" + height + "');");
  }

  public void insertVideo(String url) {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.insertVideo('" + url + "');");
  }

  public void insertVideo(String url, int width) {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.insertVideoW('" + url + "', '" + width + "');");
  }

  public void insertVideo(String url, int width, int height) {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.insertVideoWH('" + url + "', '" + width + "', '" + height + "');");
  }

  public void insertAudio(String url) {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.insertAudio('" + url + "');");
  }

  public void insertYoutubeVideo(String url) {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.insertYoutubeVideo('" + url + "');");
  }

  public void insertYoutubeVideo(String url, int width) {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.insertYoutubeVideoW('" + url + "', '" + width + "');");
  }

  public void insertYoutubeVideo(String url, int width, int height) {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.insertYoutubeVideoWH('" + url + "', '" + width + "', '" + height + "');");
  }

  public void insertLink(String href, String title) {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.insertLink('" + href + "', '" + title + "');");
  }

  public void insertTodo() {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.setTodo('" + Utils.getCurrentTime() + "');");
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

  protected void exec(final String trigger) {
    if (isReady) {
      load(trigger);
    } else {
      postDelayed(new Runnable() {
        @Override
        public void run() {
          exec(trigger);
        }
      }, 100);
    }
  }

  private void load(String trigger) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      evaluateJavascript(trigger, null);
    } else {
      loadUrl(trigger);
    }
  }

  protected class EditorWebViewClient extends WebViewClient {
    @Override
    public void onPageFinished(WebView view, String url) {
      isReady = url.equalsIgnoreCase(SETUP_HTML);
      if (mLoadListener != null) {
        mLoadListener.onAfterInitialLoad(isReady);
      }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      String decode = Uri.decode(url);

      if (TextUtils.indexOf(url, CALLBACK_SCHEME) == 0) {
        callback(decode);
        return true;
      } else if (TextUtils.indexOf(url, STATE_SCHEME) == 0) {
        stateCheck(decode);
        return true;
      }

      return super.shouldOverrideUrlLoading(view, url);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
      final String url = request.getUrl().toString();
      String decode = Uri.decode(url);

      if (TextUtils.indexOf(url, CALLBACK_SCHEME) == 0) {
        callback(decode);
        return true;
      } else if (TextUtils.indexOf(url, STATE_SCHEME) == 0) {
        stateCheck(decode);
        return true;
      }
      return super.shouldOverrideUrlLoading(view, request);
    }
  }
}
