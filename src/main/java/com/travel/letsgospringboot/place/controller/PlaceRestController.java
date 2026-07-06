package com.travel.letsgospringboot.place.controller;

import com.travel.letsgospringboot.common.PageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.travel.letsgospringboot.auth.AppUserDetails;

import com.travel.letsgospringboot.place.service.PlaceService;
import com.travel.letsgospringboot.place.vo.PlaceVO;
import com.travel.letsgospringboot.place.vo.VisitItemVO;
import com.travel.letsgospringboot.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PlaceRestController {

    private final PlaceService placeService;

    @GetMapping("/leisureListAjax")
    public List<PlaceVO> getLeisureListAjax(
            @RequestParam(value = "sortOrder", defaultValue = "distance") String sortOrder,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "keyword", required = false) String keyword) {
        String sortBy = ("like".equalsIgnoreCase(sortOrder) || "popular".equalsIgnoreCase(sortOrder)) ? "like"
                : "title";
        return placeService.searchPlaces("LEISURE", category, keyword, sortBy);
    }


    @GetMapping("/list/leisure")
    public PageResponse<PlaceVO> leisureList(Model model,
                              @RequestParam(value = "category", required = false) String category,
                              @RequestParam(value = "keyword", required = false) String keyword,
                              @RequestParam(value = "sortOrder", defaultValue = "distance") String sortOrder,
                              @RequestParam(value = "page", defaultValue = "1") int page,
                              @RequestParam(value = "size", defaultValue = "12") int size) {

        String sortBy = "popular".equalsIgnoreCase(sortOrder) ? "like" : "title";
        PageResponse<PlaceVO> leisure = placeService.getPlaceListPaged("LEISURE", category, keyword, sortBy, page, size);
        return leisure;
    }

    @GetMapping("/list/restaurant")
    public PageResponse<PlaceVO> restaurantList(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "sortOrder", defaultValue = "name") String sortOrder,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "12") int size) {

        String sortBy = "popular".equalsIgnoreCase(sortOrder) ? "like" : "title";
        return placeService.getPlaceListPaged("RESTAURANT", category, keyword, sortBy, page, size);
    }

    @GetMapping("/list/stay")
    public PageResponse<PlaceVO> stayList(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "sortOrder", defaultValue = "name") String sortOrder,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "12") int size) {

        String sortBy = "popular".equalsIgnoreCase(sortOrder) ? "like" : "title";
        return placeService.getPlaceListPaged("STAY", category, keyword, sortBy, page, size);
    }

    @PostMapping("/placeLikeAjax")
    public Map<String, Object> likePlace(
            @RequestParam("placeId") String placeId,
            @RequestParam("placeType") String placeType,
            @AuthenticationPrincipal AppUserDetails userDetails) {

        if (userDetails == null) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }

        try {
            placeService.setPlaceLikeCount(placeId);
            int updatedCount = placeService.getPlaceLikeCount(placeType, placeId);

            Map<String, Object> response = new HashMap<>();
            response.put("result", "success");
            response.put("likeCount", updatedCount);
            return response;
        } catch (Exception e) {
            throw new PlaceOperationException("좋아요 처리에 실패했습니다.");
        }
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/controller")
    public ResponseEntity<?> handleControllerPost(
            @RequestParam("cmd") String cmd,
            @RequestParam(value = "placeId", required = false) String placeId,
            @RequestParam(value = "placeType", required = false) String placeType,
            HttpSession session) {

        if ("addCartAction".equals(cmd)) {
            if (placeId != null && !placeId.isEmpty()) {
                List<PlaceVO> cart = (List<PlaceVO>) session.getAttribute("placeCartList");
                if (cart == null) {
                    cart = new ArrayList<>();
                    session.setAttribute("placeCartList", cart);
                }
                boolean exists = false;
                for (PlaceVO p : cart) {
                    if (p.getPlaceId() != null && p.getPlaceId().toString().equals(placeId)) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    PlaceVO place = placeService.getPlaceById(placeId);
                    if (place != null) {
                        place.setPlaceType(placeType != null ? placeType : "LEISURE");
                        cart.add(place);
                    }
                }
            }
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("Unknown command");
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/removeCartItemsAjax")
    public ResponseEntity<?> removeCartItems(
            @RequestParam("placeIds") String placeIds,
            HttpSession session) {

        List<PlaceVO> cart = (List<PlaceVO>) session.getAttribute("placeCartList");
        if (cart != null && placeIds != null && !placeIds.trim().isEmpty()) {
            String[] ids = placeIds.split(",");
            Set<String> idSet = new HashSet<>(Arrays.asList(ids));
            cart.removeIf(p -> p.getPlaceId() != null && idSet.contains(p.getPlaceId().toString()));
        }
        return ResponseEntity.ok().build();
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/addCartToScheduleAjax")
    public ResponseEntity<Map<String, Object>> addCartToSchedule(
            @RequestParam("placeIds") String placeIdsStr,
            @RequestParam(value = "myScheduleId", required = false) String myScheduleId,
            @AuthenticationPrincipal AppUserDetails userDetails,
            HttpSession session) {

        if (userDetails == null) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }

        String loginUser = userDetails.getUsername();

        if (placeIdsStr == null || placeIdsStr.trim().isEmpty()) {
            throw new InvalidInputException("새로 담은 장소가 없습니다.");
        }

        String[] placeIds = placeIdsStr.split(",");
        List<String> addedPlaceIds = new ArrayList<>();
        int added = 0;

        String targetScheduleId = myScheduleId;
        try {
            if (targetScheduleId == null || targetScheduleId.trim().isEmpty()) {
                targetScheduleId = "SCH_" + UUID.randomUUID().toString().substring(0, 8);
                placeService.insertMySchedule(targetScheduleId, "새 일정", LocalDateTime.now(), loginUser);
            }

            List<VisitItemVO> existingItems = placeService.getVisitItemsByScheduleId(targetScheduleId);
            int nextOrder = existingItems.size() + 1;

            for (String pidStr : placeIds) {
                try {
                    Long placeId = Long.parseLong(pidStr.trim());
                    placeService.insertVisitItem(nextOrder, 0.0, placeId, targetScheduleId, "SCHEDULE");
                    addedPlaceIds.add(pidStr.trim());
                    added++;
                    nextOrder++;
                } catch (NumberFormatException nfe) {
                }
            }

            List<PlaceVO> cart = (List<PlaceVO>) session.getAttribute("placeCartList");
            if (cart != null) {
                Set<String> idSet = new HashSet<>(addedPlaceIds);
                cart.removeIf(p -> p.getPlaceId() != null && idSet.contains(p.getPlaceId().toString()));
            }

            Map<String, Object> result = new HashMap<>();
            result.put("ok", true);
            result.put("added", added);
            result.put("addedPlaceIds", addedPlaceIds);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            throw new PlaceOperationException("일정 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/restaurantListAjax")
    public List<PlaceVO> getRestaurantListAjax(
            @RequestParam(value = "sortOrder", defaultValue = "name") String sortOrder,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "keyword", required = false) String keyword) {
        String sortBy = "popular".equalsIgnoreCase(sortOrder) ? "like" : "title";
        return placeService.searchPlaces("RESTAURANT", category, keyword, sortBy);
    }

    @GetMapping("/stayListAjax")
    public List<PlaceVO> getStayListAjax(
            @RequestParam(value = "sortOrder", defaultValue = "name") String sortOrder,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "keyword", required = false) String keyword) {
        String sortBy = "popular".equalsIgnoreCase(sortOrder) ? "like" : "title";
        return placeService.searchPlaces("STAY", category, keyword, sortBy);
    }

    @GetMapping("/placeDetailAjax")
    public PlaceVO getPlaceDetail(
            @RequestParam("placeId") String placeId) {
        return placeService.getPlaceByPlaceId(placeId);
    }

    @GetMapping("/placeDetailByTitleAjax")
    public PlaceVO getPlaceDetailByTitle(
            @RequestParam("placeType") String placeType,
            @RequestParam("title") String title) {
        return placeService.getPlaceByTitle(placeType, title);
    }

    @GetMapping("/searchNearbyPlacesAjax")
    public List<PlaceVO> searchNearbyPlaces(
            @RequestParam("placeType") String placeType,
            @RequestParam("centerLat") String centerLat,
            @RequestParam("centerLon") String centerLon,
            @RequestParam("radiusKm") Double radiusKm,
            @RequestParam(value = "orderByLike", defaultValue = "false") boolean orderByLike,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "keyword", required = false) String keyword) {
        return placeService.searchNearbyPlaces(placeType, centerLat, centerLon, radiusKm, orderByLike, category,
                keyword);
    }

    @GetMapping("/getPlaceCountAjax")
    public int getPlaceCount(
            @RequestParam("placeType") String placeType) {
        return placeService.getPlaceCount(placeType);
    }

    @GetMapping("/getPlacesAjax")
    public List<PlaceVO> getPlaces() {
        return placeService.getPlaces();
    }
}
