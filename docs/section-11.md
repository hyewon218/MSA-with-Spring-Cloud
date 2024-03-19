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
- Enter password: **test1357**
- use mydb;


## Orders Microservice에서 MariaDB 연동
### 라이브러리 추가
- mariadb-java-client

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/a8c9009c-d99e-4499-87db-c37be447e1a7" width="70%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/5dbf4b70-ffb3-4d1d-919a-18a8a217d572" width="60%"/><br>
- driver-class-name: org.mariadb.jdbc.Driver
- url: jdbc:mariadb://localhost:3306/mydb
- username: root
- password: test1357


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
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/7eecbf69-7233-4d67-8d91-baf0db322e7b" width="30%"/><br>

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

## Kafka Connect 설치 ①
- curl -O http://packages.confluent.io/archive/6.1/confluent-community-6.1.0.tar.gz
- tar xvf confluent-community-6.1.0.tar.gz
- cd /Users/choihyewon/Desktop/Work/kafka_2.13-3.6.1

### Kafka Connect 설정 (기본으로 사용)
- /Users/choihyewon/Desktop/Work/kafka_2.13-3.6.1/config/connect-distributed.properties

### Topic 목록 확인
./bin/kafka-topics.sh --bootstrap-server localhost:9092 -list
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/12adb8b6-7776-48e0-b93c-d3ae4e1ab0af" width="80%"/><br>


### Kafka Connect 실행 (confluent-6.1.0)
./bin/connect-distributed ./etc/kafka/connect-distributed.properties<br>

- Topic 목록 확인<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/0f9a3a98-e712-4ccc-a737-4502f7027b92" width="80%"/><br>
- connect-configs, connect-offsets, connect-status 추가된 모습


### JDBC Connector 설치
- https://www.confluent.io/hub/confluentinc/kafka-connect-jdbc
- confluentinc-kafka-connect-jdbc-10.7.6.zip

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/6e54283b-36f3-4640-90c1-6d6342322951" width="80%"/><br>

카프카 커넥트를 통해서 데이터를 한쪽에서 읽어와서 다른쪽으로 전달하기 위해서는 사용하고자하는 타겟에 맞는 JDBC Connector가 필요하다.<br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/5e564bd2-87b5-4374-8e1e-a7c030446f46" width="80%"/><br>


### etc/kafka/connect-distributed.properties 파일 마지막에 아래 plugin 정보 추가 (confluent-6.1.0)<br>
- plugin.path=/Users/choihyewon/Desktop/Work/confluentinc-kafka-connect-jdbc-10.7.6/lib

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/1d6bf5c0-5e74-4e73-9f04-2840ebe41ef9" width="100%"/><br>

카프카 커넥트의 설정파일인 connect-distributed.properties 에서 커넥터를 추가연동하기 위해서는 파일을 변경해야 한다.<br>

### JdbcSourceConnector에서 MariaDB 사용하기 위해 mariadb 드라이버 복사 
./share/java/kafka/ 폴더에 mariadb-java-client-2.7.2.jar 파일 복사

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/90b0690a-7ac4-4973-97a8-939e84edbb6f" width="80%"/><br>
- ./share/java/kafka/ 폴더

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/90b0690a-7ac4-4973-97a8-939e84edbb6f" width="80%"/><br>
- mariadb-java-client-2.7.2.jar 파일

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/02ac7324-822c-4581-919f-2fdbf4c39824" width="100%"/><br>
- 복사

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/22b427c8-d8c9-47a1-90d0-0fe8c1b7fb35" width="50%"/><br>

<br>

## Kafka Source Connect 사용
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/8da3952a-4c29-429d-8a0c-1cac3744918f" width="80%"/><br>
- 먼저 Kafka Connect 실행

### Kafka Source Connect 추가 (MariaDB)

```json
{
    "name" : "my-source-connect",
    "config" : {
        "connector.class" : "io.confluent.connect.jdbc.JdbcSourceConnector",
        "connection.url":"jdbc:mysql://localhost:3306/mydb",
        "connection.user":"root",
        "connection.password":"test1357",
        "mode": "incrementing",
        "incrementing.column.name" : "id",
        "table.whitelist":"users",
        "topic.prefix" : "my_topic_",
        "tasks.max" : "1"
    }
}
```
curl -X POST -d @- http://localhost:8083/connectors --header "content-Type:application/json"
- name: 커넥트이름
- mode: incrementing - 데이터가 등록되면서 데이터를 자동으로 증가시키는 모드
- incrementing.column.name: 자동으로 증가될 컬럼
- table.whitelist: 데이터베이스에 특정한 값을 저장하면 데이터베이스를 체크하고있다가 변경사항이 생기면 가져와서 토픽에 저장한다. 해당 작업 시에 whitelist의 테이블을 체크하는 것
- topic.prefix: 감지 내용을 저장할 위치 -> my_topic_users

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/2e0bfdab-a443-479f-8d52-6f494315ef9d" width="80%"/><br>

#### kafka Connect 목록 확인
curl http://localhost:8083/connectors | jq
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/582f2931-7c3c-4873-bcb1-2924be7422ab" width="80%"/><br>
#### kafka Connect 확인
curl http://localhost:8083/connectors/my-source-connect/status | jq
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/0ab1e689-0099-400e-8fb9-6fe329389adc" width="80%"/><br>


### mydb 변동사항 생성 - insert
```sql
insert into users(user_id, pwd, name) values('user1', 'test1111', 'User name');
```


<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/c00987da-2e8e-4a82-aa82-e4229e103a4d" width="80%"/><br>


<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/002ebecd-d23d-4ea0-b413-0130511c24a9" width="60%"/><br>
- 토픽이 생성된 모습 (my_topic_users)<br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/6c10b1c0-b1a5-4179-bdaa-2933e188f1b6" width="100%"/><br>
- Consumer를 통해 topic에 들어온 json정보를 확인할 수 있다.

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/0385bacf-5405-4d50-8da6-6e4aa052ae54" width="100%"/><br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/11dfbbc7-542d-4b64-be4e-2971cc1b0ad0" width="80%"/><br>
- topic 을 이용해 database 에 자료를 저장하고 싶다면 이런 형식으로 전달을 해야 한다.
- schema : 데이터 구조

<br>

## Kafka Sink Connect 사용
Kafka Connect에 아래 내용 전달
```json
{
  "name":"my-sink-connect",
  "config":{
    "connector.class":"io.confluent.connect.jdbc.JdbcSinkConnector",
    "connection.url":"jdbc:mysql://localhost:3306/mydb",
    "connection.user":"root",
    "connection.password":"test1357",
    "auto.create":"true",
    "auto.evolve":"true",
    "delete.enabled":"false",
    "tasks.max":"1",
    "topics":"my_topic_users"
  }
}
```
curl -X POST -d @- http://localhost:8083/connectors --header "content-Type:application/json"

- 싱크 커넥트는 토픽에서 데이터를 가져와서 사용하는 **사용처**
- topics의 value가 사용처가 된다. 따라서 현재 설정은 mydb에 `my_topic_users`라는 테이블이 생성된다.
- `auto.create`: **토픽과 같은 이름의 테이블을 생성**해주겠다는 옵션
- 스키마는 토픽에 저장된 메시지를 바탕으로 결정된다.

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/313fe3cb-8e90-4438-b932-c5d436e28aaa" width="80%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/d4b88f74-51be-4938-bf27-0579f60256f4" width="80%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/597acf10-1fa2-44f2-b3a4-7aee7887cf41" width="30%"/><br>


<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/710ba514-f2c3-4d53-bff3-7738111f823a" width="80%"/><br>
- 데이터를 insert하면? 새로운 테이블이 생성됨을 확인할 수 있다.

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/04d290ba-1d37-4a9a-a942-b3d0a63b392f" width="80%"/><br>
- 데이터를 insert 하면 `users` 테이블 + `my_topic_users` 까지 데이터가 저장된다. -> source connect 를 통해 전달된 데이터가 sink connect 에 의해 연결된 테이블에 저장된다.

<br>

### kafka producer를 이용해서 Kafka Topic에 데이터 직접 전송
- kafka-console-producer에서 데이터 전송 -> Topic에 추가 -> MariaDB에 추가<br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/a826e316-c67d-487c-ab20-9655cface40f" width="80%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/ae4bdb6c-1720-4311-b31d-59a581e35416" width="80%"/><br>


<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/9f91350a-6493-4220-9e64-11f22da78575" width="80%"/><br>
- users 테이블 : 자신이 가진 데이터를 topic 에 넣는 
- my_topic_users 테이블 : sink connect 와 연결되어 있는 