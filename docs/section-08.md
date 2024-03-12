# Spring Cloud Bus
각각의 Microservice 가 좀 더 효율적으로 변경된 사항을 가져갈 수 있는 Spring Cloud Bus

## Spring Cloud Bus 개요
이전에 사용한 Actuator 를 사용하는 방법은 만약 수십, 수백개의 application 이 존재한다고 가정하면 각각의 어플리케이션마다 수동으로 refresh 를 호출해야 한다.<br>
이러한 문제점을 해결할 수 있는 것이 Spring Cloud Bus

- 분산 시스템의 노드(Microservice)를 경량 메시지 브로커(RabbitMQ)와 연결
- 상태 및 구성에 대한 변경 사항을 연결된 노드에게 전달(Broadcast)

기존에는 하나의 어플리케이션에서 다른 어플리케이션을 연동하는 방식은 p2p 방식 직접적으로 이뤄졌다.<br>
중간에 요청사항을 가진 미들웨어(Messaging Server, RabbitMQ)를 배치함으로써 좀 더 안정적이고 보내는 쪽과 받는 쪽이 서로에게 신경쓰지 않을 수 있다.<br>


<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/cbc7df30-9054-40df-bd6a-c445979ab3f6" width="100%"/><br>

Spring Cloud Bus 에 연결된 다양한 Microservice 에 데이터의 갱신을 push 방식으로 전달하는데, AMQP 라는 프로토콜을 사용한다.<br>

### AMQP
- 메시지 지향, 큐잉, 라우팅(P2P), 신뢰성, 보안
- Erlang, **RabbitMQ**에서 사용

### Kafka 프로젝트
- Apache Software Foundation이 scalar 언어로 개발한 오픈 소스 메시지 브로커 프로젝트
- 분산형 스트리밍 플랫폼
- 대용량의 데이터를 처리 가능한 메시징 시스템

### RabbitMQ vs. Kafka

#### RabbitMQ -> 보다 적은 데이터를 안전하게 전달함을 보장시키기 위한 솔루션
- 메시지 브로커
- 초당 20+ 메시지를 소비자(메시지를 받고자하는 시스템)에게 전달
- 메시지 전달 보장, 시스템 간 메시지 전달
- 브로커, 소비자 중심

#### Kafka -> 대용량 데이터를 빠른 시간내에 처리하기 위한 솔루션
- 초당 100k+ 이상의 이벤트 처리
- Pub/Sub, Topic(저장소)에 메시지 전달
- Ack를 기다리지 않고 전달 가능
- 생산자(보낸이) 중심

Config Server 에서 Repository 를 통해 변경된 데이터를 가져오면 이전에는 각각의 Microservice 에서 refresh 를 호출하는 방식

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/425d8234-38f3-4f62-baeb-b92a71e4a33b" width="100%"/><br>
Cloud Bus Server 에 연결된 각각의 Microservice 는 외부에서 POST 방식으로 `/busrefresh` 를 Spring Cloud Bus 에 연결된 <br>
아무 Microservice 에게 호출하면 호출받은 Microservice 는 Spring Cloud Bus 에게 알려주고 Bus 와 연결된 또 다른 Microservice 에 전달한다.

<br>

### RabbitMQ 설치 및 실행
```shell
$ brew update

$ brew install rabbitmq

$ rabbitmq-server

# 설치 시 python 관련 오류(위 이미지 참조) 발생 하면, 아래 커맨드 실행
$ xcode-select --install
```

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/5924b7da-d689-41e6-8779-7743d7d2f5c1" width="100%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/185c72de-95b8-4454-82cb-f60f6ace3394" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/48fa6d1a-5d11-4d6d-ba19-4ee41352e99a" width="100%"/><br>


<br>

### Config Server
라이브러리 추가
- AMQP for Spring Cloud Bus, Actuator

### Users Microservice, Gateway Service
라이브러리 추가
- AMQP for Spring Cloud Bus

### Config Server, Users Microservice, Gateway Service
application.yml 수정
```yaml
spring:
  application:
    name: config-service
  rabbitmq:
    host: 127.0.0.1
    port: 5672
    username: guest
    password: guest
...

management:
  endpoint:
    web:
      exposure:
        include: refresh, health, beans, httptrace, busrefresh
```
- RabbitMQ를 웹 브라우저에서 접속할 때의 port는 15672, 시스템에서 amqp 프로토콜을 사용할때는 5672 port를 사용한다.

#### local git repo 에 application.yml 추가
```yaml
token:
  expiration_time: 86400000 # 만료 기간 -> 하루
  secret: user_token_native_application # 토큰 키
gateway:
  ip: 127.0.0.1
```


#### Remote git repository 에서 yml 수정
```yaml
token:
  expiration_time: 86400000 # 만료 기간 -> 하루
  secret: user_token_native_application_changed_#1 # 토큰 키
gateway:
  ip: 127.0.0.1
```
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/ed568f4d-3e4d-48e9-be13-cbfb89c935be" width="70%"/><br>
- 
- user service, apigateway service 같은 secret key 사용하여 오류 x <br>

#### 업데이트 전(127.0.0.1:8000/user-service/actuator/busrefresh x)
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/5924b7da-d689-41e6-8779-7743d7d2f5c1" width="60%"/><br>

#### 업데이트 후(127.0.0.1:8000/user-service/actuator/busrefresh o)
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/c9141159-83a0-4a35-8ee7-d50531bea392" width="100%"/><br>
#### UserServiceApplication
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/0fe3f607-8032-4191-8909-d8fb321c8fa1" width="100%"/><br>
#### ApigatewayServiceApplication
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/319e34ec-423c-45e7-9297-9e1a5a371409" width="100%"/><br>
- user micreservice에 변경됨을 알려주면 rebbitMQ에 연결되어 있는 다른 모든 곳에 해당하는 메세지가 다 push 기능으로 전달된다.


<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/e17e83a6-2b5d-4866-badc-f3bfee8aa1dd" width="100%"/><br>
- 토큰이 변경되어 인증을 통과하지 못하는 모습<br>
  - User micreservice 의 AthenticationFilter 에서 `token.secret` busrefresh 로 변경되었다.(갱신됨)
  - Apigateway service 의 AthorizationHeaderFilter 에서 `token.secret` busrefresh 로 변경되었다.(갱신됨)<br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/d85039d7-e422-42bb-8689-02a73abb167d" width="100%"/><br>
- 재로그인 후 성공


<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/185c72de-95b8-4454-82cb-f60f6ace3394" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/185c72de-95b8-4454-82cb-f60f6ace3394" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/185c72de-95b8-4454-82cb-f60f6ace3394" width="60%"/><br>
user-service 에 /busrefresh를 호출했는데 apigateway-service 또한 refresh 되는 모습을 확인할 수 있다.