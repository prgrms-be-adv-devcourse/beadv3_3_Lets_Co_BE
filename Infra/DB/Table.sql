/* =========================
   Global / Database Settings
   ========================= */
# CREATE DATABASE IF NOT EXISTS `GutJJeu`
#     DEFAULT CHARACTER SET utf8mb4
#     COLLATE utf8mb4_unicode_ci;

USE `GutJJeu`;

SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET default_storage_engine = InnoDB;

SET FOREIGN_KEY_CHECKS = 0;

/* =========================
   Users
   ========================= */
CREATE TABLE `Users` (
    `Users_IDX` BIGINT NOT NULL AUTO_INCREMENT,
    `ID` VARCHAR(254) NOT NULL,                                         # AES-256 CBC 암호화
    `PW` VARCHAR(255) NOT NULL,                                         # BCrypt 암호화
    `Failed_Login_Attempts` SMALLINT UNSIGNED NOT NULL DEFAULT 0,
    `Locked_Until` DATETIME(6) NULL,
    `Role` VARCHAR(20) NOT NULL DEFAULT 'USERS',
    `Membership` VARCHAR(20) NOT NULL DEFAULT 'STANDARD',

    `agree_Terms_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `agree_Privacy_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `Created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `Updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    `Del` TINYINT(1) NOT NULL DEFAULT 2,

    PRIMARY KEY (`Users_IDX`),

    KEY `UK_Users_ID` (`ID`),

    CONSTRAINT `CK_Users_Del` CHECK (`Del` IN (0,1,2)),
    CONSTRAINT `CK_Users_Role` CHECK ( `Role` IN ('ADMIN', 'USERS', 'SELLER')),
    CONSTRAINT `CK_Users_Membership` CHECK ( Membership IN ('VIP', 'GOLD', 'SILVER', 'STANDARD'))
);

CREATE TABLE `Users_Information` (
    `Users_IDX` BIGINT NOT NULL,
    `Pre_PW` VARCHAR(255) NULL,
    `Mail` VARCHAR(255) NOT NULL,                                       # AES-256 GCM 암호화
    `Gender` VARCHAR(10) NULL DEFAULT 'OTHER',
    `Balance` DECIMAL(19,2) NOT NULL DEFAULT 0,

    `Name` VARCHAR(512) NOT NULL,                                       # AES-256 GCM 암호화
    `Phone_Number` VARCHAR(512) NULL,                                   # AES-256 GCM 암호화
    `Birth` VARCHAR(512) NULL,                                          # AES-256 GCM 암호화

    `Default_Address` BIGINT NULL,
    `Default_Card` BIGINT NULL,
    `agree_Marketing_at` DATETIME(6) NULL,
    `Del` TINYINT(1) NOT NULL DEFAULT 2,

    KEY `IX_UI_Default_Address` (`Default_Address`),
    KEY `IX_UI_Default_Card` (`Default_Card`),

    PRIMARY KEY (`Users_IDX`),

    CONSTRAINT `CK_UsersInfo_Del` CHECK (`Del` IN (0,1,2)),
    CONSTRAINT `CK_UsersInfo_Balance` CHECK (`Balance` >= 0),
    CONSTRAINT `CK_Users_Gender` CHECK ( `Gender` IN('MALE', 'FEMALE', 'OTHER'))
);

CREATE TABLE `Users_Address` (
    `Address_IDX` BIGINT NOT NULL AUTO_INCREMENT,
    `Users_IDX` BIGINT NOT NULL,
    `Address_Code` VARCHAR(255) NOT NULL,                #UUID
    `Recipient` VARCHAR(512) NOT NULL,                   # AES-256 GCM 암호화
    `Address` VARCHAR(2048) NOT NULL,                    # AES-256 GCM 암호화
    `Address_Detail` VARCHAR(2048) NULL,                 # AES-256 GCM 암호화
    `Phone_Number` VARCHAR(512) NOT NULL,                # AES-256 GCM 암호화
    `Del` TINYINT(1) NOT NULL DEFAULT 0,

    PRIMARY KEY (`Address_IDX`),

    KEY `IX_UsersAddress_Users` (`Users_IDX`),
    KEY `IX_UsersAddress_Code` (`Address_Code`),

    CONSTRAINT `CK_UsersAddress_Del` CHECK (`Del` IN (0,1))
);

CREATE TABLE `Users_Card` (
    `Card_IDX` BIGINT NOT NULL AUTO_INCREMENT,
    `Users_IDX` BIGINT NOT NULL,
    `Card_Code` VARCHAR(100) NOT NULL,                     #UUID
    `Card_Brand` VARCHAR(255) NOT NULL,                    # AES-256 GCM 암호화
    `Card_Name` VARCHAR(255) NOT NULL,                     # AES-256 GCM 암호화
    `Card_Token` VARCHAR(512) NOT NULL,                    # AES-256 GCM 암호화
    `Exp_Month` TINYINT UNSIGNED NOT NULL,
    `Exp_Year` SMALLINT UNSIGNED NOT NULL,
    `Del` TINYINT(1) NOT NULL DEFAULT 0,


    PRIMARY KEY (`Card_IDX`),

    KEY `IX_UsersCard_Users` (`Users_IDX`),
    KEY `IX_UsersCard_Code` (`Card_Code`),

    CONSTRAINT `CK_UsersCard_Del` CHECK (`Del` IN (0,1)),
    CONSTRAINT `CK_UsersCard_ExpMonth` CHECK (`Exp_Month` BETWEEN 1 AND 12)
);

CREATE TABLE `Users_Verifications` (
    `Verification_IDX` BIGINT NOT NULL AUTO_INCREMENT,
    `Users_IDX` BIGINT NOT NULL,
    `Purpose` VARCHAR(20) NOT NULL,
    `Code` VARCHAR(255) NOT NULL,                                    #RandomCode
    `Created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `Expires_at` DATETIME(6) NOT NULL,
    `Verified_at` DATETIME(6) NULL,
    `Status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    `Del` TINYINT(1) NOT NULL DEFAULT 0,

    PRIMARY KEY (`Verification_IDX`),

    KEY `IX_UsersVerifications_Users` (`Users_IDX`),
    KEY `IX_UsersVerifications_UsersPurpose_Created` (`Users_IDX`, `Purpose`, `Created_at`),

    CONSTRAINT `CK_UsersVerifications_Del` CHECK (`Del` IN (0, 1)),
    CONSTRAINT `CK_UsersVerifications_Status` CHECK (`Status` IN ('PENDING','VERIFIED','EXPIRED','FAILED','LOCKED','CANCELLED')),
    CONSTRAINT `CK_UsersVerifications_Purpose` CHECK (`Purpose` in ('SIGNUP','FIND_ID','RESET_PW','CHANGE_PW','CHANGE_EMAIL','LOGIN_2FA','DELETE_ACCOUNT','SELLER_SIGNUP','SELLER_DELETE'))
);

# CREATE TABLE `Users_Login` (
#     `Login_IDX` BIGINT NOT NULL AUTO_INCREMENT,
#     `Users_IDX` BIGINT NOT NULL,
#     `Token` VARCHAR(255) CHARACTER SET ascii NOT NULL,                                    #JWT 평문 저장
#     `Last_Used_At` DATETIME(6) NULL,
#     `Revoked_at`   DATETIME(6) NULL,
#     `Revoke_Reason` VARCHAR(60) CHARACTER SET ascii COLLATE ascii_general_ci NULL,
#
#     PRIMARY KEY (`Login_IDX`),
#
#     UNIQUE (`Token`),
#
#     KEY `IX_UsersLogin_Users` (`Users_IDX`),
#     KEY `IX_UsersLogin_Revoked` (`Revoked_at`),
#
#     CONSTRAINT `CK_UsersLogin_RevokedReason` CHECK ((`Revoked_at` IS NULL AND `Revoke_Reason` IS NULL) OR (`Revoked_at` IS NOT NULL AND `Revoke_Reason` IS NOT NULL))
# );

/* =========================
   Seller / Settlement
   ========================= */
CREATE TABLE `Seller` (
    `Seller_IDX` BIGINT NOT NULL AUTO_INCREMENT,
    `Users_IDX`  BIGINT NOT NULL,
    `Seller_Name` VARCHAR(512) NOT NULL,                      # AES-256 GCM 암호화
    `Business_License` VARCHAR(512) NOT NULL,                # AES-256 GCM 암호화
    `Bank_Brand` VARCHAR(512) NOT NULL,                      # AES-256 GCM 암호화
    `Bank_Name` VARCHAR(512) NOT NULL,                       # AES-256 GCM 암호화
    `Bank_Token` VARCHAR(2048) NOT NULL,
    `Created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `Updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    `Del` TINYINT(1) NOT NULL DEFAULT 0,

    PRIMARY KEY (`Seller_IDX`),
    UNIQUE (`Business_License`),
    CONSTRAINT `CK_Seller_Del` CHECK (`Del` IN (0,1,2))
);

/* =========================
   Products
   ========================= */
CREATE TABLE `Products` (
    `Products_IDX` BIGINT NOT NULL AUTO_INCREMENT,
    `Seller_IDX` BIGINT NOT NULL,
    `Products_Code` VARCHAR(50) NOT NULL,               #UUID
    `Products_Category` BIGINT NOT NULL,
    `Products_IP` BIGINT NOT NULL,                      # Products_Category랑 같은 테이블 공유
    `Products_Name` VARCHAR(200) NOT NULL,
    `Description` MEDIUMTEXT NULL,
    `Price` DECIMAL(19,2) NOT NULL,
    `Sale_Price` DECIMAL(19,2) NOT NULL DEFAULT 0,
    `Stock` INT NOT NULL DEFAULT 0,
    `Status` VARCHAR(20) NOT NULL,
    `Opened_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `View_Count` BIGINT NOT NULL DEFAULT 0,
    `Created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `Updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    `Del` TINYINT(1) NOT NULL DEFAULT 0,

    PRIMARY KEY (`Products_IDX`),

    UNIQUE KEY `UK_Products_Code` (`Products_Code`),
    KEY `IX_Products_Seller` (`Seller_IDX`),
    KEY `IX_Products_Category` (`Products_Category`),
    KEY `IX_Products_IP` (`Products_IP`),

    CONSTRAINT `CK_Products_Del` CHECK (`Del` IN (0,1)),
    CONSTRAINT `CK_Products_Price` CHECK (`Price` >= 0),
    CONSTRAINT `CK_Products_SalePrice` CHECK (`Sale_Price` = 0 OR (`Sale_Price` >= 0 AND `Sale_Price` <= `Price`)),
    CONSTRAINT `CK_Products_Stock` CHECK (`Stock` >= 0)
);

CREATE TABLE `Products_Category` (
    `Category_IDX` BIGINT NOT NULL AUTO_INCREMENT,
    `Type` VARCHAR(20) NOT NULL,
    `Path` VARCHAR(255) NOT NULL,

    `Category_Code` VARCHAR(50) NOT NULL,               #UUID
    `Category_Name` VARCHAR(50) NOT NULL,

    `Parent_IDX` BIGINT NULL,

    `Del` TINYINT(1) NOT NULL DEFAULT 0,

    PRIMARY KEY (`Category_IDX`),

    UNIQUE KEY `UK_ProductsCategory_Code` (`Category_Code`),

    KEY `IX_ProductsCategory_Parent` (`Parent_IDX`),

    CONSTRAINT `CK_ProductsCategory_Del` CHECK (`Del` IN (0,1)),
    CONSTRAINT `CK_ProductsCategory_Type` CHECK (`Type` IN ('CATEGORY', 'IP'))

);

CREATE TABLE `Product_Option` (
    `Option_Group_IDX` BIGINT NOT NULL AUTO_INCREMENT,
    `Products_IDX` BIGINT NOT NULL,
    `Option_Code` VARCHAR(50) NOT NULL,                   #UUID
    `Option_Name` VARCHAR(50) NOT NULL,
    `Sort_Orders` INT NOT NULL DEFAULT 0,
    `Option_Price` DECIMAL(19,2) NOT NULL,
    `Option_Sale_Price` DECIMAL(19,2) NOT NULL DEFAULT 0,
    `Stock` INT NOT NULL DEFAULT 0,
    `Status` VARCHAR(20) NOT NULL,
    `Created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `Updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    `Del` TINYINT(1) NOT NULL DEFAULT 0,

    PRIMARY KEY (`Option_Group_IDX`),

    INDEX (Products_IDX, Del, Sort_Orders),

    KEY `IX_ProductOption_Products` (`Products_IDX`),
    KEY `IX_ProductOption_Code` (`Option_Code`),

    UNIQUE KEY `UK_ProductOption_Code` (`Products_IDX`, `Option_Code`),

    CONSTRAINT `CK_ProductOption_Del` CHECK (`Del` IN (0,1)),
    CONSTRAINT `CK_ProductOption_Price` CHECK (`Option_Price` >= 0),
    CONSTRAINT `CK_ProductOption_SalePrice` CHECK (`Option_Sale_Price` = 0 OR (`Option_Sale_Price` >= 0 AND `Option_Sale_Price` <= `Option_Price`)),
    CONSTRAINT `CK_ProductOption_Stock` CHECK (`Stock` >= 0)
);

/* =========================
   Orders / Orders_Item
   ========================= */
CREATE TABLE `Orders` (
    `Orders_IDX` BIGINT NOT NULL AUTO_INCREMENT,
    `Orders_Code` VARCHAR(64) NOT NULL,                 #UUID
    `Users_IDX` BIGINT NOT NULL,
    `Recipient` VARCHAR(512) NULL,                      #AES-256
    `Address` VARCHAR(2048) NULL,                       #AES-256
    `Address_Detail` VARCHAR(2048) NULL,                #AES-256
    `Phone_Number` VARCHAR(512) NULL,                   #AES-256
    `Card_IDX` BIGINT NULL,
    `Status` VARCHAR(20) NOT NULL,
    `Items_Amount` DECIMAL(19,2) NOT NULL DEFAULT 0,
    `Discount_Amount` DECIMAL(19,2) NOT NULL DEFAULT 0,
    `Shipping_Fee` DECIMAL(19,2) NOT NULL DEFAULT 0,
    `Total_Amount` DECIMAL(19,2) NOT NULL DEFAULT 0,
    `Created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `Updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    `Del` TINYINT(1) NOT NULL DEFAULT 0,

    PRIMARY KEY (`Orders_IDX`),

    UNIQUE KEY `UK_Orders_Code` (`Orders_Code`),

    KEY `IX_Orders_Users` (`Users_IDX`),
    KEY `IX_Orders_Card` (`Card_IDX`),

    CONSTRAINT `CK_Orders_Del` CHECK (`Del` IN (0,1)),
    CONSTRAINT `CK_Orders_Amounts` CHECK (
     `Items_Amount` >= 0 AND `Discount_Amount` >= 0 AND `Shipping_Fee` >= 0 AND `Total_Amount` >= 0
     )
);

CREATE TABLE `Orders_Item` (
    `Orders_Item_IDX` BIGINT NOT NULL AUTO_INCREMENT,
    `Orders_IDX` BIGINT NOT NULL,
    `Products_Code` VARCHAR(200) NOT NULL,
    `Products_Name` VARCHAR(200) NOT NULL,
    `Option_Code` VARCHAR(200) NOT NULL,
    `Option_Name` VARCHAR(50) NOT NULL,
    `Price` DECIMAL(19,2) NOT NULL,
    `Sale_Price` DECIMAL(19,2) NOT NULL DEFAULT 0,
    `Quantity` INT NOT NULL,
    `Created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `Updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    `Del` TINYINT(1) NOT NULL DEFAULT 0,

    PRIMARY KEY (`Orders_Item_IDX`),

    KEY `IX_OrdersItem_Orders` (`Orders_IDX`),
    KEY `IX_OrdersItem_Products` (`Products_Code`),
    KEY `IX_OrdersItem_Option` (`Option_Code`),

    CONSTRAINT `CK_OrdersItem_Del` CHECK (`Del` IN (0,1)),
    CONSTRAINT `CK_OrdersItem_Quantity` CHECK (`Quantity` > 0),
    CONSTRAINT `CK_OrdersItem_Price` CHECK (`Price` >= 0),
    CONSTRAINT `CK_OrdersItem_SalePrice` CHECK (`Sale_Price` = 0 OR (`Sale_Price` >= 0 AND `Sale_Price` <= `Price`))
);

/* =========================
   Payment / Settlement_History
   ========================= */
CREATE TABLE `Payment` (
        `Payment_IDX` BIGINT NOT NULL AUTO_INCREMENT,
        `Users_IDX` BIGINT NOT NULL,
        `Status` VARCHAR(30) NOT NULL,
        `Type` VARCHAR(20) NOT NULL,
        `Payment_Key` VARCHAR(255) NULL,
        `Amount` DECIMAL(19,2) NOT NULL DEFAULT 0,
        `Orders_IDX` BIGINT NULL,
        `Card_IDX` BIGINT NULL,
        `Created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

        PRIMARY KEY (`Payment_IDX`),

        INDEX (Users_IDX, Created_at),

        KEY `IX_Payment_Users` (`Users_IDX`),
        KEY `IX_Payment_Orders` (`Orders_IDX`),
        KEY `IX_Payment_Card` (`Card_IDX`),

        CONSTRAINT `CK_Payment_Status` CHECK (`Status` IN ('PAYMENT','CHARGE','REFUND')),
        CONSTRAINT `CK_Payment_Type` CHECK (`Type` IN ('CARD','DEPOSIT','TOSS_PAY'))
);

CREATE TABLE `Settlement_History` (
    `Settlement_IDX` BIGINT NOT NULL AUTO_INCREMENT,
    `Seller_IDX` BIGINT NOT NULL,
    `Type` VARCHAR(30) NOT NULL,
    `Payment_IDX` BIGINT NOT NULL,
    `Amount` DECIMAL(19,2) NOT NULL DEFAULT 0,
    `Created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `Updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    `Del` TINYINT(1) NOT NULL DEFAULT 0,

    PRIMARY KEY (`Settlement_IDX`),

    KEY `IX_SettlementHistory_Seller` (`Seller_IDX`),
    KEY `IX_SettlementHistory_Payment` (`Payment_IDX`),

    CONSTRAINT `CK_SettlementHistory_Del` CHECK (`Del` IN (0,1)),
    CONSTRAINT `CK_SettlementHistory_Amount` CHECK (`Amount` >= 0),
    CONSTRAINT `CK_SettlementHistory_Type` CHECK (`Type` in ('ORDERS_CONFIRMED','SETTLE_PAYOUT','CANCEL_ADJUST'))
);

/* =========================
   Review
   ========================= */
CREATE TABLE `Review` (
    `Review_IDX` BIGINT NOT NULL AUTO_INCREMENT,
    `Products_IDX` BIGINT NOT NULL,
    `Users_IDX` BIGINT NOT NULL,
    `Orders_Item_IDX` BIGINT NOT NULL,
    `Evaluation` INT NOT NULL,
    `Content` MEDIUMTEXT NOT NULL,
    `Created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `Updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    `Del` TINYINT(1) NOT NULL DEFAULT 0,

    PRIMARY KEY (`Review_IDX`),

    UNIQUE KEY `UK_Review_OrdersItem` (`Orders_Item_IDX`),

    KEY `IX_Review_Products` (`Products_IDX`),
    KEY `IX_Review_Users` (`Users_IDX`),

    CONSTRAINT `CK_Review_Del` CHECK (`Del` IN (0,1)),
    CONSTRAINT `CK_Review_Evaluation` CHECK (`Evaluation` BETWEEN 1 AND 5)
);

/* =========================
   Customer_Service
   ========================= */
CREATE TABLE `Customer_Service` (
    `Customer_Service_IDX`  BIGINT NOT NULL AUTO_INCREMENT,
    `Customer_Service_Code` VARCHAR(64) NOT NULL,           #UUID

    `Users_IDX`     BIGINT NOT NULL,
    `User_Name` VARCHAR(255) NOT NULL,


    `Type`     VARCHAR(20) CHARACTER SET ascii COLLATE ascii_general_ci NULL,
    `Category` VARCHAR(30) CHARACTER SET ascii COLLATE ascii_general_ci NULL,
    `Products_IDX` BIGINT NULL,
    `Status`   VARCHAR(20) CHARACTER SET ascii COLLATE ascii_general_ci NULL DEFAULT 'DRAFT',
    `Priority` VARCHAR(10) NULL,

    `Title`      VARCHAR(200) NOT NULL,
    `Is_Private` TINYINT(1) NOT NULL DEFAULT 0,
    `View_Count` BIGINT NOT NULL DEFAULT 0,

    `Published_at` DATETIME(6) NULL,
    `Is_Pinned`    TINYINT(1) NOT NULL DEFAULT 0,

    `Created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `Updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    `Del`        TINYINT(1) NOT NULL DEFAULT 0,



    PRIMARY KEY (`Customer_Service_IDX`),

    UNIQUE KEY `UK_Customer_Service_Code` (`Customer_Service_Code`),

    KEY `IX_Customer_Service_Users_IDX` (`Users_IDX`),
    KEY `IX_Customer_Service_Products_IDX` (`Products_IDX`),

    CONSTRAINT `CK_Customer_Service_Type` CHECK (`Type` IN ('NOTICE', 'QNA_ADMIN', 'QNA_PRODUCT')),
    CONSTRAINT `CK_Customer_Service_Category` CHECK (`Category` IS NULL OR `Category` IN (
                                                            'ORDER',        -- 주문
                                                            'PAYMENT',      -- 결제
                                                            'REFUND',       -- 환불/취소
                                                            'SHIPPING',     -- 배송
                                                            'RETURN',       -- 반품/교환
                                                            'PRODUCT',      -- 상품(재고/옵션/정보)
                                                            'ACCOUNT',      -- 계정/로그인/개인정보
                                                            'COUPON',       -- 쿠폰/포인트/적립(필요시)
                                                            'SYSTEM',       -- 시스템/오류/버그
                                                            'ETC'           -- 기타
                                                        )
                                                    ),
    CONSTRAINT `CK_Customer_Service_Status` CHECK (`Status` IN (
                                                            'DRAFT',        -- 작성중
                                                            'PUBLISHED',    -- 게시됨
                                                            'HIDDEN',       -- 숨김(비공개/관리자 숨김)
                                                            'CLOSED',       -- 종료(문의 종료)
                                                            'ANSWERED',     -- 답변완료(Q&A)
                                                            'WAITING'       -- 답변대기(Q&A)
                                                        )
                                                    )
);

/* =========================
   Customer_Service_Detail
   ========================= */
CREATE TABLE `Customer_Service_Detail` (
    `Customer_Service_Detail_IDX`  BIGINT NOT NULL AUTO_INCREMENT,
    `Customer_Service_Detail_Code` VARCHAR(64) NOT NULL,             #UUID

    `Users_IDX`             BIGINT NOT NULL,
    `User_Name` VARCHAR(255) NOT NULL,
    `Customer_Service_IDX` BIGINT NOT NULL,

    `Parent_IDX`           BIGINT NULL,

    `Content` MEDIUMTEXT NOT NULL,

    `Created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `Updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    `Del`        TINYINT(1) NOT NULL DEFAULT 0,

    PRIMARY KEY (`Customer_Service_Detail_IDX`),

    UNIQUE KEY `UK_Customer_Service_Detail_Code` (`Customer_Service_Detail_Code`),

    KEY `IX_CSD_Parent_IDX` (`Parent_IDX`),
    KEY `IX_CSD_Users_IDX` (`Users_IDX`),
    KEY `IX_CSD_Customer_Service_IDX` (`Customer_Service_IDX`)
);

CREATE TABLE `File` (
    `File_IDX` BIGINT NOT NULL AUTO_INCREMENT,
    `File_Origin` VARCHAR(255) NOT NULL,
    `File_Name` VARCHAR(255) NOT NULL,
    `File_Type` VARCHAR(10) NOT NULL,
    `File_Path` VARCHAR(255) NULL,

    `Ref_Table` VARCHAR(100) NOT NULL,
    `Ref_Index` BIGINT NOT NULL,

    `Created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `Updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    `Del` TINYINT(1) NOT NULL DEFAULT 0,

    PRIMARY KEY (`File_IDX`),

    INDEX (Ref_Table, Ref_Index, Del),

    KEY `IX_File_Name` (`File_Name`),
    KEY `IX_File_Path` (`File_Path`),

    CONSTRAINT `CK_File_Del` CHECK (`Del` IN (0,1))
);

/* =========================
   Assistant / Chat Bot
   ========================= */
CREATE TABLE `Assistant` (
    `Assistant_IDX` BIGINT NOT NULL AUTO_INCREMENT,
    `Assistant_Code` VARCHAR(64) NOT NULL,               # UUID (채팅 토큰)
    `Users_IDX` BIGINT NULL,                             # 회원인 경우 연결 (선택적)

    `IP_Address` VARCHAR(45) NOT NULL,                   # IPv6 대응을 위한 길이
    `User_Agent` VARCHAR(512) NULL,                      # 브라우저 정보 (보안 검증용)

    `Created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `Updated_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    `Last_Activity_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6), # 마지막 활동 시간
    `Del` TINYINT(1) NOT NULL DEFAULT 0,

    PRIMARY KEY (`Assistant_IDX`),

    UNIQUE KEY `UK_Assistant_Code` (`Assistant_Code`),
    KEY `IX_Assistant_Users` (`Users_IDX`),
    KEY `IX_Assistant_IP` (`IP_Address`),

    CONSTRAINT `CK_Assistant_Del` CHECK (`Del` IN (0, 1))
);

CREATE TABLE `Assistant_Chat` (
    `Chat_IDX` BIGINT NOT NULL AUTO_INCREMENT,
    `Assistant_IDX` BIGINT NOT NULL,                 # Assistant 테이블 참조

    `Prompt` MEDIUMTEXT NULL ,
    `Question` MEDIUMTEXT NOT NULL,                  # 사용자 질문
    `Answer` MEDIUMTEXT NULL,                        # AI 응답
    `Prompt_Tokens` INT NULL,
    `Answer_Tokens` INT NULL,
    `Total_Tokens` INT NULL,
    `Duration_MS` BIGINT NULL,

    `Created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `Del` TINYINT(1) NOT NULL DEFAULT 0,

    PRIMARY KEY (`Chat_IDX`),

    KEY `IX_AssistantChat_Assistant` (`Assistant_IDX`),
    KEY `IX_AssistantChat_Created` (`Created_at`),

    CONSTRAINT `CK_AssistantChat_Del` CHECK (`Del` IN (0, 1))
);

/* =========================
   Foreign Keys (moved to bottom)
   ========================= */
ALTER TABLE `Users_Information`
    ADD CONSTRAINT `FK_UsersInformation_Users`
        FOREIGN KEY (`Users_IDX`) REFERENCES `Users`(`Users_IDX`);

ALTER TABLE `Users_Address`
    ADD CONSTRAINT `FK_UsersAddress_Users`
        FOREIGN KEY (`Users_IDX`) REFERENCES `Users`(`Users_IDX`);

ALTER TABLE `Users_Card`
    ADD CONSTRAINT `FK_UsersCard_Users`
        FOREIGN KEY (`Users_IDX`) REFERENCES `Users`(`Users_IDX`);

ALTER TABLE `Users_Verifications`
    ADD CONSTRAINT `FK_UsersVerifications_Users`
        FOREIGN KEY (`Users_IDX`) REFERENCES `Users`(`Users_IDX`);

ALTER TABLE `Seller`
    ADD CONSTRAINT `FK_Seller_Users`
        FOREIGN KEY (`Users_IDX`) REFERENCES `Users`(`Users_IDX`);

ALTER TABLE `Products`
    ADD CONSTRAINT `FK_Products_Seller`
        FOREIGN KEY (`Seller_IDX`) REFERENCES `Seller`(`Seller_IDX`);

ALTER TABLE `Products`
    ADD CONSTRAINT `FK_Products_Category`
        FOREIGN KEY (`Products_Category`) REFERENCES `Products_Category` (`Category_IDX`);

ALTER TABLE `Products`
    ADD CONSTRAINT `FK_Products_IP`
        FOREIGN KEY (`Products_IP`) REFERENCES `Products_Category` (`Category_IDX`);

ALTER TABLE `Product_Option`
    ADD CONSTRAINT `FK_ProductOption_Products`
        FOREIGN KEY (`Products_IDX`) REFERENCES `Products`(`Products_IDX`);

ALTER TABLE `Products_Category`
    ADD CONSTRAINT `FK_ProductsCategory_Parent`
        FOREIGN KEY (`Parent_IDX`) REFERENCES `Products_Category` (`Category_IDX`);

ALTER TABLE `Users_Information`
    ADD CONSTRAINT `FK_UI_Default_Address`
        FOREIGN KEY (`Default_Address`) REFERENCES `Users_Address` (`Address_IDX`),
    ADD CONSTRAINT `FK_UI_Default_Card`
        FOREIGN KEY (`Default_Card`) REFERENCES `Users_Card` (`Card_IDX`);


ALTER TABLE `Orders`
    ADD CONSTRAINT `FK_Orders_Users`
        FOREIGN KEY (`Users_IDX`) REFERENCES `Users`(`Users_IDX`);

ALTER TABLE `Orders`
    ADD CONSTRAINT `FK_Orders_Card`
        FOREIGN KEY (`Card_IDX`) REFERENCES `Users_Card`(`Card_IDX`);

ALTER TABLE `Orders_Item`
    ADD CONSTRAINT `FK_OrdersItem_Orders`
        FOREIGN KEY (`Orders_IDX`) REFERENCES `Orders`(`Orders_IDX`);

ALTER TABLE `Payment`
    ADD CONSTRAINT `FK_Payment_Users`
        FOREIGN KEY (`Users_IDX`) REFERENCES `Users`(`Users_IDX`);

ALTER TABLE `Payment`
    ADD CONSTRAINT `FK_Payment_Orders`
        FOREIGN KEY (`Orders_IDX`) REFERENCES `Orders`(`Orders_IDX`);

ALTER TABLE `Payment`
    ADD CONSTRAINT `FK_Payment_Card`
        FOREIGN KEY (`Card_IDX`) REFERENCES `Users_Card`(`Card_IDX`);

ALTER TABLE `Settlement_History`
    ADD CONSTRAINT `FK_SettlementHistory_Seller`
        FOREIGN KEY (`Seller_IDX`) REFERENCES `Seller`(`Seller_IDX`);

ALTER TABLE `Settlement_History`
    ADD CONSTRAINT `FK_SettlementHistory_Payment`
        FOREIGN KEY (`Payment_IDX`) REFERENCES `Payment`(`Payment_IDX`);

ALTER TABLE `Review`
    ADD CONSTRAINT `FK_Review_Products`
        FOREIGN KEY (`Products_IDX`) REFERENCES `Products`(`Products_IDX`);

ALTER TABLE `Review`
    ADD CONSTRAINT `FK_Review_Users`
        FOREIGN KEY (`Users_IDX`) REFERENCES `Users`(`Users_IDX`);

ALTER TABLE `Review`
    ADD CONSTRAINT `FK_Review_OrdersItem`
        FOREIGN KEY (`Orders_Item_IDX`) REFERENCES `Orders_Item`(`Orders_Item_IDX`);

ALTER TABLE `Customer_Service`
    ADD CONSTRAINT `FK_Customer_Service_Users`
        FOREIGN KEY (`Users_IDX`) REFERENCES `Users`(`Users_IDX`);

ALTER TABLE `Customer_Service`
    ADD CONSTRAINT `FK_Customer_Service_Products`
        FOREIGN KEY (`Products_IDX`) REFERENCES `Products`(`Products_IDX`);

ALTER TABLE `Customer_Service_Detail`
    ADD CONSTRAINT `FK_CSD_Parent`
        FOREIGN KEY (`Parent_IDX`) REFERENCES `Customer_Service_Detail`(`Customer_Service_Detail_IDX`);

ALTER TABLE `Customer_Service_Detail`
    ADD CONSTRAINT `FK_CSD_Users`
        FOREIGN KEY (`Users_IDX`) REFERENCES `Users`(`Users_IDX`);

ALTER TABLE `Customer_Service_Detail`
    ADD CONSTRAINT `FK_CSD_Customer_Service`
        FOREIGN KEY (`Customer_Service_IDX`) REFERENCES `Customer_Service`(`Customer_Service_IDX`);

ALTER TABLE `Assistant`
    ADD CONSTRAINT `FK_Assistant_Users`
        FOREIGN KEY (`Users_IDX`) REFERENCES `Users`(`Users_IDX`);

ALTER TABLE `Assistant_Chat`
    ADD CONSTRAINT `FK_AssistantChat_Assistant`
        FOREIGN KEY (`Assistant_IDX`) REFERENCES `Assistant`(`Assistant_IDX`);

SET FOREIGN_KEY_CHECKS = 1;
