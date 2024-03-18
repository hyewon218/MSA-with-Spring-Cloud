# 데이터 동기화를 위한 Apache Kafka 활용 - 1
## Apache Kafka 개요
- Apache software Foundation의 Scalar 언어로 된 오픈 소스 메시지 브로커 프로젝트
- 실시간 데이터 피드를 관리하기 위해 통일된 높은 처리량, 낮은 지연 시간을 지닌 플랫폼 제공

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/ae463a69-aa7a-4a3e-be73-389fd07ca5fb" width="80%"/><br>
- 각각의 DB는 다양한 서비스에 데이터를 End-to-End 방식으로 전달한다고 가정하면, 서로 다른 데이터 파이프라인 연결 구조를 가졌기 때문에<br>
  Mysql에서 전달해줄 수 있는 시스템을 Oracle or mongodb에서 사용할 수 없다는 단점을 가진다.
- 따라서 확장이 어려운 구조

## kafka 탄생 배경
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/810e7924-fc29-43f3-b296-bba859866273" width="80%"/><br>
- 모든 시스템으로 데이터를 실시간으로 전송하여 처리할 수 있는 시스템
- 데이터가 많아지더라도 확장이 용이한 시스템

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/eb102bb9-090d-49df-ab2f-17dc56d5002b" width="80%"/><br>
- Kafka의 등장으로 각각의 DB는 자신들이 전송하는 데이터가 어떠한 시스템으로 전달되는지 상관하지 않고 Kafka에만 보내면 된다.
<br> 누가 보내고 누가 받는지 신경 X
- 또한 각각의 서비스에서도 DB를 상관하지않고 Kafka에서만 데이터를 받아오면 되기때문에 **단일 format을 유지할 수 있다**.

## Kafka Broker
### 카프카의 서버
- 실행된 Kafka 애플리케이션 **서버**
- **3대 이상**의 Broker Cluster 구성
- Zookeeper 연동
    - 역할: 메타데이터(Broker ID, Controller ID 등) 저장
    - Controller 정보 저장
- n개 Broker 중 1대는 Controller 기능 수행
    - Controller 역할(leader)
        - 각 Broker에게 담당 파티션 할당 수행
        - Broker 정상 동작 모니터링 관리

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/c728ac00-4591-46d4-9002-bcaf6814b0e6" width="70%"/><br>
- 3개 이상의 Broker와 클러스터 구조를 가지는 것을 권장한다.
- 여러 개의 브로커들이 서로 밀접하게 연결되면서 한 곳에 저장된 메시지를 다른 곳에 공유해줌으로써 하나의 Broker가 문제가 생기면 대신할 수 있는 Broker를 둔다.
- 서버의 상태, 장애 체크, 복구를 해주는 코디네이터 시스템과 연동하는데 Kafka에서 사용하는 코디네이터가 Zookeeper

<br>

## Apache Kafka 설치
#### Kafka 홈페이지
- http://kafka.apache.org

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/55074d3f-d2f7-4585-8fe3-576634022ad4" width="70%"/><br>

#### Zookeeper 및 Kafka 서버 기동
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/c8daeedf-8f65-4b46-aa7a-0b153f67b592" width="100%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/351dd701-f9fc-4d1f-a119-3ad3a4e00792" width="100%"/><br>


<br>

## Apache Kafka 사용 - Producer/Consumer
1. 카프카에 메시지를 보내고 카프카가 메시지를 저장하고 있다가 다른 Consumer에게 메시지를 전달해주는 시나리오
2. 데이터베이스의 자료가 변경되었을 때(INSERT, UPDATE) 데이터베이스로부터 카프카가 변경된 데이터에 대한 메시지를 가져오고<br>
   값을 다른 쪽의 데이터베이스, 스토리지, 서비스에 전달해주는 카프카 커넥트 기능 시나리오

<br>

### Ecosystem(시나리오) 1 - Kafka Client
- Kafka와 데이터를 주고받기 위해 사용하는 Java 라이브러리
    - https://mvnrepository.com/artifact/org.apache.kafka/kafka-clients
- Producer, Consumer, Admin, Stream 등 Kafka관련 API 제공
- 다양한 3rd party library 존재: C/C++, Node.js, Python, .NET 등
    - https://cwiki.apache.org/confluence/display/KAFKA/Clients

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/e377d33b-750d-4582-bea1-247c38ba3ce7" width="80%"/><br>
필요한 메시지를 각 서비스끼리 End-to-End 방식으로 전달하는게 아니라 가운데 Kafka라는 클러스터링 시스템을 두고, <br>
보내는 쪽에서는 카프카에 데이터를 보내고 받는 쪽에서도 Kafka를 통해서 받음으로써 누가 메시지를 보내고 누구에게 메시지를 보내는지에 대한 **의존성을 제거**할 수 있다.

### Kafka 서버 기동
#### Zookeeper 및 Kafka 서버 기동

```shell
/Users/choihyewon/Desktop/Work/kafka_2.13-3.6.1/bin/zookeeper-server-start.sh /Users/choihyewon/Desktop/Work/kafka_2.13-3.6.1/config/zookeeper.properties
/Users/choihyewon/Desktop/Work/kafka_2.13-3.6.1/bin/kafka-server-start.sh /Users/choihyewon/Desktop/Work/kafka_2.13-3.6.1/config/server.properties
````
```shell
./bin/zookeeper-server-start.sh ./config/zookeeper.properties
./bin/kafka-server-start.sh ./config/server.properties
````

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/d685d1a7-133f-405a-9ec8-8576221d78c3" width="60%"/><br>


#### Topic 생성
```shell
./bin/kafka-topics.sh --create --topic quickstart-events --bootstrap-server localhost:9092 --partitions 1
```
- kafka로 producer가 메시지를 보내게 되면 데이터는 `Topic`에 저장된다.
- Topic에 관심이 있는 consumer는 해당 **Topic을 등록**하게 된다.
- Topic에 전달된 내용물이 있을 경우에 해당 Topic에 전달된 메시지를 **Topic을 등록한 Consumer들에게 일괄적으로 전달**해주는 방식
    - --topic {토픽 이름}
    - --bootstrap-server {카프카 서버 주소} : 해당 주소에 토픽을 생성
    - --partition 1 : 멀티 클러스터링 구조를 구성했을 때 토픽에 전달된 메시지를 몇군데 나눠서 저장할지 정하는 옵션

#### Topic 목록 확인
```shell
./bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```

#### Topic 정보 확인
```shell
./bin/kafka-topics.sh --describe --topic quickstart-events --bootstrap-server localhost:9092
````
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/44eb03df-359b-4e39-96d2-87aef064a7a9" width="100%"/><br>

<br>


### Kafka Producer/Consumer 테스트
콘솔창 4개
1. zookeeper-server 실행
2. kafka-server 실행<br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/7bd1d537-71de-43dd-b4da-780d33ff0e28" width="100%"/><br>

3. producer
4. consumer<br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/0e3acc6e-6b9a-4178-8413-e0e40845b308" width="100%"/><br>
   <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/d530240f-f1e8-466a-8232-d702c72427c5" width="100%"/><br>
#### 메시지 생산(보내는 쪽)
```shell
./bin/kafka-console-producer.sh --broker-list localhost:9092 --topic quickstart-events
```
- --broker-list {보낼 서버 주소}
- --topic {토픽 이름}

#### 메시지 소비(받는 쪽)
```shell
./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic quickstart-events --from-beginning
```
- --bootstrap-server {받을 서버 주소}
- --topic {토픽 이름}
- --from-beginning : 처음부터 받아옴

- 동시에 여러 consumer가 있다고 하더라도 producer가 보냈던 메세지를 같이 처리할 수 있는 시스템으로 사용할 수 있다.

<br>

## Apache Kafka 사용 - Kafka Connect
### Ecosystem(시나리오) 2 - Kafka Connect
특별하게 프로그래밍없이 Configuration만 가지고 데이터를 특정한 곳에서 받아와서 다른 쪽으로 이동시켜주는 기능

- Kafka Connect를 통해 Data를 Import/Export 가능
- 코드 없이 Configuration으로 데이터를 이동
- Standalone mode, Distribution mode 지원
    - **RESTful API 통해 지원**
    - **Stream 또는 Batch 형태로 데이터 전송 가능**
    - **커스텀 Connector를 통한 다양한 Plugin 제공 (File, S3, Hive, Mysql, etc ...)**
- 파일로부터 데이터를 받아와서(import) 파일로 데이터를 전송(export) / 데이터베이스로부터 데이터를 받아와서 파일로 데이터 전송

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/7479a4df-194e-4ad8-aceb-74741033ad76" width="100%"/><br>
- Kafka Connect Source: 데이터를 가져오는 쪽
- Kafka Connect Sink: 데이터를 보내는 쪽

<br>

### MariaDB 설치
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/271e9e5f-6602-40f0-9e4a-b5eff6c20356" width="70%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/5772d1d0-9de1-4757-8d5b-1424259dff5e" width="80%"/><br>

## Orders Microservice에서 MariaDB 연동
### 라이브러리 추가
- mariadb-java-client

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/a8c9009c-d99e-4499-87db-c37be447e1a7" width="70%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/271e9e5f-6602-40f0-9e4a-b5eff6c20356" width="70%"/><br>

### 테이블 생성
```sql
create table users(
    id int auto_increment primary key,
    user_id varchar(20),
    pwd varchar(20),
    name varchar(20),
    created_at datetime default NOW()
);
```

```sql
create table orders (
    id int auto_increment primary key,
    product_id varchar(20) not null,
    qty int default 0,
    unit_price int default 0,
    total_price int default 0,
    user_id varchar(50) not null,
    order_id varchar(50) not null,
    created_at datetime default NOW()
);
```

