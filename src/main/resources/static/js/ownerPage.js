let currentTab = "pending";     // pending, today, approve
let currentPage = 0;
let totalPages = 0;
const pageSize = 10;

// 식당 카테고리 채우기, 첫화면(승인대기 목록) load
document.addEventListener("DOMContentLoaded", async () => {
    await loadOwnerDiners();
    await loadPendingBookings(0);

document.getElementById("btnEdit")?.addEventListener("click", () => toggleEditMode(true));
    document.getElementById("btnCancel")?.addEventListener("click", () => {
        toggleEditMode(false);
        loadMyInfo(); // 취소하면 원래 데이터로 원복
    });
document.getElementById("btnSave")?.addEventListener("click", () => updateMember());
})

// add select Event
document.getElementById("dinerSelect")?.addEventListener("change", () => {
    currentPage = 0;
    tabLoaders[currentTab](0);
});

// click event 달기위한 map
const tabMenu = {
    '#tab1': 'pending',
    '#tab2': 'today',
    '#tab3': 'approve',
    '#tab4': 'reviews',
    '#tab5': 'ownerInfo'
}

// add EventListener
document.querySelectorAll('.nav-link').forEach(link => {
    link.addEventListener('click', e => {
        const targetTab = tabMenu[e.target.getAttribute('href')];
        if(targetTab) {
            currentTab = targetTab;
            currentPage = 0;
            tabLoaders[currentTab](0);
        }
    });
});

// add page move button event
document.getElementById("prevPageBtn")?.addEventListener("click", () => movePage(-1));
document.getElementById("nextPageBtn")?.addEventListener("click", () => movePage(1));

/* Tab menu */
const tabLoaders = {
    pending: (page) => loadBookings({ pending: true, page }),
    today: (page) => loadBookings({
        pending: false,
        date: new Date().toISOString().substring(0, 10),
        page
    }),
    approve: (page) => loadBookings({ pending: false, page }),
    reviews: (page) => loadReviews(page),
    ownerInfo: (page) => loadMyInfo(page)
};

// fetch Method
async function fetchJson(url, options = {}) {
    const res = await fetch(url, options);
    if(!res.ok) {
        throw {
            status: res.status,
            message: await res.text()
        };
    }
    const contentType = res.headers.get("content-type");
    if (contentType && contentType.includes("application/json")) {
        return res.json();
    }

    return null;
}

// load 식당 목록(카테고리)
async function loadOwnerDiners() {
    const select = document.getElementById("dinerSelect");
    const diners = await fetchJson("/api/owner/diners");

    select.innerHTML = `<option value="">전체</option>`;
    diners.forEach(d =>
        select.innerHTML += `<option value="${d.id}">${d.dinerName}</option>`
    );

}

// 승인대기목록, 오늘의 예약, 확정된 예약 목록
async function loadBookings({pending, date = null, page = 0}) {
    const dinerId = document.getElementById("dinerSelect")?.value || "";

    let url = `/api/owner?pending=${pending}&page=${page}&size=${pageSize}`;

    if(dinerId) url += `&dinerId=${dinerId}`;
    if(date) url += `&date=${date}`;

    try {
        const data = await fetchJson(url);
        updatePaginationInfo(data);

        if(currentTab === "pending") {
            renderPendingTable(data.content);
        }
        else if(currentTab === "today") {
            renderTodayTable(data.content);
        }
        else {
            renderApprovedTable(data.content);
        }
    }
    catch(e) {
        console.error(e);
    }
}

// load Reviews
async function loadReviews(page = 0) {
    const dinerId = document.getElementById('dinerSelect')?.value || "";
    const url = `/api/owner/reviews?dinerId=${dinerId}&page=${page}&size=${pageSize}`;

    try {
        const data = await fetchJson(url);
        updatePaginationInfo(data);
        renderReviewTable(data.content);
    }
    catch(e) {
        console.error(e);
    }
}

// 첫화면 구현을 위한 승인대기목록 load method
async function loadPendingBookings(page = 0) {
    currentTab = "pending";
    await loadBookings({pending: true, page});
}

// Rendering Table
function renderPendingTable(data) {
    const tbody = document.getElementById("bookRequestTable");

    if(!data.length) {
        return renderEmptyRow(tbody, 5, "승인 대기 예약 없음");
    }

    tbody.innerHTML = data.map(b => `
        <tr>
            <td>${b.dinerName}</td>
            <td>${formatDate(b.bookingDate)}</td>
            <td>${b.personnel}</td>
            <td>${b.memberName}</td>
            <td>
                <button class="btn btn-success btn-sm"
                        onclick="approveBooking(${b.bookId})">승인</button>
                <button class="btn btn-danger btn-sm"
                        onclick="rejectBooking(${b.bookId})">반려</button>
            </td>
        </tr>
    `).join("");
}

function renderTodayTable(data) {
    const tbody = document.getElementById("todayBookListTable");

    if(!data.length) {
        return renderEmptyRow(tbody, 4, "오늘 예약 없음");
    }

    tbody.innerHTML = data.map(b => `
        <tr>
            <td>${b.dinerName}</td>
            <td>${formatDate(b.bookingDate)}</td>
            <td>${b.personnel}</td>
            <td>${b.memberName}</td>
        </tr>
    `).join("");
}

function renderApprovedTable(data) {
    const tbody = document.getElementById("approveBookListTable");

    if(!data.length) {
        return renderEmptyRow(tbody, 4, "승인 완료 예약 없음");
    }

    tbody.innerHTML = data.map(b => `
        <tr>
            <td>${b.dinerName}</td>
            <td>${formatDate(b.bookingDate)}</td>
            <td>${b.personnel}</td>
            <td>${b.memberName}</td>
        </tr>
    `).join("");
}

function renderReviewTable(data) {
    const tbody = document.getElementById("reviewListTable");
    if(!data.length) return renderEmptyRow(tbody, 5, "작성된 리뷰가 없습니다.");

    tbody.innerHTML = data.map(r => `
        <tr>
            <td>${r.memberUsername}</td>
            <td>${r.dinerName}</td>
            <td class="text-warning">${"⭐".repeat(r.rating)}</td>
            <td>${r.comment}</td>
            <td>${formatDate(r.createTime)}</td>
        </tr>
    `).join("");
}

// process 승인 반려
async function processBooking(url, message, successMessage) {
    if(!confirm(message)) return;

    try {
        await fetchJson(url, {method: "PUT"});
        alert(successMessage);

        if(currentTab === "pending") {
            await loadPendingBookings(currentPage);
        }
    }
    catch(e) {
        console.error(e);
        alert("처리 실패");
    }
}
// 승인, 반려 처리
async function approveBooking(id) {
    await processBooking(`/api/owner/${id}/approve`, "승인하시겠습니까?", "승인 완료");
}
async function rejectBooking(id) {
    await processBooking(`/api/owner/${id}/reject`, "반려하시겠습니까?", "반려 처리 완료");
}

function renderEmptyRow(tbody, col, msg) {
    tbody.innerHTML = `
        <tr>
            <td colspan="${col}" class="text-center text-muted">${msg}</td>
        </tr>
    `;
}
// 날짜 출력 스타일
function formatDate(dt) {
    return dt.replace("T", " ").substring(0, 16);
}

/* 페이지 버튼 구현 */
function movePage(delta) {
    const nextPage = currentPage + delta;
    if (nextPage < 0 || nextPage >= totalPages) return;
    tabLoaders[currentTab](nextPage);
}

/* 페이지 번호 */
function renderPagination() {
    const ul = document.getElementById("pagination");
    if (!ul || totalPages <= 1) {
        ul.innerHTML = "";
        return;
    }

    let html = "";

    // 이전
    html += `
        <li class="page-item ${currentPage === 0 ? "disabled" : ""}">
            <a class="page-link" href="#" data-page="${currentPage - 1}">&laquo;</a>
        </li>
    `;

    const start = Math.max(0, currentPage - 2);
    const end = Math.min(totalPages - 1, currentPage + 2);

    for (let i = start; i <= end; i++) {
        html += `
            <li class="page-item ${i === currentPage ? "active" : ""}">
                <a class="page-link" href="#" data-page="${i}">
                    ${i + 1}
                </a>
            </li>
        `;
    }

    // 다음
    html += `
        <li class="page-item ${currentPage >= totalPages - 1 ? "disabled" : ""}">
            <a class="page-link" href="#" data-page="${currentPage + 1}">&raquo;</a>
        </li>
    `;

    ul.innerHTML = html;

    // 이벤트 위임
    ul.querySelectorAll("a.page-link").forEach(link => {
        link.addEventListener("click", (e) => {
            e.preventDefault();
            const page = Number(e.target.dataset.page);
            if (!isNaN(page)) {
                tabLoaders[currentTab](page);
            }
        });
    });
}

// 탭 이동 or load 된 데이터가 생기면 Page update
function updatePaginationInfo(data) {
    currentPage = data.number;
    totalPages = data.totalPages;
    renderPagination();
}

// 회원 정보 불러오기
function loadMyInfo() {
    fetch("/api/owner/info")
        .then(res => res.json())
        .then(data => {
            const emailInput = document.getElementById("emailId");

            document.getElementById("myUsername").value = data.username;
            document.getElementById("myName").value = data.name;

            let emailStyle = data.email || "";
            if (emailStyle === "@") emailStyle = "";

            const displayEl = document.getElementById("emailDisplay");
            if (displayEl) displayEl.value = emailStyle;

            if (data.email && data.email.includes("@")) {
                const parts = data.email.split("@");
                document.getElementById("emailId").value = parts[0];
                document.getElementById("emailDomainInput").value = parts[1];
            } else {
                document.getElementById("emailId").value ="";
                document.getElementById("emailDomainInput").value = "";
            }

            const phoneStyle = data.phone || "";
            document.getElementById("phoneDisplay").value = phoneStyle;
            document.getElementById("myPhone").value = phoneStyle;

            if(data.email) {
                emailInput.dataset.origin = data.email;
            }
            toggleEditMode(false);
        })
        .catch(err => console.error("회원정보 로드 실패:", err));
}

// 회원 정보 수정
function updateMember() {
    const invalidInputs = document.querySelectorAll(".tab-pane.active .is-invalid");
    if(invalidInputs.length > 0) {
        alert("입력 항목 중 오류가 있습니다. 메세지를 확인해주세요.");
        invalidInputs[0].focus();
        return;
    }

    const pwd = document.getElementById("newPassword").value;
    const pwdConfirm = document.getElementById("pwdConfirm").value;

    if(pwd && pwd !== pwdConfirm) {
        alert("비밀번호가 일치하지 않습니다.");
        return;
    }

    const emailId = document.getElementById("emailId").value.trim();
    const domain = document.getElementById("emailDomainInput").value.trim();
    let fullEmail = "";

    if (emailId !== "" && domain !== "") {
        fullEmail = emailId + "@" + domain;
    } else if (emailId === "" && domain === "") {
        fullEmail = "";
    } else {
        alert("이메일 주소를 정확히 입력해주세요.");
        return;
    }
    const phoneValue = document.getElementById("myPhone").value;

    const data = {
        email:fullEmail,
        phone: phoneValue,
        password: document.getElementById("newPassword").value,
        passwordConfirm: document.getElementById("pwdConfirm").value
    };

    fetch("/api/owner/update", {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    })
        .then(res => {
            if (res.ok) {
                alert("수정되었습니다.");
                toggleEditMode(false);
                loadMyInfo(); // 저장 성공 시 최신 정보 다시 로드
            } else {
                alert("수정 실패: 입력 정보를 확인해주세요.");
            }
        })
        .catch(err => console.error("Update Error:", err));
}

// 화면 모드 전환 함수 토글
function toggleEditMode(isEdit) {
    // 1. 보기 모드 요소들 (텍스트만 보이는 상태)
    const viewElements = [
        document.getElementById("emailDisplay"),
        document.getElementById("phoneDisplay"),
        document.getElementById("pwdViewRow"),
        document.getElementById("btnEdit")
    ];

    // 2. 수정 모드 요소들 (입력창, 저장/취소 버튼)
    const editElements = [
        document.getElementById("emailEditGroup"),
        document.getElementById("phoneEditGroup"),
        document.getElementById("pwdEditGroup"),
        document.getElementById("btnCancel"),
        document.getElementById("btnSave")
    ];

    // 3. 상태에 따라 보여주기/숨기기 토글
    if (isEdit) {
        // 수정 모드일 때
        viewElements.forEach(el => el && (el.style.display = "none"));
        editElements.forEach(el => {
            if (el) {
                // 그룹(div)인 경우 flex, 버튼 등은 inline-block 등 상황에 맞게
                if (el.classList.contains("input-group") || el.classList.contains("edit-mode-row")) {
                    el.style.display = "flex";
                } else {
                    el.style.display = "inline-block";
                }
            }
        });
        // 도메인 선택창 활성화 등 추가 로직이 필요하면 여기에 작성
        document.getElementById("emailDomainSelect").disabled = false;
        document.getElementById("emailId").readOnly = false;
        document.getElementById("emailDomainInput").readOnly = false;

    } else {
        // 보기 모드일 때 (취소하거나 저장 후)
        viewElements.forEach(el => el && (el.style.display = "block")); // 혹은 원래 스타일대로
        editElements.forEach(el => el && (el.style.display = "none"));
    }
}