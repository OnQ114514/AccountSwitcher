package iafenvoy.accountswitcher.utils;

import com.google.gson.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class StringUtil {
    private static final Pattern UNICODE = Pattern.compile("(\\\\u(\\w{4}))");
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * 将字符转换为 Unicode 码点 (排除 ASCII)
     * @param str 要转换的字符串
     * @return 转换后的字符串
     */
    public static String stringToUnicode(String str) {
        StringBuilder unicodeBytes = new StringBuilder();

        for (int i = 0; i < str.length(); i++) {
            if (str.codePointAt(i) > 127) {     // 可能有问题，先不管了（（（
                String hexB = Integer.toHexString(str.charAt(i));
                if (hexB.length() <= 2) hexB = "00" + hexB;
                unicodeBytes.append("\\u").append(hexB);
            } else {
                unicodeBytes.append(str.charAt(i));
            }
        }
        return unicodeBytes.toString();
    }

    public static String unicodeToString(String str) {
        Matcher matcher = UNICODE.matcher(str);
        char ch;
        while (matcher.find()) {
            ch = (char) Integer.parseInt(matcher.group(2), 16);
            str = str.replace(matcher.group(1), String.valueOf(ch));
        }
        return str;
    }

    public static String prettyJson(String json) {
        return prettyJson(JsonParser.parseString(json));
    }

    public static String prettyJson(JsonElement element) {
        return gson.toJson(element);
    }
}
