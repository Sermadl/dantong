package org.jenga.dantong.user.service;

import java.time.YearMonth;
import java.util.Optional;
import org.jenga.dantong.user.exception.DkuFailedCrawlingException;
import org.jenga.dantong.user.model.DkuAuth;
import org.jenga.dantong.user.model.StudentDuesStatus;
import org.jenga.dantong.user.model.dto.response.StudentInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

public class DkuCrawlService {

    private final String studentInfoPath;

    private final WebClient webClient;

    private final String feeInfoApiPath;

    public DkuCrawlService(@Qualifier WebClient webClient,
        @Value("${dku.student-info.info-api-path}") String studentInfoPath,
        @Value("${dku.student-info.fee-api-path}") String feeInfoApiPath) {
        this.webClient = webClient;
        this.studentInfoPath = studentInfoPath;
        this.feeInfoApiPath = feeInfoApiPath;
    }


    protected String requestWebInfo(DkuAuth auth, String uri) {
        String result;
        try {
            result = makeRequestWebInfo(auth, uri)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        } catch (Throwable t) {
            throw new DkuFailedCrawlingException(t);
        }

        if (result == null) {
            throw new DkuFailedCrawlingException("Failed to crawl");
        }

        return result;
    }

    protected <T> T requestPortal(DkuAuth auth, String uri, Class<T> clazz) {
        T result;
        try {
            result = makeRequest(auth, uri, "https://portal.dankook.ac.kr/p/S01/")
                .retrieve()
                .bodyToMono(clazz)
                .block();
        } catch (Throwable t) {
            throw new DkuFailedCrawlingException(t);
        }

        if (result == null) {
            throw new DkuFailedCrawlingException("Failed to crawl");
        }

        return result;
    }

    protected WebClient.RequestBodySpec makeRequestWebInfo(DkuAuth auth, String uri) {
        return makeRequest(auth, uri, "https://webinfo.dankook.ac.kr/");
    }

    private WebClient.RequestBodySpec makeRequest(DkuAuth auth, String uri, String referer) {
        return webClient.post()
            .uri(uri)
            .cookies(auth.authCookies())
            .header("Referer", referer);
    }

    public static String getSemester(YearMonth yearMonth) {
        int month = yearMonth.getMonthValue();
        if (month >= 2 && month <= 8) {
            return "1";
        } else {
            return "2";
        }
    }

    /**
     * 학생 정보를 크롤링해옵니다.
     *
     * @param auth 인증 토큰
     * @return 학생 정보
     */
    public StudentInfo crawlStudentInfo(DkuAuth auth) {
        String html = requestWebInfo(auth, studentInfoPath);
        return parseStudentInfoHtml(html);
    }

    /**
     * 학생회비 납부 정보를 크롤링해옵니다.
     *
     * @param auth 인증 토큰
     * @return 학생 정보
     */
    public StudentDuesStatus crawlStudentDues(DkuAuth auth, YearMonth yearMonth) {
        String html = requestWebInfo(auth, feeInfoApiPath);
        return parseDuesStatusHtml(html, yearMonth);
    }

    private StudentDuesStatus parseDuesStatusHtml(String html, YearMonth yearMonth) {
        Document doc = Jsoup.parse(html);

        try {
            Element table = doc.getElementById("tbl_semList");
            Elements rows = table.select("tbody tr");
            if (rows.isEmpty()) {
                throw new DkuFailedCrawlingException(
                    ("table is empty"));
            }

            for (int i = 0; i < rows.size(); i++) {
                Element row = rows.get(0);
                Elements cols = row.select("td");
                if (cols.size() != 6) {
                    throw new DkuFailedCrawlingException(
                        ("table column size is not 6"));
                }

                if (!isThisPaidDues(yearMonth, cols)) {
                    continue;
                }

                String needFees = cols.get(4).text();
                String paidFees = cols.get(5).text();

                if (needFees.equals(paidFees)) {
                    return StudentDuesStatus.PAID;
                } else {
                    return StudentDuesStatus.NOT_PAID;
                }
            }

            return StudentDuesStatus.NOT_PAID;
        } catch (NullPointerException e) {
            throw new DkuFailedCrawlingException(e);
        }
    }

    private static boolean isThisPaidDues(YearMonth yearMonth, Elements cols) {
        String year = cols.get(1).text();
        String semester = cols.get(2).text();
        String type = cols.get(3).text();

        if (!type.equals("학생회비")) {
            return false;
        }

        if (!String.valueOf(yearMonth.getYear()).equals(year)) {
            return false;
        }

        return semester.equals(getSemester(yearMonth));
    }

    private StudentInfo parseStudentInfoHtml(String html) {
        Document doc = Jsoup.parse(html);

        String studentName = getElementValueOrThrow(doc, "nm");
        String studentId = getElementValueOrThrow(doc, "stuid");
        String studentState = getElementValueOrThrow(doc, "scregStaNm");

        String major, department = "";

        try {
            major = getElementValueOrThrow(doc, "pstnOrgzNm");
            major = major.trim();

            int spaceIdx = major.lastIndexOf(' ');
            if (spaceIdx >= 0) {
                department = major.substring(0, spaceIdx);
                major = major.substring(spaceIdx + 1);
            }
        } catch (Throwable t) {
            throw new DkuFailedCrawlingException(t);
        }

        return new StudentInfo(studentName, studentId, major,
            department);
    }

    private String getElementValueOrThrow(Document doc, String id) {
        String value = Optional.ofNullable(doc.getElementById(id))
            .map(Element::val)
            .orElseThrow(() -> new DkuFailedCrawlingException(new NullPointerException(id)));
        if (value.isBlank()) {
            throw new DkuFailedCrawlingException(new NullPointerException(id));
        }
        return value;
    }
}
