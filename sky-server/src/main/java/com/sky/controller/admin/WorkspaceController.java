package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "工作空间")
@RequestMapping("/admin/workspace")
@Slf4j
public class WorkspaceController {
    @Autowired
    private WorkspaceService workspaceService;

    @GetMapping("/businessData")
    @ApiOperation("近日运营数据")
    public Result<BusinessDataVO> todayBusiness(){
        BusinessDataVO businessDataVO = workspaceService.businessData();
        return Result.success(businessDataVO);
    }

    @GetMapping("/overviewSetmeals")
    @ApiOperation("统计套餐数目")
    public Result<SetmealOverViewVO> setmeals(){
        SetmealOverViewVO setmealOverViewVO = workspaceService.setmeals();
        return Result.success(setmealOverViewVO);
    }

    @GetMapping("/overviewDishes")
    @ApiOperation("统计菜品数目")
    public Result<DishOverViewVO> dish(){
        DishOverViewVO dishOverViewVO = workspaceService.dish();
        return Result.success(dishOverViewVO);
    }

    @GetMapping("/overviewOrders")
    public Result<OrderOverViewVO> order(){
        OrderOverViewVO orderOverViewVO = workspaceService.order();
        return Result.success(orderOverViewVO);
    }
}
