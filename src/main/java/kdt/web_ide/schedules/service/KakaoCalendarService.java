package kdt.web_ide.schedules.service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import jakarta.transaction.Transactional;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import kdt.web_ide.members.dto.response.TokenResponse;
import kdt.web_ide.members.entity.Member;
import kdt.web_ide.members.service.MemberService;
import kdt.web_ide.schedules.entity.Schedule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class KakaoCalendarService {
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  private final MemberService memberService;
  private static final String KAKAO_CALENDAR_POST_URL =
      "https://kapi.kakao.com/v2/api/calendar/create/event";
  private static final String KAKAO_CALENDAR_UPDATE_URL =
      "https://kapi.kakao.com/v2/api/calendar/update/event/host";
  private static final String KAKAO_CALENDAR_DELETE_URL =
      "https://kapi.kakao.com/v2/api/calendar/delete/event";

  public String getAccessToken(Member member) {
    TokenResponse kakaoToken = memberService.getKakaoAccessToken(member.getMemberId());
    return kakaoToken.getAccessToken();
  }

  // 일정 생성
  @Transactional
  public String registerSchedule(Member member, Schedule schedule) {
    try {
      String accessToken = getAccessToken(member);

      // 요청 헤더 설정
      HttpHeaders headers = new HttpHeaders();
      headers.set("Authorization", "Bearer " + accessToken);
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

      MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
      requestBody.add("calendar_id", "primary");

      Map<String, Object> event = new HashMap<>();
      event.put("title", schedule.getTitle());

      // time 설정
      Map<String, Object> time = new HashMap<>();
      time.put("start_at", convertToSeoul(schedule.getStartAt()));
      time.put("end_at", convertToSeoul(schedule.getEndAt()));
      time.put("all_day", false);
      time.put("lunar", false);
      event.put("time", time);

      // location 설정
      Map<String, Object> location = new HashMap<>();
      location.put("name", schedule.getLocationName());
      location.put("location_id", schedule.getLocationId());
      location.put("address", schedule.getAddress());
      location.put("latitude", schedule.getLatitude());
      location.put("longitude", schedule.getLongitude());
      event.put("location", location);
      event.put("color", "RED");

      String eventJson = objectMapper.writeValueAsString(event);
      requestBody.add("event", eventJson);

      log.info("Request Body: " + requestBody);

      // HttpEntity 생성
      HttpEntity<MultiValueMap<String, String>> requestEntity =
          new HttpEntity<>(requestBody, headers);

      // API 호출
      ResponseEntity<String> response =
          restTemplate.exchange(
              KAKAO_CALENDAR_POST_URL, HttpMethod.POST, requestEntity, String.class);

      log.info("Response: " + response.getBody());

      // 응답 처리
      Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
      return (String) responseMap.get("event_id");

    } catch (Exception e) {
      throw new RuntimeException("일정 등록 실패: " + e.getMessage());
    }
  }

  // UTC로 변환하는 메소드
  private String convertToSeoul(ZonedDateTime zonedDateTime) {
    try {
      ZonedDateTime seoulTimeMinus9 = zonedDateTime.minusHours(9);
      return seoulTimeMinus9.format(DateTimeFormatter.ISO_INSTANT);
    } catch (Exception e) {
      log.error("시간 변환 오류: ", e);
      throw new RuntimeException("시간 변환 실패: " + e.getMessage());
    }
  }

  @Transactional
  public void updateSchedule(Member member, Schedule schedule, String eventId) {
    try {
      String accessToken = getAccessToken(member);
      // 요청 헤더 설정
      HttpHeaders headers = new HttpHeaders();
      headers.set("Authorization", "Bearer " + accessToken);
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

      MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();

      requestBody.add("event_id", eventId);
      requestBody.add("calendar_id", "primary");

      Map<String, Object> event = new HashMap<>();
      event.put("title", schedule.getTitle());

      Map<String, Object> time = new HashMap<>();
      time.put("start_at", convertToSeoul(schedule.getStartAt()));
      time.put("end_at", convertToSeoul(schedule.getEndAt()));
      time.put("all_day", false);
      time.put("lunar", false);

      event.put("time", time);

      Map<String, Object> location = new HashMap<>();
      location.put("name", schedule.getLocationName());
      location.put("location_id", schedule.getLocationId());
      location.put("address", schedule.getAddress());
      location.put("latitude", schedule.getLatitude());
      location.put("longitude", schedule.getLongitude());

      event.put("location", location);
      String eventJson = objectMapper.writeValueAsString(event);
      requestBody.add("event", eventJson);

      HttpEntity<MultiValueMap<String, String>> requestEntity =
          new HttpEntity<>(requestBody, headers);

      // API 호출
      ResponseEntity<String> response =
          restTemplate.exchange(
              KAKAO_CALENDAR_UPDATE_URL, HttpMethod.POST, requestEntity, String.class);

      log.info("Update Response: " + response.getBody());
    } catch (Exception e) {
      throw new RuntimeException("일정 수정 실패: " + e.getMessage());
    }
  }

  @Transactional
  public void deleteSchedule(Member member, String eventId) {
    try {
      String accessToken = getAccessToken(member);

      // 요청 헤더 설정
      HttpHeaders headers = new HttpHeaders();
      headers.set("Authorization", "Bearer " + accessToken);

      // DELETE 요청에서 URL 쿼리 파라미터로 event_id 전달
      String url = KAKAO_CALENDAR_DELETE_URL + "?event_id=" + eventId;

      // 요청 보내기
      HttpEntity<String> requestEntity = new HttpEntity<>(headers);
      ResponseEntity<String> response =
          restTemplate.exchange(url, HttpMethod.DELETE, requestEntity, String.class);

      log.info("Delete Response: " + response.getBody());
    } catch (Exception e) {
      throw new RuntimeException("일정 삭제 실패: " + e.getMessage());
    }
  }
}
