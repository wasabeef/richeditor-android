RichText for Android
=============

`RichText for Android` is a beautiful Rich Text `WYSIWYG Editor` for `Android`.

Supported Functions
---
*   Bold
*   Italic
*   Subscript
*   Superscript
*   Strikethrough
*   Underline
*   Justify Left
*   Justify Center
*   Justify Right
*   Heading 1
*   Heading 2
*   Heading 3
*   Heading 4
*   Heading 5
*   Heading 6
*   Undo
*   Redo
*   Indent
*   Outdent
*   Insert Image
*   Insert Link
*   Text Color
*   Background Color

Demo
---

How do I use it?
---

### Setup

##### Gradle
```groovy
repositories {
    jcenter()
}

dependencies {
}
```

### Functions

**Bold**
```java
RichEditor editor = (RichEditor) findViewById(R.id.editor);
editor.setBold();
```

**Italic**
```java
RichEditor editor = (RichEditor) findViewById(R.id.editor);
editor.setItalic();
```

**Insert Image**
```java
RichEditor editor = (RichEditor) findViewById(R.id.editor);
editor.insertImage("https://raw.githubusercontent.com/wasabeef/art/master/twitter.png","twitter");
```

**Others**  
Please refer the [samples](https://github.com/wasabeef/richeditor-android/blob/master/sample/src/main/java/jp/wasabeef/sample/MainActivity.java) for usage.

Requirements
--------------
Android 4+

Developed By
-------
Daichi Furiya (Wasabeef) - <dadadada.chop@gmail.com>

<a href="https://twitter.com/wasabeef_jp">
<img alt="Follow me on Twitter"
src="https://raw.githubusercontent.com/wasabeef/art/master/twitter.png" width="75"/>
</a>

Thanks
-------

* Inspired by `ZSSRichTextEditor` in [nnhubbard](https://github.com/nnhubbard/ZSSRichTextEditor).

License
-------

    Copyright 2015 Wasabeef

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
