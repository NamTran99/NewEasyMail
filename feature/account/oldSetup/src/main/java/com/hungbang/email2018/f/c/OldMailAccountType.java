package com.hungbang.email2018.f.c;

public enum OldMailAccountType {
    GOOGLE(1), OUTLOOK(2), YANDEX(3), YAHOO(4), AOL(5), MAIL_COM(6), OTHER(7);

    private final int value;

    OldMailAccountType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static OldMailAccountType fromInt(int value) {
        for (OldMailAccountType type : OldMailAccountType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("No enum constant with value " + value);
    }
}
