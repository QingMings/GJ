package com.yhl.gj.commons.enums;

public enum DelFlagEnum {

    DEPLOY(0,"未删除"),
    DEL(1,"删除"),
    UPDATE(2,"更新"),
    ;

    private Integer code;

    private String desc;

    private DelFlagEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
    public static DelFlagEnum getByStatus(Integer status) {
        DelFlagEnum[] values = DelFlagEnum.values();
        for (DelFlagEnum enums : values) {
            if(enums.getCode().equals(status)) {
                return enums;
            }
        }
        return null;
    }
}
