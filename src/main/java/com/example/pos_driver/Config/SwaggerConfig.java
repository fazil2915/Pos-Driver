package com.example.pos_driver.Config;

import io.swagger.v3.oas.models.OpenAPI;
        import io.swagger.v3.oas.models.info.Info;
        import io.swagger.v3.oas.models.info.Contact;
        import org.springframework.context.annotation.Bean;
        import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("POS DRIVER API")
                        .version("1.0")
                        .description("API Documentation for Pos Driver")
                        .contact(new Contact().name("APT").email("appliedpaymentstech007@gmail.com"))
                );
    }
}
