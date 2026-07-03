package com.travel.letsgospringboot.myschedule.repository;

import com.travel.letsgospringboot.myschedule.vo.ColleagueVO;
import com.travel.letsgospringboot.myschedule.vo.MapScheduleVO;
import com.travel.letsgospringboot.myschedule.vo.MyScheduleVO;
import com.travel.letsgospringboot.myschedule.vo.RouteScheduleVO;
import com.travel.letsgospringboot.myschedule.vo.ScheduleCreateVO;
import com.travel.letsgospringboot.myschedule.vo.ScheduleDetailVO;
import com.travel.letsgospringboot.myschedule.vo.ScheduleSummaryVO;
import com.travel.letsgospringboot.myschedule.vo.ScheduleTitleUpdateVO;
import com.travel.letsgospringboot.myschedule.vo.ShareVO;
import com.travel.letsgospringboot.myschedule.vo.StartAtUpdateVO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
@Mapper
public interface MyScheduleRepository {

    // ---- 목록 조회 ----
    List<MyScheduleVO> getMyScheduleListAllByDate(String userId);

    List<MyScheduleVO> getMyScheduleListAllByTitle(String userId);

    List<MyScheduleVO> getMyScheduleListSharedByDate(String userId);

    List<MyScheduleVO> getMyScheduleListSharedByTitle(String userId);

    List<MyScheduleVO> getMyScheduleListSearchByDate(String userId, String keyword);

    List<MyScheduleVO> getMyScheduleListSearchByTitle(String userId, String keyword);

    List<MyScheduleVO> getMyScheduleListSearchSharedByDate(String userId, String keyword);

    List<MyScheduleVO> getMyScheduleListSearchSharedByTitle(String userId, String keyword);

    // ---- 삭제 ----
    int deleteVisitItemsByScheduleId(String scheduleId);

    int deleteScheduleById(String scheduleId);

    int deleteScheduleByIdAndUserId(String scheduleId, String userId);

    boolean deleteVisitItemById(String visitItemId);

    // ---- 수정 ----
    int updateVisitItem(String visitItemId, int visitOrder, String distanceToNext);

    int updateSchedule(String scheduleId, String scheduleTitle, String startAt, String budgetDetail,
                       String todoDetail, int isShared, String userId);

    boolean setMyScheduleTitle(ScheduleTitleUpdateVO scheduleTitleUpdateVO);

    boolean setTodoDetail(String scheduleId, String todoDetail);

    boolean setBudgetDetail(String scheduleId, String budgetDetail);

    boolean setStartAt(StartAtUpdateVO startAtUpdateVO);

    boolean setCompanionPermission(String myScheduleId, String sharedUserId, String permission);

    boolean markScheduleShared(String myScheduleId);

    // ---- 단건 조회 ----
    ScheduleDetailVO getScheduleDetail(String scheduleId);

    String getTodoDetail(String scheduleId);

    String getBudgetDetail(String scheduleId);

    String getScheduleTitle(String scheduleId);

    String getStartAt(String scheduleId);

    String getNextMyScheduleIdCandidate();

    int checkMyScheduleIdExists(String id);

    String getLastPostId();

    int isScheduleOwnedByUser(String scheduleId, String userId);

    int hasReadSchedulePermission(String scheduleId, String userId);

    int hasWriteSchedulePermission(String scheduleId, String userId);

    String getSchedulePermission(String scheduleId, String userId);

    // ---- 삽입 ----
    boolean addMySchedule(ScheduleCreateVO scheduleCreateVO);

    boolean addVisitItem(int visitOrder, String placeId, String scheduleId);

    boolean addCompanion(String myScheduleId, String sharedUserId);

    int shareToPostInsert(ShareVO shareVO);

    int shareVisitItemsToPost(String myScheduleId);

    // ---- 결과 매핑 조회 ----
    List<ScheduleSummaryVO> listMyScheduleIdAndTitle(String userId);

    List<RouteScheduleVO> getScheduleRoute(String scheduleId);

    List<MapScheduleVO> getMapSchedule(String scheduleId);

    List<ColleagueVO> getCompanionList(String myScheduleId);
}
