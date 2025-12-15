document.addEventListener('DOMContentLoaded', function() {

    /* 네이버 지도 연동  */
    const latInput = document.getElementById('dinerLat'); // 위도(dy)
    const lngInput = document.getElementById('dinerLng'); // 경도(dx)

    // 네이버 지도 API가 로드되었고, 좌표값이 있을 때만 실행
    if (typeof naver !== 'undefined' && latInput && lngInput) {
        const lat = parseFloat(latInput.value);
        const lng = parseFloat(lngInput.value);

        // 좌표값 확인
        if (!isNaN(lat) && !isNaN(lng)) {
            const mapOptions = {
                center: new naver.maps.LatLng(lat, lng), // DB 좌표를 지도의 중심으로
                zoom: 16, // 확대 레벨 (1~14, 숫자가 클수록 확대)
                scaleControl: false,
                logoControl: false,
                mapDataControl: false,
                zoomControl: true,
                minZoom: 6
            };

            // 지도 생성
            const map = new naver.maps.Map('map', mapOptions);

            // 빨간색 핀(마커) 찍기
            new naver.maps.Marker({
                position: new naver.maps.LatLng(lat, lng),
                map: map
            });
        }
    }


    /* 달력 및 예약 기능 */
    // [설정] 현재 날짜 기준
    let date = new Date();
    let currYear = date.getFullYear();
    let currMonth = date.getMonth();

    // DOM 요소
    const currentDate = document.querySelector("#currentYearMonth");
    const daysTag = document.querySelector("#calendar-days");
    const prevNextIcon = document.querySelectorAll("#prevMonth, #nextMonth");

    const displayDate = document.getElementById("displayDate");
    const displayTime = document.getElementById("displayTime");
    const displayPersonnel = document.getElementById("displayPersonnel");

    /* 요일 헤더(일~토) 자동 생성 */
    const calendarHeader = document.getElementById("calendarHeader");
    const dayNames = ["일", "월", "화", "수", "목", "금", "토"];
    let headerHtml = "";

    dayNames.forEach((day, index) => {
        let colorClass = "";

        // 일요일(0)은 빨간색, 토요일(6)은 파란색 클래스 추가
        if (index === 0) colorClass = "text-danger";
        else if (index === 6) colorClass = "text-primary";

        headerHtml += `<span class="${colorClass}">${day}</span>`;
    });

    if (calendarHeader) {
        calendarHeader.innerHTML = headerHtml;
    }

    // 달력
    const renderCalendar = () => {
        let firstDayofMonth = new Date(currYear, currMonth, 1).getDay();
        let lastDateofMonth = new Date(currYear, currMonth + 1, 0).getDate();
        let liTag = "";

        const holidays = {
            '1-1': '새해',
            '3-1': '삼일절',
            '5-5': '어린이날',
            '12-25': '성탄절'
        };

        // 빈 날짜 채우기
        for (let i = firstDayofMonth; i > 0; i--) {
            liTag += `<div class="day inactive"></div>`;
        }

        // 이번 달 날짜 채우기
        for (let i = 1; i <= lastDateofMonth; i++) {

            let checkDateObj = new Date(currYear, currMonth, i);
            let todayObj = new Date();
            todayObj.setHours(0, 0, 0, 0);

            let isToday = "";
            let isPast = "";

            if (checkDateObj.getTime() === todayObj.getTime()) {
                isToday = "today";
            } else if (checkDateObj < todayObj) {
                isPast = "inactive";
            }

            let checkDateStr = `${currMonth + 1}-${i}`;
            let holidayText = holidays[checkDateStr] ? `<span class="holiday-name">${holidays[checkDateStr]}</span>` : '';
            let isHoliday = holidays[checkDateStr] ? "holiday" : "";

            let checkDay = new Date(currYear, currMonth, i).getDay();
            let isSunday = (checkDay === 0) ? "sunday" : "";

            liTag += `<div class="day ${isToday} ${isPast} ${isHoliday} ${isSunday}" data-day="${i}">
                <span>${i}</span>
                ${holidayText}
              </div>`;
        }

        currentDate.innerText = `${currYear}.${String(currMonth + 1).padStart(2, '0')}`;
        daysTag.innerHTML = liTag;

        addDateClickEvent();
    }

    // 날짜 클릭 이벤트
    const addDateClickEvent = () => {
        const days = document.querySelectorAll(".day");
        days.forEach(day => {
            if(day.classList.contains("inactive")) return;

            day.addEventListener("click", () => {
                document.querySelector(".day.selected")?.classList.remove("selected");
                day.classList.add("selected");

                const selectedDay = day.getAttribute("data-day");
                const dayOfWeek = new Date(currYear, currMonth, selectedDay).getDay();
                const dayNames = ['일', '월', '화', '수', '목', '금', '토'];

                // 하단 요약 정보 업데이트
                displayDate.innerText = `${currYear}.${String(currMonth + 1).padStart(2, '0')}.${String(selectedDay).padStart(2, '0')} (${dayNames[dayOfWeek]})`;
                displayDate.classList.add("text-primary-custom");
            });
        });
    }

    // 달력 이전/다음 버튼
    prevNextIcon.forEach(icon => {
        icon.addEventListener("click", () => {
            currMonth = icon.id === "prevMonth" ? currMonth - 1 : currMonth + 1;

            if(currMonth < 0 || currMonth > 11) {
                date = new Date(currYear, currMonth, new Date().getDate());
                currYear = date.getFullYear();
                currMonth = date.getMonth();
            } else {
                date = new Date();
            }
            renderCalendar();
        });
    });

    /* 시간 버튼 생성 및 클릭 이벤트 */
    const timeContainer = document.getElementById("timeSlotsContainer");

    // 시간대 목록
    const times = [
        "11:00", "11:30", "12:00", "12:30", "13:00", "13:30", "14:00",
        "17:00", "17:30", "18:00", "18:30", "19:00", "19:30", "20:00"
    ];

    // 버튼 HTML 자동 생성
    let timeHtml = "";
    times.forEach(time => {
        timeHtml += `<button type="button" class="btn btn-outline-secondary btn-sm">${time}</button>`;
    });

    // HTML에 넣기
    if (timeContainer) {
        timeContainer.innerHTML = timeHtml;
    }

    // 버튼 클릭 이벤트 연결 (버튼이 생성된 후에)
    const timeButtons = document.querySelectorAll(".time-slots-grid .btn");

    timeButtons.forEach(btn => {
        btn.addEventListener("click", function() {
            // 이미 선택된 버튼들 초기화
            timeButtons.forEach(b => {
                b.classList.remove("btn-success", "text-white");
                b.classList.add("btn-outline-secondary");
            });

            // 클릭한 버튼만 활성화
            this.classList.remove("btn-outline-secondary");
            this.classList.add("btn-success", "text-white");

            // 하단 요약 정보 업데이트
            if(displayTime) {
                displayTime.innerText = this.innerText;
                displayTime.classList.add("text-primary-custom");
            }
        });
    });

    // 인원수 버튼 (+,-)
    const guestInput = document.getElementById("guestCount");

    // 인원수 변경 시 하단 요약 업데이트
    const updateGuestSummary = (count) => {
        if(displayPersonnel) {
            displayPersonnel.innerText = `${count}명`;
            displayPersonnel.classList.add("text-primary-custom");
        }
    };

    document.getElementById("btnMinus").addEventListener("click", () => {
        let val = parseInt(guestInput.value);
        if(val > 1) {
            guestInput.value = val - 1;
            updateGuestSummary(val - 1);
        }
    });
    document.getElementById("btnPlus").addEventListener("click", () => {
        let val = parseInt(guestInput.value);
        if(val < 20) {
            guestInput.value = val + 1;
            updateGuestSummary(val + 1);
        }
    });

    // 페이지 로드 시 인원수 초기값 반영
    updateGuestSummary(guestInput.value);

    // 초기 실행
    renderCalendar();

    /* 예약하기 버튼 클릭 */
    const btnReserve = document.getElementById("btnReserve");

    btnReserve.addEventListener("click", function() {
        // 날짜가 있는지 확인
        const selectedDate = document.querySelector(".day.selected");
        if (!selectedDate) {
            alert("📅 날짜를 먼저 선택해주세요.");
            return;
        }
        // 시간이 있는지 확인
        const selectedTime = document.querySelector(".time-slots-grid .btn-success");
        if (!selectedTime) {
            alert("⏰ 방문하실 시간을 선택해주세요.");
            return;
        }
        // 모든 선택이 완료되었으면 알림 띄우기
        alert("🎉 예약이 완료되었습니다!");

        //  확인 누르면 마이페이지로
        location.href = "/myPage";
    });

});