# 암호화 처리를 위한 Encryption과 Decryption

## 대칭키와 비대칭키
- Encryption(암호화): 일반적인 데이터(plane text)를 암호화해서 사람이 알 수 없도록 변경하는 작업
- Decryption(복호화): Encryption 데이터를 원래 데이터로 바꾸는 작업

### Encryption types
- Symmetric Encryption (Shared) -> 대칭 암호화방식
  - 암호화에 사용한 키와 복호화에 사용한 키를 동일하게 사용하는 방식
- Symmetric Encryption (Shared) -> 대칭 암호화방식
  - 암호화에 사용한 키와 복호화에 사용한 키를 다르게 사용하는 방식
  - 비대칭 암호화 방식에서 사용되는 각각의 키를 private key, public key라고 한다.
  - 암호화시에 private key를 사용하고 복호화시에 public key를 사용한다고 정의되지 않았고 다만 복호화시에 암호화에 사용되지 않은 키를 사용한다.

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/c0338a48-9759-411a-91ed-8560efb6ae19" width="60%"/><br>

일반적인 평문 데이터를 yml에서 보관하게되는데, 데이터베이스 암호, IP 주소와 같은 데이터는 암호화되어 저장해야 한다.<br>
사용하는 시점에 암호화된 데이터를 복호화하여 사용하는 흐름

## 대칭키를 이용한 암호화
#### Config Server
라이브러리 추가
- bootstrap<br>
암호화를 위해서는 키값이 필요하다.

#### bootstrap.yml
```yaml
encrypt:
  key: abcdefghijklmnopqrstuvwxyz0123456789
```

### git repository: spring-cloud-config

#### users-service.yml
```yaml
spring:
  datasource: #jpa 에서 자동으로 테이블 생성
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb
    username: sa
    password: '{cipher}572d1067ebe05d91c0fcd7ba1520aab3ba1a02fa52f67f5c5c8f929ab4cc3f6c'
```

#### Users Microservice
users-service의 application.yml, bootstrap.yml 수정 -> Config의 user-service.yml로 이동<br>
데이터베이스를 연동하는 Datasource 부분을 Config에서 별도의 파일로 분리

#### application.yml
```yaml
spring:
  ...

#  datasource:
#    driver-class-name: org.h2.Driver
#    url: jdbc:h2:mem:testdb
```
#### bootstrap.yml
```yaml
spring:
  cloud:
    config:
      uri: http://127.0.0.1:8888
      name: user-service 
```

#### encrypt

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/96ee0540-0d85-4244-b960-fac69a7b7ea3" width="80%"/><br>

#### decrypt

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/dca5c6cf-1f78-4bc1-918e-1e892c572b49" width="80%"/><br>

#### 결과

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/0957e6d8-cf72-4fa8-8623-fd69dd8045bb" width="80%"/><br>

- 웹 브라우저에서 읽혀질 때는 복호화된 데이터
- 각각의 Microservice에서 읽힐 때 복호화된 데이터로 읽힌다.

#### 암호화된 데이터를 임의로 변경
#### users-service.yml
```yaml
spring:
  datasource: #jpa 에서 자동으로 테이블 생성
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb
    username: sa
    password: '{cipher}572d1067ebe05d91c0fcd7ba1520aab3ba1a02fa52f67f5c5c8f929ab4cc3f6c_wrong'

token:
  expiration_time: 864000000
  secret: user_token_native_users_service_default

gateway:
  ip: 127.0.0.1
```
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/bab51f2b-d3b2-4ce1-a04e-9862cf1f9162" width="80%"/><br>

<br>

## 비대칭키를 이용한 암호화

JDK keytool 이용
- mkdir ${user.home}/Desktop/Work/keystore

```shell
keytool -genkeypair -alias apiEncryptionKey -keyalg RSA -dname "CN=Hyewon Choi, OU=API Development, O=won.co.kr, L=Seoul, C=KR" -keypass "won1234" -keystore apiEncryptionKey.jks -storepass "won1234"
```
- alias를 통해서 호출, 사용
- dname을 통해서 서명정보 추가, 부가정보 입력
- 사용되는 알고리즘 RSA

#### 공개키 생성
위에서 만든 키로 부터 공개키를 생성

```shell
keytool -export -alias apiEncryptionKey -keystore apiEncryptionKey.jks -rfc -file trustServer.cer
```

#### 인증서 파일(.cer)을 jks 파일로 변경
```shell
keytool -import -alias trustServer -file trustServer.cer -keystore publicKey.jks
```
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/e8a9dd5e-e848-4c62-b4ae-dd6217399eea" width="50%"/><br>


#### Config Server
이전의 config 정보를 변경

#### bootstrap.yml
```yaml
encrypt:
  #  key: abcdefghijklmnopqrstuvwxyz0123456789
  key-store: # 비대칭키 암호화
    location: file://{user.home}/Desktop/Work/keystore/apiEncryptionKey.jks
    password: won1234
    alias: apiEncryptionKey
```

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/596a6893-d44e-4dd3-8965-d0fcbb293028" width="80%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/1f31e3bd-c69c-4768-90a8-b0f85fe9039c" width="80%"/><br>

#### users-service.yml
```yaml
spring:
  datasource: #jpa 에서 자동으로 테이블 생성
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb
    username: sa
    password: '{cipher}AQBY5Ctn4KJs9QnpLVIA6hGyRqigtdtr8xY7gQdusYMv0apPXA4YauzyMco3hBr/Bh7eh7qYVG0blRiwThINlmj+M/qLDydfYFcsEX4SxCqE38QU+3mCJPimkk65FIwAeTPi8AHj4Rmczh3dAJLu6zX8iyWsbjGMUt5XUaf4chDkRS1vTtEbmV7+oi0JFJJEqV9qROmJJOj88JAxxcbHqCV9Tn7I27TecGQMgrb7ju99T4gS4Bb2YjRx9yqGjsAcms5fi8oQcPDHkfbh1YMcz1QTGBWPO3ZLBIb1CKCL90lDLTIcISHeiWLK4vOZkF9e37zKc7C1S+UtNQClryX4WSHof5g6vbpM6T64uxiy4pevbRzH/YbALYZkwKAvG4AywuI='

token:
  expiration_time: 86400000 # 만료 기간 -> 하루
  secret: secret-key # 토큰 키
gateway:
  ip: 127.0.0.1
```

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/10a087bc-067b-4257-b35a-e1eeba554537" width="80%"/><br>

## Gateway의 token secret 정보 암호화
#### apigateway-service : config-service application.yml

```yaml
token:
  expiration_time: 864000000
  secret: '{cipher}AQAjewIF3yA62AoMF0HEm940HYboFKAYXnGi5hXINBlOF0h0bv2vENzBl3YB/fvcgdCk6j49gKVNo7SJ6E/D7hHF0J+itQFuiwGFr1f9odZBUz90w/MpAsrcFxwWpRajZ1dskLYyECPNEFXsrkeFiMS1EmxEjJpthBypjmJ2xzatK0VPeW38OcxJSprFSvufvnAWG4tbSjMm9JvFto4ikN5zAzILQuRtML3I7SZrYDmrU096z9eVz+v8hpRX/uMpFd73Io7x8gwQ1CLHyKqBMs4PF8wJwYJZOa6Ru46UKZued2jessHctDeqJ55BYzrwcQWXtVyvN38EnwosGLn7znnzMWRaI7ByzGWTmNhmjf42kGeTGBz8Z49E0CxCNZ5R7APpBpK34+t2585b50pZvB0JwGLcIIRteIEzZO+te9aYJw=='

gateway:
  ip: 127.0.0.1
```

yml을 하나로 모아서 처리할 수도 있다.

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/0254ecd5-0e1b-4b13-86ba-d79a08d8fedc" width="80%"/><br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/9c010a0a-ec14-4e11-9926-9c85ad4f931c" width="80%"/><br>

`users-service.yml` 와 `ecommerce.yml` 모두 `application.yml`이라는 상위 설정을 가지고 있으므로 토큰 정보와 같은 공통되는 설정은 `application.yml`에 기입한다.