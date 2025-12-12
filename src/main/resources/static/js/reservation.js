document.addEventListener('DOMContentLoaded', function() {

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

    // 달력 그리기
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

        // 1. 빈 날짜 채우기
        for (let i = firstDayofMonth; i > 0; i--) {
            liTag += `<div class="day inactive"></div>`;
        }

        // 2. 이번 달 날짜 채우기
        for (let i = 1; i <= lastDateofMonth; i++) {
            let isToday = i === new Date().getDate() &&
            currMonth === new Date().getMonth() &&
            currYear === new Date().getFullYear() ? "today" : "";

            let checkDate = `${currMonth + 1}-${i}`;
            let holidayText = holidays[checkDate] ? `<span class="holiday-name">${holidays[checkDate]}</span>` : '';
            let isHoliday = holidays[checkDate] ? "holiday" : "";

            let checkDay = new Date(currYear, currMonth, i).getDay();
            let isSunday = (checkDay === 0) ? "sunday" : "";

            liTag += `<div class="day ${isToday} ${isHoliday} ${isSunday}" data-day="${i}">
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

                displayDate.innerText = `${currYear}.${String(currMonth + 1).padStart(2, '0')}.${String(selectedDay).padStart(2, '0')} (${dayNames[dayOfWeek]})`;
                displayDate.classList.add("text-primary");
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

    // 시간 버튼 클릭
    const timeButtons = document.querySelectorAll(".time-slots-grid .btn");
    timeButtons.forEach(btn => {
        btn.addEventListener("click", function() {
            if(this.classList.contains("disabled")) return;

            timeButtons.forEach(b => {
                b.classList.remove("btn-success", "text-white");
                b.classList.add("btn-outline-secondary");
            });

            this.classList.remove("btn-outline-secondary");
            this.classList.add("btn-success", "text-white");

            displayTime.innerText = this.innerText;
        });
    });

    // 인원수 버튼
    const guestInput = document.getElementById("guestCount");
    document.getElementById("btnMinus").addEventListener("click", () => {
        let val = parseInt(guestInput.value);
        if(val > 1) guestInput.value = val - 1;
    });
    document.getElementById("btnPlus").addEventListener("click", () => {
        let val = parseInt(guestInput.value);
        if(val < 20) guestInput.value = val + 1;
    });

    // 초기 실행
    renderCalendar();
});