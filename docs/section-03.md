# E-commerce 애플리케이션
## E-commerce 애플리케이션 개요

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/930896a6-1b25-4b79-b73b-2ef918748f9a" width="60%"/><br>

Microservice 는 바운더리를 잘 나누는 것이 중요, 현재 USER-SERVICE 에는 사용자의 정보만 가지고 있어야 하는데 주문확인 로직이 존재?<br>
- 따라서 사용자가 주문을 확인한다면 USER-SERVICE 에서 ORDER-SERVICE 로 주문 조회를 시도한다. <br>
-> **Microservice 간 통신: Feign Client Or RestTemplate**
- 주문 로직은 A라는 상품을 10개 주문한다면 CATALOG-SERVICE 에서 가지고 있는 재고 수량을 업데이트 해야한다.<br> 
-> **메시지 큐잉 서비스 이용: Kafka**
  - 주문이 들어오면 ORDER-SERVICE 에서 수량 업데이트 정보를 kafka 에 보낸다(produce).
  - 해당 정보에 관심이 있는 CATALOG-SERVICE 에서 수량 정보를 가져가서(subscribe) 데이터베이스를 업데이트한다.

<br>

## E-commerce 애플리케이션 구성
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/e394af5e-7f84-47c2-8792-8b8312a66f68" width="80%"/><br>

### part 1
- Eureka 를 이용해서 Registry Service 를 생성
- CATALOG, USER, ORDER SERVICE 를 Eureka 에 등록
- CATALOG SERVICE 와 ORDER SERVICE 간 데이터를 주고받기 위해서 메시지 큐잉 서버인 kafka 이용
- 외부에서 요청이 들어왔을 때 Spring Cloud 의 API Gateway 를 이용해서 부하 분산과 서비스 라우팅

### part 2
- Configuration Service 를 등록해줌으로써 기존에 Microservice 가 가지고 있어야 할 환경 설정 정보를<br> 
Microservice 안에서 구현하는 것이 아닌 외부 서비스에 등록 후 참조하는 것으로 변경

<br>

초창기 구성(후속 강의로..)<Br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/896743a2-5557-4f67-bb16-a7012812f289" width="80%"/><br>
- 쿠버네티스를 이용한 클러스팅 구조
- 이러한 쿠버네티스 환경에 Container Runtime 으로써 Docker 를 이용
- Docker 를 이용해서 3가지 Microservice 를 배포
- 각각의 Microservice 를 모니터링하기 위해서 Grapaga 또는 Prometheus 를 연동
- 개발한 코드를 깃헙에 등록하면 pipeline 으로 연결된 시스템에 의해서 자동으로 빌드, 패키징, 도커 이미지 생성,<br>
이미지를 도커 레지스트리에 등록되어 쿠버네티스에서 배포되는 과정까지 pipeline 으로 구성
- 외부에서 클라이언트 요청이 들어왔을 때 처리할 수 있는 Ingress Controller 인 Nginx 를 이용

<br>

## 전체 애플리케이션 구성요소
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/d35e7916-26d4-4fb6-b3b3-0762efe5b220" width="80%"/><br>

## 애플리케이션 API 목록
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/b17d7a01-f38c-41c1-ad11-aa9657d79bad" width="80%"/><br>