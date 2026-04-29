DROP TABLE IF EXISTS person;

CREATE TABLE person (
                        id          BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name        VARCHAR(50)  NOT NULL,
                        jumin       VARCHAR(14),
                        jumin_enc   VARCHAR(500),
                        enc_status  VARCHAR(1) DEFAULT 'N'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 샘플 데이터
INSERT INTO person (name, jumin, enc_status) VALUES ('홍길동', '900101-1234567', 'N');
INSERT INTO person (name, jumin, enc_status) VALUES ('김철수', '850315-1098765', 'N');
INSERT INTO person (name, jumin, enc_status) VALUES ('이영희', '920722-2345678', 'N');
INSERT INTO person (name, jumin, enc_status) VALUES ('박민준', '781203-1567890', 'N');
INSERT INTO person (name, jumin, enc_status) VALUES ('최수연', '950830-2678901', 'N');