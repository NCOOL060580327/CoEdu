package kdt.web_ide.schedules.dto.response;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import kdt.web_ide.members.entity.Member;
import kdt.web_ide.schedules.entity.Schedule;
import kdt.web_ide.schedules.entity.ScheduleMember;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ScheduleResponseDto {
  private Long id;
  private String title;
  private String locationId;
  private String address;
  private double latitude;
  private double longitude;
  private ZonedDateTime startAt;
  private ZonedDateTime endAt;
  private String locationName;
  private List<MemberDto> members;

  public ScheduleResponseDto(Schedule schedule, List<ScheduleMember> scheduleMembers) {
    this.id = schedule.getId();
    this.title = schedule.getTitle();
    this.locationId = schedule.getLocationId();
    this.address = schedule.getAddress();
    this.latitude = schedule.getLatitude();
    this.longitude = schedule.getLongitude();
    this.startAt = schedule.getStartAt();
    this.endAt = schedule.getEndAt();
    this.locationName = schedule.getLocationName();
    this.members =
        scheduleMembers.stream()
            .map(scheduleMember -> new MemberDto(scheduleMember.getMember()))
            .collect(Collectors.toList());
  }

  @Getter
  @NoArgsConstructor
  public static class MemberDto {
    private Long memberId;
    private String nickname;

    public MemberDto(Member member) {
      this.memberId = member.getMemberId();
      this.nickname = member.getNickName();
    }
  }
}
