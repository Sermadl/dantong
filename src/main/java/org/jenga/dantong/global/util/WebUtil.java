package org.jenga.dantong.global.util;

import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

public class WebUtil {

    public static List<ResponseCookie> extractCookies(HttpHeaders headers) {
        List<ResponseCookie> cookies = new ArrayList<>();
        List<String> cookieHeaders = headers.get("Set-Cookie");

        if (cookieHeaders == null) {
            return cookies;
        }

        for (String value : cookieHeaders) {
            String[] blocks = value.split(";");
            String[] cookieData = blocks[0].split("=");
            ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieData[0].trim(),
                cookieData[1].trim());

            for (int i = 1; i < blocks.length; i++) {
                builder = parseCookieProperties(builder, blocks[i]);
            }

            cookies.add(builder.build());
        }

        return cookies;
    }

    private static ResponseCookie.ResponseCookieBuilder parseCookieProperties(
        ResponseCookie.ResponseCookieBuilder builder, String block) {
        String[] blockEntry = block.split("=");

        if (blockEntry.length == 1) {
            String name = blockEntry[0].trim();
            if (name.equalsIgnoreCase("httponly")) {
                builder = builder.httpOnly(true);
            } else if (name.equalsIgnoreCase("secure")) {
                builder = builder.secure(true);
            }
        } else if (blockEntry.length == 2) {
            String name = blockEntry[0].trim();
            String value = blockEntry[1].trim();
            if (name.equalsIgnoreCase("domain")) {
                builder = builder.domain(value);
            } else if (name.equalsIgnoreCase("path")) {
                builder = builder.path(value);
            } else if (name.equalsIgnoreCase("max-age")) {
                builder = builder.maxAge(Long.parseLong(value));
            }
        }

        return builder;
    }
}
