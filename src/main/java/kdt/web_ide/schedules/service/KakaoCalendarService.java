package kdt.web_ide.schedules.service;

import java.util.HashMap;
import java.util.Map;

import jakarta.transaction.Transactional;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import kdt.web_ide.members.entity.Member;
import kdt.web_ide.members.oAuth.OAuthApiClient;
import kdt.web_ide.schedules.entity.Schedule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class KakaoCalendarService {
  private final RestTemplate restTemplate;
  private final ObjectMapper objectMapper;

  OAuthApiClient oAuthApiClient;
  private static final String KAKAO_CALENDAR_POST_URL =
      "https://kapi.kakao.com/v2/api/calendar/create/event";
  private static final String KAKAO_CALENDAR_UPDATE_URL =
      "https://kapi.kakao.com/v2/api/calendar/update/event/host";
  private static final String KAKAO_CALENDAR_DELETE_URL =
      "https://kapi.kakao.com/v2/api/calendar/delete/event";

  @Transactional
  public String registerSchedule(Member member, Schedule schedule) {
    try {
      String accessToken = member.getKakaoRefreshToken();
      // 요청 헤더 설정
      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(accessToken);
      headers.setContentType(MediaType.APPLICATION_JSON);

      Map<String, Object> requestBody = new HashMap<>();

      Map<String, Object> event = new HashMap<>();
      event.put("title", schedule.getTitle());

      Map<String, Object> time = new HashMap<>();
      time.put("start_at", schedule.getStartAt());
      time.put("end_at", schedule.getEndAt());
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
      requestBody.put("calendar_id", "primary");
      requestBody.put("event", event);

      String jsonRequestBody = objectMapper.writeValueAsString(requestBody);

      HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);

      // API 호출
      ResponseEntity<String> response =
          restTemplate.exchange(
              KAKAO_CALENDAR_POST_URL, HttpMethod.POST, requestEntity, String.class);

      log.info("Response: " + response.getBody());
      Map<String, Object> responseMap = objectMapper.readValue(response.getBody(), Map.class);
      return (String) responseMap.get("event_id");

    } catch (Exception e) {
      throw new RuntimeException("일정 등록 실패: " + e.getMessage());
    }
  }

  @Transactional
  public void updateSchedule(Member member, Schedule schedule, String eventId) {
    try {
      String accessToken = member.getKakaoRefreshToken();

      // 요청 헤더 설정
      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(accessToken);
      headers.setContentType(MediaType.APPLICATION_JSON);

      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("event_id", eventId);
      requestBody.put("calendar_id", "primary");
      requestBody.put("recur_update_type", "ALL");

      Map<String, Object> event = new HashMap<>();
      event.put("title", schedule.getTitle());

      Map<String, Object> time = new HashMap<>();
      time.put("start_at", schedule.getStartAt());
      time.put("end_at", schedule.getEndAt());
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
      requestBody.put("event", event);

      String jsonRequestBody = objectMapper.writeValueAsString(requestBody);
      HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);

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
      String accessToken = member.getRefreshToken();

      // 요청 헤더 설정
      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(accessToken);
      headers.setContentType(MediaType.APPLICATION_JSON);

      Map<String, Object> requestBody = new HashMap<>();
      requestBody.put("event_id", eventId);
      requestBody.put("calendar_id", "primary");

      String jsonRequestBody = objectMapper.writeValueAsString(requestBody);
      HttpEntity<String> requestEntity = new HttpEntity<>(jsonRequestBody, headers);

      // API 호출
      ResponseEntity<String> response =
          restTemplate.exchange(
              KAKAO_CALENDAR_DELETE_URL, HttpMethod.POST, requestEntity, String.class);

      log.info("Delete Response: " + response.getBody());
    } catch (Exception e) {
      throw new RuntimeException("일정 삭제 실패: " + e.getMessage());
    }
  }
}
