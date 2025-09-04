package com.jobatda.back.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@RestController
public class DatabaseTestController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/test/db")
    public String testDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            String dbName = metaData.getDatabaseProductName();
            String dbVersion = metaData.getDatabaseProductVersion();
            String url = metaData.getURL();
            
            // 테이블 목록 조회
            ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            List<String> tableNames = new ArrayList<>();
            while (tables.next()) {
                tableNames.add(tables.getString("TABLE_NAME"));
            }
            
            return String.format(
                "데이터베이스 연결 성공!<br>" +
                "DB: %s %s<br>" +
                "URL: %s<br>" +
                "테이블 개수: %d<br>" +
                "테이블 목록: %s",
                dbName, dbVersion, url, tableNames.size(), tableNames.toString()
            );
        } catch (Exception e) {
            return "데이터베이스 연결 실패: " + e.getMessage();
        }
    }
}