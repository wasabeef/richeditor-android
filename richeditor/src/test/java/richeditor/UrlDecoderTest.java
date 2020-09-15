package richeditor;

import android.net.Uri;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class UrlDecoderTest {

  @Test
  public void urlDecodeTest() throws Exception {
    String encoded = "re-callback://%20%20%3Cdiv%3E%3Cfont%20face=%22Arial%22%20size=%222%22%20color=%22#333333%22%3EDJK%20Besteck%3C/font%3E%3Cb%3E%3Cfont%20face=%22Arial%22%20size=%222%22%20color=%22#333333%22%3E&nbsp;Kenntnisse++_%20++ 下 ぁ ص%3C/font%3E%3C/b%3E%3C/div%3E%20";
    String decoded = Uri.decode(encoded);

    String test = "re-callback://  <div><font face=\"Arial\" size=\"2\" color=\"#333333\">DJK Besteck</font><b><font face=\"Arial\" size=\"2\" color=\"#333333\">&nbsp;Kenntnisse++_ ++ 下 ぁ ص</font></b></div> ";

    Assert.assertEquals(test, decoded);
  }

}
