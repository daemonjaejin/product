지금까지 진행 내용 정리
✅ 완료된 전체 작업
1단계. AWS 계정 준비

✅ AWS 계정 생성
✅ Root 계정 MFA 설정
2단계. IAM 사용자 생성

✅ IAM 사용자 생성
└── 사용자명 : kms-dev-user
└── 정책     : AWSKeyManagementServicePowerUser
✅ Access Key 발급 및 저장
3단계. KMS 키 생성

✅ KMS 고객 관리형 키 생성
└── 별칭    : alias/test
└── 키 유형 : 대칭 (AES-256)
✅ 키 정책 설정 (루트/관리/암복호화/Grant 권한)
4단계. 로컬 환경 설정

✅ AWS CLI 설치 및 configure 설정
✅ CLI 암복호화 테스트 성공
5단계. Spring Boot 프로젝트 생성

✅ 프로젝트 생성
└── Spring Boot 3.5.14
└── Java 21
└── Gradle - Groovy
└── 패키지 : com.sams.product

✅ build.gradle 의존성 추가
└── spring-boot-starter-batch
└── spring-boot-starter-web
└── spring-boot-starter-jdbc
└── software.amazon.awssdk:kms
└── com.mysql:mysql-connector-j

✅ application.yml 설정
└── MySQL : localhost:13307/sams
└── KMS   : alias/test / ap-northeast-2
└── Batch : 자동실행 OFF
6단계. 핵심 코드 작성

✅ KmsConfig.java
└── KmsClient Bean 등록
└── ~/.aws/credentials 자동 인증

✅ KmsEncryptionService.java
└── encrypt() : 평문 → KMS → Base64 암호문
└── decrypt() : Base64 암호문 → KMS → 평문
└── 테스트 성공 ✅

✅ EnvelopeEncryptionService.java
└── KMS로 DEK 발급 (1회 호출)
└── DEK로 AES-GCM 로컬 암호화 (빠름)
└── 저장 형식: encryptedDek::iv::encryptedData
└── 테스트 성공 ✅
7단계. Batch + API 코드 작성

✅ DB 설계 (MySQL - sams.person)
└── jumin      : 평문 주민번호
└── jumin_enc  : 암호화된 주민번호
└── enc_status : N(미전환) / Y(전환완료)

✅ Person.java           - 도메인
✅ PersonRepository.java - DB 조회/저장
✅ BatchConfig.java      - Reader/Processor/Writer
✅ PersonController.java - REST API

✅ 앱 실행 성공
✅ GET /api/persons → 5건 데이터 조회 성공
🔜 남은 작업

B-완료 단계
└── POST /api/persons/encrypt (배치 실행) 테스트

C-1. React 화면 (포트 3000)
└── 목록 조회
└── 배치 실행 버튼
└── 실행 전/후 데이터 비교

C-2. Vue 화면 (포트 5173)
└── 같은 기능, Vue로 구현
└── React 코드와 비교 학습
현재 API 상태

GET  /api/persons         ✅ 동작 확인
POST /api/persons/encrypt 🔜 배치 실행 (다음 테스트)