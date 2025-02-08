package kdt.web_ide.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
@EnableWebMvc
public class SwaggerConfig implements WebMvcConfigurer {

  @Bean
  public OpenAPI api() {
    // SecurityScheme 정의 (JWT Bearer Token 방식)
    SecurityScheme securityScheme =
        new SecurityScheme()
            .type(SecurityScheme.Type.APIKEY) // APIKEY 타입
            .in(SecurityScheme.In.HEADER) // 헤더에 Authorization
            .name("Authorization"); // 헤더 이름

    // SecurityRequirement 정의 (JWT Bearer Token을 요구)
    SecurityRequirement securityRequirement = new SecurityRequirement().addList("Bearer Token");

    // OpenAPI 객체 구성
    return new OpenAPI()
        .components(new Components().addSecuritySchemes("Bearer Token", securityScheme))
        .addSecurityItem(securityRequirement) // 모든 요청에 대해 Bearer Token 요구
        .addServersItem(new Server().url("/").description("로컬 서버"))
        .info(new Info().title("코에듀 API 명세서").description("코에듀 API 명세서입니다.").version("v0.0.1"));
  }
}
