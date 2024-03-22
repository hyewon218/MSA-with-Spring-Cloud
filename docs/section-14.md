# Microservice 모니터링
## Micrometer 개요
### Micrometer + Monitoring
자바 기반의 어플리케이션에 각종 지표를 수집하는 용도로 사용<br>
분산된 여러개의 독립적인 소프트웨어로 구성되어있기 때문에 각종 서버들이 잘 작동 중인지, 문제가 생긴 곳이 있는지, 병목 현상이 있는지, ... 파악해서 바로 자원을 재할당하는 기능이 필요하다.

> Monitoring
> 현재 CPU 사용량, 메소드의 사용량, 네트워크 트래픽이 발생했고 어느정도 사용되고 있는지, 사용자의 요청이 몇번 호출, ... 수치화된 좌표를 도식화해주는 기능

### Micrometer
- https://micrometer.io/
- JVM 기반의 애플리케이션 Metrics 제공
- Spring Framework 5, spring boot 2부터 Spring의 Metrics 처리
- Prometheus등의 다양한 모니터링 시스템을 지원

### Timer
- 짧은 지연 시간, 이벤트의 사용 빈도를 측정
- 시계열로 이벤트의 시간, 호출 빈도 등을 제공
- `@Timed` 제공

<br>

#### Microservice 수정
라이브러리 추가 -micrometer-registry-prometheus

#### application.yml
```yaml

...
management:
  endpoints:
    web:
      exposure:
        include: refresh, health, beans, busrefresh, info, metrics, prometheus
```
- metrics, prometheus 추가

#### UserController
```java
@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {

    private final Environment env;
    private final UserService userService;

    @GetMapping("/health_check")
    @Timed(value = "users.status", longTask = true)
    public String status() {
        return String.format("It's Working in User Service"
                        + ", port(local.server.port)=" + env.getProperty("local.server.port")
                        + ", port(server.port)=" + env.getProperty("server.port")
                        + ", gateway ip=" + env.getProperty("gateway.ip")
                        + ", token secret=" + env.getProperty("token.secret")
                        + ", token expiration time=" + env.getProperty("token.expiration_time"));
    }

    @GetMapping("/welcome")
    @Timed(value = "users.welcome", longTask = true)
    public String welcome() {
//        return greeting.getMessage();
        return env.getProperty("greeting.message");
    }
...
}
```
- `@Timed` 추가<br>
`status()`, `welcome()`을 사용자가 호출하게되면 호출된 정보가 Micrometer에서 기록되고 기록된 정보는 추후에 연결될 prometheus에서 사용할 수 있다.

#### welcome(), status() 호출
metrics<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/12d97fb1-ed36-40f6-9d42-6c1df22a865a" width="50%"/><br>
prometheus<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/ddc7d638-8ce2-401f-99db-c55b4ea687d8" width="50%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/cf24c82f-5f7f-44fe-a6a4-5ebe23e5eb27" width="50%"/><br>

<br>

## Prometheus와 Grafana 개요
### Prometheus
- Metrics를 수집하고 모니터링 및 알람에 사용되는 오픈소스 애플리케이션
- 2016년부터 CNCF에서 관리되는 2버째 공식 프로젝트
- Level DB -> Time Seres Database(TSDB)
- Pull 방식의 구조와 다양한 Metrics Exporter 제공
- 시계열 DB에 Metrics 저장 -> 조회 가능(Query)
프로메테우스에서 스프링 클라우드가 수집한 정보를 가지고 와서 시계열 DB화하여 저장하면 저장된 정보를 가지고 Grafana가 시각화한다.

### Grafana
- 데이터 시각화, 모니터링 및 분석을 위한 오픈소스 애플리케이션
- 시계열 데이터를 시각화하기 위한 대시보드 제공

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/4d16ff47-192b-4530-99fc-6c504f6e08c9" width="30%"/><br>

#### prometheus 설치 후 prometheus.yml 파일 수정
Prometheus 다운로드
- https://prometheus.io/download/

#### prometheus.yml
```yaml
...
  - job_name: "user-service"
    scrape_interval: 15s
    metrics_path: "/user-service/actuator/prometheus"
    static_configs:
      - targets: ["127.0.0.1:8000"]
  - job_name: "apigateway-service"
    scrape_interval: 15s
    metrics_path: "/actuator/prometheus"
    static_configs:
      - targets: ["127.0.0.1:8000"]
  - job_name: "order-service"
    scrape_interval: 15s
    metrics_path: "order-service/actuator/prometheus"
    static_configs:
      - targets: ["127.0.0.1:8000"]
```
- 어디에서 정보를 수집해올것인지 타겟을 지정한다.

#### prometheus 실행 - Dashboard
```shell
./prometheus --config.file=prometheus.yml
```
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/0d32bb63-2e64-4fc3-946c-e057499539dc" width="100%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/e17abfd1-87e6-4b18-9c69-5aba30121a2f" width="100%"/><br>
- http_server_requests_seconds_count 지표 검색

### Grafana 다운 및 실행
- https://grafana.com/<br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/7bae6ba1-9da5-4e12-af7f-fec8fd74ef2d" width="30%"/><br>
```shell
./bin/grafana-server
```
- http://127.0.0.1:3000
- ID: admin, PW: admin

## Prometheus와 Grafana의 연동과 DashBoard 구성

### Grafana Dashboard
- JVM(Micrometer)
- Prometheus
- Spring Cloud Gateway


#### Data Source 추가 -> prometheus
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/86225f38-28f9-40b6-b87a-99259bfeacb0" width="100%"/><br>

#### Dashboard import - JVM(micrometer)
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/0cf4d8eb-1480-4d49-aa47-16f12828da87" width="60%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/458a4d4b-0894-42f1-9ddd-d7c82466c76c" width="60%"/><br>

#### Dashboard import - Prometheus 2.0 overview
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/93a5afcb-504d-4fbf-8f4d-585817400601" width="60%"/><br>



#### Dashboard import - Spring Cloud Gateway
현재 서버의 값이 다르기 때문에 정확한 데이터가 나오지 않는 것
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/21000e3a-df44-4c58-b2de-33a8f4b7c3cc" width="100%"/><br>
