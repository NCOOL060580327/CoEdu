package kdt.web_ide.post.service;

import kdt.web_ide.common.exception.CustomException;
import kdt.web_ide.common.exception.ErrorCode;
import kdt.web_ide.file.service.S3Service;
import kdt.web_ide.post.dto.PostRequestDto;
import kdt.web_ide.post.dto.PostResponseDto;
import kdt.web_ide.post.entity.Post;
import kdt.web_ide.post.entity.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final S3Service s3Service;

    public PostResponseDto createPost(PostRequestDto requestDto) {
        // 파일 제목 자동 생성
        String fileName = generateFileName(requestDto.getName(), String.valueOf(requestDto.getLanguage()));
        String filePath = uploadEmptyFileToS3(fileName, requestDto.getBoardId());

        Post post = Post.builder()
                .boardId(requestDto.getBoardId())
                .name(requestDto.getName())
                .language(requestDto.getLanguage())
                .filePath(filePath)
                .build();

        Post savedPost = postRepository.save(post);
        return mapToResponseDto(savedPost);
    }

    public PostResponseDto getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        return mapToResponseDto(post);
    }

    public List<PostResponseDto> getAllPosts() {
        return postRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    private String uploadEmptyFileToS3(String fileName, Integer boardId) {
        try {
            String s3Path = "boards/" + boardId + "/" + fileName;
            ByteArrayInputStream emptyContent = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
            s3Service.uploadFile(emptyContent, s3Path, "text/plain");
            return s3Path;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.POST_NOT_FOUND);
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

    private PostResponseDto mapToResponseDto(Post post) {
        return PostResponseDto.builder()
                .id(post.getId())
                .boardId(post.getBoardId())
                .name(post.getName())
                .language(post.getLanguage())
                .filePath(post.getFilePath())
                .createdAt(post.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }

    public String downloadFile(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        return s3Service.getFileUrl(post.getFilePath());
    }

    public String executeFile(Long postId, String input) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        return s3Service.executeFile(post.getFilePath(), input);
    }

    public void modifyFileContent(Long postId, String newContent) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
        s3Service.modifyFileContent(post.getFilePath(), newContent);
    }
}


