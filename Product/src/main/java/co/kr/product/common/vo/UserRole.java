package co.kr.product.common.vo;

public enum UserRole {
    USER,
    SELLER,
    ADMIN;

    public static boolean isSeller(String inputRole){
        return UserRole.SELLER.name().equals(inputRole);
    }

    public static boolean isAdmin(String inputRole){
        return UserRole.ADMIN.name().equals(inputRole);
    }

    public static boolean isStaff(String inputRole){
        return UserRole.SELLER.name().equals(inputRole) ||
                UserRole.ADMIN.name().equals(inputRole);
    }
}
