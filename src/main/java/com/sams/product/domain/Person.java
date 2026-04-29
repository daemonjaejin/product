package com.sams.product.domain;

import lombok.Data;

@Data
public class Person {
    private Long   id;
    private String name;
    private String jumin;       // 평문 주민번호
    private String juminEnc;    // 암호화된 주민번호
    private String encStatus;   // N: 미전환, Y: 전환완료
}