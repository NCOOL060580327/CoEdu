package kdt.web_ide.schedules.service;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import kdt.web_ide.boards.entity.Board;
import kdt.web_ide.boards.entity.BoardRepository;
import kdt.web_ide.common.exception.CustomException;
import kdt.web_ide.common.exception.ErrorCode;
import kdt.web_ide.members.entity.Member;
import kdt.web_ide.members.entity.repository.MemberRepository;
import kdt.web_ide.schedules.dto.request.ScheduleSaveRequestDto;
import kdt.web_ide.schedules.dto.request.ScheduleUpdateRequestDto;
import kdt.web_ide.schedules.dto.response.ScheduleListResponseDto;
import kdt.web_ide.schedules.dto.response.ScheduleResponseDto;
import kdt.web_ide.schedules.entity.Schedule;
import kdt.web_ide.schedules.entity.ScheduleMember;
import kdt.web_ide.schedules.entity.repository.ScheduleMemberRepository;
import kdt.web_ide.schedules.entity.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

  private final BoardRepository boardRepository;
  private final ScheduleRepository scheduleRepository;
  private final ScheduleMemberRepository scheduleMemberRepository;
  private final MemberRepository memberRepository;
  private final KakaoCalendarService kakaoCalendarService;

  // 일정 생성
  @Transactional
  public ScheduleResponseDto saveSchedule(
      ScheduleSaveRequestDto requestDto, Long boardId, Member creator) {
    Board board =
        boardRepository
            .findById(boardId)
            .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

    if ((requestDto.getStartAt()).isAfter(requestDto.getEndAt()))
      throw new CustomException(ErrorCode.DATETIME_ERROR);
    // 5분 단위로 떨어지는지 확인
    if (requestDto.getStartAt() != null && requestDto.getStartAt().getMinute() % 5 != 0) {
      throw new CustomException(ErrorCode.INVALID_SCHEDULE_FORMAT);
    }
    if (requestDto.getEndAt() != null && requestDto.getEndAt().getMinute() % 5 != 0) {
      throw new CustomException(ErrorCode.INVALID_SCHEDULE_FORMAT);
    }

    Schedule schedule = requestDto.toEntity(board);
    scheduleRepository.save(schedule);

    if (requestDto.getMembers() == null || requestDto.getMembers().isEmpty()) {
      List<Member> members = new ArrayList<>();
      members.add(creator);
      List<ScheduleMember> scheduleMembers = requestDto.toMemberList(schedule, members);
      scheduleMemberRepository.saveAll(scheduleMembers);
      return new ScheduleResponseDto(schedule, scheduleMembers);
    }

    // 멤버 식별 코드 리스트 추출
    List<String> identificationCodes =
        requestDto.getMembers().stream()
            .map(ScheduleSaveRequestDto.MemberDto::getIdentificationCode)
            .collect(Collectors.toList());

    List<Member> foundMembers = memberRepository.findByIdentificationCodeIn(identificationCodes);
    // 요청된 멤버 중 누락된 멤버 확인
    if (foundMembers.size() != identificationCodes.size()) {
      throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }

    foundMembers.add(creator);

    List<ScheduleMember> scheduleMembers = requestDto.toMemberList(schedule, foundMembers);
    scheduleMemberRepository.saveAll(scheduleMembers);
    return new ScheduleResponseDto(schedule, scheduleMembers);
  }

  // 일정 조회
  @Transactional
  public List<ScheduleListResponseDto> findByBoardIdAndDate(Long boardId, LocalDate date) {
    Board board =
        boardRepository
            .findById(boardId)
            .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));
    List<Schedule> scheduleList = scheduleRepository.findByBoard(board); // 로그용
    if (scheduleList.size() > 0) log.info(scheduleList.get(0).toString()); // 로그용
    List<Schedule> schedules = scheduleRepository.findByStartAtDateAndBoardId(date, boardId);
    return schedules.stream().map(ScheduleListResponseDto::new).collect(Collectors.toList());
  }

  // 일정 상세 조회
  @Transactional
  public ScheduleResponseDto findByScheduleId(Long scheduleId) {
    List<Schedule> scheduleList = scheduleRepository.findAll();
    if (scheduleList.size() > 0) log.info(scheduleList.get(0).toString());
    Schedule schedule =
        scheduleRepository
            .findById(scheduleId)
            .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));
    List<ScheduleMember> scheduleMembers = scheduleMemberRepository.findBySchedule(schedule);
    return new ScheduleResponseDto(schedule, scheduleMembers);
  }

  // 일정 삭제
  @Transactional
  public void deleteById(Long scheduleId, Member member) {
    Schedule schedule =
        scheduleRepository
            .findById(scheduleId)
            .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));
    // 일정에 참여한 멤버인지 확인
    ScheduleMember currentScheduleMember =
        scheduleMemberRepository.findByScheduleAndMember(schedule, member);
    if (currentScheduleMember == null) {
      throw new CustomException(ErrorCode.SCHEDULE_ACCESS_ERROR);
    }
    scheduleMemberRepository.deleteBySchedule(schedule);
    if (currentScheduleMember.getEventId() != null)
      kakaoCalendarService.deleteSchedule(member, currentScheduleMember.getEventId());
    scheduleRepository.deleteById(scheduleId);
  }

  // 일정 수정
  @Transactional
  public ScheduleResponseDto updateSchedule(
      ScheduleUpdateRequestDto requestDto, Long scheduleId, Member currentMember) {

    Schedule schedule =
        scheduleRepository
            .findById(scheduleId)
            .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

    // 일정 저장
    requestDto.applyChangesToSchedule(schedule);
    scheduleRepository.save(schedule);

    // 일정에 참여한 멤버인지 확인
    ScheduleMember currentScheduleMember =
        scheduleMemberRepository.findByScheduleAndMember(schedule, currentMember);
    if (currentScheduleMember == null) {
      throw new CustomException(ErrorCode.SCHEDULE_ACCESS_ERROR);
    }

    if (currentScheduleMember.getEventId() != null) {
      kakaoCalendarService.updateSchedule(
          currentMember, schedule, currentScheduleMember.getEventId());
    }

    if (requestDto.getMembers() == null || requestDto.getMembers().isEmpty()) {
      // 추가 멤버가 없다면, 기존 멤버 리스트 조회
      List<ScheduleMember> scheduleMembers = scheduleMemberRepository.findBySchedule(schedule);
      return new ScheduleResponseDto(schedule, scheduleMembers);
    }

    List<ScheduleMember> existingMembers = scheduleMemberRepository.findBySchedule(schedule);
    Set<Long> existingMemberIds =
        existingMembers.stream()
            .map(sm -> sm.getMember().getMemberId())
            .collect(Collectors.toSet());

    // 새로 요청된 멤버 리스트 조회
    List<String> identificationCodes =
        requestDto.getMembers().stream()
            .map(ScheduleUpdateRequestDto.MemberDto::getIdentificationCode)
            .collect(Collectors.toList());

    List<Member> newMembers = memberRepository.findByIdentificationCodeIn(identificationCodes);
    List<Member> membersToAdd = new ArrayList<>();
    for (Member newMember : newMembers) {
      if (existingMemberIds.contains(newMember.getMemberId())) {
        log.info(newMember.getIdentificationCode() + "는 기존 멤버입니다.");
        throw new CustomException(
            ErrorCode.MEMBER_ALREADY_IN_SCHEDULE, newMember.getIdentificationCode());
      } else {
        log.info(newMember.getIdentificationCode() + "을(를) 초대합니다.");
        membersToAdd.add(newMember);
      }
    }

    // 멤버 추가
    List<ScheduleMember> newScheduleMembers =
        membersToAdd.stream()
            .map(member -> ScheduleMember.builder().schedule(schedule).member(member).build())
            .toList();

    scheduleMemberRepository.saveAll(newScheduleMembers);
    return new ScheduleResponseDto(schedule, newScheduleMembers);
  }

  // 멤버 ID로 삭제
  @Transactional
  public void removeMemberFromSchedule(Long scheduleId, Long memberId, Member currentMember) {
    Schedule schedule =
        scheduleRepository
            .findById(scheduleId)
            .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));

    // 일정에 참여한 멤버인지 확인
    ScheduleMember currentScheduleMember =
        scheduleMemberRepository.findByScheduleAndMember(schedule, currentMember);
    if (currentScheduleMember == null) {
      throw new CustomException(ErrorCode.SCHEDULE_ACCESS_ERROR);
    }

    Member member =
        memberRepository
            .findById(memberId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    ScheduleMember scheduleMember =
        scheduleMemberRepository.findByScheduleAndMember(schedule, member);

    if (scheduleMember == null) {
      throw new CustomException(ErrorCode.MEMBER_NOT_FOUND);
    }

    scheduleMemberRepository.delete(scheduleMember);
    log.info("멤버 ID: " + memberId + "가 일정에서 삭제되었습니다.");

    // 카카오톡 캘린더에 일정 추가
    if (currentScheduleMember.getEventId() != null) {
      kakaoCalendarService.deleteSchedule(member, currentScheduleMember.getEventId());
    }
  }

  // 카카오톡 일정 추가
  @Transactional
  public void registerKakaoSchedule(Long scheduleId, Member member) {
    Schedule schedule =
        scheduleRepository
            .findById(scheduleId)
            .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));
    String eventId = kakaoCalendarService.registerSchedule(member, schedule);
    ScheduleMember scheduleMember =
        scheduleMemberRepository.findByScheduleAndMember(schedule, member);
    scheduleMember.setEventId(eventId);
    scheduleMemberRepository.save(scheduleMember);
  }
}
