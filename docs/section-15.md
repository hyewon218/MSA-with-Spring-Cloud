# 애플리케이션 배포를 위한 컨테이너 가상화
## Virtualization
-  물리적인 컴퓨터 리소스를 다른 시스템이나 애플리케이션에서 사용할 수 있도록 제공
  - 플랫폼 가상화
  - 리소스 가상화
- 하이퍼바이저(Hypervisor)
  - Virtual Machine Manager (VMM)
  - 다수의 운영체제를 동시에 실행하기 위한 논리적 플랫폼
  - Type 1: Native or Bare-metal
  - Type 2: Hosted<br>
    <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/bc571967-a79b-4519-8c5c-77878b92fa9c" width="50%"/><br>

## Container Virtualization

- OS Virtualization
  - Host OS 위에 Guest OS 전체를 가상화
  - VMWare, VirtualBox
  - 자유도가 높으나, 시스템에 부하가 많고 느려짐<br>
    <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/1fe127f9-dac0-4dff-88d7-02afa23ca350" width="30%"/><br>
- Container Virtualization
  - Host OS가 가진 리소스를 적게 사용하며, 필요한 프로세스 실행 
  - 최소한의 라이브러리와 도구만 포함
  - Container의 생성 속도 **빠름**<br>
    <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/993acd46-8729-40d4-8557-e9c047c4c5e7" width="30%"/><br>

## Container Image
- Container 실행에 필요한 **설정 값**
  - 상태값 X, Immutable
- Image를 가지고 **실체화** -> **Container**

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/e1177481-9a97-4580-9ca0-f8dcbf31b65b" width="80%"/><br>
- 각각의 Image로 Container 를 실행할 수 있다.

## DockerFile
- Docker lmage를 생성하기 위한 스크립트 파일
- 자체 DSL(Domain-Specific language) 언어 사용 》 이미지 생성과정 기술

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/932471b4-3aac-465d-a451-b541fa5a2440" width="40%"/><br>

## Docker 컨테이너
- 컨테이너 실행
  ```shell
  $ docker run [OPTIONS] IMAGE[:TAG|@DIGEST] [COMMAND] [ARG...]
  ```
  - run = create + start
  - tag 이름 명시하지 않으면 latest 자동으로 붙게 됨

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/74129b7d-5949-44d6-a667-f53deceaafc6" width="50%"/><br>

docker hub 에서 다운받아서 실행하기
  ```shell
  $ docker pull ubuntu:16.04
  ```
 ```shell
  $ docker run ubuntu:16.04
  ```
컨테이너 목록 확인
 ```shell
  $ docker container ls
  ```
 ```shell
  $ docker ps
  ```
종료된 컨테이너 확인
 ```shell
  $ docker container ls -a
  ```
 ```shell
  $ docker ps -a
  ```

## 컨테이너 생성과 실행
컨테이너 생성
```shell
$ docker run -d -p 3306:3306 -e MYSQL_ALLOW_EMPTY_PASSWORD=true --name mysql mysql:5.7
```
- 앞 포트번호 : host pc 에서 접근하고자 하는 포트 번호
- 뒤 포트번호 : 컨테이너에서 응답하기 위한 포트 번호 
- 컨테이너 안에 있는 3306 포트와 host pc 가 가진 3306 포트 연결해서 host pc 에서 접속을 해서 사용할 수 있도록 

컨테이너 실행
```shell
$ docker exec -it mysql bash
```

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/c3377cf5-c65f-4136-8e12-dd0336c96678" width="50%"/><br>

## Docker 이미지 생성과 Public registry에 Push
### Dockerfile for Users Microservice
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/507eae18-c44b-4e40-a8f8-11209a759c18" width="50%"/><br>

<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/bdfd9133-fd20-45a2-84c2-fed7721da359" width="70%"/><br>

### Users Microservice
```shell
mvn clean complie package -DskipTest=true
````
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/ad3c69b5-e93d-4076-b8b3-473520197a67" width="20%"/>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/4dea5606-6f25-49c5-a70a-ead15a229c6f" width="30%"/><br>



<br>

```shell
$ docker build -t won1110218/users-service:1.0 .
```
- . : 현재 디렉토리에 있는 도커파일을 가지고 이미지를 만든다
```shell
$ docker push won1110218/users-service:1.0
```
- 도커허브에 올리기<br>
<img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/a2117c81-45bd-4387-aa33-ade9b6987210" width="70%"/><br>


```shell
$ docker pull won1110218/users-service:1.0
```
- 도커허브에서 다운로드 받아오기<br>
  <img src="https://github.com/hyewon218/kim-jpa2/assets/126750615/e3d56dad-44ed-4f03-8f8a-e7ac7e8fef5c" width="50%"/><br>