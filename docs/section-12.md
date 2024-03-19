# 데이터 동기화를 위한 Apache Kafka 활용 - 2
## Orders Microservice와 Catalogs Microservice에 Kafka Topic의 적용
### 데이터 동기화 1 - Orders -> Catalogs
- **Orders** Service에 요청 된 주문의 수량 정보를 **Catalogs** Service에 반영 -> **수량 감소**
  - 기존 monolithic 어플리케이션이라면 단일 데이터베이스를 쓰고 있기 때문에 Orders Service 와 Catalog Service 가 같은 데이터베이스이다.
- Orders Service에서 Kafka Topic으로 메시지 전송 -> `Producer`
- Catalog Service에서 Kafka Topic에 전송 된 메시지 취득 -> `Consumer`

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/27997809-67e1-4404-97ae-6ff770c12837" width="80%"/><br>
Orders Service에서 Kafka로 상품의 수량 관련 정보를 전달하면 Kafka의 **Topic에 저장**되었다가<br>
**Topic을 등록한 Consumer**가 변경된 데이터 값을 가져가서 자신의 테이블에 반영시키는 형태

### ⭐️ Catalogs Service : Consumer
라이브러리 추가
- spring-kafka

#### KafkaConsumerConfig
```java
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> properties = new HashMap<>();

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092"); // Kafka 주소
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "consumerGroupId"); // 그룹아이디란 카프카에서 토픽에 쌓여있는 메시지를 가져가는 Consumer를 그룹핑할 수 있다. 
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class); // key와 value가 한 세트로 저장되어있을 때 값을 가져와서 해석, 둘다 String
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(properties);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory =
                new ConcurrentKafkaListenerContainerFactory<>();
        kafkaListenerContainerFactory.setConsumerFactory(consumerFactory());
        return kafkaListenerContainerFactory;
    }
}
```
- `ConsumerFactory` : Topic에 접속하기 위한 정보를 가진 Factory 빈 생성
- `ConcurrentKafkaListenerContainerFactory` : **Topic에 변경사항이 존재하는 이벤트를 리스닝**하는 빈
- `properties.put(ConsumerConfig.GROUP_ID_CONFIG, "consumerGroupId")`; : 그룹아이디란 카프카에서 **Topic에 쌓여있는 메시지를 가져가는 Consumer를 그룹핑**할 수 있다.

> ConsumerConfig.GROUP_ID_CONFIG는 Consumer가 속한 Consumer 그룹의 ID를 설정한다.<br>
> Consumer 그룹은 **같은 토픽을 소비하는 Consumer들의 그룹**으로, 그룹 내의 모든 Consumer는 토픽의 서로 다른 파티션에서 메시지를 읽어들인다.<br>
> 이를 통해 메시지 처리를 **병렬화**하여 **처리 속도를 향상**시킬 수 있으며, Consumer가 **실패**할 경우 **다른 Consumer가 해당 Consumer의 파티션을 처리**하여 **고가용성을 제공**할 수 있습니다.
> ConcurrentKafkaListenerContainerFactory는 Spring의 `@KafkaListener` 어노테이션이 붙은 메서드에 주입되어 사용되며, 메시지를 동시에 처리할 수 있는 메시지 리스너 컨테이너를 생성합니다.

#### kafkaConsumer
```java
@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

    private final CatalogRepository catalogRepository;

    @KafkaListener(topics = "example-order-topic")
    public void updateQty(String kafkaMessage) {
        log.info("Kafka Message: =====> " + kafkaMessage);

        Map<Object, Object> map = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            map = mapper.readValue(kafkaMessage, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        CatalogEntity entity = catalogRepository.findByProductId((String) map.get("productId"));
        if (entity != null) {
            entity.setStock(entity.getStock() - (Integer) map.get("qty"));
            catalogRepository.save(entity);
        }
    }
}
```
- `@KafkaListener` 를 이용해서 데이터를 가져와 데이터베이스를 업데이트

<br>

### ⭐️ Orders Service : Producer
라이브러리 추가
- spring-kafka

<br>

#### KafkaProducerConfig
```java
@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```
- `BOOTSTRAP_SERVERS_CONFIG` : Producer가 처음으로 연결할 **Kafka 브로커의 위치를 설정**한다. 현재 코드의 경우 localhost의 9092 포트에 위치하도록 설정했다.
- `KEY_SERIALIZER_CLASS_CONFIG & VALUE_SERIALIZER_CLASS_CONFIG` : Producer가 Key와 Value 값의 데이터를 **Kafka 브로커로 전송하기 전**에 데이터를 byte array로 변환하는 데 사용하는 직렬화 메커니즘을 설정한다. <br>
  Kafka는 네트워크를 통해 데이터를 전송하기 때문에, 객체를 byte array로 변환하는 직렬화 과정이 필요합니다. 따라서, StringSerializer를 사용해 직렬화했습니다.
- `KafkaTemplate` : **토픽에 데이터를 전달**하기 위해서 사용되는 빈

> KafkaTemplate는 Spring Kafka에서 제공하는 Kafka Producer를 Wrapping 한 클래스다.<br>
> KafkaTemplate는 Kafka에 메시지를 보내는 여러 메서드를 제공하며, 이 메서드를 사용하여 브로커로 메시지를 보내기 위해 <br>
> 직접 Kafka Producer API를 사용하는 대신 `send`와 같은 메서드를 통해 더 편리하고 간결한 코드로 메시지를 보낼 수 있다.

<br>

#### KafkaProducer
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public OrderDto send(String kafkaTopic, OrderDto orderDto) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "";

        try {
            jsonInString = mapper.writeValueAsString(orderDto);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        kafkaTemplate.send(kafkaTopic, jsonInString);
        log.info("Kafka Producer send data from the Order microservice: " + orderDto);

        return orderDto;
    }
}
```
- `jsonInString = mapper.writeValueAsString(orderDto);` : 주문 정보를 **json 포맷**으로 전달하기 위해서 변환
- 현재는 토픽에 용도가 단순히 메시지를 전달하는 용도로만 쓰이고 메시지를 가져가는 Consumer에서 다시 해석하는 과정을 거치기때문에
- 단순히 OrderDto값을 직렬화해서 보내도 상관없다. 따라서 이전처럼 Schema 정보를 넣는 행위가 필요없다.
  - 직렬화 : 인코딩/ 역직렬화 : 원래 형태로 풀어씀
<br>

#### OrderController
```java
@RestController
@RequestMapping("/order-service")
@Slf4j
@RequiredArgsConstructor
public class OrderController {

    private final Environment env;
    private final OrderService orderService;
    private final KafkaProducer kafkaProducer;
...

    @PostMapping("/{userId}/orders")
    public ResponseEntity<ResponseOrder> createOrder(@PathVariable("userId") String userId,
                                                     @RequestBody RequestOrder orderDetails) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        OrderDto orderDto = mapper.map(orderDetails, OrderDto.class);
        orderDto.setUserId(userId);
        /* jpa */
        OrderDto createdOrder = orderService.createOrder(orderDto);
        ResponseOrder responseOrder = mapper.map(createdOrder, ResponseOrder.class);

        kafkaProducer.send("example-order-topic", orderDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseOrder);
    }
...
}
```
#### 주문 전 catalog
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/93223330-df46-4c98-a1fd-a0827881506e" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/be4f020a-ec59-449e-8bcb-6c6a447ebb8c" width="60%"/><br>

#### 주문 후 order 증가, catalog 감소
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/cbb1ea0a-d13b-4a6b-88e2-579cd2ed8334" width="100%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/6254a064-a33d-4a61-ba5b-f8181b7299ee" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/d9826fa0-0a9d-46a6-b91f-ce94174e6c30" width="100%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/61556070-88de-4973-a935-3c5ea7c92118" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/5a0d2e37-1535-4754-984d-ce98a92dda62" width="100%"/><br>


<br>

## Multi Orders Microservice 사용에 대한 데이터 동기화 문제
### Orders Service 2개 기동
- Users의 요청 분산 처리
- Orders 데이터도 분산 저장 -> 동기화 문제

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/1a1762ba-289d-4412-9744-c4be703a03fe" width="70%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/0cd14e92-cf05-4ac2-8f11-f77c64a83d5d" width="800%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/b0e106df-7924-467a-8404-3294235042fc" width="800%"/><br>
- 주문 5개 생성
- 각 인스턴스에 2개 3개 나눠서 생성

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/64afca97-ab58-4982-be6f-a874c1444192" width="50%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/323b4b8a-fb17-4783-b26b-401a122226d9" width="50%"/><br>
- 같은 사용자 조회하게 되면 다른 결과를 확인하게 되는 문제가 발생
- Kafka 메세징 서버를 이용해서 해결

## Kafka Connect를 활용한 단일 데이터베이스를 사용
### Multiple Orders Service에서의 동기화
- Orders Service에 요청 된 주문 정보를 DB가 아니라 Kafka Topic으로 전송
- Kafka Topic에 설정 된 Kafka Sink Connect를 사용해 단일 DB에 저장 -> 데이터 동기화

### 메시지값을 Kafka Sink Connect를 이용해서 단일 데이터베이스로 전송
- Kafka topic에 메시지를 전달해주는 것이 `Source Connect`
- Topic에서 데이터를 **가져가서 사용**하는 것이 `Sink Connect`

각각의 Order Service가 가진 데이터를 제거하고 각각의 Order Service로부터 전달된 메시지 값을 메시지 큐잉 서버에 전달하게 되면<br>
메시지 서버가 가지고 있던 토픽의 데이터값을 `Sink Connect`에 의해서 **단일 데이터베이스로** 전달

## Orders Microservice 수정 - MariaDB
### Order 테이블 생성
```sql
create table orders (
  id int auto_increment primary key,
  user_id varchar(50) not null,
  product_id varchar(20) not null,
  order_id varchar(50) not null,
  qty int default 0,
  unit_price int default 0,
  total_price int default 0,
  created_at datetime default now()
)
```
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/1b3ec817-7574-4738-9c66-b240409253ca" width="40%"/><br>

<br>

### Orders Service의 JPA 데이터베이스 교체
- H2 DB -> Maria DB
```yaml
...
  datasource:
    url: jdbc:mariadb://localhost:3306/mydb
    driver-class-name: org.mariadb.jdbc.Driver
    username: root
    password: 1234
#    driver-class-name: org.h2.Driver
#    url: jdbc:h2:mem:testdb
```
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/cf46d880-e511-4d71-86db-43eb0cf1effc" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/823b1a9b-957f-42e4-b3ee-c931c3b12d7e" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/d3c40cfc-6830-44e2-9a6f-694720d0f623" width="100%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/626b856c-83fd-4831-b40a-990e559ea3e4" width="60%"/><br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/242c4bba-6176-4838-bf4b-03dafd5c354c" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/052c636a-c980-472e-aa07-4a620bb60d10" width="100%"/><br>


## Orders Microservice 수정 - Orders Kafka Topic
### Orders Service의 Controller 수정
```java
@RestController
@RequestMapping("/order-service")
@Slf4j
@RequiredArgsConstructor
public class OrderController {

    private final Environment env;
    private final OrderService orderService;
    private final KafkaProducer kafkaProducer;

    @GetMapping("/health_check")
    public String status() {
        return String.format("It's Working in Order Service on PORT %s",
                env.getProperty("local.server.port"));
    }

    @PostMapping("/{userId}/orders")
    public ResponseEntity<ResponseOrder> createOrder(@PathVariable("userId") String userId,
                                                     @RequestBody RequestOrder orderDetails) {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        OrderDto orderDto = mapper.map(orderDetails, OrderDto.class);
        orderDto.setUserId(userId);
        /* jpa */
//        OrderDto createdOrder = orderService.createOrder(orderDto);
//        ResponseOrder responseOrder = mapper.map(createdOrder, ResponseOrder.class);

        /* Send an order to the Kafka */
//        kafkaProducer.send("example-order-topic", orderDto);

        /* kafka */
        orderDto.setOrderId(UUID.randomUUID().toString());
        orderDto.setTotalPrice(orderDetails.getQty() * orderDetails.getUnitPrice());
        ResponseOrder responseOrder = mapper.map(orderDetails, ResponseOrder.class);

        kafkaProducer.send("example-category-topic", orderDto);
        orderProducer.send("orders", orderDto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseOrder); // 데이터베이스 저장 로직 제거 -> sink connector 사용하여 db 저장
    }
...
}
```
- `kafkaProducer.send("example-category-topic", orderDto);` : order와 catalog를 연동하기 위한 kafka producer
- `orderProducer.send("orders", orderDto);` : 사용자의 주문 정보를 kafka topic에 전달시키는 용도

### Orders Service에 객체 생성
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/052c636a-c980-472e-aa07-4a620bb60d10" width="100%"/><br>
- **기존에 가진 주문 정보를 어떻게 Topic에 보낼것인지**가 중요
- Topic에 쌓인 메시지들은 `Sink Connect`에 의해서 토픽의 메시지 내용들을 열어서 형태를 파악하고 해당하는 테이블에 저장된다.
- 데이터의 형태가 맞지 않으면 데이터베이스에 저장 실패
- schema: **테이블의 구조** 
    - field: 각각의 데이터베이스의 필드에 저장될 **값의 형태**
- payload: **실제 저장될 값**

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/90cb7271-ae1d-48b5-a8e9-dd952a549225" width="100%"/><br>
Schema, Field, Payload를 클래스로 만듦으로써 `ObjectMapper`와 같은 API를 이용해서 자바의 Object를 json으로 쉽게 변경이 가능해진다.

<br>

## Orders Microservice 수정 - Order Kafka Producer
### Orders Service의 OrderProducer 생성
```java
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderProducer {
    private KafkaTemplate<String, String> kafkaTemplate;

    List<Field> fields = Arrays.asList(new Field("String", true, "order_id"),
            new Field("string", true, "user_id"),
            new Field("string", true, "product_id"),
            new Field("int32", true, "qty"),
            new Field("int32", true, "total_price"),
            new Field("int32", true, "unit_price")
    );

    Schema schema = Schema.builder()
            .type("struct")
            .fields(fields)
            .optional(false)
            .name("orders")
            .build();

    public OrderDto send(String kafkaTopic, OrderDto orderDto) {
        Payload payload = Payload.builder()
                .order_id(orderDto.getOrderId())
                .user_id(orderDto.getUserId())
                .product_id(orderDto.getProductId())
                .qty(orderDto.getQty())
                .unit_price(orderDto.getUnitPrice())
                .total_price(orderDto.getTotalPrice())
                .build();

        KafkaOrderDto kafkaOrderDto = new KafkaOrderDto(schema, payload);

        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "";
        try {
            jsonInString = mapper.writeValueAsString(kafkaOrderDto);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        kafkaTemplate.send(kafkaTopic, jsonInString);
        log.info("Order Producer send data from the Order Microservice: " + kafkaTopic);

        return orderDto;
    }
}
```
- Schema는 불변이기 때문에 멤버 변수로 선언
- 실제로 변경되는 부분인 payload는 send 메서드에 지역변수로 선언

### Orders Service를 위한 Kafka Sink Connector 추가
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/63378752-0986-45f8-b99e-682d806a0e25" width="100%"/><br>
- topic 에 데이터가 추가되면 해당 데이터 값을 maria db에 update 하는 역할
```json
{
  "name":"my-order-sink-connect",
  "config":{
    "connector.class":"io.confluent.connect.jdbc.JdbcSinkConnector",
    "connection.url":"jdbc:mysql://localhost:3306/mydb",
    "connection.user":"root",
    "connection.password":"test1357",
    "auto.create":"true",
    "auto.evolve":"true",
    "delete.enabled":"false",
    "tasks.max":"1",
    "topics":"orders"
  }
}
```


### Orders Serivce 2개 기동
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/105ee595-077d-4780-824e-0abe88b0e9e3" width="100%"/><br>
#### 결과
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/0f676f6e-b5ed-4102-9c17-41734ecc3f3b" width="100%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/c78ae057-4506-4fba-91e1-08a45aab1e36" width="100%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/1d4e9585-2ede-4349-8fe6-afcd4ae585e8" width="100%"/><br>


이로써 Kafka Topic에 저장된 값을 단일 데이터베이스로 옮기기 위해서 Sink Connector를 연동 완료
> 이후에 Microservice를 확장해서 어플리케이션을 응용하고싶으면 데이터베이스에 저장되는 메시지 큐잉 서버를 Event Sourcing이라는 데이터를 저장하는<br>
> 부분과 읽어오는 부분을 분리해서 만드는 CQRS 패턴을 이용하면 좀 더 효율적으로 메시징기반의 시스템을 이용할 수 있으며 시간 순서에 의해서 메시지가<br>
> 기록된 것을 데이터베이스 업데이트하는 기능도 구현이 가능하다.

