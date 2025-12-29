# 🍽️ TableGrap

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=springboot&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A.svg?style=for-the-badge&logo=Gradle&logoColor=white)
![H2 Database](https://img.shields.io/badge/H2%20Database-004B8D?style=for-the-badge&logo=databricks&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-%23005C00.svg?style=for-the-badge&logo=Thymeleaf&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=for-the-badge&logo=Spring%20Security&logoColor=white)
> **대량의 식당 데이터를 효율적으로 관리하고 예약하는 플랫폼**

## 📖 Project Overview

**TableGrap**은 엑셀 파일로 된 대량의 식당 데이터를 손쉽게 업로드하고, 이를 분석하여 사용자에게 예약 및 조회 서비스를 제공하는 웹 애플리케이션입니다.
단순한 CRUD를 넘어, **비정형 데이터(식당 이름/카테고리) 정제 알고리즘**과 **Geocoding API를 활용한 위치 기반 서비스**를 중점적으로 구현했습니다.

## 🛠️ Tech Stack
- **Language**: Java 21
- **Framework**: Spring Boot 4.0.0
- **Build Tool**: Gradle
- **Database**: H2 Database (File Mode for Persistence)
- **Frontend**: Thymeleaf, HTML/CSS, Vanilla JS
- **Security**: Spring Security (Role-based Auth)
- **API**: Naver Maps API (Geocoding), Naver Search API (Category), Bizno API (Business registration number)

## 🌟 Key Features
> 수정 필요

### 1. 엑셀 데이터 파싱 및 자동 카테고리 분류
- **문제**: 원본 데이터의 카테고리가 불분명하거나 '기타'로 되어있는 경우가 많음.
- **해결**: 기타 카테고리의 데이터를 검색 API 를 사용하여 **분류 알고리즘** 구현
    - 정규표현식(Regex)과 Enum을 활용하여 분류 정확도 향상.
    - `Enum` 기반 설계를 통해 키워드 관리의 유지보수성 증대.

### 2. Geocoding & 데이터 영속성 관리
- 주소 데이터를 기반으로 위도/경도 좌표를 자동 추출하여 시각화 기반 마련.
- 중복 데이터 업로드 방지 로직을 통해 불필요한 API 호출 비용 절감.

### 3. 사용자 권한 분리 (RBAC)
- **Admin**: 식당 엑셀 업로드, 전체 회원 관리.
- **Owner**: 본인 식당 정보 수정 및 예약 관리.
- **Member**: 식당 검색, 예약 신청, 마이페이지 조회, 리뷰 작성.

## 📂 Project Structure
```
src/
├─ main/
│  ├─ java/
│  │  └─ com.example.demo/
│  │     ├─ config/        # 보안, 초기 데이터 설정
│  │     ├─ controller/    # MVC / REST 컨트롤러
│  │     ├─ data/
│  │     │  ├─ dto/        # 요청/응답 DTO
│  │     │  ├─ enums/      # 공통 Enum
│  │     │  ├─ model/      # JPA Entity
│  │     │  ├─ repository/# Spring Data JPA
│  │     │  └─ userDetails/# Security UserDetails
│  │     ├─ service/       # 비즈니스 로직
│  │     │  └─ impl/       # 서비스 구현체
│  │     └─ DemoApplication.java
│  │
│  └─ resources/
│     ├─ static/           # CSS / JavaScript
│     └─ templates/        # Thymeleaf View
│
└─ test/
└─ java/                # 테스트 코드
```

## 🔥 Technical Issues & Solutions
> 직접 겪은 문제 중 하나 이상 종합 필요



##### Entity
> Create ERD Diagram ( lucid.app )

## 🚀 Getting Started
1. **Clone the repository**
   ```bash
   git clone [https://github.com/devqw010102/TableGrap.git](https://github.com/devqw010102/TableGrap.git)
2. **Set Application Properties**
    ```bash
    src/main/resources/application.properties 파일에 API 키 설정이 필요합니다.
3. **Run the Project**
    ```bash
   ./gradlew bootRun
4. **Access**
   - Main : <code>http://localhost:8080</code>
   - DB Console : <code>http://localhost:8080/h2-console</code>

### 🔄 Data Processing Flow
> (예시) 데이터 흐름도 Mermaid or Diagram 으로 작성 예정
```mermaid
graph TD
    A[Excel File Upload] --> B{Category Refinement}
    B -->|Regex/Enum Matching| C[Standard Category Assigned]
    C --> D[Geocoding API Call]
    D --> E[Coordinate Data Obtained]
    E --> F{Duplicate Check}
    F -->|New Data| G[Save to H2 File Database]
    F -->|Exists| H[Skip/Update]
    G --> I[Persistence Guaranteed]