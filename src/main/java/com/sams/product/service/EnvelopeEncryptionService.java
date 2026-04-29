package com.sams.product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DataKeySpec;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.GenerateDataKeyRequest;
import software.amazon.awssdk.services.kms.model.GenerateDataKeyResponse;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnvelopeEncryptionService {

    private final KmsClient kmsClient;

    @Value("${aws.kms.key-alias}")
    private String keyAlias;

    // AES-GCM 설정값
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;   // IV 길이 (12바이트 = 96비트)
    private static final int GCM_TAG_LENGTH = 128;  // 인증 태그 길이 (비트)

    /**
     * 암호화
     * 반환값 구조: encryptedDek::iv::encryptedData (Base64)
     * DB에 이 문자열 통째로 저장
     */
    public String encrypt(String plainText) throws Exception {

        // 1단계: KMS에서 DEK 발급 (이 호출이 전부! 딱 1번만)
        GenerateDataKeyResponse dataKeyResponse = kmsClient.generateDataKey(
                GenerateDataKeyRequest.builder()
                        .keyId(keyAlias)
                        .keySpec(DataKeySpec.AES_256)
                        .build()
        );

        byte[] plaintextDek = dataKeyResponse.plaintext().asByteArray();       // 평문 DEK (암호화에 사용, 메모리에서만)
        byte[] encryptedDek  = dataKeyResponse.ciphertextBlob().asByteArray(); // 암호화된 DEK (DB에 저장)

        // 2단계: DEK로 실제 데이터 로컬 암호화 (KMS 호출 없음, 빠름!)
        byte[] iv = generateIv();
        byte[] encryptedData = aesEncrypt(plainText.getBytes(), plaintextDek, iv);

        // 3단계: 3가지를 :: 구분자로 합쳐서 하나의 문자열로 만들기
        String result = Base64.getEncoder().encodeToString(encryptedDek)
                + "::" + Base64.getEncoder().encodeToString(iv)
                + "::" + Base64.getEncoder().encodeToString(encryptedData);

        log.info("Envelope 암호화 완료 - 원문길이: {}", plainText.length());
        return result;
    }

    /**
     * 복호화
     * 입력값: encryptedDek::iv::encryptedData (encrypt()가 반환한 문자열)
     */
    public String decrypt(String encryptedPayload) throws Exception {

        // 1단계: :: 구분자로 3개 분리
        String[] parts = encryptedPayload.split("::");
        byte[] encryptedDek  = Base64.getDecoder().decode(parts[0]);
        byte[] iv            = Base64.getDecoder().decode(parts[1]);
        byte[] encryptedData = Base64.getDecoder().decode(parts[2]);

        // 2단계: KMS로 DEK 복호화 (이 호출이 전부! 딱 1번만)
        byte[] plaintextDek = kmsClient.decrypt(
                DecryptRequest.builder()
                        .ciphertextBlob(SdkBytes.fromByteArray(encryptedDek))
                        .build()
        ).plaintext().asByteArray();

        // 3단계: 복호화된 DEK로 데이터 복호화 (로컬, 빠름!)
        byte[] decryptedData = aesDecrypt(encryptedData, plaintextDek, iv);

        log.info("Envelope 복호화 완료");
        return new String(decryptedData);
    }

    // AES-GCM 암호화
    private byte[] aesEncrypt(byte[] data, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE,
                new SecretKeySpec(key, "AES"),
                new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        return cipher.doFinal(data);
    }

    // AES-GCM 복호화
    private byte[] aesDecrypt(byte[] data, byte[] key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE,
                new SecretKeySpec(key, "AES"),
                new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        return cipher.doFinal(data);
    }

    // 랜덤 IV 생성 (암호화할 때마다 새로 만들어야 함)
    private byte[] generateIv() {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}