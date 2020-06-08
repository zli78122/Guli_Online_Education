package com.atguigu.test.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class HeaderData {
    // 设置Excel表头名称
    @ExcelProperty(value = "学生编号", index = 0)
    private Integer sno;
    @ExcelProperty(value = "学生姓名", index = 1)
    private String sname;
}
