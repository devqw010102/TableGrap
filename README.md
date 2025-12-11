# TableGrap


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
<img width="1168" height="698" alt="erd_diagram" src="https://github.com/user-attachments/assets/a5240b26-efb2-410e-ad98-9fa0222e20f2" />

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

## Directory Structure
<img width="932" height="746" alt="project_structure" src="https://github.com/user-attachments/assets/b2684c04-eb99-4ed4-b834-6df6242192f4" />

//creately.com

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

