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
### EcommerceApplication.java
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/671267ea-eead-4bdc-b6b3-49c69ac63f64" width="60%"/><br>
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


