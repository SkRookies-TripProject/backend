package com.costrip.costrip_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebResourceConfig implements WebMvcConfigurer {

    // 업로드 루트 경로는 파일 저장 서비스와 동일한 설정값을 사용한다.
    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    /**
     * uploads 폴더에 저장한 파일을 브라우저에서 바로 조회할 수 있도록 매핑한다.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
