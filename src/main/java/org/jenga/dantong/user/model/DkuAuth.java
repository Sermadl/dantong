package org.jenga.dantong.user.model;

import java.util.function.Consumer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class DkuAuth {

    private final MultiValueMap<String, String> cookies = new LinkedMultiValueMap<>();

    public DkuAuth(MultiValueMap<String, String> cookies) {
        this.cookies.addAll(cookies);
    }

    public Consumer<MultiValueMap<String, String>> authCookies() {
        return map -> map.addAll(cookies);
    }
}