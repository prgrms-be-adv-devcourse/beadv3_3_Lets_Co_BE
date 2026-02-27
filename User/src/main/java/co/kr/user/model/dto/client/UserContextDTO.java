package co.kr.user.model.dto.client;

import lombok.Data;

@Data
public class UserContextDTO {
    private String role;
    private String membership;
    private String gender;
    private String birth;

    // 생년월일을 기반으로 연령대를 계산하는 편의 메서드
    public String getAgeGroup() {
        if (birth == null || birth.replaceAll("[^0-9]", "").length() < 4) {
            return "연령 미상";
        }
        try {
            String yearStr = birth.replaceAll("[^0-9]", "").substring(0, 4);
            int birthYear = Integer.parseInt(yearStr);
            int currentYear = java.time.LocalDate.now().getYear();
            int age = currentYear - birthYear;
            return (age / 10) * 10 + "대";
        } catch (Exception e) {
            return "연령 미상";
        }
    }
}