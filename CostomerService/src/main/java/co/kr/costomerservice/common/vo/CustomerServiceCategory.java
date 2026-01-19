package co.kr.costomerservice.common.vo;

public enum CustomerServiceCategory {
    ORDER,      //주문
    PAYMENT,    //결제
    REFUND,     //환불,취소
    SHIPPING,   //배송
    RETURN,     //반품,교환
    PRODUCT,    //상품
    ACCOUNT,    //계정
    COUPON,     //쿠폰,포인트,적립
    SYSTEM,     //시스템,오류,버그
    ETC         //기타
}
