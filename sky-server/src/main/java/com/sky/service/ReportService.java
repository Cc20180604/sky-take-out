package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.LocalDate;

public interface ReportService {
    /**
     * 统计每日销售总额
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO turnover(LocalDate begin, LocalDate end);

    /**
     * 统计每日新增用户
     * @param begin
     * @param end
     * @return
     */
    UserReportVO user(LocalDate begin, LocalDate end);

    /**
     * 统计订单
     * @param begin
     * @param end
     * @return
     */
    OrderReportVO order(LocalDate begin, LocalDate end);

    /**
     * 销量排行前十
     * @param begin
     * @param end
     * @return
     */
    SalesTop10ReportVO top10(LocalDate begin, LocalDate end);

    /**
     * 生成excel文件报表
     * @return
     */
    XSSFWorkbook excel();
}
