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
    document.getElementById("addBtn")?.addEventListener("click", () => addDiner());
    document.getElementById("btnDeleteOwner")?.addEventListener("click", () => deleteOwner());
});

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
    '#tab5': 'ownerInfo',
    '#tab6': 'ownerDiner'
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
    ownerInfo: (page) => loadMyInfo(page),
    ownerDiner: (page) => loadDinerInfoTab(page)
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
                    if (el.classList.contains('input-group')) {
                        el.style.display = "flex";
                    } else {
                        el.style.display = "block";
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
        //수정 후 초기화 로직
        const inputs = document.querySelectorAll('.is-valid, .is-invalid');
        inputs.forEach(input => input.classList.remove('is-valid', 'is-invalid'));
        const errorIds = ["error-emailId", "error-myPhone", "error-newPassword", "error-pwdConfirm"];
           errorIds.forEach(id => {
               const el = document.getElementById(id);
               if(el) el.innerText = "";
           });
       //비밀번호 입력창 값 비우기
       document.getElementById("newPassword").value = "";
       document.getElementById("pwdConfirm").value = "";

    }
}

//식당 관리 탭 식당 목록출력
async function loadDinerInfoTab() {
    const dinerId = document.getElementById('dinerSelect')?.value || "";
    const tbody = document.getElementById("owner-diner");
    tbody.innerHTML = `
    <tr>
        <td colspan="5" class="text-center">식당이 없습니다.</td>
    `;

    if(!dinerId){
        const url = "/api/owner/diners";
        try {
            const dinerData = await fetchJson(url);

            dinerData.forEach(d => {
                //식당 영업 상태에 따라 표시 내용 변경
                let badge;
                if(d.status === "PUBLIC"){
                    badge = `<span class="badge bg-success fs-6">영업 중</span>`;
                } else if(d.status === "CLOSED"){
                    badge = `<span class="badge bg-danger fs-6">영업 종료</span>`;
                }
                tbody.innerHTML = '';
                tbody.innerHTML += `
                <tr>
                    <td>${d.id}</td>
                    <td><a href="#" style="text-decoration: none;"
                        data-bs-toggle="modal"
                        onclick="openDinerDetailModal(${d.id}, '${d.tel ? d.tel : ''}')">
                        ${d.dinerName}</a></td>
                    <td>${badge}</td>
                    <td><button class = "btn btn-info btn-sm" onclick="changeStatus(${d.id})">상태 변경</button></td>
                    <td><button class = "btn btn-danger btn-sm" onclick="deleteDiner(${d.id})">삭제</button></td> 
                </tr>`
            })
        } catch(e) {
            console.log(e.status, e.message);
            alert("식당 목록을 불러오는데 실패 했습니다.")
        }
    } else if(dinerId) {
        const url = `/api/owner/diner/${dinerId}`
        try {
            const dinerData = await fetchJson(url)
            if (!dinerData) {
                throw new Error("식당정보를 가져올 수 없습니디ㅏ.");
            }

            //식당 영업 상태에 따라 표시 내용 변경
            let badge;
            if (dinerData.status === "PUBLIC") {
                badge = `<span class="badge bg-success fs-6">영업 중</span>`;
            } else if (dinerData.status === "CLOSED") {
                badge = `<span class="badge bg-danger fs-6">영업 종료</span>`;
            }
            tbody.innerHTML = '';
            tbody.innerHTML = `
            <tr>
                <td>${dinerData.id}</td>
                <td>${dinerData.dinerName}</td>
                <td>${badge}</td>
                <td><button class = "btn btn-info btn-sm" onclick="changeStatus(${dinerData.id})">상태 변경</button></td>
                <td><button class = "btn btn-danger btn-sm" onclick="deleteDiner(${dinerData.id})">삭제</button></td>
            </tr>`
        } catch (e) {
            console.error(e);
            alert("오류발생!" + e);
        }
    }
}

function openDinerDetailModal(id, currentTel) {
    document.getElementById("dinerTel").value = currentTel;

    //수정 버튼에 클릭 이벤트 핸들러 등록
    const editBtn = document.getElementById("editDiner");
    editBtn.onclick = function() {
        updateDiner(id); // 클로저를 통해 id 전달
    };

    // 모달 열기
    const modalEl = document.getElementById('dinerDetailModal');
    const modal = new bootstrap.Modal(modalEl);
    modal.show();
}

//식당 정보 수정
async function updateDiner(dinerId) {
    const dinerTel = document.getElementById("dinerTel").value.trim();

    if(!dinerTel) {
            alert("전화번호를 입력해주세요.");
            return;
        }

    try{
        const url = `/api/owner/update/${dinerId}`;
        const res = await fetchJson(url, {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({tel: dinerTel})
        });
        alert("식당 정보 수정이 완료되었습니다.");
        await loadDinerInfoTab();
    } catch (e) {
        console.error(e.status, e.message);
        alert("식당 정보 수정 중 오류가 발생했습니다.")
    }
}


// 식당 닫기
async function changeStatus(dinerId){
    const url = `/api/owner/status/${dinerId}`
    try{
         await fetchJson(url, {
            method: "PATCH",
        });
        alert("식당 상태 변경이 완료되었습니다.");
        await loadDinerInfoTab();
    } catch(e) {
        console.error(e.status, e.message);
        alert("상태 변경 중 오류가 발생했습니다.")
    }
}

async function deleteDiner(dinerId){
    //예약 존재 여부 먼저 확인
    const hasBooking = await hasActiveBookings(dinerId);
    if(hasBooking) {
        alert("현재 진행 중인 예약이 존재하여 식당을 삭제할 수 없습니다. \n 기존 예약을 먼저 처리해주세요.");
        return;
    }

    //식당 영업 상태 확인
    const dinerStatus = await checkStatus(dinerId);
    if(!dinerStatus){
        alert("식당이 영업 중인 상태입니다. \n 식당을 삭제하시려면 영업 종료 상태여야 합니다.")
        return;
    }
    const answer = confirm("삭제하면 복구할 수 없으며 다시 예약을 원하시면 식당을 재등록 하셔야 합니다. 그대로 삭제하시겠습니까?")
    if(answer) {
        const url = `/api/owner/delete/diner/${dinerId}`;
        try {
            const res = await fetch(url, {method: "DELETE"});
            if(res.ok){
                alert("식당이 삭제되었습니다.");
                window.location.reload();
            } else {
                const errMsg = await res.text();
                alert("식당을 삭제하는 데 실패했습니다." + errMsg);
            }
        } catch(err) {
            console.error(err);
            alert("오류 발생" + err);
        }
    }
}

//식당 영업 여부 확인
async function checkStatus(dinerId){
    const url = `/api/owner/diner/${dinerId}`

    try{
        const dinerStatus = await fetchJson(url);
        if(dinerStatus.status === "PUBLIC"){
            return false;
        } else if(dinerStatus.status === "CLOSED"){
            return true;
        }
    } catch(err) {
        console.error(err);
        alert("식당 영업 정보를 불러올 수 없습니다.")
    }
}

//예약 존재 여부 확인
async function hasActiveBookings(dinerId){
    try{
        //대기 중인 예약 확인
        const pendingUrl = `/api/owner?pending=true&dinerId=${dinerId}&page=0&size=1`
        const pendingRes = await fetchJson(pendingUrl);

        if(pendingRes?.totalElements > 0 || pendingRes?.content?.length > 0) {
            return true;
        }
        //확정 된 예약 중 삭제 신청일보다 미래인 예약 확인
        const approvedUrl = `/api/owner?pending=false&dinerId=${dinerId}&page=0&size=1`;
        const approvedRes = await fetchJson(approvedUrl);

        if (approvedRes?.content?.length > 0) {
            // 가져온 예약이 미래인지 확인 (간단한 체크)
            const bookDate = new Date(approvedRes.content[0].bookingDate);
            const now = new Date('2026-01-10');
            if (bookDate > now) {
                return true; // 미래의 확정된 예약 있음
            }
        }
        return false;
    } catch (e) {
        console.error("예약 확인 중 오류 발생" + e);
        return true;
    }
}
//식당 추가 모달 열기
function openModal() {
    const addDinerModal = new bootstrap.Modal(document.getElementById('addDinerModal'));
    addDinerModal.show();
}

//식당 추가를 위한 사업자 번호 조회
let isBizNumValid = false;
//사업자 번호로 조회
const bizNumBtn = document.getElementById("bizNumBtn");
if (bizNumBtn) {
    bizNumBtn.addEventListener("click", () => {
        const bizNum = document.getElementById("bizNum").value.trim();
        const reg = /^\d{10}$/;
        if (bizNum === "") {
            alert("사업자 번호를 입력해주세요.");
            return false;
        } else if(bizNum.length !== 10 || !reg.test(bizNum)){
            alert("올바른 사업자 번호 형식이 아닙니다.\n(-)를 제외한 숫자 10자리로 입력해주세요.");
            return false;
        } else if(reg.test(bizNum) && bizNum !== "" ) {
            fetchBizNum(bizNum);
        }
    });
}
// 사업자 번호 조회로 등록된 상호명과 db의 상호명 매칭
async function fetchBizNum(bizNum) {
    try {
        setLoadingState(bizNumBtn, true);
        // Controller의 경로(@RequestMapping + @GetMapping)에 맞춰 수정
        const url = `/api/owner/proxy/business-info?query=${bizNum}`;
        const res = await fetch(url);
        if (res.ok) {
            const data = await res.json();
            console.log(data);
            if(data && data.dinerName){
                document.getElementById("ownerDinerName").value = data.dinerName;
                isBizNumValid = true;
                setLoadingState(bizNumBtn, false);
                document.getElementById("bizNum").readOnly = true;
                document.getElementById("bizNumBtn").disabled = true;
                document.getElementById("bizNumBtn").innerText = "조회완료";
                alert("사업자 번호가 조회되었습니다");
            } else {
                setLoadingState(bizNumBtn, false);
                alert("식당 데이터가 올바르지 않습니다.");
                document.getElementById("ownerDinerName").value = "";
                isBizNumValid = false;
            }
        } else {
            const error = await res.text();
            alert("사업자 정보 조회에 실패했습니다." + error);
            // 사업자 번호 입력창 초기화
            setLoadingState(bizNumBtn, false);
            document.getElementById("ownerDinerName").value = "";
            isBizNumValid = false;
        }
    } catch (err) {
        console.error("사업자 정보 조회 오류:", err);
        alert("사업자 정보 조회 중 오류가 발생했습니다.");
    }
}

// owner가 식당을 추가할 경우
async function addDiner() {
    if (!isBizNumValid) {
        alert("사업자 번호를 조회해주세요.")
    }
    const dinerData = {
        dinerName: document.getElementById("ownerDinerName").value,
        businessNum: document.getElementById("bizNum").value
    };

    try {
        const url = `api/owner/add/diner`;
        const res = await fetchJson(url, {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json" // JSON을 보낸다고 명시
            },
            body: JSON.stringify(dinerData) // 객체를 문자열로 변환하여 전송
        });
        alert("식당 추가가 완료되었습니다.");
        window.location.reload(); // 페이지 새로고침하여 목록 갱신
    } catch (e) {
        console.error(e);
        alert(`식당 추가 실패\n상태코드: ${e.status}\n메시지: ${e.message}`);
    }
}

// owner 계정 삭제
async function deleteOwner() {
    const hasDiner = await hasOwnerDiner();
    const password = document.getElementById("deletePassword").value;
    if(!password) {
        alert("비밀번호를 입력해주세요.");
        return;
    }
    if(hasDiner) {
        alert("식당이 존재하는 경우 탈퇴하실 수 없습니다. \n 식당 삭제를 먼저 진행해주세요.");
        return;
    }
    const answer = confirm("계정을 삭제하면 복구하실 수 없습니다. 그래도 탈퇴하시겠습니까?")
    if (answer) {
        try {
            const url = `/api/owner/delete/owner`;
            const res = await fetchJson(url, {
                method: "DELETE",
                headers: {"Content-Type" : "application/json"},
                body: JSON.stringify({password: password})
            });
            alert("계정삭제가 완료되었습니다.");
            window.location.replace(`/`);
        } catch (err) {
            console.error(err);
            if (err.message) {
                alert("오류: " + err.message);
            } else {
                alert("계정 삭제 중 오류가 발생했습니다.");
            }
        }
    }
}

async function hasOwnerDiner() {
    const url = `/api/owner/diners`;
    const dinerList = await fetchJson(url);
    if (Array.isArray(dinerList) && dinerList.length > 0) {
        return true;
    }
    return false;
}

function setLoadingState(button, isLoading) {
        if (!button) return;
        if (isLoading) {
            button.disabled = true;
            button.dataset.originalText = button.innerHTML; // 원래 텍스트 저장
            button.innerHTML = `
                <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                조회 중...
            `;
        } else {
            button.disabled = false;
            button.innerHTML = button.dataset.originalText || "조회하기";
        }
    }

