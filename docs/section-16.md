# 애플리케이션 배포 - Docker Container

## Running Microservices in Local
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/3aec1520-5f7d-4bbe-9284-2a6839a2cbc3" width="60%"/><br>

## Create Bridge Network
### Docker Network
다양한 서비스들이 하나의 가상의 네트워크를 가짐으로써 서로 통신할 때 불편함이 없도록 설정

#### Bridge network
```shell
docker network create --driver bridge [브릿지 이름]
```
#### Host network
- 네트워크를 호스트로 설정하면 호스트의 네트워크 환경을 그대로 사용
- 포트 포워딩 없이 내부 애플리케이션 사용

#### None network
- 네트워크를 사용하지 않음
- io 네트워크만 사용, 외부와 단절

```shell
docker network create ecommerce-network
docker network ls
```

> 참고<br>
>`docker system prune`: stop container 삭제, 불필요한 네트워크 삭제, 사용되지 않는 이미지, 캐시 삭제

#### Docker network 생성
```shell
docker network create --gateway 172.18.0.1 --subnet 172.18.0.0/16 ecommerce-network
```

#### network 상세 조회
```shell
docker network inspect ecommerce-network
```

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/130f1f81-177d-4d5a-a836-b0feb9616598" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/54061824-7e66-49a7-b8e0-4ccef4711692" width="60%"/><br>

- 컨테이너에서 사용할 네트워크를 직접 생성하여 사용하면 좋은점은 일반적인 컨테이너는 하나의 guest os라고 생각하면 각각의 ip가 할당되는데<br> 
컨테이너들은 이러한 ip 주소를 통해 서로 통신을 하게 되는데 같은 네트워크에 포함된 컨테이너끼리는 ip 주소 외에도 컨테이너 id, **이름**을 통해서 통신이 가능해진다.

<br>

## RabbitMQ -> Docker Container
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/138ad432-14a1-44ee-83e2-8ed03d38a248" width="60%"/><br>

Configuration Service의 변경 내역을 모든 마이크로서비스에 한 번에 업데이트 시켜주기 위해서 Spring Cloud Bus를 이용했고, Spring Cloud Bus에서 사용할 수 있는 Message Queueing Server로써 RabbitMQ를 사용했다.

기존에 RabbitMQ를 Local 로 기동 -> Docker Container화

```shell
docker run -d --name rabbitmq --network ecommerce-network \
 -p 15672:15672 -p 5672:5672 -p 15671:15671 -p 5671:5671 -p 4369:4369 \
 -e RABBITMQ_DEFAULT_USER=guest \
 -e RABBITMQ_DEFAULT_PASS=guest rabbitmq:management
```

- --name: 네트워크에서 서로 통신 가능한 고유한 이름
- --network: 네트워크 지정, 네트워크를 설정하지 않으면 기본적으로 도커가 가진 bridge 네트워크를 가져와서 사용

#### 결과
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/59681fd3-4a99-481a-b4bd-a78986549f54" width="80%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/91349ffe-3054-43dc-8aa7-bfadce8323f7" width="90%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/3677c9e2-59bf-4787-99dc-b5ab2f72d819" width="60%"/><br>
- 네트워크에 rabbitmq container가 추가됨을 볼 수 있다.

<br>

## Configuration Service
- Docker Image로 변환하는 작업이 선행되어야 한다.
- Configuration Server에 포함된 암호화에 필요한 key를 복사해야 한다.
- 또한 사용되는 keyfile의 위치를 변화해야 한다.

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/61481063-05ad-4097-841d-b632a713483f" width="60%"/><br>

```yaml
encrypt:
  #  key: abcdefghijklmnopqrstuvwxyz0123456789
  key-store: # 비대칭키 암호화
    #    location: file:///Users/choihyewon/Desktop/Work/keystore/apiEncryptionKey.jks
    location: file:/apiEncryptionKey.jks
    password: won1234
    alias: apiEncryptionKey
```

#### Dockerfile 생성
```dockerfile
FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY apiEncryptionKey.jks apiEncryptionKey.jks
COPY target/config-service-1.0.jar config-service.jar
ENTRYPOINT ["java", "-jar", "config-service.jar"]
```

#### Image 빌드
```shell
docker build --tag won1110218/config-service:1.0 .
```
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/52adb2d1-1065-43a5-8aee-7e9534bfd755" width="60%"/><br>

#### docker hub에 push
```shell
docker push won1110218/config-service:1.0
```

#### Config service 실행
```shell
docker run -d -p 8888:8888 --network ecommerce-network \
 -e "spring.rabbitmq.host=rabbitmq" \
 -e "spring.profiles.active=default" \
  --name config-service won1110218/config-service:1.0
```
- 현재 설정 파일(`application.yml`)에 host가 127.0.0.1로 기입되어 있는데 실행할 때 설정을 주입
- 컨데이터 이름 rabbitmq 를 쓰게 되면 나중에 변경사항 없이 편하게 쓸 수 있다.

#### 결과
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/30c698d8-7cad-43e9-8c98-d0c94eac214f" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/f605ac42-8d76-4e4d-b3e7-12bc75f01b0a" width="60%"/><br>
- 네트워크에 2개의 컨테이너가 등록됨을 확인할 수 있다.

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/800688a8-6b88-467a-b3de-e566009438bc" width="60%"/><br>

<br>

## Discovery Service
#### Dockerfile 생성
```dockerfile
FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY target/ecommerce-1.0.jar DiscoveryService.jar
ENTRYPOINT ["java", "-jar", "DiscoveryService.jar"]
```

#### Docker Image 생성
```shell
docker build --tag won1110218/discovery-service:1.0 .
```

#### docker hub에 push
```shell
docker push won1110218/discovery-service:1.0
```
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/4615dd89-9bc5-4434-8de2-5bae89a34f89" width="60%"/><br>

#### discovery service 실행
```shell
docker run -d -p 8761:8761 --network ecommerce-network \
 -e "spring.cloud.config.uri=http://config-service:8888" \
 --name discovery-service won1110218/discovery-service:1.0
```

#### 결과
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/626e94a5-c9e7-4473-be0e-a0f47ecd525f" width="60%"/><br>
- ecommerce-network에 등록된 모습

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/f5e27c19-9783-4265-9c12-d0ba4034575a" width="60%"/><br>

<br>

## ApiGateway Service
#### Dockerfile 생성
```shell
FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY target/apigateway-service-1.0.jar apigateway-service.jar
ENTRYPOINT ["java", "-jar", "apigateway-service.jar"]
```

#### Docker Image 생성
```shell
docker build --tag won1110218/apigateway-service:1.0 .
```

#### docker hub에 push
```shell
docker push won1110218/apigateway-service:1.0
```

#### apigateway service 실행
```shell
docker run -d -p 8000:8000 --network ecommerce-network \
 -e "spring.cloud.config.uri=http://config-service:8888" \
 -e "spring.rabbitmq.host=rabbitmq" \
 -e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/" \
 --name apigateway-service \
 won1110218/apigateway-service:1.0
```
- 3가지 설정 정보(spring.cloud.config.uri, spring.rabbitmq.host, eureka.client.serviceUrl.defaultZone) 변경하여 실행

#### 결과

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/bd100fd2-354a-439a-8020-dc95dac033bf" width="60%"/><br>
- ecommerce-network에 등록된 모습

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/99b4c300-c471-4996-80a9-c90791afc875" width="60%"/><br>


<br>

## MariaDB
#### Dockerfile 생성
```shell
FROM mariadb
ENV MYSQL_ROOT_PASSWORD 1234
ENV MYSQL_DATABASE mydb
COPY ./mysql_data/mysql /var/lib/mysql
EXPOSE 3306
CMD ["--user=root"]
#ENTRYPOINT ["mysqld"]
```
- 데이터베이스를 만들때 테이블에 대한 정보가 있다면 script로 만들 수도 있고, 로컬에서 mariadb를 기동하면서 만들어둔 테이블을 컨테이너 안으로 복사할 수도 있다.
- 기존에 로컬에서 만든 db정보를 컨테이너 내부(/var/lib/mysql)에 copy

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/bd766fea-9105-4b40-bbdc-0e830bef58eb" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/87461d47-c750-406f-9dd2-07b07ab6933e" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/fc2cd79a-e0e2-490a-a31e-0d7987f12342" width="30%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/e3c2199b-5bf4-438a-bd8e-6c28e8885135" width="30%"/><br>



<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/99b4c300-c471-4996-80a9-c90791afc875" width="60%"/><br>




#### Docker Image 생성
```shell
docker build -t won1110218/my_mariadb:1.0 .
```

#### mariadb 실행
```shell
docker run -d -p 3306:3306 --network ecommerce-network --name mariadb won1110218/my_mariadb:1.0
```

#### mariadb 접속
```shell
docker exec -it mariadb /bin/bash
mysql -h127.0.0.1 -uroot -p
```
```shell
docker exec -it mariadb /bin/bash
mariadb -hlocalhost -uroot -p
```
root 권한 허용 root 계정에 어떠한 ip 주소로 접속된다고 하더라도 모든 데이터베이스에 허용할 수 있도록

```shell
grant all privileges on *.* to 'root'@'%' identified by [password];
flush privileges;
```

#### 결과
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/a049185b-00a0-480b-b8a8-9ec6533e9011" width="60%"/><br>
- ecommerce-network에 등록된 모습

<br>

## Kafka
Kafka를 사용하기 위해서는 Zookeeper와 Kafka Server라고 불리는 Kafka Broker가 필요하다.

#### Zookeeper + Kafka Standalone
- docker-compose로 실행
- git clone https://github.com/wurstmeister/kafka-docker
- docker-compose-single-broker.yml 수정

> docker compose?
> 실행하려는 도커 컨테이너를 하나의 스크립트 파일로 실행할 수 있도록 만들어주는것

`docker-compose-single-broker.yml`

```yaml
version: '2'
services:
  zookeeper:
    image: wurstmeister/zookeeper
    ports:
      - "2181:2181"
    networks:
      my-network:
        ipv4_address: 172.18.0.100
  kafka:
    # build: .
    image: wurstmeister/kafka
    ports:
      - "9092:9092"
    environment:
      KAFKA_ADVERTISED_HOST_NAME: 172.18.0.101
      KAFKA_CREATE_TOPICS: "test:1:1"
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
    depends_on:
      - zookeeper
    networks:
      my-network:
        ipv4_address: 172.18.0.101

networks:
  my-network:
    external: true
    name: ecommerce-network # 172.18.0.1~
```

#### docker compose 실행
```shell
docker-compose -f docker-compose-single-broker.yml up -d
```
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/904ee074-8a35-4272-b6c4-b449cf01b66b" width="50%"/><br>

#### docker compose 종료
```shell
docker-compose -f docker-compose-single-broker.yml down -d
```

#### 결과
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/599d3677-5528-4c41-b683-a0227eae464a" width="70%"/><br>
- zookeeper와 kafka가 컨테이너로 실행됨을 확인

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/e24ac59a-cd15-45bd-a9e2-534366b7fbf1" width="60%"/><br>
- 네트워크에도 compose에 정의한 대로 ip 할당

<br>

## Zipkin
#### docker 실행
```shell
docker run -d -p 9411:9411 \
 --network ecommerce-network \
 --name zipkin \
 openzipkin/zipkin 
```

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/ce865a11-3b99-4a71-b48b-7c55c3dd585f" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/c8ae678b-0535-4a77-8c2c-0ea088473239" width="60%"/><br>

<br>

## Monitoring
Run Prometheus + Grafana
#### Prometheus
```shell
docker run -d -p 9090:9090 \
 --network ecommerce-network \
 --name prometheus \
 -v /Users/choihyewon/Desktop/Work/prometheus-2.51.0.darwin-amd64/prometheus.yml:/etc/prometheus/prometheus.yml \
 prom/prometheus 
```
- 로컬에 존재하는 prometheus.yml을 컨테이너 내부로 복사

#### prometheus.yml 수정
```yaml
...

    static_configs:
    - targets: ["prometheus:9090"]
  - job_name: "users-service"
    scrape_interval: 15s
    metrics_path: "/users-service/actuator/prometheus"
    static_configs:
    - targets: ["apigateway-service:8000"]
  - job_name: "orders-service"
    scrape_interval: 15s
    metrics_path: "orders-service/actuator/prometheus"
    static_configs:
    - targets: ["apigateway-service:8000"]
  - job_name: "apigateway-service"
    scrape_interval: 15s
    metrics_path: "/actuator/prometheus"
    static_configs:
    - targets: ["apigateway-service:8000"]
```
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/a4a68da1-edd9-43e1-a5c5-3e9f7db4e1d3" width="60%"/><br>


### Grafana
```shell
docker run -d -p 3000:3000 \
 --network ecommerce-network \
 --name grafana \
 grafana/grafana 
```
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/cdecc38b-2905-4c8b-a3cf-db5c08c6280a" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/f2289921-0523-401e-a868-8479ce1dd77d" width="60%"/><br>


#### 현재상황
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/7805830b-87b5-4ae0-a340-d0580a19dd17" width="70%"/><br>

<br>

## User Microservice
```dockerfile
FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY target/users-service-1.0.jar users-service.jar
ENTRYPOINT ["java", "-jar", "users-service.jar"]
```
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/bdaf2587-10ce-417d-8de5-d2301677b2c4" width="70%"/><br>

#### https://github.com/hyewon218/spring-cloud-config 의 users-service.yml 수정
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/9826b3c4-e915-43b2-bf1c-582c216504a4" width="70%"/><br>


<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/7abcea47-b17b-4c46-be2c-cdd0b6dc3c9a" width="30%"/><br>
- docker build -t won1110218/users-service:1.0 .
- docker push won1110218/users-service:1.0

```shell
docker run -d --network ecommerce-network \
  --name users-service \
 -e "spring.cloud.config.uri=http://config-service:8888" \
 -e "spring.rabbitmq.host=rabbitmq" \
 -e "spring.zipkin.base-url=http://zipkin:9411/api/v2/spans" \
 -e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/" \
 -e "logging.file=/api-logs/users-ws.log" \
 won1110218/users-service:1.0
```
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/4a12274a-f2df-4ad4-b4f1-4e9bc3fa86fd" width="50%"/><br>

<br>

## Order Microservice
```dockerfile
FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY /target/orders-service-1.0.jar orders-service.jar
ENTRYPOINT ["java", "-jar", "orders-service.jar"]
```
```shell
docker run -d --network ecommerce-network \
  --name orders-service \
 -e "spring.cloud.config.uri=http://config-server:8888" \
 -e "spring.rabbitmq.host=rabbitmq" \
 -e "management.zipkin.tracing.endpoint=http://zipkin:9411/api/v2/spans" \
 -e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/" \
 -e "spring.datasource.url=jdbc:mariadb://mariadb:3306/mydb" \
 -e "logging.file=/api-logs/orders-ws.log" \
 won1110218/orders-service:1.0
```

### KafkaProducerConfig - 메시지 보낼때 사용하는 설정(ip) 변경
```java
@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.18.0.101:9092");
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

#### mariadb 모든 ip 허용


<br>

## Catalog Service
#### Dockerfile
```dockerfile
FROM openjdk:17-ea-11-jdk-slim
VOLUME /tmp
COPY /target/catalogs-service-1.0.jar catalogs-service.jar
ENTRYPOINT ["java", "-jar", "catalogs-service.jar"]
```

```shell
docker run -d --network ecommerce-network \
  --name catalogs-service \
 -e "spring.cloud.config.uri=http://config-service:8888" \
 -e "spring.rabbitmq.host=rabbitmq" \
 -e "eureka.client.serviceUrl.defaultZone=http://discovery-service:8761/eureka/" \
 -e "logging.file=/api-logs/catalogs-ws.log" \
 won1110218/catalogs-service:1.0
```

#### KafkaConsumerConfig
```java
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> properties = new HashMap<>();

        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "172.18.0.101:9092"); // Kafka 주소
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

#### Multi Profiles
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/bd3c7cc5-c198-4520-97bc-b143eef880fe" width="60%"/><br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/efbaba50-3267-44b7-a631-0220c2d3b723" width="60%"/><br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/ddd1d02c-24c7-45c9-80b6-27f0047aa983" width="60%"/><br>
