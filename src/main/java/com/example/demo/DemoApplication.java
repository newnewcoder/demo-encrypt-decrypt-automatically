package com.example.demo;

import com.example.demo.util.AesUtil;
import org.reactivestreams.Publisher;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.client.reactive.ClientHttpRequest;
import org.springframework.http.client.reactive.ClientHttpRequestDecorator;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    private static final int DEFAULT_BUFFER_SIZE = 256 * 256;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("http://localhost:8080")
                .filter((req, next) -> {
                    BodyInserter<?, ? super ClientHttpRequest> inserter = req.body();
                    ClientRequest newReq = ClientRequest.from(req)
                            .body((outputMessage, context) -> {
                                ClientHttpRequestDecorator msg = new ClientHttpRequestDecorator(outputMessage) {
                                    @Override
                                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                                        return DataBufferUtils.join(body).flatMap(buffer -> {
                                            byte[] bytes = new byte[buffer.readableByteCount()];
                                            buffer.read(bytes);
                                            DataBufferUtils.release(buffer);
                                            String data = new String(bytes, StandardCharsets.UTF_8);
                                            System.out.println("[C]data=" + data);
                                            String encrypted = AesUtil.encrypt(data);
                                            System.out.println("[C]encrypted=" + encrypted);
                                            Resource res = new ByteArrayResource(encrypted.getBytes());
                                            getHeaders().setContentLength(encrypted.getBytes().length);
                                            Flux<DataBuffer> nb = DataBufferUtils.read(res, new DefaultDataBufferFactory(), DEFAULT_BUFFER_SIZE);
                                            return super.writeWith(nb);
                                        });
                                    }
                                };
                                return inserter.insert(msg, context);
                            }).build();

                    return next.exchange(newReq)
                            .map(resp -> {
                                Flux<DataBuffer> buffer = resp.body(BodyExtractors.toDataBuffers());
                                Flux<DataBuffer> newBuffer = buffer.flatMap(dataBuffer -> {
                                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                    dataBuffer.read(bytes);
                                    DataBufferUtils.release(dataBuffer);
                                    String data = new String(bytes, StandardCharsets.UTF_8);
                                    String decrypted = AesUtil.decrypt(data);
                                    System.out.println("[C]data=" + data);
                                    Resource res = new ByteArrayResource(decrypted.getBytes());
                                    System.out.println("[C]decrypted=" + decrypted);
                                    return DataBufferUtils.read(res, new DefaultDataBufferFactory(), DEFAULT_BUFFER_SIZE);
                                });
                                return ClientResponse.from(resp).body(newBuffer).build();
                            });
                }).build();
    }
}
