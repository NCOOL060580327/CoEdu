package kdt.web_ide.post.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import kdt.web_ide.boards.entity.Board;
import kdt.web_ide.boards.entity.BoardRepository;
import kdt.web_ide.chat.entity.ChatRoom;
import kdt.web_ide.chat.entity.ChatRoomMember;
import kdt.web_ide.chat.entity.repository.ChatRoomMemberRepository;
import kdt.web_ide.chat.entity.repository.ChatRoomRepository;
import kdt.web_ide.common.exception.CustomException;
import kdt.web_ide.common.exception.ErrorCode;
import kdt.web_ide.file.service.S3Service;
import kdt.web_ide.members.entity.Member;
import kdt.web_ide.members.entity.repository.MemberRepository;
import kdt.web_ide.post.dto.PostRequestDto;
import kdt.web_ide.post.dto.PostResponseDto;
import kdt.web_ide.post.entity.Language;
import kdt.web_ide.post.entity.Post;
import kdt.web_ide.post.entity.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

  private final PostRepository postRepository;
  private final BoardRepository boardRepository;
  private final S3Service s3Service;
  private final MemberRepository memberRepository;
  private final ChatRoomRepository chatRoomRepository;
  private final ChatRoomMemberRepository chatRoomMemberRepository;
  private final ObjectMapper objectMapper;

  public PostResponseDto createPost(PostRequestDto requestDto) {
    Board board =
        boardRepository
            .findById(requestDto.getBoardId())
            .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

    String fileName =
        generateFileName(requestDto.getName(), String.valueOf(requestDto.getLanguage()));
    String filePath = uploadEmptyFileToS3(fileName, board.getId());

    Post post =
        Post.builder()
            .board(board)
            .name(requestDto.getName())
            .language(requestDto.getLanguage())
            .filePath(filePath)
            .createdAt(LocalDateTime.now())
            .build();

    Post savedPost = postRepository.save(post);

    ChatRoom chatRoom = ChatRoom.builder().post(post).build();

    chatRoomRepository.save(chatRoom);

    List<Member> boardMemberList = memberRepository.findMemberListByBoardId(board.getId());

    List<ChatRoomMember> chatRoomMemberList =
        boardMemberList.stream()
            .map(
                member ->
                    ChatRoomMember.builder()
                        .chatRoom(chatRoom)
                        .member(member)
                        .notReadCount(0)
                        .build())
            .toList();

    chatRoomMemberRepository.saveAll(chatRoomMemberList);

    return mapToResponseDto(savedPost, chatRoom.getChatRoomId().intValue());
  }

  public PostResponseDto getPostById(Long id) {
    Post post =
        postRepository
            .findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

    ChatRoom chatRoom =
        chatRoomRepository
            .findChatRoomByPost_Id(id)
            .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));

    return mapToResponseDto(post, chatRoom.getChatRoomId().intValue());
  }

  public List<PostResponseDto> getAllPosts() {
    return postRepository.findAll().stream()
        .map(
            post -> {
              ChatRoom chatRoom =
                  chatRoomRepository
                      .findChatRoomByPost_Id(post.getId().longValue())
                      .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));
              return mapToResponseDto(post, chatRoom.getChatRoomId().intValue());
            })
        .collect(Collectors.toList());
  }

  private String uploadEmptyFileToS3(String fileName, Long boardId) {
    try {
      String s3Path = "boards/" + boardId + "/" + fileName;
      ByteArrayInputStream emptyContent =
          new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
      s3Service.uploadFile(emptyContent, s3Path, "text/plain");
      return s3Path;
    } catch (Exception e) {
      throw new CustomException(ErrorCode.S3_UPLOAD_ERROR);
    }
  }

  private String generateFileName(String postName, String language) {
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    String extension = getFileExtension(language);
    return postName.replaceAll("\\s+", "_") + "_" + timestamp + "_" + UUID.randomUUID() + extension;
  }

  private String getFileExtension(String language) {
    return switch (language.toUpperCase()) {
      case "PYTHON" -> ".py";
      case "JAVA" -> ".java";
      case "JAVASCRIPT" -> ".js";
      case "C++" -> ".cpp";
      default -> ".txt";
    };
  }

  private PostResponseDto mapToResponseDto(Post post, Integer roomId) {
    return PostResponseDto.builder()
        .id(post.getId())
        .boardId(post.getBoard().getId().intValue())
        .name(post.getName())
        .language(post.getLanguage())
        .filePath(post.getFilePath())
        .createdAt(post.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
        .roomId(roomId)
        .build();
  }

  public String downloadFile(Long postId) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    return s3Service.getFileUrl(post.getFilePath());
  }

  public String executeFile(Long postId, String input) throws IOException, InterruptedException {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    return s3Service.executeFile(post.getFilePath(), input);
  }

  public void modifyFileContent(Long postId, String newContent) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    s3Service.modifyFileContent(post.getFilePath(), newContent);
  }

  public void modifyPostNameAndLanguage(Long postId, String name, Language language) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    post.setName(name);
    post.setLanguage(language);
    postRepository.save(post);
  }

  public String getFileContent(Long postId) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    return s3Service.getFileContent(post.getFilePath());
  }

  public void parsingAndModifyPostContent(Long id, String payload) {
    try {
      // JSON 파싱
      JsonNode jsonNode = objectMapper.readTree(payload);
      String newContent = jsonNode.get("newContent").asText();

      log.info("newContent: {}", newContent);

      // Post 존재 여부 확인
      Post post =
          postRepository
              .findById(id)
              .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

      // 파일 내용 수정
      modifyFileContent(Long.valueOf(post.getId()), newContent);

    } catch (IOException e) {
      throw new CustomException(ErrorCode.JSON_PROCESSING_ERROR);
    }
  }

  public void deletePost(Long postId) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

    // 2. 해당 게시글과 연결된 채팅방 조회 및 삭제
    chatRoomRepository
        .findChatRoomByPost_Id(postId)
        .ifPresent(
            chatRoom -> {
              chatRoomMemberRepository.deleteAllByChatRoom_ChatRoomId(chatRoom.getChatRoomId());
              chatRoomRepository.delete(chatRoom);
            });

    s3Service.deleteFile(post.getFilePath());

    postRepository.delete(post);
  }

  public List<PostResponseDto> getPostsByBoardId(Long boardId) {
    Board board =
        boardRepository
            .findById(boardId)
            .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

    List<Post> posts = postRepository.findByBoardId(boardId);

    return posts.stream()
        .map(
            post -> {
              ChatRoom chatRoom =
                  chatRoomRepository
                      .findChatRoomByPost_Id(Long.valueOf(post.getId()))
                      .orElseThrow(() -> new CustomException(ErrorCode.CHATROOM_NOT_FOUND));
              return mapToResponseDto(post, chatRoom.getChatRoomId().intValue());
            })
        .collect(Collectors.toList());
  }
}
