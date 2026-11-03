package cn.sycoder.controller;

import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

//@RestController
//@RequestMapping("/api")
public class MyController {


//    @RequestMapping("/login")
    public String test01(HttpServletRequest request) throws Exception{
        System.out.println("==== 收到请求 ====");
        System.out.println("method: " + request.getMethod());
        System.out.println("uri: " + request.getRequestURI());
        System.out.println("query: " + request.getQueryString());

        System.out.println("---- headers ----");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            System.out.println(name + ": " + request.getHeader(name));
        }

        String body = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        System.out.println("---- body ----");
        System.out.println(body);

        return "{\"success\":true,\"msg\":\"debug ok\"}";

    }
}
