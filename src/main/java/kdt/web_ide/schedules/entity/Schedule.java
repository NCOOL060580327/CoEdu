package kdt.web_ide.schedules.entity;

import java.time.ZonedDateTime;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonFormat;

import kdt.web_ide.boards.entity.Board;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "schedules")
public class Schedule {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "schedule_id")
  private Long id;

  private String title;

  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
      timezone = "Asia/Seoul")
  @Column(name = "start_at")
  private ZonedDateTime startAt;

  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
      timezone = "Asia/Seoul")
  @Column(name = "end_at")
  private ZonedDateTime endAt;

  @Column(name = "location_name")
  private String locationName;

  @Column(name = "location_id")
  private String locationId;

  private String address;
  private double latitude;
  private double longitude;

  @ManyToOne
  @JoinColumn(name = "board_id")
  private Board board;

  public void update(
      String title,
      ZonedDateTime startAt,
      ZonedDateTime endAt,
      String locationName,
      String locationId,
      String address,
      Double latitude,
      Double longitude) {
    this.title = title;
    this.startAt = startAt;
    this.endAt = endAt;
    this.locationName = locationName;
    this.locationId = locationId;
    this.address = address;
    this.latitude = latitude;
    this.longitude = longitude;
  }
}
