package jp.wasabeef.richeditor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

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

public final class Utils {

  private Utils() throws InstantiationException {
    throw new InstantiationException("This class is not for instantiation");
  }

  public static String toBase64(Bitmap bitmap, String type) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    if (type.contains("jpg") || type.contains("jpeg"))
      bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
    else if (type.contains("png"))
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
    else if (type.contains("webp"))
      bitmap.compress(Bitmap.CompressFormat.WEBP, 100, baos);

    return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
  }

  public static Bitmap toBitmap(Drawable drawable) {
    if (drawable instanceof BitmapDrawable) {
      return ((BitmapDrawable) drawable).getBitmap();
    }

    int width = drawable.getIntrinsicWidth();
    width = width > 0 ? width : 1;
    int height = drawable.getIntrinsicHeight();
    height = height > 0 ? height : 1;

    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    drawable.draw(canvas);

    return bitmap;
  }

  public static Bitmap decodeResource(Context context, int resId) {
    return BitmapFactory.decodeResource(context.getResources(), resId);
  }

}
