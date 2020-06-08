package com.atguigu.test.excel;

import com.alibaba.excel.EasyExcel;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestEasyExcel {

    // EasyExcel实现读操作
    @Test
    public void testRead() {
        String filename = "/Users/zhengyuli/Desktop/test.xlsx";
        EasyExcel.read(filename, HeaderData.class, new ExcelListener()).sheet().doRead();
    }

    // EasyExcel实现写操作
    @Test
    public void testWrite() {
        String filename = "/Users/zhengyuli/Desktop/test.xlsx";
        EasyExcel.write(filename, HeaderData.class).sheet("学生列表").doWrite(getData());
    }

    private List<HeaderData> getData() {
        List<HeaderData> list = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            HeaderData data = new HeaderData();
            data.setSno(i);
            data.setSname("zhengyu" + i);
            list.add(data);
        }
        return list;
    }
}
