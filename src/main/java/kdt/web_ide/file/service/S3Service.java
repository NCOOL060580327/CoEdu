package kdt.web_ide.file.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import kdt.web_ide.common.exception.CustomException;
import kdt.web_ide.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

  private final S3Client amazonS3;

  @Value("${cloud.aws.s3.bucket}")
  private String bucketName;

  public String getFileUrl(String filePath) {
    try {
      return amazonS3
          .utilities()
          .getUrl(GetUrlRequest.builder().bucket(bucketName).key(filePath).build())
          .toExternalForm();
    } catch (Exception e) {
      log.error("Error fetching S3 file URL for path: {}", filePath, e);
      throw new CustomException(ErrorCode.S3_FILE_NOT_FOUND);
    }
  }

  public void uploadFile(InputStream inputStream, String filePath, String contentType) {
    try {
      PutObjectRequest request =
          PutObjectRequest.builder()
              .bucket(bucketName)
              .key(filePath)
              .contentType(contentType)
              .build();

      amazonS3.putObject(
          request, RequestBody.fromInputStream(inputStream, inputStream.available()));
    } catch (Exception e) {
      log.error("Error uploading file to S3. Path: {}, ContentType: {}", filePath, contentType, e);
      throw new CustomException(ErrorCode.S3_UPLOAD_ERROR);
    }
  }

  public String executeFile(String filePath, String input) {
    log.info("Starting file execution. FilePath: {}, Input: {}", filePath, input);
    String localFilePath = null;
    try {
      // S3에서 파일 다운로드
      GetObjectRequest getObjectRequest =
          GetObjectRequest.builder().bucket(bucketName).key(filePath).build();
      ResponseBytes<GetObjectResponse> s3ObjectBytes = amazonS3.getObjectAsBytes(getObjectRequest);
      InputStream inputStream = new ByteArrayInputStream(s3ObjectBytes.asByteArray());
      log.info("File downloaded from S3. FilePath: {}", filePath);

      // 로컬 파일 저장
      localFilePath = saveFileLocally(filePath, inputStream);
      log.info("File saved locally at: {}", localFilePath);

      // 파일 실행
      String result = compileAndRun(localFilePath, input);
      log.info("File execution completed. Result: {}", result);
      return result;
    } catch (NoSuchKeyException e) {
      log.error("File not found in S3. FilePath: {}", filePath, e);
      throw new CustomException(ErrorCode.S3_FILE_NOT_FOUND, "File not found in S3");
    } catch (S3Exception e) {
      log.error("Error accessing S3. FilePath: {}", filePath, e);
      throw new CustomException(
          ErrorCode.S3_ACCESS_ERROR, "Error accessing S3: " + e.awsErrorDetails().errorMessage());
    } catch (Exception e) {
      log.error("Error during file execution. FilePath: {}, Input: {}", filePath, input, e);
      throw new CustomException(ErrorCode.FILE_EXECUTION_ERROR, e.getMessage());
    } finally {
      if (localFilePath != null) {
        deleteLocalFile(localFilePath);
      }
    }
  }

  public void modifyFileContent(String filePath, String newContent) {
    try (InputStream inputStream =
        new ByteArrayInputStream(newContent.getBytes(StandardCharsets.UTF_8))) {
      uploadFile(inputStream, filePath, "text/plain");
    } catch (Exception e) {
      log.error(
          "Error modifying file content on S3. FilePath: {}, NewContent: {}",
          filePath,
          newContent,
          e);
      throw new CustomException(ErrorCode.S3_UPLOAD_ERROR);
    }
  }

  private String saveFileLocally(String filePath, InputStream inputStream) throws IOException {
    Path localFilePath = Paths.get(System.getProperty("java.io.tmpdir"), extractFileName(filePath));
    Files.copy(inputStream, localFilePath);
    log.info("File saved locally at: {}", localFilePath);
    return localFilePath.toString();
  }

  public String getFileContent(String filePath) {
    try {
      GetObjectRequest getObjectRequest =
          GetObjectRequest.builder().bucket(bucketName).key(filePath).build();

      ResponseBytes<GetObjectResponse> s3ObjectBytes = amazonS3.getObjectAsBytes(getObjectRequest);
      return new String(s3ObjectBytes.asByteArray(), StandardCharsets.UTF_8);
    } catch (NoSuchKeyException e) {
      log.error("File not found in S3. FilePath: {}", filePath, e);
      throw new CustomException(ErrorCode.S3_FILE_NOT_FOUND, "File not found in S3");
    } catch (S3Exception e) {
      log.error("Error accessing S3. FilePath: {}", filePath, e);
      throw new CustomException(
          ErrorCode.S3_ACCESS_ERROR, "Error accessing S3: " + e.awsErrorDetails().errorMessage());
    }
  }

  private void deleteLocalFile(String localFilePath) {
    try {
      Files.deleteIfExists(Paths.get(localFilePath));
      log.info("Deleted local file: {}", localFilePath);
    } catch (IOException e) {
      log.error("Failed to delete local file: {}", localFilePath, e);
    }
  }

  private String compileAndRun(String localFilePath, String input)
      throws IOException, InterruptedException {
    String extension = getFileExtension(localFilePath);
    log.info("Compiling and running file. FilePath: {}, Extension: {}", localFilePath, extension);
    return switch (extension) {
      case "java" -> compileAndRunJava(localFilePath, input);
      case "py" -> runPython(localFilePath, input);
      case "js" -> runJavaScript(localFilePath, input);
      case "cpp" -> compileAndRunCpp(localFilePath, input);
      default -> {
        log.error("Unsupported file type: {}", extension);
        throw new CustomException(ErrorCode.FILE_EXTENSION_ERROR);
      }
    };
  }

  private String compileAndRunJava(String localFilePath, String input)
      throws IOException, InterruptedException {
    log.info("Compiling Java file: {}", localFilePath);
    String className = localFilePath.substring(0, localFilePath.lastIndexOf("."));
    Process compileProcess = new ProcessBuilder("javac", localFilePath).start();
    compileProcess.waitFor();

    log.info("Running Java file: {}", className);
    Process runProcess =
        new ProcessBuilder("java", "-cp", System.getProperty("java.io.tmpdir"), className).start();
    return getProcessOutput(runProcess, input);
  }

  private String runPython(String localFilePath, String input)
      throws IOException, InterruptedException {
    log.info("Running Python file: {}", localFilePath);
    Process process = new ProcessBuilder("python3", localFilePath).start();
    return getProcessOutput(process, input);
  }

  private String runJavaScript(String localFilePath, String input)
      throws IOException, InterruptedException {
    log.info("Running JavaScript file: {}", localFilePath);
    Process process = new ProcessBuilder("node", localFilePath).start();
    return getProcessOutput(process, input);
  }

  private String compileAndRunCpp(String localFilePath, String input)
      throws IOException, InterruptedException {
    log.info("Compiling C++ file: {}", localFilePath);
    String outputFile = localFilePath.replace(".cpp", ".out");
    Process compileProcess = new ProcessBuilder("g++", "-o", outputFile, localFilePath).start();
    compileProcess.waitFor();

    log.info("Running C++ file: {}", outputFile);
    Process runProcess = new ProcessBuilder(outputFile).start();
    return getProcessOutput(runProcess, input);
  }

  private String getProcessOutput(Process process, String input)
      throws IOException, InterruptedException {
    if (input != null && !input.isEmpty()) {
      try (OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream())) {
        writer.write(input);
        writer.flush();
      }
    }
    process.waitFor();

    StringBuilder errorOutput = new StringBuilder();
    try (BufferedReader errorReader =
        new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
      String errorLine;
      while ((errorLine = errorReader.readLine()) != null) {
        errorOutput.append(errorLine).append("\n");
      }
    }

    if (!errorOutput.isEmpty()) {
      log.error("Process error output: {}", errorOutput);
      throw new CustomException(ErrorCode.FILE_EXECUTION_ERROR, errorOutput.toString());
    }

    StringBuilder output = new StringBuilder();
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line).append("\n");
      }
    }
    log.info("Process standard output: {}", output);
    return output.toString();
  }

  private String getFileExtension(String filePath) {
    return filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase();
  }

  public void deleteFile(String filePath) {
    try {
      amazonS3.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(filePath).build());
      log.info("Deleted file from S3: {}", filePath);
    } catch (NoSuchKeyException e) {
      log.warn("File not found in S3 for deletion: {}", filePath);
    } catch (S3Exception e) {
      log.error("Error deleting file from S3. FilePath: {}", filePath, e);
      throw new CustomException(
          ErrorCode.S3_DELETE_ERROR,
          "Error deleting file from S3: " + e.awsErrorDetails().errorMessage());
    }
  }

  private String extractFileName(String filePath) {
    return filePath.substring(filePath.lastIndexOf("/") + 1);
  }
}
