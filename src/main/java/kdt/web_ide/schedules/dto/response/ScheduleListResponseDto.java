package kdt.web_ide.schedules.dto.response;

import java.time.ZonedDateTime;

import kdt.web_ide.schedules.entity.Schedule;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ScheduleListResponseDto {
  private Long scheduleId;
  private String title;
  private ZonedDateTime startAt;

  @Builder
  public ScheduleListResponseDto(Schedule schedule) {
    this.scheduleId = schedule.getId();
    this.title = schedule.getTitle();
    this.startAt = schedule.getStartAt();
  }
}
