package org.example.catalogsservice.messagequeue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.catalogsservice.entity.Catalogs;
import org.example.catalogsservice.repository.CatalogsRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

    private final CatalogsRepository repository;

    @KafkaListener(topics = "example-catalog-topic") // topic 에서 데이터 가져오기
    public void updateQty(String kafkaMessage) {

        log.info("Kafka Message: ->" + kafkaMessage);

        Map<Object, Object> map = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            // json string 데이터를 Java Map 으로 Deserialize
            map = mapper.readValue(kafkaMessage, new TypeReference<>() {
            });
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }

        String productId = (String) map.get("productId");
        Integer qty = (Integer) map.get("qty");

        Catalogs catalogs = repository.findByProductId(productId)
            .orElseThrow(() -> new RuntimeException("일치하는 카탈로그 정보가 존재하지 않습니다."));
        catalogs.minusStock(qty);
        repository.save(catalogs); // 재고 수량 업데이트
    }
}