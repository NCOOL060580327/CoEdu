package kdt.web_ide.members.batch;

import java.time.LocalDateTime;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import kdt.web_ide.members.entity.repository.TokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenBatchScheduler {
  private final TokenBlacklistRepository tokenBlacklistRepository;
  private final JobLauncher jobLauncher;
  private final Job tokenCleanupJob;

  @Scheduled(cron = "0 0 0,6,12,18 * * ?")
  public void runTokenCleanupJob() {
    if (tokenBlacklistRepository.countExpiredTokens(LocalDateTime.now()) > 0) {
      try {
        jobLauncher.run(tokenCleanupJob, new JobParameters());
        log.info("배치 작업 실행 완료: 블랙리스트 토큰 삭제");
      } catch (Exception e) {
        log.error("배치 실행 중 오류 발생", e);
      }
    } else {
      log.info("배치 작업 실행 스킵 : 만료된 토큰 없음");
    }
  }
}
