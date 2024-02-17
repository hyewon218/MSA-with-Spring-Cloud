# Service Discovery

## Spring Cloud Netflix Eureka
하나의 Microservice 가 세가지 인스턴스에 의해서 확장되어 개발되는 경우<br>
PC가 한 대라면 port 를 나눠서 사용하고, PC가 3대라면 같은 port 로 사용할 수 있다.(서버가 다르기 때문에)

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/b809e4a6-5f3c-4fa0-b9da-1a32a13878b7" width="30%"/><br>

모든 Microservice 는 `Spring Cloud Netflix Eureka` 에 등록해야 한다.
`Eureka` 가 해주는 역할을 `Service Discovery` 라고 한다.

> Service Discovery? - Eureka <br>
> 서버의 등록과 검색을 해주는 서비스, 외부에서 다른 서비스들이 Microservice 를 검색하기 위해서 사용되는 개념 <br>
> 일종의 전화부책 -> 예를 들어 key, value 로 저장된다면 key 에는 서버 이름 value 에는 ip와 같은 위치정보

<br>

## Eureka Service Discovery - 프로젝트 생성 (discovery-service)

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/e44b47d0-4929-4ebe-8ec8-ae2ecc834544" width="50%"/><img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/1a2dc243-a63b-46e5-b346-bd46b6fd5036" width="50%"/><br>
### pom.xml
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/7e541ad6-a850-4692-9103-4093a98b9e79" width="60%"/><br>
### DiscoveryServiceApplication.java
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/f7c94944-b097-4f8b-89bb-6d5d994954c5" width="60%"/><br>
- `@EnableEurekaServer`: Eureka 서버의 자격으로 등록, 해당 어노테이션을 만나면 Service Discovery 로써 프로젝트를 기동한다.<br>
### application.yml
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/4bb9f856-fb13-4948-ae4c-81b22fc3b10f" width="40%"/><br>
- `server.port` : Eureka 서버가 Web Service 의 성격으로 기동이 됨에 있어서 port 번호 할당
- `spring.application.name`: Microservice 를 담당하는 springboot 에서 각각의 Microservice 에 고유한 아이디를 할당
- `eureka.client`
    - Eureka 라이브러리가 포함된 상태에서 스프링이 실행되면 기본적으로 Eureka 클라이언트 역할로써 어딘가에 등록하는 작업을 시도하게 된다.
    - 그 중에서 `register-with-eureka`, `fetch-registry` 는 기본값이 true 이므로 false 로 변경해야 한다.
    - 해당 설정이 true 라면 Eureka 서버 자신을 클라이언트로 등록하는 쓸데없는 작업을 수행하게 된다. (Eureka 자기 자신은 등록하지 않는다. 서버로서 기동만!)

### 실행화면
#### http://localhost:8761
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/86d1b10c-5480-47bd-8896-10929bb99ba0" width="60%"/><br>
- 현재 등록된 Instance(= Microservice)를 확인할 수 있다.

<br>


## User Service - 프로젝트 생성 (user-service)
### 유레카 서버에 들어갈 클라이언트 서버
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/824be64e-a91b-4685-850f-019858044cca" width="60%"/><br>

### pom.xml
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/b2664e85-4b14-4c5d-a3db-2aa50bdc86ae" width="50%"/><br>

### UserServiceApplication.java
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/48e9745b-2bef-4c79-8347-b7808c729599" width="60%"/><br>

### application.yml
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/2bd99dee-a963-48fe-a1cb-de6900536d0d" width="40%"/><br>

- `eureka.client.fetch-registry=true` : Eureka 서버로부터 인스턴스들의 정보를 주기적으로 가져올 것인지를 설정하는 속성이다. true 로 설정하면, 갱신 된 정보를 받겠다는 설정
- `eureka.client.service-url`: 서버의 위치가 어디인지 항목을 지정하는 부분, 유레카 서버의 위치 폴더를 입력
  - `service-url.defaultZone: http://127.0.0.1:8761/eureka` -> 해당 url에 현재 Microservice 정보를 등록

### 유레카 서버 화면
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/04ddf846-db7f-4628-8b4b-8c49b49131d6" width="80%"/><br>

- 인스턴스가 추가된 것을 볼 수 있다.
  - Status 가 UP 상태
    - UP: 작동 중
    - DOWN: 작동 중지


## User Service - 등록
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/1a879252-d69c-4b3e-9b0e-b3555b2bbbd4" width="50%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/a89cf0ba-2f6a-44fc-9b57-2e6827306a17" width="50%"/>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/e768d9e5-86a7-4121-b7b4-27223ec72510" width="23%"/><br>

- 서버 2개 기동하기 위해 VM 옵션 추가<br>
  - `-D` : 옵션 추가
  - `server.port=9002` : 포트 번호 <br>

> 위와 같은 방법은 서버 자체의 코드를 변경하는 것이 아니다.<br> 
> 따라서 한번 작성된 코드가 다시 빌드, 배포되는 것이 아닌 서버를 기동하는 방법에 의해서 부가적인 파라미터를 전달함으로써 서버 포트를 지정할 수 있는 특징을 가진다.


<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/9e8834ca-b96a-43b2-82d4-a079299bfe20" width="90%"/><br>

### 유레카 서버 화면
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/891e10b3-906b-4344-8845-d9ff5625013a" width="90%"/><br>

> 만약 외부에서 클라리언트 요청이 USER-SERVICE 로 전달된다면 `Discovery Service` 안에서 9001번으로 전달할지 9002번으로 전달할지를<br>
> 어떤 인스턴스가 살아있는지에 대한 정보값을 **gateway** 또는 **라우팅 서비스**에 전달해주면 두가지 서비스에 의해서 분산된 서비스가 실행될 수 있게 되었다.

## 여러 개의 Instance 기동

### 1. maven 명령어로 실행
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/837b4a7b-bb00-4d31-ada8-b36b679413f7" width="70%"/><br>
- src, target 폴더, pom.xml 파일이 있는 위치에서 실행
```shell
mvn spring-boot:run -Dspring-boot.run.jvmArguments='-Dserver.port=9003'
```
- maven java version 오류 17 -> 11로 변경, spring boot 3.2.2 -> 2.7.6

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/b86ec593-11fa-4270-8cd4-eb7be4bbd4f9" width="90%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/1e596359-9770-433a-8ffc-5679522689cc" width="90%"/><br>

#### mvn clean
- 빌드 시 생성된 모든 것들을 삭제 -> target 폴더 삭제<br>
  <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/9c2868fe-b335-4bcc-948a-30f5143ce54e" width="90%"/><br>
  <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/b50b3b42-b5c4-4526-851b-983b630e86c6" width="30%"/><br>


#### mvn compile package
- `compile` -> target 폴더 생성<br>
  <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/95c3ca1a-6dba-4351-a4d5-41163970c5a0" width="90%"/><br>
  <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/f44bc716-aad8-4455-8d3c-24ae7ed6cc84" width="30%"/><br>

- `package` -> jar 파일 생성<br>
  <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/71cab4dc-bed0-4d9e-9032-bc9cdadc304f" width="90%"/><br>
  <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/ca01f4b9-4c18-439f-8e5b-efb8ef588304" width="40%"/>
  <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/4d0ed9f0-c255-4f80-9023-9efba53df490" width="40%"/><br>

### 2. java -jar -Dserver.port=9004 ./target/user-service-0.0.1-SNAPSHOT.jar
- 실행<br>
  <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/236bf468-8c4a-4f07-be8d-d7a1b91a461b" width="90%"/><br>
  <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/1bf28c1b-31b1-4e08-868f-9c5455b2940e" width="90%"/><br>

> 매번 인스턴스를 달리해서 기동할 때마다 포트번호를 지정한다는 것은 불편하므로 스프링에서 지원하는 random port 를 이용하자.

### application.yml
```yaml
server:
  port: 0

...
```
- `server.port = 0`: random port 를 사용하겠다.

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/fdb9b985-97ee-4bd1-b62a-1b7f3d79f02b" width="90%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/3016bfaf-3e29-40ac-a4be-3c5d8d105b93" width="90%"/><br>

### mvn spring-boot:run
- 실행
- 이제 이전과 같이 파라미터를 통해 port 를 넘겨주지 않아도 랜덤으로 port 가 지정됨을 볼 수 있다.<br>
  <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/3994e072-61bb-4f09-8457-146e3d2c3139" width="90%"/><br>
- 유레카 서버에서 확인해보면 하나의 인스턴스밖에 확인이 되지않는다.
  <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/c1359f21-f3cc-4e13-b157-d065c315c0b9" width="90%"/><br>
- 클라이언트에 추가적인 id값을 부여하면 해결된다.

```yaml
...

eureka:
  instance:
    instance-id:  ${spring.cloud.client.hostname}:${spring.application.instance_id:${random.value}}
  client:
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://127.0.0.1:8761/eureka
```
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/3c9d3279-26a5-452c-8ae8-6f8bdfc7da17" width="90%"/><br>

계속 스케일링 작업을 하기 위해서 user-service 를 실행할 때 마다 자동으로 port 가 부여되고 사용자는 인식할 수 없는 상태에서<br> 
여러개의 인스턴스가 만들어지고 각각의 인스턴스들은 유레카 서비스(= Discovery Service)에 등록되며, 라우팅, 게이트웨이에 의해서 필요한 작업을 호출할 수 있게 된다.

간단하게 로드밸런싱을 구현할 수 있음이 Spring Cloud 의 큰 장점이다.