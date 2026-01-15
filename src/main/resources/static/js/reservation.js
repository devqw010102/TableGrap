document.addEventListener("DOMContentLoaded", () => {

    // 설정 값(상수)
    const config = {
        NAVER_CLIENT_ID: "k0np2vmny3",
        DAY_NAMES: ["일", "월", "화", "수", "목", "금", "토"],
        HOLIDAYS: {
            '1-1': '새해', '3-1': '삼일절', '5-5': '어린이날', '12-25': '성탄절'
        },
        MIN_PERSONNEL: 1,
        MAX_PERSONNEL: 20
    }

    // 상태(변수)
    const state = {
        currYear: new Date().getFullYear(),
        currMonth: new Date().getMonth(),
        selectedDate: "",      // 전송용 ex) "2025-12-30"
        displayDate: "",       // 화면용 ex) "2025.12.30 (화)"
        selectedTime: "",      // "11:30"
        selectedPersonnel: parseInt(document.getElementById("guestCount")?.value || "2"),
        dinerId: document.querySelector('input[name="dinerId"]')?.value,
        isModifyMode: !!document.querySelector('input[name="bookId"]')?.value
    }

    // DOM 요소
    const el = {
        calendarHeader: document.getElementById("calendarHeader"),
        calendarDays: document.querySelector("#calendar-days"),
        currentYearMonth: document.querySelector("#currentYearMonth"),
        amSlots: document.getElementById("amSlotsContainer"),
        pmSlots: document.getElementById("pmSlotsContainer"),
        summaryBox: document.getElementById("topSummaryBox"),
        guestCount: document.getElementById("guestCount"),
        bookingForm: document.getElementById("bookingForm"),
        btnBook: document.getElementById("btnBook"),
        reviewTab: document.getElementById("review-tab"),
        reviewTbody: document.getElementById("review-tbody"),
        reviewDetail: document.getElementById("reviewDetail")
    };

    // API
    const API = {
        // 예약 가능 슬롯 조회
        async fetchAvailability() {
            if(!state.selectedDate) return;
            try {
                const params = new URLSearchParams( {
                    dinerId: state.dinerId,
                    date: state.selectedDate,
                    personnel: state.selectedPersonnel
                });
                const res = await fetch(`/api/myPage/availability?${params}`);
                const slots = await res.json();

                if(state.selectedTime) {
                    const currentSlot = slots.find(s => s.time === state.selectedTime);

                    if (!currentSlot || !currentSlot.available) {
                        state.selectedTime = ""; // 상태에서 시간 제거
                        alert("선택하신 시간은 해당 인원으로 예약이 불가능하여 해제되었습니다.");
                    }
                }

                UI.renderTimeSlots(slots);
                UI.updateSummary();
            }
            catch(e) {
                console.error("슬롯 로드 실패:", e);
            }
        },

        // 예약 저장, 수정
        async submitBooking(formData) {
            return await fetch(`/api/myPage/reservation`, {
                method: "POST",
                body: new URLSearchParams(formData)
            });
        },

        // 리뷰
        async loadReviews() {
            const dinerId = state.dinerId;

            if(!dinerId) {
                console.error("식당 ID를 찾을 수 없습니다.");
                return;
            }

            try {
                const res = await fetch(`/api/review/reservation/list?dinerId=${dinerId}`);

                if(res.ok) {
                    const data = await res.json();
                    console.log("받아온 데이터:", data);
                    UI.renderReviews(data);

                    const reviewCnt = data.length;
                    el.reviewTab ? el.reviewTab.innerText = `⭐ 후기(${reviewCnt})` : console.error("리뷰 로드 실패 (400/500 에러)");
                }
            }
            catch(e) {
                console.error("통신 중 오류 발생:", e);
            }
        }
    }

    const UI = {
        // 달력 Rendering
        renderCalendar() {
            const firstDay = new Date(state.currYear, state.currMonth, 1).getDay();
            const lastDate = new Date(state.currYear, state.currMonth + 1, 0).getDate();
            const today = new Date(); today.setHours(0,0,0,0);

            const limitDate = new Date();
            limitDate.setMonth(limitDate.getMonth() + 1);

            el.calendarHeader.innerHTML = config.DAY_NAMES
                .map(day => `<div class="calendar-header-day">${day}</div>`)
                .join("");

            let html = "";
            for (let i = firstDay; i > 0; i--) html += `<div class="day inactive"></div>`;

            for (let i = 1; i <= lastDate; i++) {
                const dateObj = new Date(state.currYear, state.currMonth, i);
                const dayOfWeek = dateObj.getDay();
                const dateStr = `${state.currYear}-${String(state.currMonth + 1).padStart(2, '0')}-${String(i).padStart(2, '0')}`;
                let classes = "day";
                if(dayOfWeek == 0) classes += " sunday";
                if(dayOfWeek == 6) classes += " saturday";

                if (dateObj < today) {
                    classes += " inactive out-of-range";
                } else if (dateObj > limitDate){
                    classes += " future-disabled";
                }
                if (state.selectedDate === dateStr) classes += " selected";
                if (dateObj.getTime() === today.getTime()) classes += " today";

                const holiday = config.HOLIDAYS[`${state.currMonth + 1}-${i}`];
                html += `<div class="${classes}" data-date="${dateStr}" data-day="${i}">
                            <span>${i}</span>${holiday ? `<span class="holiday-name">${holiday}</span>` : ''}
                         </div>`;
            }
            el.currentYearMonth.innerText = `${state.currYear}.${String(state.currMonth + 1).padStart(2, '0')}`;
            el.calendarDays.innerHTML = html;
        },

        // 시간 슬롯 Rendering
        renderTimeSlots(slots) {
            const createBtn = (slot) => {
                const isSelected = state.selectedTime === slot.time;
                const isDisabled = !slot.available;
                const btnClass = isDisabled ? 'disabled-time' : (isSelected ? 'btn-success text-white' : 'btn-outline-secondary');

                return `<button type="button" class="btn ${btnClass}" ${isDisabled ? 'disabled' : ''} data-time="${slot.time}">
                            ${slot.time}
                            <span class="d-block" style="font-size:0.7rem">${isDisabled ? '마감' : slot.currentCapacity + '석'}</span>
                        </button>`;
            };

            el.amSlots.innerHTML = slots.filter(s => parseInt(s.time) < 15).map(createBtn).join("");
            el.pmSlots.innerHTML = slots.filter(s => parseInt(s.time) >= 17).map(createBtn).join("");
        },

        // 요약 바 Update
        updateSummary() {
            document.getElementById("sumDate").innerText = state.displayDate || "-";
            document.getElementById("sumTime").innerText = state.selectedTime || "-";
            document.getElementById("sumPersonnel").innerText = `${state.selectedPersonnel}명`;

            const isReady = state.selectedDate && state.selectedTime;
            el.summaryBox.classList.toggle("show", isReady);
            el.summaryBox.style.display = isReady ? "flex" : "none";
        },

        // 지도 Rendering
        renderStaticMap() {
            const lat = parseFloat(document.getElementById('dinerLat')?.value);
            const lng = parseFloat(document.getElementById('dinerLng')?.value);
            const img = document.getElementById('staticMap');

            if (img && !isNaN(lat) && !isNaN(lng)) {
                img.src = `https://maps.apigw.ntruss.com/map-static/v2/raster-cors?w=750&h=500&center=${lng},${lat}&level=16&markers=pos:${lng}%20${lat}|color:Green&scale=2&X-NCP-APIGW-API-KEY-ID=${config.NAVER_CLIENT_ID}`;
            }
        },

        // 리뷰 Rendering
        renderReviews(data) {
            if(!el.reviewTbody) return;

            el.reviewTbody.innerHTML = '';

            if(!data || data.length === 0) {
                el.reviewTbody.innerHTML = '<tr><td colspan="2" class="text-center">작성한 후기가 없습니다.</td></tr>';
                return;
            }

            data.forEach(review => {
                if(review.comment.length > 30) {
                    review.comment = review.comment.substring(0, 30) + "...";
                }
                const row = `
                    <tr>
                        <td>${"⭐".repeat(review.rating)}</td>
                        <td>
                          <a href="#"
                          style="text-decoration: none"
                          data-bs-toggle="modal"
                          data-bs-target="#reviewDetailModal"
                          onclick="loadReviewDetail(${review.reviewId})">
                            ${review.comment}
                          </a>
                        </td>
                    </tr>
                `;
                el.reviewTbody.insertAdjacentHTML("beforeend", row);
            });
        },

        async loadReviewDetail(id) {
            if(!el.reviewDetail) {
                console.error("리뷰 상세 요소를 표시할 영역을 찾을 수 없습니다.");
                return;
            }

            try {
                const res = await fetch(`/api/review/${id}`);

                if(res.ok) {
                    const data = await res.json();
                    console.log("상세 리뷰 데이터:", data);
                    el.reviewDetail.innerHTML = `
                        <h5>${"⭐".repeat(data.rating)}</h5>
                        <p>${data.comment}</p>
                    `;
                }
                else {
                    const errMsg = await res.text();
                    console.error("서버 에러 메시지:", errMsg);
                    alert("상세 리뷰 로드 실패");
                }
            }
            catch(e) {
                console.error("오류 발생:", e);
            }
        },

        initModifyMode() {
            if(!state.isModifyMode) return;

            if (el.summaryBox) {
                el.summaryBox.classList.add("modify-mode");
                el.summaryBox.style.backgroundColor = "#fff3cd"; // 수정 모드용 배경색 (예시)
            }

            if (el.btnBook) {
                el.btnBook.innerText = "예약 수정하기";
                el.btnBook.classList.replace("btn-primary", "btn-warning"); // 파란색 -> 노란색
            }

            const oldDateVal = document.getElementById('oldDate')?.value; // "2025-12-30 18:30:00" 형태라고 가정
            const oldPersonnel = document.getElementById('oldPersonnel')?.value;

            if (oldDateVal) {
                const rawDate = new Date(oldDateVal);
                state.currYear = rawDate.getFullYear();
                state.currMonth = rawDate.getMonth();

                // 전송용 데이터 ("2025-12-30")
                state.selectedDate = `${state.currYear}-${String(state.currMonth + 1).padStart(2, '0')}-${String(rawDate.getDate()).padStart(2, '0')}`;
                // 시간 ("18:30")
                state.selectedTime = `${String(rawDate.getHours()).padStart(2, '0')}:${String(rawDate.getMinutes()).padStart(2, '0')}`;
                // 화면 표시용
                state.displayDate = `${state.selectedDate.replace(/-/g, '.')} (${config.DAY_NAMES[rawDate.getDay()]})`;
            }

            if (oldPersonnel) {
                state.selectedPersonnel = parseInt(oldPersonnel);
                if (el.guestCount) el.guestCount.value = state.selectedPersonnel;
            }
        }
    };

    // Event Handlers
    const Handlers = {
        init() {
            UI.initModifyMode();
            UI.renderStaticMap();

            el.calendarDays.onclick = (e) => {
                const dayEl = e.target.closest(".day:not(.inactive)");
                if (!dayEl) return;

                if(dayEl.classList.contains("future-disabled")) {
                    alert("원활한 예약 관리를 위해, 오늘부터 한 달 이내의 일정만 예약하실 수 있습니다. 양해 부탁드립니다.")
                    return;
                }

                state.selectedDate = dayEl.dataset.date;
                const d = new Date(state.selectedDate);
                state.displayDate = `${state.selectedDate.replace(/-/g, '.')} (${config.DAY_NAMES[d.getDay()]})`;

                UI.renderCalendar();
                if(state.selectedDate) API.fetchAvailability();
                UI.updateSummary();
            };

            // 시간 클릭 (위임)
            [el.amSlots, el.pmSlots].forEach(container => {
                container.onclick = (e) => {
                    const btn = e.target.closest("button:not([disabled])");
                    if (!btn) return;
                    state.selectedTime = btn.dataset.time;
                    API.fetchAvailability();
                    UI.updateSummary();
                };
            });

            // 인원 조절
            document.getElementById("btnPlus").onclick = () => Handlers.changePersonnel(1);
            document.getElementById("btnMinus").onclick = () => Handlers.changePersonnel(-1);

            // 월 이동
            document.getElementById("prevMonth").onclick = () => Handlers.moveMonth(-1);
            document.getElementById("nextMonth").onclick = () => Handlers.moveMonth(1);

            // 최종 예약 버튼
            el.btnBook.onclick = async () => {
                if (!state.selectedDate || !state.selectedTime) return alert("날짜와 시간을 선택해주세요.");

                const selectedDateObj = new Date(state.selectedDate);
                const limitDate = new Date();
                limitDate.setMonth(limitDate.getMonth() + 1);

                if(selectedDateObj > limitDate) {
                    alert("죄송합니다. 예약은 현재 날짜로부터 한 달 후까지만 가능합니다.");
                    return;
                }
                if (!confirm(state.isModifyMode ? "수정하시겠습니까?" : "예약하시겠습니까?")) return;

                const formData = new FormData(el.bookingForm);
                formData.set("bookingDate", `${state.selectedDate} ${state.selectedTime}`);
                formData.set("personnel", state.selectedPersonnel);

                const res = await API.submitBooking(formData);
                if (res.ok) { alert(await res.text()); window.location.href = "/mypage"; }
                else { alert(await res.text()); }
            };
        },

        changePersonnel(delta) {
            const next = state.selectedPersonnel + delta;
            if (next >= config.MIN_PERSONNEL && next <= config.MAX_PERSONNEL) {
                state.selectedPersonnel = next;
                el.guestCount.value = next;
                API.fetchAvailability();
                UI.updateSummary();
            }
        },

        moveMonth(delta) {
            const d = new Date(state.currYear, state.currMonth + delta);
            state.currYear = d.getFullYear();
            state.currMonth = d.getMonth();
            UI.renderCalendar();
        }
    };
    Handlers.init();
    UI.renderCalendar();
    UI.updateSummary();
    API.loadReviews();
    loadOwnerResponse();

    window.loadReviewDetail = UI.loadReviewDetail;
});

async function loadOwnerResponse() {
    const ownerId = document.getElementById("ownerId")?.value;
    const canvas = document.getElementById('responseChart');

    if (!ownerId || !canvas) return;

    try {
        const res = await fetch(`http://localhost:8000/api/owner-response/${ownerId}`);
        const data = await res.json();

        // 1. 데이터 부재 여부 판별 (API 응답의 msg 활용)
        const isNoData = (data.avg_response_min === 0 && data.msg && data.msg.includes("존재하지 않음")) || data.avg_response_min === undefined;

        // 데이터 없으면 9999로 처리해서 '느림'
        const responseTime = isNoData ? 9999 : data.avg_response_min;

        let visualValue;
        let responseText = "";
        let responseColor = "";

        if (isNoData) {
            visualValue = 0.5;
            responseText = "데이터 없음";
            responseColor = "#9ca3af";
        } else if (responseTime > 900) {
            visualValue = 0.5;
            responseText = "느림";
            responseColor = "#f87171";
        } else if (responseTime > 60) {
            visualValue = 1.5;
            responseText = "보통";
            responseColor = "#facc15";
        } else {
            visualValue = 2.5;
            responseText = "빠름";
            responseColor = "#4ade80";
        }

        const responseEl = document.getElementById("responseTimeStatus");
        if(responseEl) {
            const displayTime = isNoData ? "기록 없음" : (responseTime >= 60 ? (responseTime / 60).toFixed(1) + "시간" : responseTime + "분");
            responseEl.innerText = `${responseText} (${displayTime})`;
            responseEl.style.color = responseColor;
        }

        const ctx = canvas.getContext('2d');
        if (window.myResponseChart) window.myResponseChart.destroy();

        const gaugeNeedle = {
            id: 'gaugeNeedle',
            afterDatasetsDraw(chart) {
                const { ctx, chartArea: { width, height } } = chart;
                ctx.save();

                const needleValue = chart.config.data.datasets[0].needleValue;
                const angle = Math.PI + (Math.PI * (Math.min(needleValue, 3) / 3));
                const cx = width / 2;
                const cy = chart._metasets[0].data[0].y;

                // 바늘 그리기
                ctx.translate(cx, cy);
                ctx.rotate(angle);
                ctx.beginPath();
                ctx.moveTo(0, -3);
                ctx.lineTo(height - 40, 0);
                ctx.lineTo(0, 4);
                ctx.fillStyle = '#333';
                ctx.fill();
                ctx.restore();

                ctx.save();
                ctx.font = "bold 16px sans-serif";
                ctx.fillStyle = "#333";
                ctx.textAlign = "center";
                ctx.restore();
            }
        };

        window.myResponseChart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                datasets: [{
                    data: [1,1,1],
                    backgroundColor: ['#f87171', '#facc15', '#4ade80'],
                    needleValue: visualValue,
                    borderColor: 'white',
                    borderWidth: 2,
                    cutout: '75%',
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                rotation: 270,
                circumference: 180,
                plugins: {
                    legend: { display: false },
                    tooltip: { enabled: false }
                }
            },
            plugins: [gaugeNeedle]
        });

    } catch (e) {
        console.error("통계 로드 실패:", e);
    }
}