package com.travel.letsgospringboot.myschedule.controller;

import com.travel.letsgospringboot.common.PageResponse;
import com.travel.letsgospringboot.myschedule.controller.request.*;
import com.travel.letsgospringboot.myschedule.controller.response.ScheduleDetailResponse;
import com.travel.letsgospringboot.myschedule.service.MyScheduleService;
import com.travel.letsgospringboot.myschedule.vo.*;
import com.travel.letsgospringboot.auth.AppUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/myschedule/api")
@RequiredArgsConstructor
public class MyScheduleRestController {

    private final MyScheduleService myScheduleService;

    @GetMapping("/list")
    public PageResponse<MyScheduleVO> getMyScheduleList(@ModelAttribute SearchRequest searchRequest,
                                                        @RequestParam(value = "page", defaultValue = "1") int page,
                                                        @AuthenticationPrincipal AppUserDetails userDetails) {

        String userId = userDetails.getUsername();
        boolean isShared = searchRequest.isShared();
        boolean isSortTitle = "title".equals(searchRequest.getSortType());

        return myScheduleService.getMyScheduleListPaged(userId, searchRequest.getSearchTitle(),
                isShared, isSortTitle, page, 12);
    }

    @GetMapping("/{scheduleId}/detail")
    public ScheduleDetailResponse getScheduleDetail(@PathVariable String scheduleId,
                                                    @AuthenticationPrincipal AppUserDetails userDetails) {
        String userId = userDetails.getUsername();
        return new ScheduleDetailResponse(
                myScheduleService.getScheduleDetail(scheduleId, userId),
                myScheduleService.getScheduleRoute(scheduleId, userId),
                myScheduleService.getSchedulePermission(scheduleId, userId)
        );
    }

    @PostMapping
    public boolean addMySchedule(@RequestBody AddScheduleRequest addScheduleRequest,
                                 @AuthenticationPrincipal AppUserDetails userDetails) {
        return myScheduleService.addMySchedule(ScheduleCreateVO.builder()
                .myScheduleId(addScheduleRequest.getScheduleId())
                .title(addScheduleRequest.getTitle())
                .userId(userDetails.getUsername())
                .build());
    }

    @DeleteMapping("/{scheduleId}")
    public boolean deleteMySchedule(@PathVariable String scheduleId,
                                    @AuthenticationPrincipal AppUserDetails userDetails) {
        return myScheduleService.deleteMySchedule(scheduleId, userDetails.getUsername());
    }

    @PutMapping("/{scheduleId}/title")
    public boolean setMyScheduleTitle(@PathVariable String scheduleId,
                                      @RequestBody ScheduleTitleRequest scheduleTitleRequest,
                                      @AuthenticationPrincipal AppUserDetails userDetails) {
        return myScheduleService.setMyScheduleTitle(ScheduleTitleUpdateVO.builder()
                .title(scheduleTitleRequest.getTitle())
                .scheduleId(scheduleId)
                .userId(userDetails.getUsername())
                .build());
    }

    @PutMapping("/{scheduleId}/startAt")
    public boolean setStartAt(@PathVariable String scheduleId,
                              @RequestBody StartAtRequest startAtRequest,
                              @AuthenticationPrincipal AppUserDetails userDetails) {
        return myScheduleService.setStartAt(StartAtUpdateVO.builder()
                .scheduleId(scheduleId)
                .startAt(startAtRequest.getStartAt())
                .userId(userDetails.getUsername())
                .build());
    }

    @PutMapping("/{scheduleId}/todo")
    public boolean setTodoDetail(@PathVariable String scheduleId,
                                 @RequestBody TodoDetailRequest todoDetailRequest,
                                 @AuthenticationPrincipal AppUserDetails userDetails) {
        return myScheduleService.setTodoDetail(TodoUpdateVO.builder()
                .scheduleId(scheduleId)
                .todoDetail(todoDetailRequest.getTodoDetail())
                .userId(userDetails.getUsername())
                .build());
    }

    @PutMapping("/{scheduleId}/budget")
    public boolean setBudgetDetail(@PathVariable String scheduleId,
                                   @RequestBody BudgetDetailRequest budgetDetailRequest,
                                   @AuthenticationPrincipal AppUserDetails userDetails) {
        return myScheduleService.setBudgetDetail(BudgetUpdateVO.builder()
                .scheduleId(scheduleId)
                .budgetDetail(budgetDetailRequest.getBudgetDetail())
                .userId(userDetails.getUsername())
                .build());
    }

    @PostMapping("/{scheduleId}/share")
    public String shareToPost(@PathVariable String scheduleId,
                              @RequestBody ShareRequest shareRequest,
                              @AuthenticationPrincipal AppUserDetails userDetails) {
        return myScheduleService.shareToPost(ShareVO.builder()
                .scheduleId(scheduleId)
                .userId(userDetails.getUsername())
                .isAnonymous(shareRequest.getIsAnonymous())
                .build());
    }

    @GetMapping("/{scheduleId}/companion")
    public List<ColleagueVO> getCompanionList(@PathVariable String scheduleId,
                                              @AuthenticationPrincipal AppUserDetails userDetails) {
        return myScheduleService.getCompanionList(scheduleId, userDetails.getUsername());
    }

    @PostMapping("/{scheduleId}/companion")
    public boolean addCompanion(@PathVariable String scheduleId,
                                @RequestBody CompanionRequest companionRequest,
                                @AuthenticationPrincipal AppUserDetails userDetails) {
        return myScheduleService.addCompanion(scheduleId, companionRequest.getSharedUserId(),
                userDetails.getUsername());
    }

    @PutMapping("/{scheduleId}/companion/permission")
    public boolean setCompanionPermission(@PathVariable String scheduleId,
                                          @RequestBody CompanionPermissionRequest companionPermissionRequest,
                                          @AuthenticationPrincipal AppUserDetails userDetails) {
        return myScheduleService.setCompanionPermission(CompanionPermissionVO.builder()
                .scheduleId(scheduleId)
                .sharedUserId(companionPermissionRequest.getSharedUserId())
                .permission(companionPermissionRequest.getPermission())
                .userId(userDetails.getUsername())
                .build());
    }

    @PostMapping("/{scheduleId}/visit")
    public boolean addVisitItem(@PathVariable String scheduleId,
                                @RequestBody VisitItemRequest visitItemRequest,
                                @AuthenticationPrincipal AppUserDetails userDetails) {
        return myScheduleService.addVisitItem(VisitItemCreateVO.builder()
                .visitOrder(visitItemRequest.getVisitOrder())
                .placeId(visitItemRequest.getPlaceId())
                .scheduleId(scheduleId)
                .userId(userDetails.getUsername())
                .build());
    }

    @PutMapping("/{scheduleId}/visit/orders")
    public boolean updateVisitOrders(@PathVariable String scheduleId,
                                     @RequestBody List<VisitOrderVO> visitOrderVOS,
                                     @AuthenticationPrincipal AppUserDetails userDetails) {
        return myScheduleService.updateVisitOrders(VisitOrderUpdateVO.builder()
                .orders(visitOrderVOS)
                .scheduleId(scheduleId)
                .userId(userDetails.getUsername())
                .build());
    }

    @DeleteMapping("/{scheduleId}/visit/{visitItemId}")
    public boolean deleteVisitItemById(@PathVariable String scheduleId,
                                       @PathVariable String visitItemId,
                                       @AuthenticationPrincipal AppUserDetails userDetails) {
        return myScheduleService.deleteVisitItemById(VisitItemDeleteVO.builder()
                .visitItemId(visitItemId)
                .scheduleId(scheduleId)
                .userId(userDetails.getUsername())
                .build());
    }

    @GetMapping("/{scheduleId}/mapSchedule")
    public List<MapScheduleVO> getMapSchedule(@PathVariable String scheduleId,
                                              @AuthenticationPrincipal AppUserDetails userDetails){
        return myScheduleService.getMapSchedule(scheduleId, userDetails.getUsername());
    }

}
