package kdt.web_ide.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {
  @Bean(name = "taskExecutor")
  public TaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setCorePoolSize(5); // 최소 스레드 개수
    taskExecutor.setMaxPoolSize(10); // 최대 스레드 개수
    taskExecutor.setAwaitTerminationSeconds(30); // 스레드 종료 대기 시간
    taskExecutor.setThreadNamePrefix("compile-");
    taskExecutor.initialize();
    return taskExecutor;
  }
}
