package kdt.web_ide.common.config;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import kdt.web_ide.members.batch.TokenCleanupTasklet;
import kdt.web_ide.members.entity.repository.TokenBlacklistRepository;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

  @Bean
  public Job tokenCleanupJob(JobRepository jobRepository, Step tokenCleanupStep) {
    return new JobBuilder("tokenCleanupJob", jobRepository) // ✅ JobBuilder 직접 사용
        .start(tokenCleanupStep)
        .build();
  }

  @Bean
  public Step tokenCleanupStep(
      JobRepository jobRepository,
      Tasklet tokenCleanupTasklet,
      PlatformTransactionManager transactionManager) {
    return new StepBuilder("tokenCleanupStep", jobRepository)
        .tasklet(tokenCleanupTasklet, transactionManager)
        .build();
  }

  @Bean
  public Tasklet tokenCleanupTasklet(TokenBlacklistRepository tokenBlacklistRepository) {
    return new TokenCleanupTasklet(tokenBlacklistRepository);
  }

  @Bean
  public JobRepository jobRepository(
      DataSource dataSource, PlatformTransactionManager transactionManager) throws Exception {
    JobRepositoryFactoryBean factoryBean = new JobRepositoryFactoryBean();
    factoryBean.setDataSource(dataSource);
    factoryBean.setTransactionManager(transactionManager);
    factoryBean.afterPropertiesSet();
    return factoryBean.getObject();
  }
}
