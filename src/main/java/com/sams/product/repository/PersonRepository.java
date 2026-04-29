package com.sams.product.repository;

import com.sams.product.domain.Person;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PersonRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Person> rowMapper = (rs, rowNum) -> {
        Person p = new Person();
        p.setId(rs.getLong("id"));
        p.setName(rs.getString("name"));
        p.setJumin(rs.getString("jumin"));
        p.setJuminEnc(rs.getString("jumin_enc"));
        p.setEncStatus(rs.getString("enc_status"));
        return p;
    };

    // 미전환 데이터 조회 (배치 Reader용)
    public List<Person> findNotEncrypted() {
        return jdbcTemplate.query(
                "SELECT * FROM person WHERE enc_status = 'N'",
                rowMapper
        );
    }

    // 암호화 완료 후 저장 (배치 Writer용)
    public void updateEncrypted(Long id, String juminEnc) {
        jdbcTemplate.update(
                "UPDATE person SET jumin_enc = ?, jumin = NULL, enc_status = 'Y' WHERE id = ?",
                juminEnc, id
        );
    }

    // 전체 목록 조회 (화면 확인용)
    public List<Person> findAll() {
        return jdbcTemplate.query(
                "SELECT * FROM person ORDER BY id",
                rowMapper
        );
    }
}