package jp.wasabeef.sample;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import jp.wasabeef.richeditor.RichEditor;

public class MainActivity extends AppCompatActivity {

  private RichEditor mEditor;
  private TextView mPreview;
  private int fontsize=4;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mEditor = (RichEditor) findViewById(R.id.editor);
    mEditor.setEditorHeight(200);
    mEditor.setEditorFontSize(22);
    mEditor.setEditorFontColor(Color.RED);
    //mEditor.setEditorBackgroundColor(Color.BLUE);
    //mEditor.setBackgroundColor(Color.BLUE);
    //mEditor.setBackgroundResource(R.drawable.bg);
    mEditor.setPadding(10, 10, 10, 10);
    //mEditor.setBackground("https://raw.githubusercontent.com/wasabeef/art/master/chip.jpg");
    mEditor.setPlaceholder("Insert text here...");
    //mEditor.setInputEnabled(false);
    mEditor.LoadFont("Alita Brush","Alita Brush.ttf");
    mPreview = (TextView) findViewById(R.id.preview);

    mEditor.setOnTextChangeListener(new RichEditor.OnTextChangeListener() {
      @Override
      public void onTextChange(String text) {
        mEditor.setOnJSDataListener(value -> {
          mPreview.setText(value);
        });
        mEditor.getHtml();
      }
    });

    findViewById(R.id.action_undo).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.undo();
      }
    });

    findViewById(R.id.action_redo).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.redo();
      }
    });

    findViewById(R.id.action_remove_format).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.removeFormat();
      }
    });

    findViewById(R.id.action_pre).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.setPre();
      }
    });

    findViewById(R.id.action_bold).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.toggleBold();
      }
    });

    findViewById(R.id.action_italic).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.toggleItalic();
      }
    });

    findViewById(R.id.action_subscript).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.setSubscript();
      }
    });

    findViewById(R.id.action_superscript).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.setSuperscript();
      }
    });

    findViewById(R.id.action_strikethrough).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.toggleStrikeThrough();
      }
    });

    findViewById(R.id.action_underline).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.toggleUnderline();
      }
    });

    findViewById(R.id.action_fontsize).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (fontsize==4) fontsize=7;
          else
          fontsize=4;
        mEditor.setFontSize(fontsize);
      }
    });



    findViewById(R.id.action_font_cursive).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
       //  mEditor.setFontFamily("Alita Brush");
       mEditor.setFontFamily("cursive");
       // mEditor.getFontFamily();
      }
    });

    findViewById(R.id.action_heading1).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.setHeading(1);
      }
    });

    findViewById(R.id.action_heading2).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.setHeading(2);
      }
    });

    findViewById(R.id.action_heading3).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.setHeading(3);
      }
    });

    findViewById(R.id.action_heading4).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.setHeading(4);
      }
    });

    findViewById(R.id.action_heading5).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.setHeading(5);
      }
    });

    findViewById(R.id.action_heading6).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.setHeading(6);
      }
    });

    findViewById(R.id.action_txt_color).setOnClickListener(new View.OnClickListener() {
      private boolean isChanged;

      @Override
      public void onClick(View v) {
        // 1. get the selected text via callback
        // 2. set the text color of the selection. the color is the selection it self
        //    so the selected text is for example 'green', the colored green
        mEditor.setOnJSDataListener(new RichEditor.onJSDataListener() {
          @Override public void onDataReceived(String value) {
            if(!value.isEmpty())
                mEditor.setTextColor(value);
            }
        });

        mEditor.getSelectedText();
        //mEditor.setTextColor(isChanged ? Color.BLACK : Color.RED);
        //isChanged = !isChanged;
      }
    });

    findViewById(R.id.action_bg_color).setOnClickListener(new View.OnClickListener() {
      private boolean isChanged;

      @Override
      public void onClick(View v) {
        mEditor.setTextBackgroundColor(isChanged ? Color.TRANSPARENT : Color.YELLOW);
        isChanged = !isChanged;
      }
    });

    findViewById(R.id.action_indent).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.setIndent();
      }
    });

    findViewById(R.id.action_outdent).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.setOutdent();
      }
    });

    findViewById(R.id.action_align_left).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.setAlignLeft();
      }
    });

    findViewById(R.id.action_align_center).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.setAlignCenter();
      }
    });

    findViewById(R.id.action_align_right).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.setAlignRight();
      }
    });

    findViewById(R.id.action_blockquote).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.setBlockquote();
      }
    });

    findViewById(R.id.action_insert_bullets).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.setUnorderedList();
      }
    });

    findViewById(R.id.action_insert_numbers).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.setOrderedList();
      }
    });

    findViewById(R.id.action_insert_html).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.insertHTML("&#11088;");
      }
    });

    findViewById(R.id.action_insert_hrline).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.insertHR_Line();
      }
    });

    findViewById(R.id.action_insert_section).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.insertCollapsibleSection("Section","content");
      }
    });


    findViewById(R.id.action_insert_image).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String image;
        image=getContentResolver().SCHEME_ANDROID_RESOURCE +
          "://" + getResources().getResourcePackageName(R.drawable.insert_image)
          + '/' + getResources().getResourceTypeName(R.drawable.insert_image) + '/' + getResources().getResourceEntryName(R.drawable.bg_color);
       //  image="https://upload.wikimedia.org/wikipedia/commons/thumb/d/d4/Hyoscyamus_niger_0003.JPG/449px-Hyoscyamus_niger_0003.JPG";
       // image="https://raw.githubusercontent.com/wasabeef/art/master/chip.jpg";
        //mEditor.insertImageAsBase64(Uri.parse(image),"alt","auto","");
        mEditor.insertImage("https://raw.githubusercontent.com/wasabeef/art/master/chip.jpg", "dachshund", "","",true);
      }
    });

    findViewById(R.id.action_insert_youtube).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // 1. get the selected text via callback
        // 2. make the embedded video
        mEditor.setOnJSDataListener(new RichEditor.onJSDataListener() {
          @Override public void onDataReceived(String value) {
            if(!value.isEmpty()) {
              if(value.startsWith("https://www.youtube.com"))
                value = value.replace("watch?v=","embed/");

              // https://www.youtube.com/watch?v=3AeYHDZ2riI
              // https://www.youtube.com/embed/3AeYHDZ2riI

              mEditor.insertYoutubeVideo(value);
            }
            else
              mEditor.insertHTML("Select a youtube link");
          }
        });
        mEditor.getSelectedText();
        //mEditor.insertYoutubeVideo("https://www.youtube.com/embed/pS5peqApgUA");
      }



    });

    findViewById(R.id.action_insert_audio).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.insertAudio("https://file-examples-com.github.io/uploads/2017/11/file_example_MP3_5MG.mp3");
      }
    });

    findViewById(R.id.action_insert_video).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.insertVideo("https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/1080/Big_Buck_Bunny_1080_10s_10MB.mp4","TestVideo" ,"360","");
      }
    });

    findViewById(R.id.action_insert_link).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.insertLink("https://github.com/wasabeef", "https://github.com/wasabeef", "wasabeef");
      }
    });

    findViewById(R.id.action_insert_checkbox).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.insertCheckbox();
      }
    });

    findViewById(R.id.action_insert_table_2x2).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) { mEditor.insertTable(2,2);
      }
    });

    findViewById(R.id.action_insert_row).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.addRowToTable();
      }
    });

    findViewById(R.id.action_insert_column).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) { mEditor.addColumnToTable();
      }
    });

    findViewById(R.id.action_delete_row).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.deleteRowFromTable();
      }
    });

    findViewById(R.id.action_delete_column).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mEditor.deleteColumnFromTable();
      }
    });
  }
}
