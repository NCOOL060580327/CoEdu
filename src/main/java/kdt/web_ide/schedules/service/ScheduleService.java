package kdt.web_ide.schedules.service;

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

@Service
@RequiredArgsConstructor
public class ScheduleService {

  private final BoardRepository boardRepository;
  private final ScheduleRepository scheduleRepository;
  private final ScheduleMemberRepository scheduleMemberRepository;
  private final MemberRepository memberRepository;
  private final KakaoCalendarService kakaoCalendarService;

  // 일정 생성
  @Transactional
  public void saveSchedule(ScheduleSaveRequestDto requestDto, Long boardId, Member creator) {
    Board board =
        boardRepository
            .findById(boardId)
            .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

    Schedule schedule = requestDto.toEntity(board);
    scheduleRepository.save(schedule);

    // 멤버 식별 코드 리스트 추출
    List<String> identificationCodes =
        requestDto.getMembers().stream()
            .map(ScheduleSaveRequestDto.MemberDto::getIdentificationCode)
            .collect(Collectors.toList());

    // 한 번의 쿼리로 멤버 조회
    List<Member> foundMembers = memberRepository.findByIdentificationCodeIn(identificationCodes);

    // 요청된 멤버 중 누락된 멤버 확인
    if (foundMembers.size() != identificationCodes.size()) {
      throw new CustomException(ErrorCode.USER_NOT_FOUND);
    }

    // 생성자 추가
    foundMembers.add(creator);

    // ScheduleMember 리스트 생성 후 저장
    List<ScheduleMember> scheduleMembers = requestDto.toMemberList(schedule, foundMembers);
    scheduleMemberRepository.saveAll(scheduleMembers);
  }

  // 일정 조회
  public List<ScheduleListResponseDto> findByBoardId(Long boardId, Member member) {
    Board board =
        boardRepository
            .findById(boardId)
            .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));
    List<Schedule> schedules = scheduleRepository.findByBoard(board);

    return schedules.stream()
        .map(
            schedule -> {
              return new ScheduleListResponseDto(schedule);
            })
        .collect(Collectors.toList());
  }

  // 일정 상세 조회
  public ScheduleResponseDto findByScheduleId(Long scheduleId) {
    Schedule schedule =
        scheduleRepository
            .findById(scheduleId)
            .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));
    List<ScheduleMember> scheduleMembers = scheduleMemberRepository.findBySchedule(schedule);
    return new ScheduleResponseDto(schedule, scheduleMembers);
  }

  // 일정 삭제
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
    ScheduleMember thisMember = scheduleMemberRepository.findByScheduleAndMember(schedule, member);
    String eventId = thisMember.getEventId();
    if (eventId != null) kakaoCalendarService.deleteSchedule(member, eventId);
    scheduleRepository.deleteById(scheduleId);
  }

  // 일정 수정
  public ScheduleResponseDto updateSchedule(
      ScheduleUpdateRequestDto requestDto, Long scheduleId, Member currentMember) {
    Schedule schedule =
        scheduleRepository
            .findById(scheduleId)
            .orElseThrow(() -> new CustomException(ErrorCode.SCHEDULE_NOT_FOUND));
    requestDto.applyChangesToSchedule(schedule);
    // 일정에 참여한 멤버인지 확인
    ScheduleMember currentScheduleMember =
        scheduleMemberRepository.findByScheduleAndMember(schedule, currentMember);
    if (currentScheduleMember == null) {
      throw new CustomException(ErrorCode.SCHEDULE_ACCESS_ERROR);
    }

    List<ScheduleMember> existingMembers = scheduleMemberRepository.findBySchedule(schedule);
    Set<Long> existingMemberIds =
        existingMembers.stream()
            .map(sm -> sm.getMember().getMemberId())
            .collect(Collectors.toSet());

    // 새로 요청된 멤버 리스트 조회
    List<String> identificationCodes = requestDto.getIdentificationCodes();
    List<Member> newMembers = memberRepository.findByIdentificationCodeIn(identificationCodes);
    Set<Long> newMemberIds =
        newMembers.stream().map(Member::getMemberId).collect(Collectors.toSet());

    // 추가해야 할 멤버 찾기
    List<Member> membersToAdd =
        newMembers.stream()
            .filter(member -> !existingMemberIds.contains(member.getMemberId()))
            .toList();

    // 삭제해야 할 멤버 찾기
    List<ScheduleMember> membersToRemove =
        existingMembers.stream()
            .filter(sm -> !newMemberIds.contains(sm.getMember().getMemberId()))
            .toList();

    // 멤버 추가
    List<ScheduleMember> newScheduleMembers =
        membersToAdd.stream()
            .map(member -> ScheduleMember.builder().schedule(schedule).member(member).build())
            .toList();
    scheduleMemberRepository.saveAll(newScheduleMembers);
    // 멤버 삭제
    scheduleMemberRepository.deleteAll(membersToRemove);

    // 카카오톡 일정 추가
    ScheduleMember thisMember =
        scheduleMemberRepository.findByScheduleAndMember(schedule, currentMember);
    String eventId = thisMember.getEventId();
    if (eventId != null) kakaoCalendarService.updateSchedule(currentMember, schedule, eventId);
    return new ScheduleResponseDto(schedule, newScheduleMembers);
  }

  // 카카오톡 일정 추가
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
