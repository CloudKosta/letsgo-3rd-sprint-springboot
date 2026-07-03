package com.travel.letsgospringboot.myschedule.controller.response;

import com.travel.letsgospringboot.myschedule.vo.RouteScheduleVO;
import com.travel.letsgospringboot.myschedule.vo.ScheduleDetailVO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ScheduleDetailResponse {
    private ScheduleDetailVO schedule;
    private List<RouteScheduleVO> route;
    private String permission;
}
