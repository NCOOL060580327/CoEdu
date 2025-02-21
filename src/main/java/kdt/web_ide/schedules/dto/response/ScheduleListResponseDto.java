package kdt.web_ide.schedules.dto.response;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import kdt.web_ide.schedules.entity.Schedule;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ScheduleListResponseDto {
  private Long scheduleId;
  private String title;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
  private ZonedDateTime startAt;

  @Builder
  public ScheduleListResponseDto(Schedule schedule) {
    this.scheduleId = schedule.getId();
    this.title = schedule.getTitle();
    this.startAt = schedule.getStartAt();
  }
}
