package com.example.library.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ColumnWidth(20)
public class BookExcelVO {

    @ExcelProperty("书名")
    private String bookName;

    @ExcelProperty("作者")
    private String author;

    @ExcelProperty(value = "价格", converter = com.alibaba.excel.converters.bigdecimal.BigDecimalNumberConverter.class)
    private BigDecimal price;

    @ExcelProperty("库存")
    private Integer stock;

    @ExcelProperty("分类名称")
    private String categoryName;
}