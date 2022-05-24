package com.example.demo.controller;

import com.example.demo.util.AesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Controller
public class DemoController {

    @Autowired
    private WebClient webClient;

    // server
    @PostMapping("/server")
    @ResponseBody
    private Mono<String> server(@RequestBody String payload, @RequestHeader Map<String, String> headers) {
        // headers.forEach((key, value) -> System.out.println(String.format("Header '%s' = %s", key, value)));
        System.out.println("[S]payload=" + payload);
        String decrypted = AesUtil.decrypt(payload);
        System.out.println("[S]decrypted=" + decrypted);
        return Mono.just(AesUtil.encrypt(decrypted + " 閱 " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
    }

    // client
    @PostMapping("/client")
    @ResponseBody
    private Mono<String> client(@RequestBody String payload) {
        return webClient.post()
                .uri(b -> b.path("/server").build())
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class);
    }
}