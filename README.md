<p style="text-align: center;">
  <img src="https://github.com/niendo1/richeditor-android/raw/master/art/logo.png" width="480px" alt="Logo"/>
</p>
<p style="text-align: center;">
<a href="https://android-arsenal.com/details/1/1696"><img src="https://img.shields.io/badge/Android%20Arsenal-richeditor--android-brightgreen.svg?style=flat" alt="Android Arsenal"></a> 
<a href="https://www.apache.org/licenses/LICENSE-2.0"><img src="https://img.shields.io/badge/license-Apache%202-blue.svg" alt="License Apache 2.0"/></a>
<a href="https://jitpack.io/#niendo1/richeditor-android"><img src="https://jitpack.io/v/niendo1/richeditor-android.svg" alt="JitPack-Lib"/></a>
<a href="https://javadoc.jitpack.io/com/github/niendo1/richeditor-android/latest/javadoc/"><img src="https://github.com/niendo1/richeditor-android/raw/master/art/javadoc.svg" alt="JavaDoc"/></a>
</p>

---

This is a fork, which tries to merge changes from [Andrew-Chen-Wang/RichEditorView](https://github.com/Andrew-Chen-Wang/RichEditorView)

---

`RichEditor for Android` is a beautiful HTML `WYSIWYG Editor` for `Android` based on webview.

- _Looking for iOS? Check out_ [cjwirth/RichEditorView](https://github.com/cjwirth/RichEditorView)

Supported Functions
---

![Toolbar](./art/demo.gif)

- [x] (new?) Remove Format
- [x] Bold
- [x] Italic
- [x] Subscript
- [x] Superscript
- [x] Strikethrough
- [x] Underline
- [x] Justify Left
- [x] Justify Center
- [x] Justify Right
- [x] Blockquote
- [x] (new) Pre-Section
- [x] Heading 1
- [x] Heading 2
- [x] Heading 3
- [x] Heading 4
- [x] Heading 5
- [x] Heading 6
- [x] Undo
- [x] Redo
- [x] Indent
- [x] Outdent
- [x] (new) Insert HTML Code
- [x] (new) Insert Horizontal Line
- [x] Insert Image
- [x] (new) Insert Inline Image
- [x] Insert Youtube
- [x] Insert iframe (new)
- [x] Insert Video
- [x] Insert Audio
- [x] Insert Link
- [x] Checkbox
- [x] Text Color
- [x] (new) Text Color (String)
- [x] Text Background Color
- [x] (new) Text Background Color (String)
- [x] (new) Font Family
- [x] Text Font Size
- [x] Unordered List (Bullets)
- [x] Ordered List (Numbers)
- [x] (new) Get Selected Text
- [x] (new) Get Selected Href
- [x] (new) Table
- [x] (new) Collapsible Section
- [x] (new) Run and acquire data direct from JavaScript (requestJSData)

Attribute change of editor
---
- [x] Font Size
- [x] Background Color
- [x] Width
- [x] Height
- [x] Placeholder
- [x] Load CSS
- [x] (new) Load Font
- [x] (new) getFontFamily
- [x] State Callback 

Demo
---

![Demo](./art/demo2.gif)

How do I use it?
---

### Setup

##### Gradle

```gradle
repositories {
   mavenCentral()
   maven { url "https://jitpack.io" }
}

dependencies {
  implementation 'com.github.niendo1:richeditor-android:3.0.0'
}
```

##### Maven

```xml
<repositories>
  <repository>
  <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
  </repositories>
```

```xml
<dependency>
	    <groupId>com.github.niendo1</groupId>
        <artifactId>richeditor-android</artifactId>
	    <version>3.0.0</version>
</dependency>

```

##### leiningen

```leiningen

:repositories [["jitpack" "https://jitpack.io"]]
:dependencies [[com.github.niendo1/richeditor-android "3.0.0"]]

```

##### sbt

```sbt

resolvers += "jitpack" at "https://jitpack.io"
libraryDependencies += "com.github.niendo1" % "richeditor-android" % "3.0.0"

```

### Default Setting for Editor
---

**Height**
```java
editor.setEditorHeight(200);
```

**Font**
```java
editor.setEditorFontSize(22);
editor.setEditorFontColor(Color.RED);
```

**Background**
```java
editor.setEditorBackgroundColor(Color.BLUE);
editor.setBackgroundColor(Color.BLUE);
editor.setBackgroundResource(R.drawable.bg);
editor.setBackground("https://raw.githubusercontent.com/wasabeef/art/master/chip.jpg");
```

**Padding**
```java
editor.setPadding(10, 10, 10, 10);
```

**Placeholder**
```java
editor.setPlaceholder("Insert text here...");
```

**Others**  
Please refer
the [samples](https://github.com/niendo1/richeditor-android/blob/master/sample/src/main/java/jp/wasabeef/sample/MainActivity.java)
for usage.

### Functions for ContentEditable
---

**Bold**
```java
editor.setBold();
```

**Italic**
```java
editor.setItalic();
```

**Insert Image**
```java
editor.insertImage("https://raw.githubusercontent.com/wasabeef/art/master/twitter.png","twitter");
```

**Text Change Listener**
```java
RichEditor editor = (RichEditor) findViewById(R.id.editor);
editor.setOnTextChangeListener(new RichEditor.OnTextChangeListener() {
  @Override
  public void onTextChange(String text) {
    // Do Something
     Log.d("RichEditor", "Preview " + text);
  }
});
```

**Others**  
Please refer
the [samples](https://github.com/niendo1/richeditor-android/blob/master/sample/src/main/java/jp/wasabeef/sample/MainActivity.java)
for usage.

Requirements
--------------
Android 7+ (Level 24)

Applications using RichEditor for Android
---

Please [ping](mailto:peter@niendo.de) me or send a pull request if you would like to be added here.

| Icon                                                                                                                                               | Application                                                                         |
|----------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------|
| <img src="https://lh6.ggpht.com/6zKH_uQY1bxCwXL4DLo_uoFEOXdShi3BgmN6XRHlaJ-oA1svmq6y1PZkmO50nWQn2Lg=w300-rw" width="48" height="48" />             | [Ameba Ownd](https://play.google.com/store/apps/details?id=jp.co.cyberagent.madrid) |
| <img src="https://lh3.googleusercontent.com/st_DiIlM148vzG23ccujtBzx0tMeb7cDC5fDmLSERS-Nr8M_F-PTw4W_jWJsH9mO_b4=w300-rw" width="48" height="48" /> | [ScorePal](https://play.google.com/store/apps/details?id=com.hfd.scorepal)          |
| <img src="https://github.com/niendo1/ImapNotes3/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="48" height="48" />             | [ImapNotes3](https://f-droid.org/packages/de.niendo.ImapNotes3/)                    |

Developed By
-------
Daichi Furiya (Wasabeef) - <dadadada.chop@gmail.com>

<a href="https://twitter.com/wasabeef_jp">
<img alt="Follow me on Twitter" src="https://raw.githubusercontent.com/wasabeef/art/master/twitter.png" width="75"/>
</a>

Peter Korf (niendo) - <peter@niendo.de>

Thanks
-------

* Inspired by `ZSSRichTextEditor` in [nnhubbard](https://github.com/nnhubbard/ZSSRichTextEditor).

License
-------

    Copyright (C) 2022-2023 niendo
    Copyright (C) 2020 Wasabeef

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
