package co.kr.user.util;

import org.springframework.stereotype.Component;

/**
 * 이메일 발송에 사용될 HTML 템플릿 문자열을 제공하는 클래스입니다.
 * Java 15부터 도입된 Text Blocks (""") 기능을 사용하여 HTML 코드를 가독성 있게 작성했습니다.
 */
@Component
public class EmailTemplateProvider {

    /**
     * 모든 이메일 템플릿에서 공통적으로 사용되는 레이아웃을 생성합니다.
     * @param title 이메일 본문의 큰 제목
     * @param mainText 사용자에게 전달할 주요 메시지 내용
     * @param code 인증 코드 또는 강조할 텍스트
     * @param footerText 하단에 표시될 안내 문구
     * @return 완성된 HTML 문자열
     */
    private String getCommonLayout(String title, String mainText, String code, String footerText) {
        return """
            <div style='background-color: #f6f7f9; padding: 40px 20px; font-family: "Apple SD Gothic Neo", "Malgun Gothic", sans-serif; line-height: 1.6;'>
                <div style='max-width: 500px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; border: 1px solid #e0e0e0; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.05);'>
                    
                    <div style='background-color: #007bff; padding: 20px; text-align: center;'>
                        <h1 style='color: #ffffff; font-size: 20px; margin: 0; font-weight: 600;'>GutJJeu</h1>
                    </div>
            
                    <div style='padding: 30px;'>
                        <h2 style='color: #333; font-size: 22px; margin-top: 0; text-align: center;'>%s</h2>
                        <p style='color: #555; font-size: 16px; margin-bottom: 20px; text-align: center;'>
                            안녕하세요.<br>
                            %s
                        </p>
                        
                        <div style='background-color: #f0f4f8; padding: 20px; text-align: center; border-radius: 6px; margin: 30px 0; border: 1px dashed #007bff;'>
                            <span style='font-size: 18px; font-weight: bold; color: #007bff; word-break: break-all; display: inline-block;'>
                                %s
                            </span>
                        </div>
                        
                        <p style='color: #888; font-size: 13px; text-align: center; margin-top: 20px;'>
                            %s
                        </p>
                    </div>
            
                    <div style='background-color: #fafafa; padding: 15px; text-align: center; border-top: 1px solid #eee;'>
                        <p style='color: #aaa; font-size: 11px; margin: 0;'>
                            © 2026 GutJJeu. All rights reserved.
                        </p>
                    </div>
                </div>
            </div>
            """.formatted(title, mainText, code, footerText); // %s 위치에 파라미터 값들을 순서대로 바인딩
    }

    /**
     * 회원가입 인증 메일 템플릿을 반환합니다.
     * @param code 생성된 인증번호
     * @return HTML 형식의 이메일 본문
     */
    public String getSignupTemplate(String code) {
        return getCommonLayout(
                "이메일 인증 안내",
                "서비스 이용을 위해 아래 인증번호를 입력해 주세요.",
                code,
                "* 이 인증번호는 <strong>30분 동안만 유효</strong>합니다.<br>* 본인이 요청하지 않은 경우 이 메일을 무시해 주세요."
        );
    }

    /**
     * 아이디 찾기 인증 메일 템플릿을 반환합니다.
     * @param code 생성된 인증번호
     * @return HTML 형식의 이메일 본문
     */
    public String getFindIDTemplate(String code) {
        return getCommonLayout(
                "아이디 찾기 인증번호",
                "아이디 찾기를 위해 아래 인증번호를 입력해 주세요.",
                code,
                "* 이 인증번호는 <strong>30분 동안만 유효</strong>합니다.<br>* 본인이 요청하지 않은 경우 이 메일을 무시해 주세요."
        );
    }

    /**
     * 비밀번호 재설정 인증 메일 템플릿을 반환합니다.
     * @param code 생성된 인증번호
     * @return HTML 형식의 이메일 본문
     */
    public String getResetPasswordTemplate(String code) {
        return getCommonLayout(
                "비밀번호 찾기 인증번호",
                "비밀번호 재설정을 위해 아래 인증번호를 입력해 주세요.",
                code,
                "* 이 인증번호는 <strong>30분 동안만 유효</strong>합니다.<br>* 본인이 요청하지 않은 경우 이 메일을 무시해 주세요."
        );
    }

    /**
     * 회원 탈퇴 인증 메일 템플릿을 반환합니다.
     * @param code 생성된 인증번호
     * @return HTML 형식의 이메일 본문
     */
    public String getDeleteAccountTemplate(String code) {
        return getCommonLayout(
                "회원탈퇴 인증번호",
                "회원탈퇴 처리를 위해 아래 인증번호를 입력해 주세요.",
                code,
                "* 이 인증번호는 <strong>30분 동안만 유효</strong>합니다.<br>* 본인이 요청하지 않은 경우, 절대 타인에게 공유하지 마세요."
        );
    }

    /**
     * 판매자 등록 인증 메일 템플릿을 반환합니다.
     * @param code 생성된 인증번호
     * @return HTML 형식의 이메일 본문
     */
    public String getSellerRegisterTemplate(String code) {
        return getCommonLayout(
                "판매자 등록 인증 안내",
                "GutJJeu 판매자 입점을 환영합니다.<br>본인 확인을 위해 아래 인증번호를 입력해 주세요.",
                code,
                "* 이 인증번호는 <strong>30분 동안만 유효</strong>합니다.<br>* 인증이 완료되어야 판매자 활동이 가능합니다."
        );
    }

    /**
     * 판매자 승인 완료 안내 메일 템플릿을 반환합니다.
     * @param sellerName 승인된 판매자(상점) 이름
     * @return HTML 형식의 이메일 본문
     */
    public String getSellerApprovalTemplate(String sellerName) {
        return getCommonLayout(
                "판매자 등록 완료 안내",
                "축하합니다! 신청하신 판매자 권한 승인이 완료되었습니다.",
                sellerName,
                "* 위 계정(상점명)으로 판매 활동을 시작하실 수 있습니다.<br>* 판매자 센터에 로그인하여 상품을 등록해 보세요."
        );
    }

    /**
     * 판매자 탈퇴 인증 메일 템플릿을 반환합니다.
     * @param Code 생성된 인증번호
     * @return HTML 형식의 이메일 본문
     */
    public String getDeleteSellerTemplate(String Code) {
        return getCommonLayout(
                "판매자 탈퇴 인증번호",
                "판매자 탈퇴 처리를 위해 아래 인증번호를 입력해 주세요.",
                Code,
                "* 이 인증번호는 <strong>30분 동안만 유효</strong>합니다.<br>* 본인이 요청하지 않은 경우, 절대 타인에게 공유하지 마세요."
        );
    }
}