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

/**
 * This class provides the HTML-Editor.
 *
 *  Note: The behavior may vary between different Android, Java and webView versions
 */
public class RichEditor extends WebView implements ValueCallback<String> {

  /**
   *
   */
  public enum Type {
    BOLD, ITALIC, SUBSCRIPT, SUPERSCRIPT, STRIKETHROUGH, UNDERLINE, H1, H2, H3, H4, H5, H6, HTML, HR, ORDEREDLIST, UNORDEREDLIST, JUSTIFYCENTER, JUSTIFYFULL, JUSTIFYLEFT, JUSTIFYRIGHT
  }

  private final AtomicBoolean mEvaluateFinished = new AtomicBoolean(false);

  /**
   * Callback interface to receive data from the editor
   *
   * Example code
   * <pre>
   *    mEditor.setOnTextChangeListener(new RichEditor.OnTextChangeListener() {
   *       @Override
   *       public void onTextChange(String text) {
   *         mEditor.setOnJSDataListener(value -> {
   *
   *           // do something with value
   *
   *         });
   *         mEditor.getHtml();
   *       }
   *     });
   * </pre>
   */
  public interface onJSDataListener  {
    public void onDataReceived(String value);
  }

  /**
   * The class implements this listener to receive notifications when clicks occur
   * in the html editor
   * @todo
   */
  public interface onClickListener  {
    public void onClick(String value);
  }

  /**
   * The class implements this listener to receive notifications when clicks occur
   * in the html editor
   * @see onJSDataListener
   * @todo
   */
  public interface OnTextChangeListener {
        void onTextChange(String text);
  }

  /**
   * The class implements this listener to receive notifications when state changes occur
   * in the html editor
   * @todo
   */
  public interface OnDecorationStateListener {
    void onStateChangeListener(String text, List<Type> types);
  }

  /**
   * The class implements this listener to receive notifications the html editor is ready initialised
   */
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

  /**
   * Constructs a new RichEditor with an Activity Context object.
   * @param context
   */
  public RichEditor(Context context) {
    this(context, null);
  }

  /**
   * Constructs a new RichEditor with layout parameters.
   * @param context
   * @param attrs
   */
  public RichEditor(Context context, AttributeSet attrs) {
    this(context, attrs, android.R.attr.webViewStyle);
  }

  /**
   * Constructs a new RichEditor with layout parameters and a default style
   * @param context
   * @param attrs
   * @param defStyleAttr
   */
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
  /**
   * Creates and returns an instance of the protected inner class EditorWebViewClient.
   *
   * @return An instance of the EditorWebViewClient class.
   */
  protected EditorWebViewClient createWebviewClient() {
    return new EditorWebViewClient();
  }

  /**
   * Register a callback to be invoked when the html text is changed
   * @param listener The callback that will run This value may be null.
   */
  public void setOnTextChangeListener(OnTextChangeListener listener) {
    mTextChangeListener = listener;
  }

  /**
   * Register a callback to be invoked when the editor is clicked
   * @param listener The callback that will run This value may be null.
   */
  public void setOnClickListener(onClickListener listener) {
    mClickListener = listener;
  }

  /**
   * Register a callback to be invoked when data needs to be transferred from the editor to the main program
   * @see onJSDataListener
   * @param listener The callback that will run This value may be null.
   */
  public void setOnJSDataListener(onJSDataListener listener) {
    mJSDataListener = listener;
  }

  public void setOnDecorationChangeListener(OnDecorationStateListener listener) {
    mDecorationStateListener = listener;
  }

  /**
   * Register a callback to be invoked when the editor finished loading and is ready to use
   * @param listener The callback that will run This value may be null.
   */
  public void setOnInitialLoadListener(AfterInitialLoadListener listener) {
    mLoadListener = listener;
  }

  /**
   * Value callback
   * @param value The value.
   */
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

  private void load(String trigger) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      evaluateJavascript(trigger, null);
    } else {
      loadUrl(trigger);
    }
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

  /**
   * Load html text in the the editor.
   * <pre>
   *   SetupRichEditor();
   *   String HtmlText = "<h1>Header 1</h1>"
   *   editText.setHtml(HtmlText);
   * </pre>
   * @param contents string with html content
   */
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

  /**
   * Requests JavaScript data from the WebView by executing the specified JavaScript command.
   * This method uses the evaluateJavascript method to execute the provided JavaScript command
   * and waits for the evaluation to finish before returning.
   *
   * @param cmdJS The JavaScript command to be executed in the WebView.
   * @return true if the JavaScript command was successfully requested, false otherwise.
   *
   * @see <a href="https://stackoverflow.com/questions/38380246/espresso-how-to-call-evaluatejavascript-on-a-webview">
   * Stack Overflow: Espresso - How to call evaluateJavascript on a WebView</a>
   */
  public boolean requestJSData(String cmdJS) {
    // https://stackoverflow.com/questions/38380246/espresso-how-to-call-evaluatejavascript-on-a-webview
    mEvaluateFinished.set(false);

    // Execute the specified JavaScript command and set the evaluation flag
    evaluateJavascript(cmdJS, this);

    // Return true to indicate that the JavaScript command was successfully requested
    return true;
  }

  /**
   * Requests the complete html data from the editor. Data comes via callback
   * @see onJSDataListener()
   * <pre>
   *         mEditor.setOnJSDataListener(value -> {
   *             do something with value
   *         });
   *         mEditor.getHtml();
   * </pre>
   * @return the html content of the editor
   */
  public String getHtml() {
      requestJSData("RE.getHtml()");
      return("data can only received by callback");
  }

  /**
   * Text representation of the data that has been input into the editor view, if it has been loaded.
   * Data comes via callback.
   * @see #getHtml
   * @return the text
   */
  public boolean getText() {
    return requestJSData("RE.getText()");
  }

  /**
   * Returns selected text
   * Data comes via callback.
   * @see #getHtml
   * @return the selected text
   */
  public boolean getSelectedText() {
    return requestJSData("RE.selectedText()");
  }

  /**
   * Returns HTML-Code from selected range
   * Data comes via callback.
   * @see #getHtml
   * @return HTML-Code
   */
  public boolean getSelectedHtml() {
    return requestJSData("RE.selectedHtml()");
  }

  /**
   * The href of the current selection, if the current selection's parent is an anchor tag.
   * Will be nil if there is no href, or it is an empty string.
   * Data comes via callback.
   * @see #getHtml
   * @return false - if no selection
   */
  public boolean getSelectedHref() {
    if (!hasRangeSelection()) {
      return false;
    } else {
      return requestJSData("RE.getSelectedHref()");
    }
  }

  /**
   * Whether or not the selection has a type specifically of "Range".
   * Data comes via callback.
   * @see #getHtml()
   * @return
   */
  public boolean hasRangeSelection() {
    return requestJSData("RE.rangeSelectionExists()");
  }

  /**
   * Whether or not the selection has a type specifically of "Range" or "Caret".
   * Data comes via callback.
   * @see #getHtml
   * @return
   */
  public boolean hasRangeOrCaretSelection() {
    return requestJSData("RE.rangeOrCaretSelectionExists()");
  }

  /**
   * Sets the font color of the editor
   * <pre>
   *   mEditor.setEditorFontColor(getColor(R.color.EditorTxtColor));
   * </pre>
   * @param color
   */
  public void setEditorFontColor(int color) {
    String hex = convertHexColorString(color);
    exec("javascript:RE.setBaseTextColor('" + hex + "');");
  }

  /**
   * Sets the font size in pixels
   * @param px font size
   */
  public void setEditorFontSize(int px) {
    exec("javascript:RE.setBaseFontSize('" + px + "px');");
  }

  /**
   * sets the padding
   * @param left the left padding in pixels
   * @param top the top padding in pixels
   * @param right the right padding in pixels
   * @param bottom the bottom padding in pixels
   */
  @Override
  public void setPadding(int left, int top, int right, int bottom) {
    super.setPadding(left, top, right, bottom);
    exec("javascript:RE.setPadding('" + left + "px', '" + top + "px', '" + right + "px', '" + bottom
      + "px');");
  }

  /**
   * sets the relative padding
   * @todo same as @see #setPadding()
   * @param start the start padding in pixels
   * @param top the top padding in pixels
   * @param end the end padding in pixels
   * @param bottom the bottom padding in pixels
   */
  @Override
  public void setPaddingRelative(int start, int top, int end, int bottom) {
    // still not support RTL.
    setPadding(start, top, end, bottom);
  }

  /**
   * Sets the back color of the editor
   * @param color the color of the background
   */
  public void setEditorBackgroundColor(int color) {
    setBackgroundColor(color);
  }

  /**
   * Sets the back color of the editor
   * @param color the color of the background
   */
  @Override
  public void setBackgroundColor(int color) {
    super.setBackgroundColor(color);
  }

  /**
   * set a background resource
   * <pre>
   *   mEditor.setBackgroundResource(R.drawable.bg);
   * </pre>
   * @param resid The identifier of the resource.
   */
  @Override
  public void setBackgroundResource(int resid) {
    Bitmap bitmap = Utils.decodeResource(getContext(), resid);
    String base64 = Utils.toBase64(bitmap, "png");
    bitmap.recycle();

    exec("javascript:RE.setBackgroundImage('url(data:image/png;base64," + base64 + ")');");
  }

  /**
   * The Drawable (png) to use as the background, or null to remove the background
   * @param background The Drawable to use as the background, or null to remove the
   * background
   */
  @Override
  public void setBackground(Drawable background) {
    Bitmap bitmap = Utils.toBitmap(background);
    String base64 = Utils.toBase64(bitmap,"png");
    bitmap.recycle();

    exec("javascript:RE.setBackgroundImage('url(data:image/png;base64," + base64 + ")');");
  }

  /**
   * The URL (png) to use as the background
   * <pre>
   *   mEditor.setBackground("https://raw.githubusercontent.com/wasabeef/art/master/chip.png");
   * </pre>
   * @param url The Drawable to use as the background, or null to remove the
   * background
   */
  public void setBackground(String url) {
    exec("javascript:RE.setBackgroundImage('url(" + url + ")');");
  }

  /**
   * Sets width of the editor in pixels
   * @param px new width
   */
  public void setEditorWidth(int px) {
    exec("javascript:RE.setWidth('" + px + "px');");
  }

  /**
   * Sets height of the editor in pixels
   * @param px new height
   */
  public void setEditorHeight(int px) {
    exec("javascript:RE.setHeight('" + px + "px');");
  }

  /**
   * Sets a hint in the empty editor
   * <pre>
   *   mEditor.setPlaceholder("Insert text here...");
   * </pre>
   * @param placeholder the text
   */
  public void setPlaceholder(String placeholder) {
    exec("javascript:RE.setPlaceholderText('" + placeholder + "');");
  }

  /**
   * Make the editor writable
   * @param inputEnabled true=writable, false=read only
   */
  public void setInputEnabled(Boolean inputEnabled) {
    exec("javascript:RE.setInputEnabled(" + inputEnabled + ")");
  }

  /**
   * Loads an additional CSS file
   * @param cssFile
   */
  public void loadCSS(String cssFile) {
    String jsCSSImport =
      "(function() {" + "    var head  = document.getElementsByTagName(\"head\")[0];"
        + "    var link  = document.createElement(\"link\");" + "    link.rel  = \"stylesheet\";"
        + "    link.type = \"text/css\";" + "    link.href = \"" + cssFile + "\";"
        + "    link.media = \"all\";" + "    head.appendChild(link);" + "}) ();";
    exec("javascript:" + jsCSSImport + "");
  }

  /**
   * reverts the last action
   */
  public void undo() {
    exec("javascript:RE.undo();");
  }

  /**
   * reverts the last revert action
   */
  public void redo() {
    exec("javascript:RE.redo();");
  }

  /**
   * set html tag <code>PRE</code>
   */
  public void setPre() {
    exec("javascript:RE.setPre();");
  }

  /**
   * toggle font attribute bold
   */
  public void toggleBold() {
    exec("javascript:RE.toggleBold();");
  }

  /**
   * set font attribute bold
   */
  public void setBold(boolean enabled) {
    exec("javascript:RE.setBold(" + enabled + ");");
  }

  /**
   * toggle font attribute Italic
   */
  public void toggleItalic() {
    exec("javascript:RE.toggleItalic();");
  }

  /**
   * set font attribute Italic
   */
  public void setItalic(boolean enabled) {
    exec("javascript:RE.setItalic(" + enabled + ");");
  }

  /**
   * set font attribute Sub script
   */
  public void setSubscript() {
    exec("javascript:RE.setSubscript();");
  }

  /**
   * set font attribute super script
   */
  public void setSuperscript() {
    exec("javascript:RE.setSuperscript();");
  }

  /**
   * toggle font attribute Strike Through
   */
  public void toggleStrikeThrough() {
    exec("javascript:RE.toggleStrikeThrough();");
  }

  /**
   * set font attribute Strike Through
   * @param enabled
   */
  public void setStrikeThrough(boolean enabled) {
    exec("javascript:RE.setStrikeThrough(" + enabled + ");");
  }

  /**
   * toggle font attribute Under line
   */
  public void toggleUnderline() {
    exec("javascript:RE.toggleUnderline();");
  }

  /**
   * set font attribute Under line
   * @param enabled
   */
  public void setUnderline(boolean enabled) {
    exec("javascript:RE.setUnderline(" + enabled + ");");
  }

  /**
   * set text color by value
   * @param color
   */
  public void setTextColor(int color) {
    setTextColor(convertHexColorString(color));
  }

  /**
   * set text color by string
   * <pre>
   *   mEditor.setTextColor("red")
   * </pre>
   * @param color
   */
  public void setTextColor(String color) {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.setTextColor('" + color + "');");
  }

  /**
   * Sets Background color for text by number
   * @param color
   */
  public void setTextBackgroundColor(int color) {

    setTextBackgroundColor(convertHexColorString(color));
  }

  /**
   * Sets Background color for text by string
   * <pre>
   * mEditor.setTextColor("red")
   * </pre>
   * @param color as string
   */
  public void setTextBackgroundColor(String color) {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.setTextBackgroundColor('" + color + "');");
  }

  /**
   * set the font family
   * @see #getFontFamily for a list of available font families
   * @see #LoadFont to add new fonts
   * @param fontFamily
   */
  public void setFontFamily(String fontFamily) {
    exec("javascript:RE.setFontFamily('" + fontFamily + "');");
  }

  /**
   * loads a new font file (for example ttf)
   * @see #setFontFamily to access this font
   * @param name to access this font
   * @param url
   */
  public void LoadFont(String name, String url) {
    exec("javascript:RE.LoadFont('" + name + "','"+url+"');");
  }

  /**
   * gets a list of available fonts/font families
   * @see #setFontFamily to access one of these fonts/font families
   */
  public void getFontFamily() {
    requestJSData("javascript:RE.getFontFamily();");
  }

  /**
   * sets the font size
   * @param fontSize in steps 1 tru 7
   */
  public void setFontSize(int fontSize) {
    if (fontSize > 7 || fontSize < 1) {
      Log.e("RichEditor", "Font size should have a value between 1-7");
    }
    exec("javascript:RE.setFontSize('" + fontSize + "');");
  }

  /**
   * removes all the formats for the actual selection
   */
  public void removeFormat() {
    exec("javascript:RE.removeFormat();");
  }

  /**
   * Sets the selected text or cursor position to a specific heading level in the Rich Editor WebView.
   *
   * @param heading The heading level to set, where 1 is the highest (largest) heading and
   *                larger values represent lower (smaller) headings.
   */
  public void setHeading(int heading) {
    exec("javascript:RE.setHeading('" + heading + "');");
  }

  /**
   * Shifts the actual text selection or cursor position to the right in the Rich Editor WebView.
   * This is used to create an indentation effect.
   */
  public void setIndent() {
    exec("javascript:RE.setIndent();");
  }

  /**
   * Shifts the actual text selection or cursor position to the left in the Rich Editor WebView.
   * This is used to remove indentation.
   */
  public void setOutdent() {
    exec("javascript:RE.setOutdent();");
  }

  /**
   * Aligns the selected text or cursor position to the left in the Rich Editor WebView.
   */
  public void setAlignLeft() {
    exec("javascript:RE.setJustifyLeft();");
  }

  /**
   * Aligns the selected text or cursor position to the center in the Rich Editor WebView.
   */
  public void setAlignCenter() {
    exec("javascript:RE.setJustifyCenter();");
  }

  /**
   * Aligns the selected text or cursor position to the right in the Rich Editor WebView.
   */
  public void setAlignRight() {
    exec("javascript:RE.setJustifyRight();");
  }

  /**
   * Applies blockquote styling to the selected text or cursor position in the Rich Editor WebView.
   * This is used to create a blockquote effect.
   */
  public void setBlockquote() {
    exec("javascript:RE.setBlockquote();");
  }

  /**
   * Sets the selected text or cursor position to use bullets in the Rich Editor WebView.
   * This method is an alias for {@see #setUnorderedList()}.
   */
  public void setBullets() { setUnorderedList(); }

  /**
   * Sets the selected text or cursor position to use an unordered list (bullets) in the Rich Editor WebView.
   */
  public void setUnorderedList() { exec("javascript:RE.setUnorderedList();"); }

  /**
   * Sets the selected text or cursor position to use numbers in the Rich Editor WebView.
   * This method is an alias for {@see #setOrderedList()}.
   */
  public void setNumbers() { setOrderedList(); }

  /**
   * Sets the selected text or cursor position to use an ordered list (numbers) in the Rich Editor WebView.
   */
  public void setOrderedList() { exec("javascript:RE.setOrderedList();"); }

  /**
   * Insert a HTML string into the Rich Editor WebView.
   * Example to insert a not breakable space:
   * <pre>
   *   mEditor.insertHTML("&nbsp";)
   * </pre>
   * @param contents string with html content
   */
  public void insertHTML(String contents) {
    if (contents == null) {
       contents = "";
    }
    try {
      exec("javascript:RE.prepareInsert();");
      exec("javascript:RE.insertHTML('" + URLEncoder.encode(contents, "UTF-8") + "');");
    } catch (UnsupportedEncodingException e) {
      // No handling
    }
  }

  /**
   * Inserts a horizontal rule (line) into the Rich Editor WebView.
   * This method prepares the editor for insertion and then adds an HTML horizontal rule.
   */
  public void insertHR_Line() {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.insertHTML('<hr>');");
  }

  /**
   * Inserts a collapsible section into the Rich Editor WebView.
   *
   * @param section The title or label for the collapsible section.
   * @param content The content to be included in the collapsible section.
   */
  public void insertCollapsibleSection(String section, String content) {
    exec("javascript:RE.insertCollapsibleSection('"+section+"', '"+content+"');");
  }

  /**
   * Inserts an image into the Rich Editor as link
   * So this method can manually process the image by adjusting specific width and height to fit into different mobile screens.
   * Example to insert
   * <pre>
   *   // inserts an image with 90% page width
   *   mEditor.insertImage("https://raw.githubusercontent.com/wasabeef/art/master/chip.jpg", "Example", "90%", "", true);
   *   // inserts an image with 50% of its own width
   *   mEditor.insertImage("https://raw.githubusercontent.com/wasabeef/art/master/chip.jpg", "Example", "50%", "", false);
   *   // inserts an image 100px X 100px
   *   mEditor.insertImage("https://raw.githubusercontent.com/wasabeef/art/master/chip.jpg", "dachshund", "100","100",false);
   * </pre>
   *
   * @param url      The URI of the image to be inserted.
   * @param alt      The alternative text for the image.
   * @param width    Width of the Image; if relative=true then 100% means 100% page width.
   *                  Can be empty, if not specified in units (px or %) its px
   * @param height   Height of the Image (with units).
   *                  Can be empty, if not specified in units (px or %) its px
   * @param relative Image size is relative to page width
   */
  public void insertImage(String url, String alt, String width, String height, Boolean relative) {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.insertImage('" + url + "', '" + alt + "','" + width + "', '" + height + "', '" + relative.toString() + "');");
  }

  /**
   * Inserts an image into the Rich Editor WebView using Base64 encoding from the provided image URI.
   * This method allows manual processing of the image, adjusting width and height to fit different mobile screens.
   * Examples:
   * @param imageURI      The URI of the image to be inserted.
   * @param alt           The alternative text for the image.
   * @param width         The width of the image; if relative=true then 100% means 100% page width.
   *                       Can be empty, if not specified in units (px or %) its px
   * @param height        The height of the image.
   *                       Can be empty, if not specified in units (px or %) its px
   * @param relative      Image size is relative to page width
   * @param inSampleSize  Shrink the image size by this factor.
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
   * Will show the original size of the video.
   * So this method can manually process the image by adjusting specific width and height to fit into different mobile screens.
   *
   * @param url           The URI of the video to be inserted.
   * @param alt           The alternative text for the video.
   * @param width         The width of the video; if relative=true then 100% means 100% page width.
   *                       Can be empty, if not specified in units (px or %) its px
   * @param height        The height of the image.
   *                       Can be empty, if not specified in units (px or %) its px
   * @param relative      Video size is relative to page width
   * @param optProperties additional properties like 'autoplay muted controls loop'
   * @param height
   */
  public void insertVideo(String url, String alt, String width, String height, Boolean relative, String optProperties) {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.insertVideo('" + url + "', '" + alt + "', '" + width + "','" + height + "', '" + relative.toString() + "', '" + optProperties + "');");
  }

  /**
   * Inserts an audio element into the Rich Editor WebView at the current cursor position or selection.
   *
   * @param url The URL of the audio file to be inserted.
   * @param optProperties additional properties like 'autoplay, loop'
   */
  public void insertAudio(String url, String optProperties) {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.insertAudio('" + url + "', '" + optProperties + "');");
  }

  /**
   * Embeds another HTML page (iframe) into the current one (like for YouTube video into the Rich Editor WebView with the specified URL, width, and height.
   * This method executes JavaScript commands to prepare for video insertion and inserts the YouTube video.
   *
   * @param src    The src URL (for example https://www.openstreetmap.org/export/embed.html?bbox=12.236022949218752%2C51.28886912565582%2C12.462615966796877%2C51.39363622420581&layer=opnvkarte)
   * @param name   alternative text, if the embedded page can not be shown.
   * @param width         The width of the frame; if relative=true then 100% means 100% page width.
   *                       Can be empty, if not specified in units (px or %) its px
   * @param height        The height of the image.
   *                       Can be empty, if not specified in units (px or %) its px
   * @param relative      Frame size is relative to page width
   * @param optProperties additional properties like 'title="test", allow-forms'
   */
  public void insertIFrame(String src, String name, String width, String height, Boolean relative, String optProperties) {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.insertIFrame('" + src + "', '" + name + "', '" + "', '" + width + "', '" + height + "', '" + relative.toString() + "', '" + optProperties + "');");
  }

  /**
   * Inserts a YouTube video into the Rich Editor WebView with the specified URL, width, and height.
   * This method executes JavaScript commands to prepare for video insertion and inserts the YouTube video.
   * @see #insertIFrame
   * @param src      The YouTube video URL. (for example https://www.youtube.com/embed/3AeYHDZ2riI)
   * @param width         The width of the video; if relative=true then 100% means 100% page width
   *                       Can be empty, if not specified in units (px or %) its px
   * @param height        The height of the image
   *                       Can be empty, if not specified in units (px or %) its px
   * @param relative      Video size is relative to page width
   */
  public void insertYoutubeVideo(String src, String width, String height, Boolean relative) {
    String optProperties="frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; fullscreen\"";
    exec("javascript:RE.insertIFrame('" + src + "', '', '" + width + "', '" + height + "', '" + relative.toString() + "', '" + optProperties + "');");
  }

  /**
   * Inserts a hyperlink into the Rich Editor WebView with the specified href, text, and title.
   * This method executes JavaScript commands to prepare for link insertion and inserts the link.
   *
   * @param href  The URL to link to.
   * @param text  The text to display as the link.
   * @param title The title of the link (optional, can be empty).
   */
  public void insertLink(String href, String text, String title) {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.insertLink('" + href + "', '" + text + "', '" + title + "');");
  }

  /**
   * Inserts a check box
   */
  public void insertCheckbox() {
    exec("javascript:RE.prepareInsert();");
    exec("javascript:RE.setCheckbox();");
  }

  /**
   * Set focus
   */
  public void focusEditor() {
    requestFocus();
    exec("javascript:RE.focus();");
  }

  /**
   * Set focus on point
   * @param x
   * @param y
   */
  public void focus(Integer x, Integer y) {
    requestFocus();
    exec("javascript:RE.focusAtPoint("+ x.toString() + ", "+ y.toString() + ")");
  }

  /**
   * Clears the focus and blurs the editor
   */
  public void clearFocusEditor() {
    exec("javascript:RE.blurFocus();");
  }

  /**
   * Inserts a table with the specified number of columns and rows into the Rich Editor WebView.
   * This method executes JavaScript commands to prepare for table insertion and insert the table.
   *
   * @param col The number of columns in the table.
   * @param row The number of rows in the table.
   */
  public void insertTable(Integer col, Integer row) {
    exec("javascript:RE.prepareInsert()");
    exec("javascript:RE.insertTable("+ col.toString() + "," + row.toString() + ")");
  }

  /**
   * Checks if cursor is in a table element. If so, return true so that you can add menu items accordingly.
   *
   */
  public void isCursorInTable() {
    requestJSData("javascript:RE.isCursorInTable");
  }

  /**
   * Adds a new row to the table in the Rich Editor WebView.
   * This method executes a JavaScript command to add a new row to the table.
   */
  public void addRowToTable() {
    exec("javascript:RE.addRowToTable()");
  }

  /**
   * Deletes the currently selected row from the table in the Rich Editor WebView.
   * This method executes a JavaScript command to delete the currently selected row from the table.
   */
  public void deleteRowFromTable() {
    exec("javascript:RE.deleteRowFromTable()");
  }

  /**
   * Adds a new column to the table in the Rich Editor WebView.
   * This method executes a JavaScript command to add a new column to the table.
   */
  public void addColumnToTable() {
    exec("javascript:RE.addColumnToTable()");
  }

  /**
   * Deletes the currently selected column from the table in the Rich Editor WebView.
   * This method executes a JavaScript command to delete the currently selected column from the table.
   */
  public void deleteColumnFromTable() {
    exec("javascript:RE.deleteColumnFromTable()");
  }

  /**
   * A custom WebViewClient to handle page loading events for the RichEditor.
   * This class extends the WebViewClient and provides additional functionality
   * when a page has finished loading.
   */
  protected class EditorWebViewClient extends WebViewClient {
    /**
     * Called when the page has finished loading in the WebView.
     *
     * @param view The WebView that has finished loading the page.
     * @param url  The URL of the page that has finished loading.
     */
    @Override
    public void onPageFinished(WebView view, String url) {
      isReady = url.equalsIgnoreCase(SETUP_HTML);
      if (mLoadListener != null) {
        mLoadListener.onAfterInitialLoad(isReady);
      }
    }

    /**
     * @param view The WebView that is initiating the callback.
     * @param url The URL to be loaded.
     * @return
     */
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

    /**
     * @param view The WebView that is initiating the callback.
     * @param request Object containing the details of the request.
     * @return
     */
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
