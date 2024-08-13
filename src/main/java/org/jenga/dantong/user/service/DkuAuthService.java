package org.jenga.dantong.user.service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.jenga.dantong.global.util.WebUtil;
import org.jenga.dantong.user.exception.DkuFailedLoginException;
import org.jenga.dantong.user.model.DkuAuth;
import org.jenga.dantong.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class DkuAuthService {

    private static final Pattern ERROR_ALERT_PATTERN = Pattern.compile(
        "<li>\\s*<p\\s*class=\"warn\">\\s*(.*)\\s*</p>\\s*</li>");

    @Qualifier("chromeAgentWebClient")
    private final WebClient webClient;

    @Value("${dku.login.webinfo-api-path}")
    private final String webinfoLoginApiPath;

    @Value("${dku.login.portal-api-path}")
    private final String portalLoginApiPath;

    private final UserRepository userRepository;

    public DkuAuth loginWebInfo(String classId, String password) {
        String param = makeParam("username=%s&password=%s&tabIndex=0", classId, password);
        return login(param, webinfoLoginApiPath,
            "https://webinfo.dankook.ac.kr",
            "https://webinfo.dankook.ac.kr/member/logon.do?returnurl=http://webinfo.dankook.ac.kr:80/main.do&sso=ok");
    }

    public DkuAuth loginDku(String studentId, String password) {
        String param = makeParam(
            "user_id=%s&user_password=%s&auto_login=N&returnurl=https://portal.dankook.ac.kr",
            studentId, password);
        return login(param, portalLoginApiPath,
            "https://portal.dankook.ac.kr",
            "https://portal.dankook.ac.kr/login.jsp");
    }

    private String makeParam(String format, String... params) {
        String[] encodedParams = new String[params.length];
        for (int i = 0; i < params.length; i++) {
            encodedParams[i] = URLEncoder.encode(params[i], StandardCharsets.UTF_8);
        }
        return String.format(format, encodedParams);
    }

    private DkuAuth login(String param, String uri, String origin, String referer) {
        MultiValueMap<String, String> cookies = new LinkedMultiValueMap<>();

        ResponseEntity<String> response = tryLogin(param, uri, origin, referer);
        HttpHeaders headers = response.getHeaders();
        addMappedCookies(cookies, headers);

        URI ssoLocation = headers.getLocation();
        response = trySSOAuth(ssoLocation, cookies, origin);
        addMappedCookies(cookies, response.getHeaders());

        return new DkuAuth(cookies);
    }

    private void addMappedCookies(MultiValueMap<String, String> dest, HttpHeaders src) {
        //TODO webUtil 구현
        List<ResponseCookie> cookies = WebUtil.extractCookies(src);
        for (ResponseCookie ent : cookies) {
            dest.add(ent.getName(), ent.getValue());
        }
    }

    private ResponseEntity<String> tryLogin(String param, String uri, String origin,
        String referer) {

        ResponseEntity<String> response;
        try {
            response = webClient.post()
                .uri(uri)
                .header("Origin", origin)
                .header("Referer", referer)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromValue(param))
                .retrieve()
                .toEntity(String.class)
                .block();
        } catch (Throwable t) {
            throw new DkuFailedLoginException(t);
        }

        validateResponse(response);

        HttpStatusCode statusCode = response.getStatusCode();
        if (statusCode == HttpStatus.OK) {
            throwLoginFailedWithAlertMessage(response);
        }

        validateStatusCode(statusCode);
        return response;
    }

    private ResponseEntity<String> trySSOAuth(URI ssoURI, MultiValueMap<String, String> cookies,
        String referer) {
        ResponseEntity<String> response;

        try {
            response = webClient.post()
                .uri(ssoURI)
                .cookies(map -> map.addAll(cookies))
                .header("Referer", referer)
                .retrieve()
                .toEntity(String.class)
                .block();
        } catch (Throwable t) {
            throw new DkuFailedLoginException(t);
        }

        validateResponse(response);
        validateStatusCode(response.getStatusCode());
        return response;
    }

    private static void throwLoginFailedWithAlertMessage(ResponseEntity<String> response) {
        String responseBody = response.getBody();
        if (responseBody == null) {
            throw new DkuFailedLoginException();
        }

        String loginMessage = extractLoginMessage(responseBody);
        if (loginMessage != null) {
            throw new DkuFailedLoginException(loginMessage);
        } else {
            throw new DkuFailedLoginException();
        }
    }

    private static String extractLoginMessage(String html) {
        Matcher matcher = ERROR_ALERT_PATTERN.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static void validateResponse(ResponseEntity<String> response) {
        if (response == null) {
            throw new DkuFailedLoginException(new NullPointerException("response"));
        }
    }

    private static void validateStatusCode(HttpStatusCode statusCode) {
        if (statusCode != HttpStatus.FOUND) {
            throw new DkuFailedLoginException(new ResponseStatusException(statusCode));
        }
    }

}
