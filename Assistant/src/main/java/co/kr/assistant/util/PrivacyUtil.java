package co.kr.assistant.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class PrivacyUtil {

    private final Set<String> surnames = new HashSet<>();
    private final Set<String> stopwords = new HashSet<>();
    private final Set<String> triggers = new HashSet<>();

    public static final String PRIVACY_TAG = "<PRIVACY_MASK>";

    // 하이픈 유무 및 공백 대응 정규식
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b01[016789](\\s*|-?)\\d{3,4}(\\s*|-?)\\d{4}\\b");
    private static final Pattern RRN_PATTERN = Pattern.compile("\\b\\d{6}(\\s*|-?)[1-4]\\d{6}\\b");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");

    // 성명 탐지 정규식 (성씨 + 공백허용 한글 1~2자)
    private static final String NAME_REGEX = "(%s)(\\s*[가-힣]){1,2}";

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            loadJson(mapper, "data/korean_surnames.json", "surnames", surnames);
            loadJson(mapper, "data/name_stopwords.json", "stopwords", stopwords);
            loadJson(mapper, "data/context_triggers.json", "triggers", triggers);
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
        masked = EMAIL_PATTERN.matcher(masked).replaceAll(PRIVACY_TAG);
        masked = PHONE_PATTERN.matcher(masked).replaceAll(PRIVACY_TAG);
        masked = RRN_PATTERN.matcher(masked).replaceAll(PRIVACY_TAG);
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
            // 공백 제거 후 불용어 체크
            if (stopwords.contains(word.replaceAll("\\s", ""))) continue;

            sb.append(text, lastEnd, matcher.start());
            sb.append(PRIVACY_TAG);
            lastEnd = matcher.end();
        }
        sb.append(text.substring(lastEnd));
        return sb.toString();
    }
}