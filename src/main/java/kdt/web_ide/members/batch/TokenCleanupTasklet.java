package kdt.web_ide.members.batch;

import java.time.LocalDateTime;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import kdt.web_ide.members.entity.repository.TokenBlacklistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class TokenCleanupTasklet implements Tasklet {
  private final TokenBlacklistRepository tokenBlacklistRepository;

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
    int deletedCount = tokenBlacklistRepository.deleteExpiredTokens(LocalDateTime.now());
    log.info("배치 작업 실행: 블랙리스트에서 만료된 토큰 {}개 삭제 완료", deletedCount);
    return RepeatStatus.FINISHED;
  }
}
