package eastmeet.backend5.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI().info(
            new Info()
                .title("Programmers DDD Practice Backend Application API")
                .version("v1")
                .contact(new Contact().name("eastmeet"))
                .description("프로그래머스 도메인 주도개발(DDD) 연습 API 명세서입니다.")
        );
    }

}
