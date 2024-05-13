package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.OrderDetail;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrdersMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.models.auth.In;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrdersMapper ordersMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    /**
     * 统计每日销售总额
     * @return
     */
    @Override
    public TurnoverReportVO turnover(LocalDate begin, LocalDate end) {
        //遍历出日期
        List<LocalDate> dates = new ArrayList<>();
        while (begin.isBefore(end.plusDays(1))){
            dates.add(begin);
            begin = begin.plusDays(1);
        }
        //遍历出每日营业额
        List<Integer> turnovers = new ArrayList<>();
        for (LocalDate date : dates) {
            LocalDateTime minDate = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime maxDate = LocalDateTime.of(date, LocalTime.MAX);
            Integer turnover = ordersMapper.sum(minDate, maxDate);
            turnovers.add(turnover);
        }

        return new TurnoverReportVO(StringUtils.join(dates, ','),StringUtils.join(turnovers, ','));
    }

    /**
     * 统计每日新增用户
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO user(LocalDate begin, LocalDate end) {
        //遍历出日期
        List<LocalDate> dates = new ArrayList<>();
        while (begin.isBefore(end.plusDays(1))){
            dates.add(begin);
            begin = begin.plusDays(1);
        }

        //遍历出 当日用户总数 当日新增用户
        List<Integer> totalUser = new ArrayList<>();
        List<Integer> newUser = new ArrayList<>();
        for (LocalDate date : dates) {
            LocalDateTime minDateTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime maxDateTime = LocalDateTime.of(date, LocalTime.MAX);
            //当天的用户总数
            Integer totalNum = userMapper.countUserBefore(maxDateTime);
            //当天新增的用户总数
            Integer todayNum = userMapper.countUser(minDateTime, maxDateTime);

            totalUser.add(totalNum);
            newUser.add(todayNum);
        }

        return UserReportVO.builder().dateList(StringUtils.join(dates, ',')).newUserList(StringUtils.join(newUser,',')).totalUserList(StringUtils.join(totalUser,',')).build();
    }
    /**
     * 统计订单
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO order(LocalDate begin, LocalDate end) {
        //遍历出日期
        List<LocalDate> dates = new ArrayList<>();
        while (begin.isBefore(end.plusDays(1))){
            dates.add(begin);
            begin = begin.plusDays(1);
        }
        //每日订单数，以逗号分隔，例如：260,210,215
        List<Integer> orderCountList = new ArrayList<>();
        //每日有效订单数，以逗号分隔，例如：20,21,10
        List<Integer> validOrderCountList = new ArrayList<>();
        //订单完成率
        Double orderCompletionRate = null;
        //订单总数
        Integer orderAllNum = 0;
        //有效订单总数
        Integer validAllNum = 0;
        //查询
        for (LocalDate date : dates) {
            LocalDateTime minDateTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime maxDateTime = LocalDateTime.of(date, LocalTime.MAX);
            //每日订单数
            Integer orderNum = ordersMapper.orderCount(minDateTime, maxDateTime);
            orderCountList.add(orderNum);
            //每日有效订单数
            validAllNum = ordersMapper.validOrderCount(minDateTime, maxDateTime);
            validOrderCountList.add(validAllNum);
        }
        //订单总数(形参begin被覆盖,使用dates第一个参数作为开始)
        orderAllNum = ordersMapper.orderCount(LocalDateTime.of(dates.get(0), LocalTime.MIN),LocalDateTime.of(end, LocalTime.MAX));
        //订单完成率
        if (validAllNum != 0){
            orderCompletionRate = Double.valueOf(validAllNum) / Double.valueOf(orderAllNum);
        }
        return OrderReportVO.builder()
                //日期
                .dateList(StringUtils.join(dates, ','))
                //每日订单数
                .orderCountList(StringUtils.join(orderCountList, ','))
                //每日有效订单数
                .validOrderCountList(StringUtils.join(validOrderCountList, ','))
                //总订单数
                .totalOrderCount(orderAllNum)
                //总有效订单数
                .validOrderCount(validAllNum)
                //订单完成率
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     * 指定时间
     * 销量排行前十
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO top10(LocalDate begin, LocalDate end) {
        List<String> name = new ArrayList<>();
        List<Integer> number = new ArrayList<>();
        List<GoodsSalesDTO> goodsSalesDTOS = orderDetailMapper.top10(LocalDateTime.of(begin, LocalTime.MIN), LocalDateTime.of(end, LocalTime.MAX));
        for (GoodsSalesDTO goodsSalesDTO : goodsSalesDTOS) {
            name.add(goodsSalesDTO.getName());
            number.add(goodsSalesDTO.getNumber());
        }
        return SalesTop10ReportVO.builder().nameList(StringUtils.join(name,',')).numberList(StringUtils.join(number,',')).build();
    }
    /**
     * 生成excel文件报表
     * @return
     */
    @Override
    public XSSFWorkbook excel() {
        //创建工作簿，对应整个xlsx文件
        XSSFWorkbook workbook = new XSSFWorkbook();
        //创建sheet，对应excel的单个sheet
        XSSFSheet sheet = workbook.createSheet("销售量");
        //创建行，对应excel中的一行
        XSSFRow row = sheet.createRow(0);
        //创建单元格，对应row中的一格
        XSSFCell cell = row.createCell(0);
        //单元格设置值
        cell.setCellValue("1");
        return workbook;
    }


}
