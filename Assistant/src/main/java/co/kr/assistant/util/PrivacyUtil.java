package co.kr.assistant.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 사용자 입력 문자열에서 개인정보를 탐지하고 마스킹하는 유틸리티 클래스입니다.
 * 다국어(영문) 성명 패턴 대응 및 객체 생성 비용 최적화가 적용되었습니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PrivacyUtil {

    private final ObjectMapper objectMapper;

    private final Set<String> surnames = new HashSet<>();
    private final Set<String> stopwords = new HashSet<>();
    private final Set<String> triggers = new HashSet<>();

    public static final String EMAIL_MASK = "<이메일_숨김>";
    public static final String PHONE_MASK = "<전화번호_숨김>";
    public static final String RRN_MASK = "<주민번호_숨김>";
    public static final String NAME_MASK = "<이름_숨김>";

    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b01[016789][\\s\\-]*\\d{3,4}[\\s\\-]*\\d{4}\\b");
    private static final Pattern RRN_PATTERN = Pattern.compile("\\b\\d{6}[\\s\\-]*[1-4]\\d{6}\\b");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

    // [보강] 영문 성명 패턴 (대문자로 시작하는 단어 2개 이상, 예: John Doe)
    private static final Pattern ENGLISH_NAME_PATTERN = Pattern.compile("\\b([A-Z][a-z]+)\\s+([A-Z][a-z]+)\\b");

    private static final String NAME_REGEX = "(%s)(\\s*[가-힣]){1,2}";

    @PostConstruct
    public void init() {
        try {
            loadJson(objectMapper, "data/korean_surnames.json", "surnames", surnames);
            loadJson(objectMapper, "data/name_stopwords.json", "stopwords", stopwords);
            loadJson(objectMapper, "data/context_triggers.json", "triggers", triggers);
        } catch (Exception e) {
            log.error("개인정보 보호 데이터 로드 실패: ", e);
        }
    }

    private void loadJson(ObjectMapper mapper, String file, String node, Set<String> target) throws Exception {
        try (InputStream is = new ClassPathResource(file).getInputStream()) {
            JsonNode root = mapper.readTree(is);
            root.get(node).forEach(n -> target.add(n.asText()));
        }
    }

    public String maskAll(String text) {
        if (text == null || text.isEmpty()) return text;

        String masked = text;
        masked = EMAIL_PATTERN.matcher(masked).replaceAll(EMAIL_MASK);
        masked = PHONE_PATTERN.matcher(masked).replaceAll(PHONE_MASK);
        masked = RRN_PATTERN.matcher(masked).replaceAll(RRN_MASK);

        // 영문 성명 마스킹 추가
        masked = ENGLISH_NAME_PATTERN.matcher(masked).replaceAll(NAME_MASK);

        masked = maskNames(masked);

        return masked;
    }

    private String maskNames(String text) {
        if (surnames.isEmpty()) return text;

        String surnamePattern = String.join("|", surnames);
        Pattern namePattern = Pattern.compile(String.format(NAME_REGEX, surnamePattern));

        Matcher matcher = namePattern.matcher(text);
        StringBuilder sb = new StringBuilder();
        int lastEnd = 0;

        while (matcher.find()) {
            String word = matcher.group();
            if (stopwords.contains(word.replaceAll("\\s", ""))) continue;

            sb.append(text, lastEnd, matcher.start());
            sb.append(NAME_MASK);
            lastEnd = matcher.end();
        }
        sb.append(text.substring(lastEnd));
        return sb.toString();
    }
}