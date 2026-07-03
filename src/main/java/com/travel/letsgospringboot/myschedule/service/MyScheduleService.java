package com.travel.letsgospringboot.myschedule.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.travel.letsgospringboot.common.PageResponse;
import com.travel.letsgospringboot.exception.AccessDeniedException;
import com.travel.letsgospringboot.exception.InvalidInputException;
import com.travel.letsgospringboot.myschedule.repository.MyScheduleRepository;
import com.travel.letsgospringboot.myschedule.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MyScheduleService {
    private final MyScheduleRepository myScheduleRepository;

    public List<MyScheduleVO> getMyScheduleListAllByDate(String userId) {
        List<MyScheduleVO> result = processMyScheduleList(myScheduleRepository.getMyScheduleListAllByDate(userId));
        return result;
    }

    public List<MyScheduleVO> getMyScheduleListAllByTitle(String userId) {
        List<MyScheduleVO> result = processMyScheduleList(myScheduleRepository.getMyScheduleListAllByTitle(userId));
        return result;
    }

    public List<MyScheduleVO> getMyScheduleListSharedByDate(String userId) {
        List<MyScheduleVO> result = processMyScheduleList(myScheduleRepository.getMyScheduleListSharedByDate(userId));
        return result;
    }

    public List<MyScheduleVO> getMyScheduleListSharedByTitle(String userId) {
        List<MyScheduleVO> result = processMyScheduleList(myScheduleRepository.getMyScheduleListSharedByTitle(userId));
        return result;
    }

    public List<MyScheduleVO> getMyScheduleListSearchByDate(String userId, String keyword) {
        List<MyScheduleVO> result = processMyScheduleList(myScheduleRepository.getMyScheduleListSearchByDate(userId, keyword));
        return result;
    }

    public List<MyScheduleVO> getMyScheduleListSearchByTitle(String userId, String keyword) {
        List<MyScheduleVO> result = processMyScheduleList(myScheduleRepository.getMyScheduleListSearchByTitle(userId, keyword));
        return result;
    }

    public List<MyScheduleVO> getMyScheduleListSearchSharedByDate(String userId, String keyword) {
        List<MyScheduleVO> result = processMyScheduleList(myScheduleRepository.getMyScheduleListSearchSharedByDate(userId, keyword));
        return result;
    }

    public List<MyScheduleVO> getMyScheduleListSearchSharedByTitle(String userId, String keyword) {
        List<MyScheduleVO> result = processMyScheduleList(myScheduleRepository.getMyScheduleListSearchSharedByTitle(userId, keyword));
        return result;
    }

    public PageResponse<MyScheduleVO> getMyScheduleListPaged(String userId, String searchTitle,
                                                             boolean isShared, boolean isSortTitle,
                                                             int page, int size) {
        String keyword = (searchTitle == null || searchTitle.trim().isEmpty()) ? null : searchTitle.trim();
        boolean hasKeyword = keyword != null;

        PageHelper.startPage(page, size);
        List<MyScheduleVO> rows;
        if (isShared) {
            if (hasKeyword) {
                rows = isSortTitle
                        ? myScheduleRepository.getMyScheduleListSearchSharedByTitle(userId, keyword)
                        : myScheduleRepository.getMyScheduleListSearchSharedByDate(userId, keyword);
            } else {
                rows = isSortTitle
                        ? myScheduleRepository.getMyScheduleListSharedByTitle(userId)
                        : myScheduleRepository.getMyScheduleListSharedByDate(userId);
            }
        } else if (hasKeyword) {
            rows = isSortTitle
                    ? myScheduleRepository.getMyScheduleListSearchByTitle(userId, keyword)
                    : myScheduleRepository.getMyScheduleListSearchByDate(userId, keyword);
        } else {
            rows = isSortTitle
                    ? myScheduleRepository.getMyScheduleListAllByTitle(userId)
                    : myScheduleRepository.getMyScheduleListAllByDate(userId);
        }

        PageInfo<MyScheduleVO> pageInfo = new PageInfo<>(rows);
        List<MyScheduleVO> content = processMyScheduleList(rows);
        return new PageResponse<>(content, pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal());
    }

    private List<MyScheduleVO> processMyScheduleList(List<MyScheduleVO> data) {
        for (MyScheduleVO vo : data) {
            vo.setStartAt(formatStartAt(vo.getStartAt()));
            vo.setAddr1(formatAddr1(vo.getAddr1()));
        }
        return data;
    }

    private String formatStartAt(String startAt) {
        if (startAt == null) {
            return null;
        }
        return startAt.length() >= 10 ? startAt.substring(0, 10) : startAt;
    }

    private String formatAddr1(String addr1) {
        if (addr1 == null) {
            return null;
        }
        String[] tokens = addr1.trim().split("\\s+");
        if (tokens.length <= 2) {
            return addr1.trim();
        }
        return tokens[0] + " " + tokens[1];
    }

    public boolean setMyScheduleTitle(ScheduleTitleUpdateVO scheduleTitleUpdateVO) {
        String scheduleId = scheduleTitleUpdateVO.getScheduleId();
        String userId = scheduleTitleUpdateVO.getUserId();
        if(!hasWriteSchedulePermission(scheduleId, userId))
            throw new AccessDeniedException("편집 권한이 없습니다");
        boolean result = myScheduleRepository.setMyScheduleTitle(scheduleTitleUpdateVO);
        log.info("일정 제목 수정 - scheduleId={}, userId={}", scheduleId, userId);
        return result;
    }

    public boolean setTodoDetail(TodoUpdateVO todoUpdateVO) {
        String scheduleId = todoUpdateVO.getScheduleId();
        String userId = todoUpdateVO.getUserId();
        if(!hasWriteSchedulePermission(scheduleId, userId))
            throw new AccessDeniedException("편집 권한이 없습니다");
        boolean result = myScheduleRepository.setTodoDetail(scheduleId, todoUpdateVO.getTodoDetail());
        log.info("일정 할일 수정 - scheduleId={}, userId={}", scheduleId, userId);
        return result;
    }

    public ScheduleDetailVO getScheduleDetail(String scheduleId, String userId) {
        if(!hasReadSchedulePermission(scheduleId, userId))
            throw new AccessDeniedException("존재하지 않거나 권한이 없습니다");
//        if(!isScheduleOwnedByUser(scheduleId, userId))
//           throw new AccessDeniedException("존재하지 않거나 권한이 없습니다");
        return myScheduleRepository.getScheduleDetail(scheduleId);
    }

    public String getTodoDetail(String scheduleId) {
        return myScheduleRepository.getTodoDetail(scheduleId);
    }

    public boolean setBudgetDetail(BudgetUpdateVO budgetUpdateVO) {
        String scheduleId = budgetUpdateVO.getScheduleId();
        String userId = budgetUpdateVO.getUserId();
        if(!hasWriteSchedulePermission(scheduleId, userId))
            throw new AccessDeniedException("편집 권한이 없습니다");
        boolean result = myScheduleRepository.setBudgetDetail(scheduleId, budgetUpdateVO.getBudgetDetail());
        log.info("일정 예산 수정 - scheduleId={}, userId={}", scheduleId, userId);
        return result;
    }

    public String getBudgetDetail(String scheduleId) {
        return myScheduleRepository.getBudgetDetail(scheduleId);
    }

    public String getScheduleTitle(String scheduleId) {
        return myScheduleRepository.getScheduleTitle(scheduleId);
    }

    public String getStartAt(String scheduleId) {
        return myScheduleRepository.getStartAt(scheduleId);
    }

    public boolean setStartAt(StartAtUpdateVO startAtUpdateVO) {
        String scheduleId = startAtUpdateVO.getScheduleId();
        String userId = startAtUpdateVO.getUserId();
        if(!hasWriteSchedulePermission(scheduleId, userId))
            throw new AccessDeniedException("편집 권한이 없습니다");
        boolean result = myScheduleRepository.setStartAt(startAtUpdateVO);
        log.info("일정 시작일 수정 - scheduleId={}, userId={}", scheduleId, userId);
        return result;
    }

    public boolean addMySchedule(ScheduleCreateVO scheduleCreateVO) {
        String myScheduleId = scheduleCreateVO.getMyScheduleId();
        String userId = scheduleCreateVO.getUserId();
        boolean result = myScheduleRepository.addMySchedule(scheduleCreateVO);
        log.info("내 일정 추가 - userId={}, scheduleId={}, title={}", userId, myScheduleId, scheduleCreateVO.getTitle());
        return result;
    }

    private boolean isScheduleOwnedByUser(String scheduleId, String userId) {
        boolean owned = myScheduleRepository.isScheduleOwnedByUser(scheduleId, userId) > 0;
        if (!owned) {
            log.warn("일정 접근 권한 위반 차단 - userId={}, scheduleId={}", userId, scheduleId);
        }
        return owned;
    }

    private boolean hasReadSchedulePermission(String scheduleId, String userId) {
        boolean hasPermission = myScheduleRepository.hasReadSchedulePermission(scheduleId, userId) > 0;
        if (!hasPermission) {
            log.warn("일정 접근 권한 위반 차단 - userId={}, scheduleId={}", userId, scheduleId);
        }
        return  hasPermission;
    }

    private boolean hasWriteSchedulePermission(String scheduleId, String userId) {
        boolean hasPermission = myScheduleRepository.hasWriteSchedulePermission(scheduleId, userId) > 0;
        if (!hasPermission) {
            log.warn("일정 편집 권한 위반 차단 - userId={}, scheduleId={}", userId, scheduleId);
        }
        return hasPermission;
    }

    public String getSchedulePermission(String scheduleId, String userId) {
        if (!hasReadSchedulePermission(scheduleId, userId))
            throw new AccessDeniedException("존재하지 않거나 권한이 없습니다");
        return myScheduleRepository.getSchedulePermission(scheduleId, userId);
    }

    public List<ScheduleSummaryVO> listMyScheduleIdAndTitle(String userId) {
        return myScheduleRepository.listMyScheduleIdAndTitle(userId);
    }

    public List<RouteScheduleVO> getScheduleRoute(String scheduleId, String userId) {
        if(!hasReadSchedulePermission(scheduleId, userId))
            throw new AccessDeniedException("권한이 없습니다");
//        if(!isScheduleOwnedByUser(scheduleId, userId))
//            throw new AccessDeniedException("권한이 없습니다");
        return myScheduleRepository.getScheduleRoute(scheduleId);
    }

    public List<MapScheduleVO> getMapSchedule(String scheduleId, String userId) {
        if(!hasReadSchedulePermission(scheduleId, userId))
            throw new AccessDeniedException("권한이 없습니다");
//        if(!isScheduleOwnedByUser(scheduleId, userId))
//            throw new AccessDeniedException("권한이 없습니다");
        return myScheduleRepository.getMapSchedule(scheduleId);
    }
    @Transactional
    public boolean addVisitItem(VisitItemCreateVO visitItemCreateVO) {
        String scheduleId = visitItemCreateVO.getScheduleId();
        String userId = visitItemCreateVO.getUserId();
        String placeId = visitItemCreateVO.getPlaceId();
        if(!hasWriteSchedulePermission(scheduleId, userId))
            throw new AccessDeniedException("편집 권한이 없습니다");
        boolean result = myScheduleRepository.addVisitItem(visitItemCreateVO.getVisitOrder(), placeId, scheduleId);
        log.info("방문 항목 추가 - scheduleId={}, placeId={}, userId={}", scheduleId, placeId, userId);
        return result;
    }
    @Transactional
    public boolean deleteVisitItemById(VisitItemDeleteVO visitItemDeleteVO) {
        String scheduleId = visitItemDeleteVO.getScheduleId();
        String userId = visitItemDeleteVO.getUserId();
        String visitItemId = visitItemDeleteVO.getVisitItemId();
        if(!hasWriteSchedulePermission(scheduleId, userId))
            throw new AccessDeniedException("편집 권한이 없습니다");
        boolean result = myScheduleRepository.deleteVisitItemById(visitItemId);
        log.info("방문 항목 삭제 - scheduleId={}, visitItemId={}, userId={}", scheduleId, visitItemId, userId);
        return result;
    }
    @Transactional
    public boolean addCompanion(String myScheduleId, String sharedUserId, String userId) {
        if (!isScheduleOwnedByUser(myScheduleId, userId))
            throw new AccessDeniedException("권한이 없습니다");
        if (myScheduleRepository.isScheduleOwnedByUser(myScheduleId, sharedUserId) > 0) {
            log.warn("본인 소유 일정 공유 차단 - scheduleId={}, sharedUserId={}", myScheduleId, sharedUserId);
            throw new InvalidInputException("본인이 소유한 일정에는 공유 대상으로 추가할 수 없습니다.");
        }
        boolean result = myScheduleRepository.addCompanion(myScheduleId, sharedUserId);
        myScheduleRepository.markScheduleShared(myScheduleId);
        log.info("동행자 추가 - scheduleId={}, sharedUserId={}", myScheduleId, sharedUserId);
        return result;
    }
    @Transactional
    public boolean setCompanionPermission(CompanionPermissionVO companionPermissionVO) {
        String scheduleId = companionPermissionVO.getScheduleId();
        String sharedUserId = companionPermissionVO.getSharedUserId();
        String permission = companionPermissionVO.getPermission();
        if(!isScheduleOwnedByUser(scheduleId, companionPermissionVO.getUserId()))
            throw new AccessDeniedException("권한이 없습니다");
        boolean result = myScheduleRepository.setCompanionPermission(scheduleId, sharedUserId, permission);
        log.info("동행자 권한 수정 - scheduleId={}, sharedUserId={}, permission={}", scheduleId, sharedUserId, permission);
        return result;
    }
    @Transactional
    public List<ColleagueVO> getCompanionList(String scheduleId, String userId) {
        if(!isScheduleOwnedByUser(scheduleId, userId))
            throw new AccessDeniedException("권한이 없습니다");
        return myScheduleRepository.getCompanionList(scheduleId);
    }

    @Transactional
    public boolean deleteMySchedule(String scheduleId, String userId) {
        if(!isScheduleOwnedByUser(scheduleId, userId))
            throw new AccessDeniedException("권한이 없습니다");
        myScheduleRepository.deleteVisitItemsByScheduleId(scheduleId);
        boolean result = myScheduleRepository.deleteScheduleById(scheduleId) > 0;
        log.info("일정 삭제 - scheduleId={}, userId={}", scheduleId, userId);
        return result;
    }

    @Transactional
    public boolean deleteMyScheduleList(String userId, String[] scheduleIds) {
        boolean allDeleted = scheduleIds.length > 0;
        for (String id : scheduleIds) {
            myScheduleRepository.deleteVisitItemsByScheduleId(id);
            if (myScheduleRepository.deleteScheduleByIdAndUserId(id, userId) <= 0) {
                allDeleted = false;
            }
        }
        log.info("일정 목록 삭제 - userId={}, 건수={}", userId, scheduleIds.length);
        return allDeleted;
    }

    @Transactional
    public boolean setMySchedule(ScheduleBatchUpdateVO scheduleBatchUpdateVO) {
        String scheduleId = scheduleBatchUpdateVO.getScheduleId();
        String userId = scheduleBatchUpdateVO.getUserId();
        String[] visitItemId = scheduleBatchUpdateVO.getVisitItemId();
        int[] visitOrder = scheduleBatchUpdateVO.getVisitOrder();
        String[] distanceToNext = scheduleBatchUpdateVO.getDistanceToNext();
        boolean updated = myScheduleRepository.updateSchedule(scheduleId, scheduleBatchUpdateVO.getScheduleTitle(),
                scheduleBatchUpdateVO.getStartAt(), scheduleBatchUpdateVO.getBudgetDetail(), scheduleBatchUpdateVO.getTodoDetail(),
                scheduleBatchUpdateVO.getIsShared(), userId) > 0;
        for (int i = 0; i < visitItemId.length; i++) {
            myScheduleRepository.updateVisitItem(visitItemId[i], visitOrder[i], distanceToNext[i]);
        }
        log.info("일정 일괄 수정 - scheduleId={}, userId={}, 방문항목수={}", scheduleId, userId, visitItemId.length);
        return updated;
    }

    @Transactional
    public boolean updateVisitOrders(VisitOrderUpdateVO visitOrderUpdateVO) {
        String scheduleId = visitOrderUpdateVO.getScheduleId();
        String userId = visitOrderUpdateVO.getUserId();
        List<VisitOrderVO> orders = visitOrderUpdateVO.getOrders();
        if(!hasWriteSchedulePermission(scheduleId, userId))
            throw new AccessDeniedException("편집 권한이 없습니다");
        boolean allUpdated = !orders.isEmpty();
        for (VisitOrderVO order : orders) {
            if (myScheduleRepository.updateVisitItem(
                    order.getVisitItemId(), order.getVisitOrder(), order.getDistance()) <= 0) {
                allUpdated = false;
            }
        }
        log.info("방문 순서 수정 - scheduleId={}, userId={}, 건수={}", scheduleId, userId, orders.size());
        return allUpdated;
    }

    public String allocateNextMyScheduleId() {
        String candidate;
        do {
            candidate = myScheduleRepository.getNextMyScheduleIdCandidate();
        } while (myScheduleRepository.checkMyScheduleIdExists(candidate) > 0);
        return candidate;
    }

    @Transactional
    public String shareToPost(ShareVO shareVO) {
        String scheduleId = shareVO.getScheduleId();
        String userId = shareVO.getUserId();
        if(!isScheduleOwnedByUser(scheduleId, userId))
            throw new AccessDeniedException("권한이 없습니다");
        myScheduleRepository.shareToPostInsert(shareVO);
        myScheduleRepository.shareVisitItemsToPost(scheduleId);
        String postId = myScheduleRepository.getLastPostId();
        log.info("일정 게시판 공유 - scheduleId={}, userId={}, postId={}", scheduleId, userId, postId);
        return postId;
    }
}
