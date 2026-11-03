package cn.sycoder;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.schema.TypeNameExtractor;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebFlux;

@SpringBootApplication
@MapperScan("cn.sycoder.mapper")

public class MySpringBootProject01Application {

    public static void main(String[] args) {
        SpringApplication.run(MySpringBootProject01Application.class, args);
    }


}
