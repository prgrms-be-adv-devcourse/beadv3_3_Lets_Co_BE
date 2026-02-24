package co.kr.assistant.model.dto.rag;

import lombok.Data;
import java.util.List;

/**
 * Python RAG 서버의 새로운 응답 형식을 담는 DTO입니다.
 * 최상위에 의도(intent)가 있고, 결과 목록(results)이 포함됩니다.
 */
@Data
public class RagRes {
    private String intent;          // 파악된 질문의 의도 (예: PRODUCT_INFO)
    private List<RagItem> results;  // 검색된 지식 리스트
}