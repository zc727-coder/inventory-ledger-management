package cn.sycoder.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "测试接口")
@RestController
@RequestMapping("/test")
public class TestController {

    @ApiOperation("获取欢迎消息")
    @GetMapping("/hello")
    public String hello() {
        return "Hello, Swagger! This is a test interface.";
    }

    @ApiOperation("获取当前时间")
    @GetMapping("/time")
    public String getTime() {
        return "Current time: " + java.time.LocalDateTime.now();
    }
}
