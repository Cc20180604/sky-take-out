package com.sky.utils;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;


public class POIUtil {
    /**
     * 加入一条数据
     * @param xssfSheet 表
     * @param rowIndex 行
     * @param columnIndex 列
     * @param value 值
     */
    public static void addValue(XSSFSheet xssfSheet, int rowIndex, int columnIndex, String value){
        //创建行，对应excel中的一行
        XSSFRow row = xssfSheet.createRow(rowIndex);
        //创建单元格，对应row中的一格
        XSSFCell cell = row.createCell(columnIndex);
        //单元格设置值
        cell.setCellValue(value);
    }

    public static void addColumn(XSSFSheet xssfSheet, int rowIndex, List<String> values){
        //创建行，对应excel中的一行
        XSSFRow row = xssfSheet.createRow(rowIndex);
        for (int i = 0; i < values.size(); i++){
            //创建单元格，对应row中的一格
            XSSFCell cell = row.createCell(i);
            //单元格设置值
            cell.setCellValue(values.get(i));
        }

    }
}
