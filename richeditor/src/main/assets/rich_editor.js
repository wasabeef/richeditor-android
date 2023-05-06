/**
 * Copyright (C) 2022-2023 peter@niendo.de
 * Copyright (C) 2017 Kishan Jadav
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

 /**
  * See about document.execCommand: https://developer.mozilla.org/en-US/docs/Web/API/Document/execCommand
  */

 "use strict";

var RE = {};

RE.editor = document.getElementById('editor');

// Not universally supported, but seems to work in iOS 7 and 8
document.addEventListener("selectionchange", function() {
    RE.backuprange();
});

//looks specifically for a Range selection and not a Caret selection
RE.rangeSelectionExists = function() {
    //!! coerces a null to bool
    var sel = document.getSelection();
    if (sel && sel.type == "Range") {
        return true;
    }
    return false;
};

RE.rangeOrCaretSelectionExists = function() {
    //!! coerces a null to bool
    var sel = document.getSelection();
    if (sel && (sel.type == "Range" || sel.type == "Caret")) {
        return true;
    }
    return false;
};


RE.selectedHtml = function() {
    // https://stackoverflow.com/questions/5643635/how-to-get-selected-html-text-with-javascript
    var html = "";
    if (typeof window.getSelection != "undefined") {
        var sel = window.getSelection();
        if (sel.rangeCount) {
            var container = document.createElement("div");
            for (var i = 0, len = sel.rangeCount; i < len; ++i) {
                container.appendChild(sel.getRangeAt(i).cloneContents());
            }
            html = container.innerHTML;
        }
    } else if (typeof document.selection != "undefined") {
        if (document.selection.type == "Text") {
            html = document.selection.createRange().htmlText;
        }
    }
    return encodeURIComponent(html);
}

// Returns selected text range
RE.selectedText = function() {
    if (RE.rangeSelectionExists() == true) {
        return encodeURIComponent(document.getSelection().toString());
    }
    return "";
};

RE.editor.addEventListener("input", function() {
    RE.updatePlaceholder();
    RE.backuprange();
    RE.callback("input");
});

RE.editor.addEventListener("focus", function() {
    RE.backuprange();
    RE.callback("focus");
});

RE.editor.addEventListener("blur", function() {
    RE.callback("blur");
});

RE.customAction = function(action) {
    RE.callback("action/" + action);
};

RE.updateHeight = function() {
    RE.callback("updateHeight");
}

RE.callbackQueue = [];
RE.runCallbackQueue = function() {
    if (RE.callbackQueue.length === 0) {
        return;
    }

    setTimeout(function() {
        window.location.href = "re-callback://" + RE.getCommandQueue();
    }, 0);
};

RE.getCommandQueue = function() {
    var commands = JSON.stringify( RE.callbackQueue );
    RE.callbackQueue = [];
    return commands;
};

RE.callback = function(method) {
    RE.callbackQueue.push(method);
    RE.runCallbackQueue();
};

RE.setHtml = function(contents) {
    RE.editor.innerHTML = decodeURIComponent(contents.replace(/\+/g, '%20'));
    var images = RE.editor.querySelectorAll("img");

    for (var i = 0; i < images.length; i++) {
        images[i].onload = RE.updateHeight;
    }
    RE.setElementListener("");
    RE.updatePlaceholder();
    RE.callback("loaded");
};

RE.getHtml = function() {
   return encodeURIComponent(RE.editor.innerHTML);
};

RE.getText = function() {
   return encodeURIComponent(RE.editor.innerText);
};

RE.setBaseTextColor = function(color) {
    RE.editor.style.color  = color;
};

RE.setBaseFontSize = function(size) {
    RE.editor.style.fontSize = size;
}

RE.setPadding = function(left, top, right, bottom) {
  RE.editor.style.paddingLeft = left;
  RE.editor.style.paddingTop = top;
  RE.editor.style.paddingRight = right;
  RE.editor.style.paddingBottom = bottom;
}

RE.setPlaceholderText = function(text) {
    RE.editor.setAttribute("placeholder", text);
};

RE.updatePlaceholder = function() {
    if (RE.editor.innerHTML.indexOf('img') !== -1 || (RE.editor.textContent.length > 0 && RE.editor.innerHTML.length > 0)) {
        RE.editor.classList.remove("placeholder");
    } else {
        RE.editor.classList.add("placeholder");
    }
};

RE.setFontSize = function(fontSize) {
    document.execCommand("fontSize", false, fontSize);
    //RE.editor.style.fontSize = size;
};

RE.setBackgroundColor = function(color) {
    RE.editor.style.backgroundColor = color;
};

RE.setHeight = function(size) {
    RE.editor.style.height = size;
};

RE.setTextAlign = function(align) {
    RE.editor.style.textAlign = align;
}

RE.setVerticalAlign = function(align) {
    RE.editor.style.verticalAlign = align;
}

RE.setInputEnabled = function(inputEnabled) {
    RE.editor.contentEditable = String(inputEnabled);
}

RE.undo = function() {
    document.execCommand('undo', false, null);
};

RE.redo = function() {
    document.execCommand('redo', false, null);
};

RE.setBold = function(shouldEnable) {
    let isBold = document.queryCommandState('bold');

    if (!isBold && shouldEnable)
    {
        // Enable bold formatting
        // -----------------------------
        // If bold formatting is not enabled
        // then toggle it to enable.
        RE.toggleBold();

    } else if (isBold && !shouldEnable)
    {
        // Disable bold formatting
        // -----------------------------
        // If bold formatting is enabled
        // then toggle it to disable.
        RE.toggleBold();
    }
}

RE.toggleBold = function() {
    document.execCommand('bold', false, null);
}

RE.setItalic = function(shouldEnable) {
    let isItalic = document.queryCommandState('italic');

    if (!isItalic && shouldEnable)
    {
        // Enable italic formatting
        // -----------------------------
        // If italic formatting is not enabled
        // then toggle it to enable.
        RE.toggleItalic();

    } else if (isItalic && !shouldEnable)
    {
        // Disable italic formatting
        // -----------------------------
        // If italic formatting is enabled
        // then toggle it to disable.
        RE.toggleItalic();
    }
}

RE.toggleItalic = function() {
    document.execCommand('italic', false, null);
}

RE.setSubscript = function() {
    document.execCommand('subscript', false, null);
}

RE.setSuperscript = function() {
    document.execCommand('superscript', false, null);
}

RE.setStrikeThrough = function(shouldEnable) {
    let isStrikeThrough = document.queryCommandState('strikeThrough');

    if (!isStrikeThrough && shouldEnable)
    {
        // Enable strikethrough formatting
        // -----------------------------
        // If strikethrough formatting is not enabled
        // then toggle it to enable.
        RE.toggleStrikeThrough();

    } else if (isStrikeThrough && !shouldEnable)
    {

   // Disable strikethrough formatting
        // -----------------------------
        // If strikethrough formatting is enabled
        // then toggle it to disable.
        RE.toggleStrikeThrough();
    }
}

RE.toggleStrikeThrough = function() {
    document.execCommand('strikeThrough', false, null);
}

RE.setUnderline = function(shouldEnable) {
    let isUnderline = document.queryCommandState('underline');

    if (!isUnderline && shouldEnable)
    {
        // Enable underline formatting
        // -----------------------------
        // If underline formatting is not enabled
        // then toggle it to enable.
        RE.toggleUnderline();

    } else if (isUnderline && !shouldEnable)
    {
        // Disable underline formatting
        // -----------------------------
        // If underline formatting is enabled
        // then toggle it to disable.
        RE.toggleUnderline();
    }
}

RE.toggleUnderline = function() {
    document.execCommand('underline', false, null);
}

RE.setTextColor = function(color) {
    RE.restorerange();
    document.execCommand("styleWithCSS", null, true);
    document.execCommand('foreColor', false, color);
    document.execCommand("styleWithCSS", null, false);
};

RE.setTextBackgroundColor = function(color) {
    RE.restorerange();
    document.execCommand("styleWithCSS", null, true);
    document.execCommand('hiliteColor', false, color);
    document.execCommand("styleWithCSS", null, false);
};

RE.setFontFamily = function(fontName){
    document.execCommand('fontName', false, fontName);
}

RE.LoadFont = function LoadFont(name, url) {
  let font = new FontFace(name, 'url("'+ url +'")');
  font.load().then(function(loadedFont)
  {
      document.fonts.add(loadedFont);
      //do something after the font is loaded
  }).catch(function(error) {
      RE.insertHTML(error)
      // error occurred
  });
}
RE.getFontFamily = function getFontFamily() {
   var arr = [];
   var it = document.fonts.entries();

  var arr = [];
  let done = false;

  while (!done) {
    const font = it.next();
    if (!font.done) {
      arr.push(font.value[0].family);
    } else {
      done = font.done;
    }
  }
    arr.push('monospace');
    arr.push('sans-serif');
    arr.push('serif');
    arr.push('cursive');
    arr.push('fantasy');
  // converted to set then arr to filter repetitive values
  return String(arr);
}

RE.setPre = function() {
      document.execCommand('formatBlock', false, '<pre>');
      RE.setNewParagraph();

}

RE.setNewParagraph = function() {
        var elements = document.querySelectorAll(":hover");
        var e=document.createElement("p");
        e.innerHTML = '<br>';
        elements[elements.length - 1].appendChild(e);
}

RE.setHeading = function(heading) {
    var sel = document.getSelection().getRangeAt(0).startContainer.parentNode;
    document.execCommand('formatBlock', false, sel.tagName === `H${heading}` ? '<p>' : `<h${heading}>`);
};

RE.setIndent = function() {
    document.execCommand('indent', false, null);
};

RE.setOutdent = function() {
    document.execCommand('outdent', false, null);
};

RE.setOrderedList = function() {
    // setNumbers
    document.execCommand('insertOrderedList', false, null);
};

RE.setUnorderedList = function() {
    // setBullets
    document.execCommand('insertUnorderedList', false, null);
};

function createCheckbox(node) {
    var d = document.createElement("input");
    d.setAttribute("type", "checkbox");
    if(node.checked) {
        d.setAttribute("checked", true);
    }
    d.addEventListener("change", function() {createCheckbox(this);});

    node.parentNode.insertBefore(d, node);
    node.parentNode.removeChild(node);
};

RE.setCheckbox = function() {
  var el = document.createElement("input");
    el.setAttribute("type", "checkbox");
    RE.insertHTML("&nbsp;" + el.outerHTML + "&nbsp;");
    RE.setElementListener("checkbox");
    //RE.callback("input");
};

RE.setJustifyLeft = function() {
    document.execCommand('justifyLeft', false, null);
};

RE.setJustifyCenter = function() {
    document.execCommand('justifyCenter', false, null);
};

RE.setJustifyRight = function() {
    document.execCommand('justifyRight', false, null);
};

RE.getLineHeight = function() {
    return RE.editor.style.lineHeight;
};

RE.setLineHeight = function(height) {
    RE.editor.style.lineHeight = height;
};

RE.setBlockquote = function() {
    document.execCommand('formatBlock', false, '<blockquote>');
    RE.setNewParagraph();
};

RE.insertImage = function(url, alt="", width="", height="", relative="false") {
    var img = document.createElement('img');
    img.setAttribute("src", url);
    if (alt != "") img.setAttribute("alt", alt);
    if (relative == "true") {
       if (width == "") width = "100";
         if (height == "")
             img.setAttribute("style","width: "+width+"%;");
         else
             img.setAttribute("style","width: "+width+"%; height: "+height+"%");
    } else {
       if (width != "")
          img.setAttribute("width", width);
       if(height != "")
          img.setAttribute("height", height);
    }
    img.onload = RE.updateHeight;
    RE.insertHTML(img.outerHTML);
    //RE.callback("input");
};

RE.insertVideo = function(url, alt="", width="", height="") {
    var video = document.createElement('video');
    video.setAttribute("src", url);
    video.controls = true;
    video.muted = false;
    if (alt != "") video.setAttribute("alt", alt);

    if (width == "auto")
          video.setAttribute("width", "responsive-image");
    else if (width != "") {
          video.setAttribute("width", width);
          if (height != "") video.setAttribute("height", height);
    }
    video.onload = RE.updateHeight;

    RE.insertHTML(video.outerHTML);
    //RE.callback("input");
}

RE.insertAudio = function(url, alt) {
    var html = '<audio src="' + url + '" controls></audio><br>';
    RE.insertHTML(html);
}

RE.insertYoutubeVideo = function(url, width="100%", height="100%") {
    var html = '<iframe width="' + width + '" height="' + height + '" src="' + url + '" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe><br>'
    RE.insertHTML(html);
}

RE.setElementListener = function(element) {
if (element=="section" || element=="") {
  var coll = document.getElementsByClassName("collapsible");
  var i;

  for (i = 0; i < coll.length; i++) {
      coll[i].addEventListener("click", function() {CollapsibleSection(this);});
  }
}

if (element=="checkbox" || element=="") {
  var coll = document.querySelectorAll('input[type=checkbox]');
  var i;

  for (i = 0; i < coll.length; i++) {
     coll[i].addEventListener("change", function() {createCheckbox(this);});
  }
}

if (element=="link" || element=="") {
  var coll = document.querySelectorAll('a');
  var i;

  for (i = 0; i < coll.length; i++) {
     coll[i].addEventListener("click", function() {
        var ret = [];
        ret.push({"tagName":this.tagName});
        ret.push({"href":this.href});
        ret.push({"text":this.innerHTML});
        ret.push({"title":this.title});
        window.location.href = "re-click://" + JSON.stringify(ret);
        }
     );
  }
}
}

function CollapsibleSection(node) {
          node.classList.toggle("active");
          var content = node.nextElementSibling;
          if (content.style.maxHeight){
             content.style.maxHeight = null;
          } else {
             content.style.maxHeight = content.scrollHeight + "px";
          }
        }

RE.insertCollapsibleSection = function(section, content) {
    var d = document.createElement("button");
        d.setAttribute("class", "collapsible");
        d.innerHTML = "&nbsp;" + section;
       // d.addEventListener("click", function() {CollapsibleSection(this);});
    var elements = document.querySelectorAll(":hover");
    d=elements[elements.length - 1].appendChild(d);
    var e=document.createElement("div");
    e.setAttribute("class", "content");
    e.innerHTML = '<p> ' + content + '<br><br></p>';
    elements[elements.length - 1].appendChild(e);
    // next empty element
    e=document.createElement("p");
    e.innerHTML = '<br>';
    elements[elements.length - 1].appendChild(e);
     //RE.callback("input");
     RE.setElementListener("section");
}

RE.insertHTML = function(html) {
    RE.restorerange();
    document.execCommand('insertHTML', false, html);
};

RE.insertLink = function(url, text, title) {
    RE.restorerange();
    document.execCommand("insertHTML",false,"<a href='"+url+"' title='"+title+"'>"+text+"</a>");
    RE.setElementListener("link");
    //RE.callback("input");
};

RE.insertLinkSelection = function(url, text, title) {
    RE.restorerange();
    var sel = document.getSelection();
    if (sel.toString().length == 0) {
        document.execCommand("insertHTML",false,"<a href='"+url+"' title='"+title+"'>"+text+"</a>");
    } else if (sel.rangeCount) {
        var el = document.createElement("a");
        el.setAttribute("href", url);
        el.setAttribute("title", title);

        var range = sel.getRangeAt(0).cloneRange();
        range.surroundContents(el);
        sel.removeAllRanges();
        sel.addRange(range);
    }
    //RE.callback("input");
};

RE.prepareInsert = function() {
    RE.backuprange();
};

RE.backuprange = function() {
    var selection = window.getSelection();
    if (selection.rangeCount > 0) {
      var range = selection.getRangeAt(0);
      RE.currentSelection = {
          "startContainer": range.startContainer,
          "startOffset": range.startOffset,
          "endContainer": range.endContainer,
          "endOffset": range.endOffset
      };
    }
};

RE.addRangeToSelection = function(selection, range) {
    if (selection) {
        selection.removeAllRanges();
        selection.addRange(range);
    }
};

// Programatically select a DOM element
RE.selectElementContents = function(el) {
    var range = document.createRange();
    range.selectNodeContents(el);
    var sel = window.getSelection();
    // this.createSelectionFromRange sel, range
    RE.addRangeToSelection(sel, range);
};

RE.restorerange = function() {
    var selection = window.getSelection();
    selection.removeAllRanges();
    var range = document.createRange();
    range.setStart(RE.currentSelection.startContainer, RE.currentSelection.startOffset);
    range.setEnd(RE.currentSelection.endContainer, RE.currentSelection.endOffset);
    selection.addRange(range);
};

// User editing table functionality
RE.insertTable = function(width, height) {
    var table = document.createElement("table");
    for (let i = 0; i < height; i++) {
        var row = table.insertRow();
        for (let j = 0; j < width; j++) {
            var cell = row.insertCell();
        }
    }
    RE.insertHTML(table.outerHTML);
    RE.setNewParagraph();
    //RE.callback("input");
};

function getNearestTableAncestor(htmlElementNode) {
    while (htmlElementNode) {
        htmlElementNode = htmlElementNode.parentNode;
        if (htmlElementNode.tagName.toLowerCase() === 'table') {
            return htmlElementNode;
        }
    }
    return undefined;
}

RE.isCursorInTable = function() {
    return document.querySelectorAll(":hover")[elements.length - 1] instanceof HTMLTableCellElement  
};

RE.addRowToTable = function() {
    // Add row below current cursor's
    var elements = document.querySelectorAll(":hover");
    let rowIndex = elements[elements.length - 2].rowIndex;
    let table = getNearestTableAncestor(elements[elements.length - 1]);
    let columns = table.rows[rowIndex].cells.length;
    var row = table.insertRow(rowIndex + 1);
    for (let j = 0; j < columns ; j++) {
        var cell = row.insertCell();
    }
};

RE.deleteRowFromTable = function() {
    // Deletes the current cursor's row
    var elements = document.querySelectorAll(":hover");
    let rowIndex = elements[elements.length - 2].rowIndex;
    let table = getNearestTableAncestor(elements[elements.length - 1]);
    table.deleteRow(rowIndex);
};

RE.addColumnToTable = function() {
    // Add column to the right of current cursor's
    var elements = document.querySelectorAll(":hover");
    let columnIndex = elements[elements.length - 1].cellIndex;
    let row = elements[elements.length - 2];
    row.insertCell(columnIndex + 1);
}

RE.deleteColumnFromTable = function() {
    // Deletes the current cursor's column
    var elements = document.querySelectorAll(":hover");
    let columnIndex = elements[elements.length - 1].cellIndex;
    let row = elements[elements.length - 2];
    row.deleteCell(columnIndex);
};

/**
Recursively search element ancestors to find a element nodeName e.g. A
**/
var _findNodeByNameInContainer = function(element, nodeName, rootElementId) {
    if (element.nodeName == nodeName) {
        return element;
    } else {
        if (element.id === rootElementId) {
            return null;
        }
        _findNodeByNameInContainer(element.parentElement, nodeName, rootElementId);
    }
};

var isAnchorNode = function(node) {
    return ("A" == node.nodeName);
};

RE.getAnchorTagsInNode = function(node) {
    var links = [];

    while (node.nextSibling !== null && node.nextSibling !== undefined) {
        node = node.nextSibling;
        if (isAnchorNode(node)) {
            links.push(node.getAttribute('href'));
        }
    }
    return links;
};

RE.countAnchorTagsInNode = function(node) {
    return RE.getAnchorTagsInNode(node).length;
};

/**
 * If the current selection's parent is an anchor tag, get the href.
 * @returns {string}
 */
RE.getSelectedHref = function() {
    var href, sel;
    href = '';
    sel = window.getSelection();
    if (!RE.rangeOrCaretSelectionExists()) {
        return null;
    }

    var tags = RE.getAnchorTagsInNode(sel.anchorNode);
    //if more than one link is there, return null
    if (tags.length > 1) {
        return null;
    } else if (tags.length == 1) {
        href = tags[0];
    } else {
        var node = _findNodeByNameInContainer(sel.anchorNode.parentElement, 'A', 'editor');
        href = node.href;
    }
    return href ? href : null;
};

// Returns the cursor position relative to its current position onscreen.
// Can be negative if it is above what is visible
RE.getRelativeCaretYPosition = function() {
    var y = 0;
    var sel = window.getSelection();
    if (sel.rangeCount) {
        var range = sel.getRangeAt(0);
        var needsWorkAround = (range.startOffset == 0)
        /* Removing fixes bug when node name other than 'div' */
        // && range.startContainer.nodeName.toLowerCase() == 'div');
        if (needsWorkAround) {
            y = range.startContainer.offsetTop - window.pageYOffset;
        } else {
            if (range.getClientRects) {
                var rects = range.getClientRects();
                if (rects.length > 0) {
                    y = rects[0].top;
                }
            }
        }
    }
    return y;
};

window.onload = function() {
    RE.callback("ready");
};

RE.enabledEditingItems = function(e) {
    var items = [];
    if (document.queryCommandState('bold')) {
        items.push('bold');
    }
    if (document.queryCommandState('italic')) {
        items.push('italic');
    }
    if (document.queryCommandState('subscript')) {
        items.push('subscript');
    }
    if (document.queryCommandState('superscript')) {
        items.push('superscript');
    }
    if (document.queryCommandState('strikeThrough')) {
        items.push('strikeThrough');
    }
    if (document.queryCommandState('underline')) {
        items.push('underline');
    }
    if (document.queryCommandState('insertOrderedList')) {
        items.push('orderedList');
    }
    if (document.queryCommandState('insertUnorderedList')) {
        items.push('unorderedList');
    }
    if (document.queryCommandState('justifyCenter')) {
        items.push('justifyCenter');
    }
    if (document.queryCommandState('justifyFull')) {
        items.push('justifyFull');
    }
    if (document.queryCommandState('justifyLeft')) {
        items.push('justifyLeft');
    }
    if (document.queryCommandState('justifyRight')) {
        items.push('justifyRight');
    }
    if (document.queryCommandState('insertHorizontalRule')) {
        items.push('horizontalRule');
    }
    var formatBlock = document.queryCommandValue('formatBlock');
    if (formatBlock.length > 0) {
        items.push(formatBlock);
    }

    window.location.href = "re-state://" + encodeURI(items.join(','));
}

RE.focus = function() {
    var range = document.createRange();
    range.selectNodeContents(RE.editor);
    range.collapse(false);
    var selection = window.getSelection();
    selection.removeAllRanges();
    selection.addRange(range);
    RE.editor.focus();
};

RE.focusAtPoint = function(x, y) {
    var range = document.caretRangeFromPoint(x, y) || document.createRange();
    var selection = window.getSelection();
    selection.removeAllRanges();
    selection.addRange(range);
    RE.editor.focus();
};

RE.blurFocus = function() {
    RE.editor.blur();
};

RE.removeFormat = function() {
    document.execCommand('formatBlock', false, 'p');
    document.execCommand('removeFormat', false, null);
};

/*
// Event Listeners
RE.editor.addEventListener("input", RE.callback);
RE.editor.addEventListener("keyup", function(e) {
    var KEY_LEFT = 37, KEY_RIGHT = 39;
    if (e.which == KEY_LEFT || e.which == KEY_RIGHT) {
        RE.enabledEditingItems(e);
    }
});
RE.editor.addEventListener("click", RE.enabledEditingItems);
*/

//https://stackoverflow.com/questions/18552336/prevent-contenteditable-adding-div-on-enter-chrome
RE.editor.addEventListener("keydown", function(e) {
let isList=document.queryCommandState('insertOrderedList') || document.queryCommandState('insertUnorderedList');
 if(e.keyCode===13 && !isList){ //enter && shift
   document.execCommand('insertLineBreak');
   e.preventDefault(); //Prevent default browser behavior
   }
});
