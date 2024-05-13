package com.sky.service;

import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.springframework.stereotype.Service;


public interface WorkspaceService {

    BusinessDataVO businessData();

    SetmealOverViewVO setmeals();

    DishOverViewVO dish();

    OrderOverViewVO order();
}
