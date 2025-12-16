﻿# TableGrap


## 사전 토의 결과
<pre>
	def : <b>식당 예약 프로젝트</b>

	1. 프론트 UI 중요도 높음
	2. 로직, 예외(동시 예약, 잔여 좌석, 테이블별 인원 매칭) 설계 많음
	3. 테스트케이스 많음(오픈, 마감시간 예약, 휴무일, 영업시간 변경...etc
	4. 백엔드 위주
</pre>

## Definition
<pre>
  프로젝트 이름 : TableGrap
  
  Spring 의존성 : h2 database, spring web, spring data jpa, lombok, Thymeleaf, spring security, Validation + ...ETC
  
  Entity : 식당, 예약 현황(식당 이름), 회원(Many to one, 리뷰), 완료된 예약(구현)
</pre>

##### VIEW
| 이름(예명) | 기능 |
| --------- | ----- |
| Main | Index(음식 카테고리, 로그인) |
| login | 로그인 | 
| register | 회원가입 |
| myPage | 마이페이지(예약 목록, 회원 수정) |
| adminPage | 관리자 페이지(통계, 예약 현황...etc) |
| reservation | 예약 페이지 |

##### Entity
<img width="749" height="550" alt="erd_diagram" src="https://github.com/user-attachments/assets/509a5fd8-ba50-4eb2-83a1-a84612a86b0b" />


// lucid.app


-----

## Implement Function Constraint(Exception)
<pre>
	1. 식당 목록(지도, 키워드 검색)
	2. 예약(동시간대 여러 예약 처리)
	3. 예약 순서 미루기
	4. 대기열(시간)
	5. 회원의 예약 순서가 가까워지면 알람(Message or Alert)
	6. 예약 성공 시 알람(캘린더)에 추가
</pre>

---

## 📂 TableGrap Project Structure

```bash
📁 TableGrap
├─ 📁 .gradle
├─ 📁 .idea
├─ 📁 build
├─ 📁 gradle
│   └─ 📁 wrapper
├─ 📁 src
│   ├─ 📁 main
│   │   ├─ 📁 java
│   │   │   └─ 📁 com
│   │   │       └─ 📁 example
│   │   │           └─ 📁 demo
│   │   │               ├─ 📁 config
│   │   │               │   ├─ ☕ AdminDataInitializer.java		// admin 권한 부여
│   │   │               │   └─ ☕ SecurityConfiguration.java	// Security 설정
│   │   │               ├─ 📁 controller
│   │   │               │   ├─ ☕ AdminController.java			// 파일(식당) 업로드 
│   │   │               │   ├─ ☕ HomeController.java			// 페이지 이동
│   │   │               │   ├─ ☕ ManageController.java			// 관리자 페이지
│   │   │               │   └─ ☕ MemberController.java			// 회원
│   │   │               ├─ 📁 data
│   │   │               │   ├─ 📁 dto
│   │   │               │   │   ├─ ☕ CoordinateDto.java		// 식당 좌표
│   │   │               │   │   └─ ☕ MemberDto.java			// 회원가입
│   │   │               │   ├─ 📁 model
│   │   │               │   │   ├─ ☕ Authority.java			// 권한 테이블
│   │   │               │   │   ├─ ☕ Book.java					// 예약 테이블
│   │   │               │   │   ├─ ☕ Diner.java				// 식당 테이블
│   │   │               │   │   ├─ ☕ Member.java				// 회원 테이블
│   │   │               │   │   └─ ☕ MemberUserDetails.java	// 회원 권한 세팅
│   │   │               │   └─ 📁 repository
│   │   │               │       ├─ 🟢 AuthorityRepository.java		// 권한
│   │   │               │       ├─ 🟢 BookRepository.java			// 예약
│   │   │               │       ├─ 🟢 DinerRepository.java			// 식당
│   │   │               │       └─ 🟢 MemberRepository.java			// 회원
│   │   │               ├─ 📁 service
│   │   │               │   ├─ 🟢 DinerExcelService.java			// 파일(식당) 관련 서비스 Interface
│   │   │               │   ├─ 🟢 DinerService.java					// 식당 관련 서비스 Interface
│   │   │               │   ├─ 🟢 GeocodingService.java				// 식당 좌표 api 서비스 Class
│   │   │               │   ├─ 🟢 MemberService.java				// 회원 관련 서비스 Interface
│   │   │               │   └─ 📁 impl								// 실제 구현 파일 폴더
│   │   │               │       ├─ ☕ CustomUserDetailsService.java
│   │   │               │       ├─ ☕ DinerExcelServiceImpl.java
│   │   │               │       ├─ ☕ DinerServiceImpl.java
│   │   │               │       ├─ ☕ GeocodingServiceImpl.java
│   │   │               │       └─ ☕ MemberServiceImpl.java
│   │   │
│   │   └─ 📁 resources
│   │       ├─ 📁 static
│   │       │   └─ 📄 register.js				// 회원가입 스크립트
│   │       ├─ 📁 templates
│   │       │   ├─ 📁 admin
│   │       │   │   └─ 🌐 adminPage.html		// 관리자 페이지
│   │       │   ├─ 📁 fragment
│   │       │   │   └─ 🌐 common.html			// header, footer 파일
│   │       │   ├─ 📁 reservation
│   │       │   │   └─ 🌐 reservation.html		// 예약 페이지
│   │       │   └─ 📁 user
│   │       │       ├─ 🌐 login.html			// 로그인 페이지
│   │       │       ├─ 🌐 logout.html			// 로그아웃 페이지
│   │       │       ├─ 🌐 myPage.html			// 마이페이지
│   │       │       ├─ 🌐 ownerPage.html		// 식당주인 페이지
│   │       │       └─ 🌐 register.html			// 회원가입 페이지
│   │       │
│   │       ├─ 🌐 index.html					// 메인 페이지
│   │       ├─ ⚙️ application.properties		// 설정
│   │       ├─ 🗄️ schema.sql					// h2 DB 스키마
│   │       └─ 📊 test_diner.xlsx				// 테스트용 식당 정보 파일
├─ 📝 README.md
├─ 📝 HELP.md
├─ ⚙️ build.gradle
├─ ⚙️ gradlew
├─ ⚙️ gradlew.bat
├─ ⚙️ settings.gradle
├─ ⚙️ .gitignore
└─ ⚙️ .gitattributes
```

---

#### FLOW
<pre>
  1. 식당 테이블에 api 값을 불러와서 초기값(울산광역시 만)
  
  2. 메인 페이지
	  2-1) 로그인, 회원가입 : 회원 테이블
	  2-2) 카테고리 : 식당 리스트 출력 -> 예약 버튼 클릭시 로그인 페이지로 이동

  3. 로그인 상태
	  3-1) 마이페이지
	  3-2) 회원 수정
</pre>

#### Commit Message

| commit | func |
| ------ | ---- |
| add | 기능 추가|
| modify | 수정 |
| fix | 버그 수정|
| delete | 삭제 |
| style | 스타일/포맷팅 |
| test | 테스트 코드 |

<img width="1920" height="1040" alt="Spring Initializr - Chrome 25-12-11 오후 12_13_34" src="https://github.com/user-attachments/assets/2906b98f-c8a2-407b-a2f9-91fe73a381e7" />

