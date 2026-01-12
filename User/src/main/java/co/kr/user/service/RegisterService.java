package co.kr.user.service;

public class RegisterService implements RegisterServiceImpl{


    @Override
    public String checkDuplicate(String email) {


        return "이메일 사용이 가능합니다.";
    }
}
