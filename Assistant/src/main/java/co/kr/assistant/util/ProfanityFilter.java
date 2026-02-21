package co.kr.assistant.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ProfanityFilter {

    private final List<String> bannedWords = new ArrayList<>();
    private Pattern filterPattern;
    public static final String PROFANITY_TAG = "<PROFANITY_MASK>";

    @PostConstruct
    public void init() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ClassPathResource resource = new ClassPathResource("data/banned_words.json");

            try (InputStream is = resource.getInputStream()) {
                JsonNode root = mapper.readTree(is);
                JsonNode wordsNode = root.get("banned_words");

                if (wordsNode.isArray()) {
                    for (JsonNode node : wordsNode) {
                        String word = node.asText();
                        // 각 글자 사이에 공백(\\s*)을 허용하는 정규식 생성
                        StringBuilder pattern = new StringBuilder();
                        for (char c : word.toCharArray()) {
                            if (pattern.length() > 0) pattern.append("\\s*");
                            pattern.append(Pattern.quote(String.valueOf(c)));
                        }
                        bannedWords.add(pattern.toString());
                    }
                }
            }

            String patternString = String.join("|", bannedWords);
            this.filterPattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
            log.info("욕설 필터 로드 완료: {} 개의 단어 (띄어쓰기 대응 적용)", bannedWords.size());
        } catch (Exception e) {
            log.error("욕설 필터 로드 실패: ", e);
        }
    }

    public String filter(String text) {
        if (text == null || text.isEmpty() || filterPattern == null) return text;
        return filterPattern.matcher(text).replaceAll(PROFANITY_TAG);
    }

    public int getCleanLength(String text) {
        if (text == null || text.isEmpty() || filterPattern == null) return 0;
        int bannedTotalLength = 0;
        Matcher matcher = filterPattern.matcher(text);
        while (matcher.find()) {
            bannedTotalLength += (matcher.end() - matcher.start());
        }
        return text.trim().length() - bannedTotalLength;
    }
}