package kdt.web_ide.boards.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kdt.web_ide.boards.dto.request.BoardSaveRequesetDto;
import kdt.web_ide.boards.dto.request.BoardUpdateRequestDto;
import kdt.web_ide.boards.dto.response.BoardResponseDto;
import kdt.web_ide.boards.dto.response.BoardUserResponseDto;
import kdt.web_ide.boards.service.BoardService;
import kdt.web_ide.members.service.CustomUserDetails;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "게시판 API")
@RequestMapping("/api/boards")
public class BoardController {

  private final BoardService boardService;

  @Operation(summary = "게시판 생성 API", description = "게시판 제목 입력은 필수입니다.")
  @PostMapping("")
  public ResponseEntity<?> saveBoard(
      @RequestBody BoardSaveRequesetDto requestDto,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    BoardResponseDto boardResponse = boardService.saveBoard(requestDto, userDetails.getMember());
    return ResponseEntity.status(HttpStatus.OK).body(boardResponse);
  }

  // 게시판 수정
  @Operation(summary = "게시판 수정 API", description = "게시판 제목을 수정합니다. 리더만 수정 가능합니다.")
  @PutMapping("/{boardId}")
  public ResponseEntity<BoardResponseDto> updateBoard(
      @PathVariable Long boardId,
      @RequestBody @Valid BoardUpdateRequestDto requestDto,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    BoardResponseDto updatedBoard =
        boardService.updateBoard(requestDto, boardId, userDetails.getMember());
    return ResponseEntity.status(HttpStatus.OK).body(updatedBoard);
  }

  // 게시판 삭제
  @Operation(summary = "게시판 삭제 API", description = "게시판을 삭제합니다. 리더만 삭제 가능합니다.")
  @DeleteMapping("/{boardId}")
  public ResponseEntity<String> deleteBoard(
      @PathVariable Long boardId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    boardService.deleteBoard(boardId, userDetails.getMember());
    return ResponseEntity.status(HttpStatus.OK).body("게시판이 삭제되었습니다.");
  }

  //  // 게시판 인원 초대
  //  @Operation(summary = "게시판 멤버 초대 API", description = "이메일로 멤버를 초대합니다. 리더만 초대 가능합니다.")
  //  @PostMapping("/{boardId}/invite")
  //  public ResponseEntity<BoardUserResponseDto> inviteMember(
  //      @PathVariable Long boardId,
  //      @RequestBody @Valid BoardUserInviteRequestDto requestDto,
  //      @AuthenticationPrincipal CustomUserDetails userDetails) {
  //    BoardUserResponseDto invitedUser =
  //        boardService.inviteMember(requestDto, boardId, userDetails.getMember());
  //    return ResponseEntity.ok(invitedUser);
  //  }

  // 게시판 인원 조회
  @Operation(summary = "게시판 인원 조회 API", description = "해당 게시판의 모든 멤버를 조회합니다.")
  @GetMapping("/{boardId}/members")
  public ResponseEntity<List<BoardUserResponseDto>> findBoardUsers(
      @PathVariable Long boardId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    List<BoardUserResponseDto> members =
        boardService.findBoardUsersById(boardId, userDetails.getMember());
    return ResponseEntity.ok(members);
  }

  // 게시판 멤버 삭제
  @Operation(summary = "게시판 멤버 삭제 API", description = "게시판에서 특정 멤버를 삭제합니다. 리더만 삭제 가능합니다.")
  @DeleteMapping("/{boardId}/members/{memberId}")
  public ResponseEntity<String> removeMember(
      @PathVariable Long boardId,
      @PathVariable Long memberId,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    boardService.removeMember(boardId, memberId, userDetails.getMember());
    return ResponseEntity.status(HttpStatus.OK).body("해당 멤버가 삭제되었습니다.");
  }

  // 게시판 상세 조회
  @Operation(summary = "게시판 상세 조회 API", description = "해당 게시판의 정보를 조회합니다.")
  @GetMapping("/{boardId}")
  public ResponseEntity<BoardResponseDto> getBoardDetails(@PathVariable Long boardId) {
    BoardResponseDto boardDetails = boardService.getBoardDetails(boardId);
    return ResponseEntity.ok(boardDetails);
  }

  // 내 게시판 목록 조회
  @Operation(summary = "사용자가 속한 게시판 목록 조회 API", description = "사용자가 속한 게시판 목록을 조회합니다.")
  @GetMapping("/my")
  public ResponseEntity<List<BoardResponseDto>> getMyBoards(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    List<BoardResponseDto> myBoards = boardService.getMyBoards(userDetails.getMember());
    return ResponseEntity.ok(myBoards);
  }
}
