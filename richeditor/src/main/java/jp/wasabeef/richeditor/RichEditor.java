package jp.wasabeef.richeditor;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Copyright (C) 2022-2023 niendo
 * Copyright (C) 2017 Kishan Jadav
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

public class RichEditor extends WebView implements ValueCallback<String> {

  public enum Type {
    BOLD, ITALIC, SUBSCRIPT, SUPERSCRIPT, STRIKETHROUGH, UNDERLINE, H1, H2, H3, H4, H5, H6, HTML, HR, ORDEREDLIST, UNORDEREDLIST, JUSTIFYCENTER, JUSTIFYFULL, JUSTIFYLEFT, JUSTIFYRIGHT
  }

  private final AtomicBoolean mEvaluateFinished = new AtomicBoolean(false);

  public interface onJSDataListener  {
    public void onDataReceived(String value);
    //public void onActionFailure(Throwable throwableError);
  }

  public interface onClickListener  {
    public void onClick(String value);
    //public void onActionFailure(Throwable throwableError);
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

  private static final String SETUP_HTML = "file:///android_asset/rich_editor.html";
  private static final String CALLBACK_SCHEME = "re-callback://";
  private static final String STATE_SCHEME = "re-state://";
  private static final String CLICK_SCHEME= "re-click://";
  private boolean isReady = false;
  private OnTextChangeListener mTextChangeListener;
  private onClickListener mClickListener;
  private onJSDataListener mJSDataListener;
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

  public void setOnClickListener(onClickListener listener) {
    mClickListener = listener;
  }

  public void setOnJSDataListener(onJSDataListener listener) {
    mJSDataListener = listener;
  }

  public void setOnDecorationChangeListener(OnDecorationStateListener listener) {
    mDecorationStateListener = listener;
  }

  public void setOnInitialLoadListener(AfterInitialLoadListener listener) {
    mLoadListener = listener;
  }

  @Override public void onReceiveValue(String value) {

    String unescaped= null;
    try {
      unescaped = URLDecoder.decode(value, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    if (!"null".equals(unescaped)) {

      unescaped = unescaped.substring(1, unescaped.length() - 1)  // remove wrapping quotes
        .replace("\\\\", "\\")        // unescape \\ -> \
        .replace("\\\"", "\"")        // unescape \" -> "
        .replace("\\u003C", "<");    // unescape \u003c" -> <
    }

    if (mJSDataListener != null) {
      mJSDataListener.onDataReceived(unescaped);
    }
  }
  
   private void callback(String value) {

    if (mTextChangeListener != null) {
      mTextChangeListener.onTextChange(value.replaceFirst(CALLBACK_SCHEME, ""));
    }
  }

  private void callback_click(String value) {
    if (mClickListener != null) {
      mClickListener.onClick(value.replaceFirst(CLICK_SCHEME, ""));
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
    final int[] attrsArray = new int[] {
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
  }

  public String getHtml() {
      requestJSData("RE.getHtml()");
      return("data can only received by callback");
  }

  /// Text representation of the data that has been input into the editor view, if it has been loaded.
  public boolean getText() {
    return requestJSData("RE.getText()");
  }

  /// Returns selected text
  public boolean getSelectedText() {
    return requestJSData("RE.selectedText()");
  }

  /// Returns HTML-Code from selected range
  public boolean getSelectedHtml() {
    return requestJSData("RE.selectedHtml()");
  }

  /// The href of the current selection, if the current selection's parent is an anchor tag.
  /// Will be nil if there is no href, or it is an empty string.
  public boolean getSelectedHref() {
    if (!hasRangeSelection()) {
      return false;
    } else {
      return requestJSData("RE.getSelectedHref()");
    }
  }

  /// Whether or not the selection has a type specifically of "Range".
  public boolean hasRangeSelection() {
    return requestJSData("RE.rangeSelectionExists()");
  }

  /// Whether or not the selection has a type specifically of "Range" or "Caret".
  public boolean hasRangeOrCaretSelection() {
    return requestJSData("RE.rangeOrCaretSelectionExists()");
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
    String base64 = Utils.toBase64(bitmap, "png");
    bitmap.recycle();

    exec("javascript:RE.setBackgroundImage('url(data:image/png;base64," + base64 + ")');");
  }

  @Override
  public void setBackground(Drawable background) {
    Bitmap bitmap = Utils.toBitmap(background);
    String base64 = Utils.toBase64(bitmap,"png");
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
    exec("javascript:RE.setPlaceholderText('" + placeholder + "');");
  }

  public void setInputEnabled(Boolean inputEnabled) {
    exec("javascript:RE.setInputEnabled(" + inputEnabled + ")");
  }

  public void loadCSS(String cssFile) {
    String jsCSSImport =
      "(function() {" + "    var head  = document.getElementsByTagName(\"head\")[0];"
        + "    var link  = document.createElement(\"link\");" + "    link.rel  = \"stylesheet\";"
        + "    link.type = \"text/css\";" + "    link.href = \"" + cssFile + "\";"
        + "    link.media = \"all\";" + "    head.appendChild(link);" + "}) ();";
    exec("javascript:" + jsCSSImport + "");
  }

  public void undo() {
    exec("javascript:RE.undo();");
  }

  public void redo() {
    exec("javascript:RE.redo();");
  }

  public void setPre() {
    exec("javascript:RE.setPre();");
  }

  public void toggleBold() {
    exec("javascript:RE.toggleBold();");
  }

  public void setBold(boolean enabled) {
    exec("javascript:RE.setBold(" + enabled + ");");
  }

  public void toggleItalic() {
    exec("javascript:RE.toggleItalic();");
  }

  public void setItalic(boolean enabled) {
    exec("javascript:RE.setItalic(" + enabled + ");");
  }

  public void setSubscript() {
    exec("javascript:RE.setSubscript();");
  }

  public void setSuperscript() {
    exec("javascript:RE.setSuperscript();");
  }

  public void toggleStrikeThrough() {
    exec("javascript:RE.toggleStrikeThrough();");
  }

  public void setStrikeThrough(boolean enabled) {
    exec("javascript:RE.setStrikeThrough(" + enabled + ");");
  }

  public void toggleUnderline() {
    exec("javascript:RE.toggleUnderline();");
  }

  public void setUnderline(boolean enabled) {
    exec("javascript:RE.setUnderline(" + enabled + ");");
  }

  public void setTextColor(int color) {
    setTextColor(convertHexColorString(color));
  }

  public void setTextColor(String color) {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.setTextColor('" + color + "');");
  }

  public void setTextBackgroundColor(int color) {

    setTextBackgroundColor(convertHexColorString(color));
  }

  public void setTextBackgroundColor(String color) {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.setTextBackgroundColor('" + color + "');");
  }

  public void setFontFamily(String fontFamily) {
    exec("javascript:RE.setFontFamily('" + fontFamily + "');");
  }

  public void LoadFont(String name, String url) {
    exec("javascript:RE.LoadFont('" + name + "','"+url+"');");
  }

  public void getFontFamily() {
    requestJSData("javascript:RE.getFontFamily();");
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

  public void setBullets() { setUnorderedList(); }
  public void setUnorderedList() { exec("javascript:RE.setUnorderedList();"); }

  public void setNumbers() { setOrderedList(); }
  public void setOrderedList() { exec("javascript:RE.setOrderedList();"); }

  public void insertHTML(String text) {
    exec("javascript:RE.prepareInsert();");
    text = text.replace("\n", "<br>")
      .replace("\\", "\\\\")
      .replace("\"", "\\\"");        // unescape \\ -> \
    exec("javascript:RE.insertHTML('" + text + "');");
  }

  public void insertHR_Line() {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.insertHTML('<hr>');");
  }

  public void insertCollapsibleSection(String section, String content) {
    exec("javascript:RE.insertCollapsibleSection('"+section+"', '"+content+"');");
  }

  /**
   * {@link RichEditor#insertImage(String, String, String, String, Boolean)} will show the original size of the image.
   * So this method can manually process the image by adjusting specific width and height to fit into different mobile screens.
   *
   * @param url
   * @param alt
   * @param width    Width of the Image; if relative=true then 100 means 100% page width
   * @param height   Height of the Image
   * @param relative Image size is relative to page width
   */
  public void insertImage(String url, String alt, String width, String height, Boolean relative) {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.insertImage('" + url + "', '" + alt + "','" + width + "', '" + height + "', '" + relative.toString() + "');");
  }

  /**
   * {@link RichEditor#insertImageAsBase64(Uri, String, String, String, Boolean, Integer)} will show the original size of the image.
   * So this method can manually process the image by adjusting specific width and height to fit into different mobile screens.
   *
   * @param imageURI
   * @param alt
   * @param width        Width of the Image; if relative=true then 100 means 100% page width
   * @param height       Height of the Image
   * @param relative     Image size is relative to page width
   * @param inSampleSize Shrink Image size
   */
  public void insertImageAsBase64(Uri imageURI, String alt, String width, String height, Boolean relative, Integer inSampleSize) {
    InputStream inputStream = null;
    try {
      inputStream = getContext().getContentResolver().openInputStream(imageURI);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    BitmapFactory.Options options = new BitmapFactory.Options();
    options.inSampleSize = inSampleSize;
    Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
    try {
      inputStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    String type = getContext().getContentResolver().getType(imageURI).toLowerCase();
    String tag = "data:" + type + ";charset=utf-8;base64,";
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.insertImage('" + tag + Utils.toBase64(bitmap, type) + "','" + alt + "','" + width + "', '" + height + "', '" + relative.toString() + "');");
    //exec("javascript:RE.insertImage('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU5ErkJggg==','alt');");
  }

  /**
   * {@link RichEditor#insertVideo(String, String, String, String)} will show the original size of the video.
   * So this method can manually process the image by adjusting specific width and height to fit into different mobile screens.
   *
   * @param url
   * @param alt
   * @param width Width of the video; auto=100% page width
   * @param height
   */
  public void insertVideo(String url, String alt, String width, String height) {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.insertVideo('" + url + "', '" + alt + "', '" + width + "' '" + height + "');");
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
    exec("javascript:RE.insertYoutubeVideo('" + url + "', '" + width + "');");
  }

  public void insertYoutubeVideo(String url, int width, int height) {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.insertYoutubeVideo('" + url + "', '" + width + "', '" + height + "');");
  }

  public void insertLink(String href, String text, String title) {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.insertLink('" + href + "', '" + text + "', '" + title + "');");
  }

  public void insertCheckbox() {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.setCheckbox();");
  }

  public void focusEditor() {
    requestFocus();
    exec("javascript:RE.focus();");
  }

  public void focus(Integer x, Integer y) {
    requestFocus();
    exec("javascript:RE.focusAtPoint("+ x.toString() + ", "+ y.toString() + ")");
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
        @Override public void run() {
           exec(trigger);
        }
      }, 100);
    }
  }

  // MARK: Table functionalities
  public void insertTable(Integer col, Integer row) {
    exec("javascript:RE.prepareInsert()");
    exec("javascript:RE.insertTable("+ col.toString() + "," + row.toString() + ")");
  }

  /// Checks if cursor is in a table element. If so, return true so that you can add menu items accordingly.
  public void isCursorInTable() {
    requestJSData("javascript:RE.isCursorInTable");
  }

  public void addRowToTable() {
    exec("javascript:RE.addRowToTable()");
  }

  public void deleteRowFromTable() {
    exec("javascript:RE.deleteRowFromTable()");
  }

  public void addColumnToTable() {
    exec("javascript:RE.addColumnToTable()");
  }

  public void deleteColumnFromTable() {
    exec("javascript:RE.deleteColumnFromTable()");
  }

  private void load(String trigger) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      evaluateJavascript(trigger, null);
    } else {
      loadUrl(trigger);
    }
  }

  public boolean requestJSData(String cmdJS) {
    // https://stackoverflow.com/questions/38380246/espresso-how-to-call-evaluatejavascript-on-a-webview
    mEvaluateFinished.set(false);

     evaluateJavascript(cmdJS, this);
     return true;
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
      } else if (TextUtils.indexOf(url, CLICK_SCHEME) == 0) {
        callback_click(decode);
        return true;
      }
      return super.shouldOverrideUrlLoading(view, request);
    }
  }
}
