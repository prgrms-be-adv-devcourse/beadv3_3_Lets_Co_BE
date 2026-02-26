package co.kr.assistant.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 텍스트 내의 욕설 및 금칙어를 필터링하는 유틸리티 클래스입니다.
 * ReDoS 방어 및 객체 생성 비용 최적화가 적용되어 있습니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor // [추가됨] Spring 빈 주입을 위한 롬복 어노테이션
public class ProfanityFilter {

    // [추가됨] Spring Boot가 미리 생성해 둔 글로벌 ObjectMapper 빈을 주입받아 재사용
    private final ObjectMapper objectMapper;

    private final List<String> bannedWords = new ArrayList<>();
    private Pattern filterPattern;
    public static final String PROFANITY_TAG = "<PROFANITY_MASK>";

    // 악의적인 초장문 입력에 의한 서버 과부하를 막기 위한 텍스트 길이 제한
    private static final int MAX_TEXT_LENGTH = 2000;

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("data/banned_words.json");

            try (InputStream is = resource.getInputStream()) {
                // [변경됨] 직접 생성(new ObjectMapper())하지 않고 주입받은 objectMapper 재사용
                JsonNode root = objectMapper.readTree(is);
                JsonNode wordsNode = root.get("banned_words");

                if (wordsNode.isArray()) {
                    for (JsonNode node : wordsNode) {
                        String word = node.asText();
                        StringBuilder pattern = new StringBuilder();
                        for (char c : word.toCharArray()) {
                            if (pattern.length() > 0) {
                                pattern.append("\\s*+"); // ReDoS 백트래킹 방어
                            }
                            pattern.append(Pattern.quote(String.valueOf(c)));
                        }
                        bannedWords.add(pattern.toString());
                    }
                }
            }

            String patternString = String.join("|", bannedWords);
            this.filterPattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
            log.info("욕설 필터 로드 완료: {} 개의 단어 (ReDoS 백트래킹 방어 및 객체 최적화 적용)", bannedWords.size());
        } catch (Exception e) {
            log.error("욕설 필터 로드 실패: ", e);
        }
    }

    public String filter(String text) {
        if (text == null || text.isEmpty() || filterPattern == null) return text;

        if (text.length() > MAX_TEXT_LENGTH) {
            text = text.substring(0, MAX_TEXT_LENGTH);
        }

        return filterPattern.matcher(text).replaceAll(PROFANITY_TAG);
    }

    public int getCleanLength(String text) {
        if (text == null || text.isEmpty() || filterPattern == null) return 0;

        if (text.length() > MAX_TEXT_LENGTH) {
            text = text.substring(0, MAX_TEXT_LENGTH);
        }

        int bannedTotalLength = 0;
        Matcher matcher = filterPattern.matcher(text);
        while (matcher.find()) {
            bannedTotalLength += (matcher.end() - matcher.start());
        }
        return text.trim().length() - bannedTotalLength;
    }
}