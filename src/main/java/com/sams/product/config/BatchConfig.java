package com.sams.product.config;

import com.sams.product.domain.Person;
import com.sams.product.repository.PersonRepository;
import com.sams.product.service.EnvelopeEncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final PersonRepository personRepository;
    private final EnvelopeEncryptionService envelopeEncryptionService;

    // Job 정의
    @Bean
    public Job encryptionJob(JobRepository jobRepository, Step encryptionStep) {
        return new JobBuilder("encryptionJob", jobRepository)
                .incrementer(new RunIdIncrementer()) // 매번 새 실행 ID 부여
                .start(encryptionStep)
                .build();
    }

    // Step 정의
    @Bean
    public Step encryptionStep(JobRepository jobRepository,
                               PlatformTransactionManager transactionManager,
                               ItemReader<Person> reader) { // ← 주입받도록 변경
        return new StepBuilder("encryptionStep", jobRepository)
                .<Person, Person>chunk(10, transactionManager)
                .reader(reader)        // ← 직접 호출(reader()) 말고 주입받은 것 사용
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    @StepScope  // ← 이게 핵심!
    public ItemReader<Person> reader() {
        return new ListItemReader<>(personRepository.findNotEncrypted());
    }

    // Processor: 암호화
    @Bean
    public ItemProcessor<Person, Person> processor() {
        return person -> {
            log.info("암호화 처리 중 - id: {}, name: {}", person.getId(), person.getName());
            String encrypted = envelopeEncryptionService.encrypt(person.getJumin());
            person.setJuminEnc(encrypted);
            return person;
        };
    }

    // Writer: DB 저장
    @Bean
    public ItemWriter<Person> writer() {
        return items -> {
            for (Person person : items) {
                personRepository.updateEncrypted(person.getId(), person.getJuminEnc());
                log.info("저장 완료 - id: {}, name: {}", person.getId(), person.getName());
            }
        };
    }
}