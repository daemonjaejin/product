package com.sams.product;

import com.sams.product.service.EnvelopeEncryptionService;
import com.sams.product.service.KmsEncryptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        BatchAutoConfiguration.class
})
class KmsEncryptionServiceTest {

    @Autowired
    EnvelopeEncryptionService envelopeEncryptionService;

    @Autowired
    KmsEncryptionService kmsEncryptionService;

    @Test
    void test() throws Exception{
        String original = "주민등록번호: 900101-1234567";

        // 암호화
        String encrypted = envelopeEncryptionService.encrypt(original);
        System.out.println("=== Envelope 암호문 ===");
        System.out.println(encrypted);

        // 복호화
        String decrypted = envelopeEncryptionService.decrypt(encrypted);
        System.out.println("=== Envelope 복호문 ===");
        System.out.println(decrypted);

        assert original.equals(decrypted) : "원문과 복호문이 다릅니다!";
        System.out.println("✅ Envelope 암복호화 일치 확인 완료!");
    }
}