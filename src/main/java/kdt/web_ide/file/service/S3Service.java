package kdt.web_ide.file.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import kdt.web_ide.common.exception.CustomException;
import kdt.web_ide.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public String getFileUrl(String filePath) {
        return amazonS3.getUrl(bucketName, filePath).toString();
    }

    public void uploadFile(InputStream inputStream, String filePath, String contentType) {
        try {
            amazonS3.putObject(new PutObjectRequest(bucketName, filePath, inputStream, null));
        } catch (Exception e) {
            throw new CustomException(ErrorCode.S3_UPLOAD_ERROR);
        }
    }

    public String executeFile(String filePath, String input) {
        try {
            // S3 파일 다운로드
            InputStream inputStream = amazonS3.getObject(new GetObjectRequest(bucketName, filePath)).getObjectContent();
            String localFilePath = saveFileLocally(filePath, inputStream);

            // 실행 로직
            return compileAndRun(localFilePath, input);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_EXECUTION_ERROR);
        }
    }

    public void modifyFileContent(String filePath, String newContent) {
        try (InputStream inputStream = new ByteArrayInputStream(newContent.getBytes(StandardCharsets.UTF_8))) {
            amazonS3.putObject(new PutObjectRequest(bucketName, filePath, inputStream, null));
        } catch (Exception e) {
            throw new CustomException(ErrorCode.S3_UPLOAD_ERROR);
        }
    }

    private String saveFileLocally(String filePath, InputStream inputStream) throws IOException {
        String localFilePath = System.getProperty("java.io.tmpdir") + "/" + extractFileName(filePath);
        try (FileOutputStream fileOutputStream = new FileOutputStream(localFilePath)) {
            inputStream.transferTo(fileOutputStream);
        }
        return localFilePath;
    }

    private String compileAndRun(String localFilePath, String input) throws IOException, InterruptedException {
        String extension = getFileExtension(localFilePath);
        if ("java".equals(extension)) {
            return compileAndRunJava(localFilePath, input);
        } else if ("py".equals(extension)) {
            return runPython(localFilePath, input);
        } else if ("js".equals(extension)) {
            return runJavaScript(localFilePath, input);
        } else if ("cpp".equals(extension)) {
            return compileAndRunCpp(localFilePath, input);
        } else {
            throw new IllegalArgumentException(ErrorCode.FILE_EXTENSION_ERROR.getMessage());
        }
    }

    private String compileAndRunJava(String localFilePath, String input) throws IOException, InterruptedException {
        String className = localFilePath.substring(0, localFilePath.lastIndexOf("."));
        Process compileProcess = new ProcessBuilder("javac", localFilePath).start();
        compileProcess.waitFor();

        Process runProcess = new ProcessBuilder("java", "-cp", System.getProperty("java.io.tmpdir"), className).start();
        return getProcessOutput(runProcess, input);
    }

    private String runPython(String localFilePath, String input) throws IOException, InterruptedException {
        Process process = new ProcessBuilder("python3", localFilePath).start();
        return getProcessOutput(process, input);
    }

    private String runJavaScript(String localFilePath, String input) throws IOException, InterruptedException {
        Process process = new ProcessBuilder("node", localFilePath).start();
        return getProcessOutput(process, input);
    }

    private String compileAndRunCpp(String localFilePath, String input) throws IOException, InterruptedException {
        String outputFile = localFilePath.replace(".cpp", ".out");
        Process compileProcess = new ProcessBuilder("g++", "-o", outputFile, localFilePath).start();
        compileProcess.waitFor();

        Process runProcess = new ProcessBuilder(outputFile).start();
        return getProcessOutput(runProcess, input);
    }

    private String getProcessOutput(Process process, String input) throws IOException, InterruptedException {
        if (input != null && !input.isEmpty()) {
            try (OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream())) {
                writer.write(input);
                writer.flush();
            }
        }
        process.waitFor();

        // 에러 스트림 확인
        try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            StringBuilder errorOutput = new StringBuilder();
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                errorOutput.append(errorLine).append("\n");
            }
            System.err.println("Error Output: " + errorOutput.toString());
        }

        // 표준 출력 확인
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            System.out.println("Standard Output: " + output.toString()); // 로그 추가
            return output.toString();
        }
    }


    private String getFileExtension(String filePath) {
        return filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase();
    }

    private String extractFileName(String filePath) {
        return filePath.substring(filePath.lastIndexOf("/") + 1);
    }
}
