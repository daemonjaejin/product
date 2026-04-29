package com.sams.product.controller;

import com.sams.product.domain.Person;
import com.sams.product.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // React, Vue 에서 호출 허용
public class PersonController {

    private final PersonRepository personRepository;
    private final JobLauncher jobLauncher;
    private final Job encryptionJob;

    // 전체 목록 조회
    @GetMapping
    public List<Person> findAll() {
        return personRepository.findAll();
    }

    // 배치 실행
    @PostMapping("/encrypt")
    public ResponseEntity<String> runBatch() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis()) // 매번 다른 파라미터로 실행
                    .toJobParameters();

            jobLauncher.run(encryptionJob, params);
            return ResponseEntity.ok("배치 실행 완료!");

        } catch (Exception e) {
            log.error("배치 실행 실패", e);
            return ResponseEntity.internalServerError().body("배치 실행 실패: " + e.getMessage());
        }
    }
}