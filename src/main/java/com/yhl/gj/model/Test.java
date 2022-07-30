package com.yhl.gj.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "HZGJ.GJ_TEST")
public class Test {
    @TableId(value = "ID", type = IdType.ASSIGN_UUID)
    private String id;

    @TableField(value = "\"NAME\"")
    private String name;

    @TableField(value = "EMAIL")
    private String email;

    public static final String COL_ID = "ID";

    public static final String COL_NAME = "NAME";

    public static final String COL_EMAIL = "EMAIL";
}