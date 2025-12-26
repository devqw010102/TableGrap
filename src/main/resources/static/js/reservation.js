document.addEventListener('DOMContentLoaded', function() {
    loadReviews()

// api & 설정값
    const NAVER_CLIENT_ID = "k0np2vmny3";
    const DAY_NAMES = ["일", "월", "화", "수", "목", "금", "토"];
    const TIME_SLOTS = [
        "11:00", "11:30", "12:00", "12:30", "13:00", "13:30", "14:00",  // 오전
        "17:00", "17:30", "18:00", "18:30", "19:00", "19:30", "20:00"   // 오후
    ];
    const HOLIDAYS = {
        '1-1': '새해',
        '3-1': '삼일절',
        '5-5': '어린이날',
        '12-25': '성탄절'
    };

    let date = new Date();
    let currYear = date.getFullYear();
    let currMonth = date.getMonth();

    // 선택 데이터 상단 (하단 없앰)
    let selectedDate = "";
    let selectedTime = "";
    let selectedPersonnel = document.getElementById("guestCount")?.value || "2";

    // DOM 요소 선택
    const amSection = document.getElementById("amSlotsContainer");
    const pmSection = document.getElementById("pmSlotsContainer");
    const topSummaryBox = document.getElementById("topSummaryBox");

    const daysSection = document.querySelector("#calendar-days");
    const currentDate = document.querySelector("#currentYearMonth");
    const inputPersonnel = document.getElementById("guestCount");
    const bookingForm = document.getElementById("bookingForm");

    // 지도
    const latInput = document.getElementById('dinerLat');
    const lngInput = document.getElementById('dinerLng');
    const staticMapImg = document.getElementById('staticMap');

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

    // 상단 요약 업데이트
    const updateAllSummaries = () => {
        // 선택된 값
        const isAllSelected = selectedDate !== "" && selectedTime !== "";

        const sumDate = document.getElementById("sumDate");
        const sumTime = document.getElementById("sumTime");
        const sumPersonnel = document.getElementById("sumPersonnel");

        if(sumDate) sumDate.innerText = selectedDate || "-";
        if(sumTime) sumTime.innerText = selectedTime || "-";
        if(sumPersonnel) sumPersonnel.innerText = `${selectedPersonnel}명`;

        // 모두 선택하면 뿅
        if (topSummaryBox) {
            if (isAllSelected) {
                topSummaryBox.style.display = "flex";
                setTimeout(() => topSummaryBox.classList.add("show"), 10);
            } else {
                topSummaryBox.classList.remove("show");
                topSummaryBox.style.display = "none";
            }
        }
    };

    // 달력 오늘부터 1년 뒤까지만 선택 가능하게( 특정 날짜는 아니고 달로 26년 12월까지)
    // 달력 렌더링
    const renderCalendar = () => {
        const firstDayofMonth = new Date(currYear, currMonth, 1).getDay();
        const lastDateofMonth = new Date(currYear, currMonth + 1, 0).getDate();
        let liTag = "";
        const todayObj = new Date();
        todayObj.setHours(0, 0, 0, 0);

        for (let i = firstDayofMonth; i > 0; i--) liTag += `<div class="day inactive"></div>`;

        for (let i = 1; i <= lastDateofMonth; i++) {
            const checkDateObj = new Date(currYear, currMonth, i);
            const checkDateStr = `${currMonth + 1}-${i}`;
            let statusClass = "";
            const formattedCheck = `${currYear}.${String(currMonth + 1).padStart(2, '0')}.${String(i).padStart(2, '0')}`;

            if (checkDateObj.getTime() === todayObj.getTime()) statusClass = "today";
            if (selectedDate.includes(formattedCheck)) statusClass += " selected";
            if (checkDateObj < todayObj) statusClass += " inactive out-of-range";

            const holidayText = HOLIDAYS[checkDateStr] ? `<span class="holiday-name">${HOLIDAYS[checkDateStr]}</span>` : '';
            liTag += `<div class="day ${statusClass}" data-day="${i}"><span>${i}</span>${holidayText}</div>`;
        }
        currentDate.innerText = `${currYear}.${String(currMonth + 1).padStart(2, '0')}`;
        daysSection.innerHTML = liTag;
        attachDateClickEvents();
    };

    // 날짜 클릭
    const attachDateClickEvents = () => {
        document.querySelectorAll(".day").forEach(day => {
            day.addEventListener("click", () => {
                if (day.classList.contains("out-of-range")) {
                    alert("예약 가능한 날짜가 아닙니다.(오늘부터 1년 이내만 가능합니다)");
                    return;
                }
                document.querySelector(".day.selected")?.classList.remove("selected");
                day.classList.add("selected");

                const dayVal = day.getAttribute("data-day");
                const selDate = new Date(currYear, currMonth, dayVal);
                selectedDate = `${currYear}.${String(currMonth + 1).padStart(2, '0')}.${String(dayVal).padStart(2, '0')} (${DAY_NAMES[selDate.getDay()]})`;
                // selectedTime = "";
                renderTimeSlots(selDate.toDateString() === new Date().toDateString());
                updateAllSummaries();
            });
        });
    };

    // 시간 오전/오후 분리 및 지난 시간 비활성화
    // 시간 슬롯 렌더링
    const renderTimeSlots = (isToday = false) => {
        const now = new Date();
        const currentHour = now.getHours();
        const currentMinute = now.getMinutes();

        const createButton = (time) => {
            const [hour, minute] = time.split(':').map(Number);
            let isDisabled = isToday && (hour < currentHour || (hour === currentHour && minute <= currentMinute));
            const isSelected = (time === selectedTime);
            const activeClass = isSelected ? 'btn-success text-white' : 'btn-outline-secondary';

            return `<button type="button" class="btn ${isDisabled ? 'disabled-time' : activeClass}" ${isDisabled ? 'disabled' : ''}>${time}</button>`;
        };
        // 오전 7개 오후 7개
        if (amSection) amSection.innerHTML = TIME_SLOTS.filter(t => parseInt(t.split(':')[0]) <= 14).map(createButton).join("");
        if (pmSection) pmSection.innerHTML = TIME_SLOTS.filter(t => parseInt(t.split(':')[0]) >= 17).map(createButton).join("");
        attachTimeClickEvents();
    };

    // 시간 클릭 이벤트 분리
    function attachTimeClickEvents() {
        document.querySelectorAll(".time-slots-grid .btn").forEach(btn => {
            btn.addEventListener("click", function() {
                if (this.classList.contains('disabled-time')) return;
                document.querySelectorAll(".time-slots-grid .btn").forEach(b => {
                    b.classList.remove("btn-success", "text-white");
                    b.classList.add("btn-outline-secondary");
                });
                this.classList.replace("btn-outline-secondary", "btn-success");
                this.classList.add("text-white");
                selectedTime = this.innerText;
                updateAllSummaries();
            });
        });
    }

    // 인원 클릭
    document.getElementById("btnMinus")?.addEventListener("click", () => {
        if (parseInt(inputPersonnel.value) > 1) {
            inputPersonnel.value = --selectedPersonnel;
            updateAllSummaries();
        }
    });
    // 인원수 맥스 20
    document.getElementById("btnPlus")?.addEventListener("click", () => {
        if (parseInt(inputPersonnel.value) < 20) {
            inputPersonnel.value = ++selectedPersonnel;
            updateAllSummaries();
        }
    });
    // 달력 클릭
    document.querySelectorAll("#prevMonth, #nextMonth").forEach(icon => {
        icon.addEventListener("click", () => {
            const nextMonthDate = icon.id === "prevMonth" ? new Date(currYear, currMonth, - 1) : new Date(currYear, currMonth + 1);
            const today = new Date();
            const oneYearLater = new Date();
            oneYearLater.setFullYear(today.getFullYear() + 1);

            if (nextMonthDate < new Date(today.getFullYear(), today.getMonth(), 1) ||
                nextMonthDate > new Date(oneYearLater.getFullYear(), oneYearLater.getMonth(), 1)) {
            alert("예약은 오늘부터 1년 이내만 가능합니다.");
            return;
        }
        currMonth = nextMonthDate.getMonth();
        currYear = nextMonthDate.getFullYear();
        renderCalendar();
    });
});

    // 수정 모드 초기화
    const bookIdInput = document.querySelector('input[name="bookId"]');

    if(bookIdInput && bookIdInput.value) {
        if (topSummaryBox) {
           topSummaryBox.classList.add("modify-mode");
        }
        const oldDateInput = document.getElementById('oldDate');
        const oldPersonnelInput = document.getElementById('oldPersonnel');
        if (oldPersonnelInput) {
            selectedPersonnel = oldPersonnelInput.value;
            if(inputPersonnel) inputPersonnel.value = selectedPersonnel;
        }
        if (oldDateInput && oldDateInput.value) {
            const rawDate = new Date(oldDateInput.value);
            currYear = rawDate.getFullYear(); currMonth = rawDate.getMonth();
            selectedDate = `${currYear}.${String(currMonth+1).padStart(2,'0')}.${String(rawDate.getDate()).padStart(2,'0')} (${DAY_NAMES[rawDate.getDay()]})`;
            selectedTime = `${String(rawDate.getHours()).padStart(2,'0')}:${String(rawDate.getMinutes()).padStart(2,'0')}`;
        }
        if (document.getElementById("btnBook")) {
            const btnBook = document.getElementById("btnBook");
            btnBook.innerText = "수정하기";
            btnBook.classList.add("modify-btn");
        }    }

    // Msg
    document.getElementById("btnBook")?.addEventListener("click", async function () {
      if(!selectedDate || !selectedTime) {
          alert("날짜와 시간을 선택해주세요.");
          return;
      }
      const dateOnly = selectedDate.split(' ')[0].replace(/\./g, '-');
      const fullBookingDate = `${dateOnly} ${selectedTime}`;
      const dinerName = document.getElementById("dinerName")?.innerText || "식당";

      const formData = new FormData(bookingForm);
      formData.set("bookingDate", fullBookingDate);
      formData.set("personnel", selectedPersonnel);

      const bookIdInput = document.querySelector('input[name="bookId"]');
      if(bookIdInput && bookIdInput.value) {
          formData.set("bookId", bookIdInput.value);
      }
      if(confirm(bookIdInput?.value ? "수정하시겠습니까?" :"예약하시겠습니까?")) {
          try {
              const response = await fetch("/api/myPage/reservation", {
                  method: "POST",
                  body: new URLSearchParams(formData)
              });

              if(response.ok) {
                  const successMsg = await response.text();
                  alert(successMsg);
                  window.location.href = "/mypage";
              } else {
                  const errorMsg = await response.text();
                  console.log("Server Error Message:", errorMsg)
                  alert(errorMsg);
              }
          } catch (error) {
              console.error("Error:", error);
              alert("통신 중 오류가 발생했습니다.")
          }
      }
    });

    // 실행
    renderCalendar();
    if(selectedDate) {
        const [y, m, d] = selectedDate.split(' ')[0].split('.').map(Number);
        renderTimeSlots(new Date(y, m-1, d).toDateString() === new Date().toDateString());
    } else {
        renderTimeSlots();
    }
    updateAllSummaries();
    //loadReviews();
});

    //async 리뷰 불러오기 함수
    async function loadReviews() {
        // HTML의 hidden input에서 dinerId를 가져오기
        const dinerIdInput = document.querySelector('input[name="dinerId"]');
        const dinerId = dinerIdInput ? dinerIdInput.value : null;

        if (!dinerId) {
            console.error("식당 ID를 찾을 수 없습니다.");
            return;
        }

        const url = `/api/review/reservation/list?dinerId=${dinerId}`;

        try {
            const res = await fetch(url);
            if (res.ok) {
                const data = await res.json();
                console.log("받아온 데이터:", data);
                renderReviews(data);

                //후기 탭 제목
                const reviewCnt = data.length;
                const reviewTab = document.getElementById("review-tab");
                if(reviewTab){
                    reviewTab.innerText = `⭐ 후기(${reviewCnt})`;
                }
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
          //글자 수 초과시 ... 처리
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
            tbody.insertAdjacentHTML('beforeend', row);
        });
    }

    //리뷰모달에 상세 리뷰 출력
    async function loadReviewDetail(reviewId) {
        const reviewDetail = document.getElementById("reviewDetail");
        if(!reviewDetail) {
          console.error("리뷰 상세 요소를 표시할 영역을 찾을 수 없습니다.");
          return;
        }
        const url = `/api/review/${reviewId}`;
        try {
            const res = await fetch(url);
            if (res.ok) {
                const data = await res.json();
                console.log("상세 리뷰 데이터:", data);
                reviewDetail.innerHTML = `
                    <h5>${"⭐".repeat(data.rating)}</h5>
                    <p>${data.comment}</p>
                `;
            } else {
                const errMsg = await res.text();
                console.error("서버 에러 메시지:", errMsg);
                alert("상세 리뷰 로드 실패");
            }
        } catch(e) {
            console.error("오류 발생:", e);
        }
    }