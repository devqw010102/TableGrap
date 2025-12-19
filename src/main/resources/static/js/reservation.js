document.addEventListener('DOMContentLoaded', function() {
// api & 중복 수정
    const NAVER_CLIENT_ID = "k0np2vmny3";
    const DAY_NAMES = ["일", "월", "화", "수", "목", "금", "토"];
    const TIME_SLOTS = [
        "11:00", "11:30", "12:00", "12:30", "13:00", "13:30", "14:00",
        "17:00", "17:30", "18:00", "18:30", "19:00", "19:30", "20:00"
    ];
    const HOLIDAYS = {
        '1-1': '새해',
        '3-1': '삼일절',
        '5-5': '어린이날',
        '12-25': '성탄절'
    };

    // DOM 요소
    // 지도
    const latInput = document.getElementById('dinerLat');
    const lngInput = document.getElementById('dinerLng');
    const staticMapImg = document.getElementById('staticMap');

    // 달력 UI
    const currentDateElem = document.querySelector("#currentYearMonth");
    const daysContainer = document.querySelector("#calendar-days");
    const calendarHeader = document.getElementById("calendarHeader");
    const prevNextIcons = document.querySelectorAll("#prevMonth, #nextMonth");

    // 시간 및 인원 UI
    const timeContainer = document.getElementById("timeSlotsContainer");
    const guestInput = document.getElementById("guestCount");
    const btnMinus = document.getElementById("btnMinus");
    const btnPlus = document.getElementById("btnPlus");

    // 하단 요약 정보
    const summaryDate = document.getElementById("displayDate");
    const summaryTime = document.getElementById("displayTime");
    const summaryPersonnel = document.getElementById("displayPersonnel");

    // 예약 폼
    const btnBook = document.getElementById("btnBook");
    const bookingForm = document.getElementById("bookingForm");
    const inputCombinedDate = document.getElementById("combinedBookingDate");
    const inputPersonnel = document.getElementById("inputPersonnel");

    // 상태 변수
    let date = new Date();
    let currYear = date.getFullYear();
    let currMonth = date.getMonth();

    // 초기화
    /* 네이버 지도 (Static Map) 로드 */
    if (latInput && lngInput && staticMapImg) {
        const lat = parseFloat(latInput.value);
        const lng = parseFloat(lngInput.value);

        if (!isNaN(lat) && !isNaN(lng)) {
            const staticMapUrl = `https://maps.apigw.ntruss.com/map-static/v2/raster-cors?`
                + `w=750&h=500`
                + `&center=${lng},${lat}`
                + `&level=16`
                + `&markers=type:d|size:mid|pos:${lng}%20${lat}|color:Green|label:식당|viewSizeRatio:0.7`
                + `&scale=2`
                + `&X-NCP-APIGW-API-KEY-ID=${NAVER_CLIENT_ID}`;

            staticMapImg.src = staticMapUrl;
        } else {
            console.error("유효하지 않은 위도/경도 값입니다.");
        }
    } else {
        console.error("지도 표시를 위한 필수 요소가 누락되었습니다.");
    }

    /* 달력 요일 헤더 */
    if (calendarHeader) {
        calendarHeader.innerHTML = DAY_NAMES.map((day, index) => {
            let colorClass = "";
            if (index === 0) colorClass = "text-danger"; // 일요일
            else if (index === 6) colorClass = "text-primary"; // 토요일
            return `<span class="${colorClass}">${day}</span>`;
        }).join("");
    }

    /* 시간 버튼 생성 */
    if (timeContainer) {
        timeContainer.innerHTML = TIME_SLOTS.map(time =>
            `<button type="button" class="btn btn-outline-secondary btn-sm">${time}</button>`
        ).join("");
    }

    // 주요 함수
    /* 달력 렌더링 함수 */
    const renderCalendar = () => {
        const firstDayofMonth = new Date(currYear, currMonth, 1).getDay();
        const lastDateofMonth = new Date(currYear, currMonth + 1, 0).getDate();
        let liTag = "";

        // 지난달 빈 날짜 채우기
        for (let i = firstDayofMonth; i > 0; i--) {
            liTag += `<div class="day inactive"></div>`;
        }

        // 이번 달 날짜 채우기
        const todayObj = new Date();
        todayObj.setHours(0, 0, 0, 0); // 시간 초기화

        for (let i = 1; i <= lastDateofMonth; i++) {
            const checkDateObj = new Date(currYear, currMonth, i);
            const checkDateStr = `${currMonth + 1}-${i}`;
            const checkDay = checkDateObj.getDay();

            // today, holiday
            let statusClass = "";
            if (checkDateObj.getTime() === todayObj.getTime()) statusClass = "today";
            else if (checkDateObj < todayObj) statusClass = "inactive";

            let holidayClass = HOLIDAYS[checkDateStr] ? "holiday" : "";
            let sundayClass = (checkDay === 0) ? "sunday" : "";

            // 휴일 텍스트
            const holidayText = HOLIDAYS[checkDateStr]
                ? `<span class="holiday-name">${HOLIDAYS[checkDateStr]}</span>`
                : '';

            liTag += `<div class="day ${statusClass} ${holidayClass} ${sundayClass}" data-day="${i}">
                        <span>${i}</span>
                        ${holidayText}
                      </div>`;
        }

        currentDateElem.innerText = `${currYear}.${String(currMonth + 1).padStart(2, '0')}`;
        daysContainer.innerHTML = liTag;

        attachDateClickEvents(); // 렌더링 후 클릭 이벤트 재연결
    };

    /* 날짜 클릭 이벤트 연결 (렌더링 될 때마다 호출) */
    const attachDateClickEvents = () => {
        const days = document.querySelectorAll(".day");
        days.forEach(day => {
            if (day.classList.contains("inactive")) return;

            day.addEventListener("click", () => {
                // 기존 선택 제거
                document.querySelector(".day.selected")?.classList.remove("selected");
                // 새 선택 추가
                day.classList.add("selected");

                const selectedDay = day.getAttribute("data-day");
                const dayOfWeek = new Date(currYear, currMonth, selectedDay).getDay();

                // 하단 요약 업데이트
                if (summaryDate) {
                    summaryDate.innerText = `${currYear}.${String(currMonth + 1).padStart(2, '0')}.${String(selectedDay).padStart(2, '0')} (${DAY_NAMES[dayOfWeek]})`;
                    summaryDate.classList.add("text-primary-custom");
                }
            });
        });
    };

    /* 인원수 요약 업데이트 함수 */
    const updateGuestSummary = (count) => {
        if (summaryPersonnel) {
            summaryPersonnel.innerText = `${count}명`;
            summaryPersonnel.classList.add("text-primary-custom");
        }
    };

    // 이벤트 리스너

    /* 달력 이전/다음 버튼 */
    prevNextIcons.forEach(icon => {
        icon.addEventListener("click", () => {
            currMonth = icon.id === "prevMonth" ? currMonth - 1 : currMonth + 1;

            if (currMonth < 0 || currMonth > 11) {
                date = new Date(currYear, currMonth, new Date().getDate());
                currYear = date.getFullYear();
                currMonth = date.getMonth();
            } else {
                date = new Date();
            }
            renderCalendar();
        });
    });

    /* 시간 버튼 클릭 */     // 이벤트 위임??
    const timeButtons = document.querySelectorAll(".time-slots-grid .btn");
    timeButtons.forEach(btn => {
        btn.addEventListener("click", function() {
            // 초기화
            timeButtons.forEach(b => {
                b.classList.remove("btn-success", "text-white");
                b.classList.add("btn-outline-secondary");
            });
            // 활성화
            this.classList.remove("btn-outline-secondary");
            this.classList.add("btn-success", "text-white");

            // 요약 업데이트
            if (summaryTime) {
                summaryTime.innerText = this.innerText;
                summaryTime.classList.add("text-primary-custom");
            }
        });
    });

    /* 인원수 조절 버튼 */
    if (btnMinus && btnPlus && guestInput) {
        btnMinus.addEventListener("click", () => {
            let val = parseInt(guestInput.value);
            if (val > 1) {
                guestInput.value = --val;
                updateGuestSummary(val);
            }
        });

        btnPlus.addEventListener("click", () => {
            let val = parseInt(guestInput.value);
            if (val < 20) {
                guestInput.value = ++val;
                updateGuestSummary(val);
            }
        });

        // 초기 로드시 반영
        updateGuestSummary(guestInput.value);
    }

    /* 예약하기 버튼 (폼 전송) */
    if (btnBook) {
        btnBook.addEventListener("click", function() {
            // 유효성 검사
            const selectedDateElem = document.querySelector(".day.selected");
            const selectedTimeElem = document.querySelector(".time-slots-grid .btn-success");

            if (!selectedDateElem) {
                alert("📅 날짜를 먼저 선택해주세요.");
                return;
            }
            if (!selectedTimeElem) {
                alert("⏰ 방문하실 시간을 선택해주세요.");
                return;
            }

            // 데이터 취합
            const day = selectedDateElem.getAttribute("data-day");
            const time = selectedTimeElem.innerText;
            const guestCount = guestInput.value;

            const formattedMonth = String(currMonth + 1).padStart(2, '0');
            const formattedDay = String(day).padStart(2, '0');
            const finalDateTime = `${currYear}-${formattedMonth}-${formattedDay} ${time}`;

            // 폼 데이터 세팅
            if (inputCombinedDate) inputCombinedDate.value = finalDateTime;
            if (inputPersonnel) inputPersonnel.value = guestCount;

            // 최종 확인 및 전송
            if (confirm(`${finalDateTime}에 ${guestCount}명으로 예약하시겠습니까?`)) {
                bookingForm.submit();
            }
        });
    }

    // 초기 달력 렌더링 실행
    renderCalendar();

    // 예약 수정
    const bookIdInput = document.querySelector('input[name="bookId"]');
    const oldDateInput = document.getElementById('oldDate');
    const oldPersonnelInput = document.getElementById('oldPersonnel');

    if(bookIdInput && bookIdInput.value) {

        if (oldPersonnelInput) {
            const count = oldPersonnelInput.value;
            if (guestInput) guestInput.value = count;
            if (inputPersonnel) inputPersonnel.value = count;
            updateGuestSummary(count);
        }
        if (oldDateInput) {
            const rawDate = oldDateInput.value;
            if (inputCombinedDate) inputCombinedDate.value = rawDate;

            const dateParts = rawDate.split('T');
            const datePart = dateParts[0];
            const timePart = dateParts[1].substring(0, 5);

            if (summaryDate) {
                summaryDate.innerText = datePart;
                summaryDate.classList.add("text-primary-custom")
            }
            if (summaryTime) {
                summaryTime.innerText = timePart;
                summaryTime.classList.add("text-primary-custom")
            }
        }

        if (btnBook) {
            btnBook.innerText = "수정하기";
        }
    }
    //리뷰를 불러오기
    document.getElementById("review-tab").addEventListener("click", () => {
        loadReviews()});

    //async 리뷰 불러오기 함수
    async function loadReviews() {
        // HTML의 hidden input에서 dinerId를 가져오기
        const dinerIdInput = document.querySelector('input[name="dinerId"]');
        const dinerId = dinerIdInput ? dinerIdInput.value : null;

        if (!dinerId) {
            console.error("식당 ID를 찾을 수 없습니다.");
            return;
        }

        const url = `/api/review/list?dinerId=${dinerId}`;

        try {
            const res = await fetch(url);
            if (res.ok) {
                const data = await res.json();
                console.log("받아온 데이터:", data);
                renderReviews(data);
            } else {
                console.error("리뷰 로드 실패 (400/500 에러)");
            }
        } catch (e) {
            console.error("통신 중 오류 발생:", e);
        }
    }

    function renderReviews(data) {
        const tbody = document.getElementById("review-tbody");
        if (!tbody) return;

        tbody.innerHTML = "";

        if (!data || data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="2" class="text-center">작성한 후기가 없습니다.</td></tr>';
            return;
        }

        // 데이터를 반복하며 테이블 행(tr) 생성
        data.forEach(review => {
            const row = `
                <tr>
                    <td>${review.rating}/5</td>
                    <td class="text-start">${review.comment}</td>
                </tr>
            `;
            tbody.insertAdjacentHTML('beforeend', row);
        });
    }
});