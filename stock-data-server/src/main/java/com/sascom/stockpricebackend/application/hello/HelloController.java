package com.sascom.stockpricebackend.application.hello;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sascom.stockpricebackend.application.kis.properties.TrName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequestMapping("/hello")
@RequiredArgsConstructor
@RestController
public class HelloController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;
    private final RedisTemplate redisTemplate;


    @GetMapping
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello world!");
    }

    @PostMapping("/hoka")
    public ResponseEntity<String> hoka() throws IOException {
        log.info("HelloController.hoka() is called.");
        Resource resource = resourceLoader.getResource("classpath:adjusted_hoka_v5.json");
        String content = new String(Files.readAllBytes(Paths.get(resource.getURI())));

        List<Object> dataList = objectMapper.readValue(content, new TypeReference<List<Object>>() {});

        // 1초마다 데이터를 전송
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        for (int i = 0; i < dataList.size(); i++) {
            final int index = i;
            executorService.schedule(() -> {
                messagingTemplate.convertAndSend(TrName.REALTIME_HOKA.getDest() + "/005930", dataList.get(index));
            }, i, TimeUnit.SECONDS);
        }
        executorService.shutdown();

        return ResponseEntity.ok("Hoka!");
    }

    @PostMapping("/purchase")
    public ResponseEntity<String> purchase() throws IOException {
        log.info("HelloController.purchase() is called.");
        Resource resource = resourceLoader.getResource("classpath:generated_stock_data_v6.json");
        String content = new String(Files.readAllBytes(Paths.get(resource.getURI())));

        List<StockDataDto> dataList = objectMapper.readValue(content, new TypeReference<List<StockDataDto>>() {});
//        Map<String, String> dataList = objectMapper.readValue(content, new TypeReference<Map<String, String>>() {
//        });
        // 1초마다 데이터를 전송
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        for (int i = 0; i < dataList.size(); i++) {
            final int index = i;
            executorService.schedule(() -> {
                messagingTemplate.convertAndSend(TrName.REALTIME_PURCHASE.getDest() + "/005930", dataList.get(index));
                redisTemplate.convertAndSend(TrName.REALTIME_PURCHASE.getDest() + "/005930", dataList.get(index));
            }, i, TimeUnit.SECONDS);
        }
        executorService.shutdown();

        return ResponseEntity.ok("purchase!");
    }
}
