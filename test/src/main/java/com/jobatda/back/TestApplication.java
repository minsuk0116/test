package com.jobatda.back;

import com.jobatda.back.config.NaverApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Spring Boot 애플리케이션의 메인 클래스
 * 
 * 이 클래스는 애플리케이션의 진입점 역할을 하며 다음과 같은 설정을 포함합니다:
 * - @SpringBootApplication: 자동 설정, 컴포넌트 스캔, 설정 클래스 등록을 자동으로 처리
 * - @EnableConfigurationProperties: NaverApiProperties 설정 클래스를 활성화
 * - @EntityScan: JPA 엔티티를 스캔할 패키지 경로 지정 (domain 하위의 모든 entity 패키지)
 * - @EnableJpaRepositories: JPA 리포지토리를 스캔할 패키지 경로 지정 (domain 하위의 모든 repository 패키지)
 */
@SpringBootApplication
@EnableConfigurationProperties(NaverApiProperties.class) // 네이버 API 설정 속성을 활성화
@EntityScan("com.jobatda.back.domain.*.entity") // 모든 도메인의 엔티티 클래스를 스캔
@EnableJpaRepositories("com.jobatda.back.domain.*.repository") // 모든 도메인의 리포지토리 인터페이스를 스캔
public class TestApplication {

	/**
	 * 애플리케이션의 메인 메서드
	 * Spring Boot 애플리케이션을 시작하는 진입점
	 * 
	 * @param args 명령행 인수
	 */
	public static void main(String[] args) {
		SpringApplication.run(TestApplication.class, args);
	}

}
