package kdt.web_ide.post.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kdt.web_ide.members.service.CustomUserDetails;
import kdt.web_ide.post.dto.PostRequestDto;
import kdt.web_ide.post.dto.PostResponseDto;
import kdt.web_ide.post.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Tag(name = "게시글", description = "게시글 API")
public class PostController {

  private final PostService postService;
  private final SimpMessagingTemplate simpMessagingTemplate;

  @PostMapping
  @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성하고 빈 파일을 S3에 생성합니다.")
  public ResponseEntity<PostResponseDto> createPost(
      @RequestBody PostRequestDto requestDto, @AuthenticationPrincipal UserDetails userDetails) {
    PostResponseDto response = postService.createPost(requestDto);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{id}")
  @Operation(summary = "게시글 조회", description = "특정 ID의 게시글을 조회합니다.")
  public ResponseEntity<PostResponseDto> getPost(
      @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
    PostResponseDto response = postService.getPostById(id);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  @Operation(summary = "전체 게시글 조회", description = "모든 게시글을 조회합니다.")
  public ResponseEntity<List<PostResponseDto>> getAllPosts(
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    List<PostResponseDto> response = postService.getAllPosts();
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}/download")
  @Operation(summary = "게시글 파일 다운로드", description = "특정 게시글의 파일을 다운로드합니다.")
  public ResponseEntity<String> downloadPostFile(
      @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
    String fileContent = postService.downloadFile(id);
    return ResponseEntity.ok(fileContent);
  }

  @CrossOrigin
  @PostMapping("/{id}/execute")
  @Operation(summary = "게시글 파일 실행", description = "특정 게시글의 파일을 실행합니다.")
  public ResponseEntity<String> executePostFile(
      @PathVariable Long id,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody(required = false) String input)
      throws IOException, InterruptedException {
    String output = postService.executeFile(id, input);
    return ResponseEntity.ok(output);
  }

  @PutMapping("/{id}/modify")
  @Operation(summary = "게시글 파일 수정", description = "특정 게시글의 파일을 수정합니다.")
  public ResponseEntity<String> modifyPostFile(
      @PathVariable Long id,
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @RequestBody String newContent) {
    postService.modifyFileContent(id, newContent);
    return ResponseEntity.ok("File modified successfully.");
  }

  @PutMapping("/{id}/update")
  @Operation(summary = "게시글 수정", description = "특정 게시글을 수정합니다.")
  public ResponseEntity<String> updatePost(
      @PathVariable Long id,
      @RequestBody PostRequestDto requestDto,
      @AuthenticationPrincipal CustomUserDetails userDetails) {
    postService.modifyPostNameAndLanguage(id, requestDto.getName(), requestDto.getLanguage());
    return ResponseEntity.ok("Post updated successfully.");
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "게시글 삭제", description = "특정 게시글을 삭제합니다.")
  public ResponseEntity<String> deletePost(
      @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
    postService.deletePost(id);
    return ResponseEntity.ok("Post deleted successfully.");
  }

  @GetMapping("/{id}/content")
  @Operation(summary = "게시글 파일 내용 조회", description = "특정 게시글의 파일 내용을 조회합니다.")
  public ResponseEntity<String> getFileContent(
      @PathVariable Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
    String content = postService.getFileContent(id);
    return ResponseEntity.ok(content);
  }

  @MessageMapping("/posts/edit/{id}")
  public void editPostContent(@DestinationVariable("id") Long id, @Payload String newContent) {
    postService.parsingAndModifyPostContent(id, newContent);
    // postService.modifyFileContent(id, newContent);
    simpMessagingTemplate.convertAndSend("/ide/edit/" + id, newContent);
  }

  @MessageMapping("/posts/{id}/run")
  @SendTo("/topic/posts/{id}/output")
  public String runPostContent(@PathVariable Long id, String input)
      throws IOException, InterruptedException {
    return postService.executeFile(id, input);
  }

  @GetMapping("/{boardId}")
  @Operation(summary = "특정 게시판의 게시글 조회", description = "특정 게시판에 속한 모든 게시글을 조회합니다.")
  public ResponseEntity<List<PostResponseDto>> getPostsByBoardId(
      @PathVariable Long boardId, @AuthenticationPrincipal CustomUserDetails userDetails) {
    List<PostResponseDto> response = postService.getPostsByBoardId(boardId);
    return ResponseEntity.ok(response);
  }
}
