package co.kr.product.common.vo;

public enum UserRole {
    USER,
    SELLER,
    ADMIN;

    public static boolean isSeller(UserRole inputRole){
        return UserRole.SELLER.equals(inputRole);
    }

    public static boolean isAdmin(UserRole inputRole){
        return UserRole.ADMIN.equals(inputRole);
    }

    public static boolean isStaff(UserRole inputRole){
        return UserRole.SELLER.equals(inputRole) ||
                UserRole.ADMIN.equals(inputRole);
    }


}
