package com.sams.product.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.EncryptRequest;

import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class KmsEncryptionService {

    private final KmsClient kmsClient;

    @Value("${aws.kms.key-alias}")
    private String keyAlias;

    // 평문 → 암호문
    public String encrypt(String plainText) {
        EncryptRequest request = EncryptRequest.builder()
                .keyId(keyAlias)
                .plaintext(SdkBytes.fromUtf8String(plainText))
                .build();

        byte[] cipherBytes = kmsClient.encrypt(request)
                .ciphertextBlob()
                .asByteArray();

        String encoded = Base64.getEncoder().encodeToString(cipherBytes);
        log.info("암호화 완료 - 원문길이: {}, 암호문길이: {}", plainText.length(), encoded.length());
        return encoded;
    }

    // 암호문 → 평문
    public String decrypt(String encryptedBase64) {
        byte[] cipherBytes = Base64.getDecoder().decode(encryptedBase64);

        DecryptRequest request = DecryptRequest.builder()
                .ciphertextBlob(SdkBytes.fromByteArray(cipherBytes))
                .build();

        String plainText = kmsClient.decrypt(request)
                .plaintext()
                .asUtf8String();

        log.info("복호화 완료");
        return plainText;
    }
}