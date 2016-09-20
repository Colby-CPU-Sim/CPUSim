/**
 * Sample code by Mark Johnson, JavaWorld, April 2000.
 * Code is may be used for any legal purpose, including commercial
 * purposes, with no warranty expressed or implied.
 * email: mark.johnson@javaworld.com
 */

package cpusim.xml;

public class HtmlEncoder
{
    /**
     * Encode a string to be written to an HTML file, escaping
     * all characters in [<>&"] with entity references.
     * @return java.lang.String
     * @param sStringToEncode java.lang.String
     */
    public static String sEncode(String sStringToEncode)
    {
        StringBuffer sbResult = new StringBuffer();

        for (int i = 0; i < sStringToEncode.length(); i++) {
            char c = sStringToEncode.charAt(i);
            switch (c) {
                case '<':
                    sbResult.append("&lt;");
                    break;
                case '>':
                    sbResult.append("&gt;");
                    break;
                case '&':
                    sbResult.append("&amp;");
                    break;
                case '"':
                    sbResult.append("&quot;");
                    break;
                default:
                    sbResult.append(c);
            }
        }
        return new String(sbResult);
    }
}