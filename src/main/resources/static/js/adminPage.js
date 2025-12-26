const pageSize = 10;

document.addEventListener("DOMContentLoaded", () => {
    /* Tab event */
    const tabActions = {
        "#list": () => loadDiners(0),
        "#stats": () => loadMembers(0),
        "#reservationList": () => loadReservations(0),
        "#ownerList": () => loadOwners(0),
        "#reviewList": () => loadReviews(0),
        "#dashboard": loadDashboard
    };

    Object.entries(tabActions).forEach(([hash, handler]) => {
        document.querySelector(`a[href="${hash}"]`)
            ?.addEventListener("click", handler);
    });

    // 중복 업로드 방지
    const uploadForm = document.getElementById("excelUploadForm")
    const submitBtn = uploadForm?.querySelector('button[type="submit"]');
    if (uploadForm && submitBtn) {
        uploadForm.addEventListener("submit", () => {
            submitBtn.disabled = true;
            submitBtn.innerHTML = `
                <span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                업로드 중...
            `;
        });
    }
});

/* Create fetch method */
async function fetchJson(url, options = {}) {
    const res = await fetch(url, options);

    if (!res.ok) {
        const msg = await res.text();
        throw { status: res.status, message: msg };
    }

    return res.status === 204 ? null : res.json();
}

/* empty column rendering method */
function renderEmptyRow(tbody, colSpan, message) {
    if (!tbody) return;

    tbody.innerHTML = `
        <tr>
            <td colspan="${colSpan}" class="text-center text-muted">
                ${message}
            </td>
        </tr>
    `;
}

/* Table rendering method */
function renderTable({ tbodyId, colSpan, data, rowRenderer, emptyMessage }) {
    const tbody = document.getElementById(tbodyId);
    if (!tbody) return;

    if (!data || data.length === 0) {
        renderEmptyRow(tbody, colSpan, emptyMessage);
        return;
    }

    tbody.innerHTML = data.map(rowRenderer).join("");
}

function formatDate(dt) {
    return dt?.replace("T", " ").substring(0, 16) ?? "-";
}

/* 식당 목록 */
async function loadDiners(page = 0) {
    try {
        const url = `/api/adminPage/diners?page=${page}&size=${pageSize}`;
        const data = await fetchJson(url);

        renderTable({
            tbodyId: "dinerTable",
            colSpan: 6,
            data: data.content,
            emptyMessage: "등록된 식당이 없습니다.",
            rowRenderer: d => `
                <tr>
                    <td>${d.id}</td>
                    <td>${d.category}</td>
                    <td>${d.dinerName}</td>
                    <td>${d.location}</td>
                    <td>${d.tel ?? "-"}</td>
                    <td>${d.ownerName}</td>
                </tr>
            `
        });

        renderPagination('list', data, 'loadDiners');
    } catch {
        renderEmptyRow(
            document.getElementById("dinerTable"),
            6,
            "식당 목록을 불러오지 못했습니다."
        );
    }
}

/* 회원 목록 */
async function loadMembers(page = 0) {
    try {
        const url = `/api/adminPage/members?page=${page}&size=${pageSize}`;
        const data = await fetchJson(url);

        renderTable({
            tbodyId: "memberTable",
            colSpan: 5,
            data: data.content,
            emptyMessage: "회원 정보가 없습니다.",
            rowRenderer: m => `
                <tr>
                    <td>${m.id}</td>
                    <td>${m.name}</td>
                    <td>${m.username}</td>
                    <td>${m.phone ?? "-"}</td>
                    <td>${m.email}</td>
                </tr>
            `
        });

        renderPagination('stats', data, 'loadMembers');
    } catch {
        renderEmptyRow(
            document.getElementById("memberTable"),
            5,
            "회원 정보를 불러오지 못했습니다."
        );
    }
}

/* 예약 목록 */
async function loadReservations(page = 0) {
    try {
        const url = `/api/adminPage/books?page=${page}&size=${pageSize}`;
        const data = await fetchJson(url);

        renderTable({
            tbodyId: "reservationTable",
            colSpan: 7,
            data: data.content,
            emptyMessage: "예약 목록이 없습니다.",
            rowRenderer: b => `
                <tr>
                    <td>${b.id}</td>
                    <td>${formatDate(b.createDate)}</td>
                    <td>${b.dinerName}</td>
                    <td>${formatDate(b.bookingDate)}</td>
                    <td>${b.personnel}</td>
                    <td>${b.memberName}</td>
                    <td>${b.success}</td>
                </tr>
            `
        });

        renderPagination('reservationList', data, 'loadReservations');
    } catch (e) {
        renderEmptyRow(
            document.getElementById("reservationTable"),
            7,
            e.message || "예약 목록을 불러오지 못했습니다."
        );
    }
}

/* 사장 목록 */
async function loadOwners(page = 0) {
    try {
        const url = `/api/adminPage/owners?page=${page}&size=${pageSize}`;
        const data = await fetchJson(url);

        renderTable({
            tbodyId: "ownerListTable",
            colSpan: 6,
            data: data.content,
            emptyMessage: "사장 목록이 없습니다.",
            rowRenderer: o => `
                <tr>
                    <td>${o.dinerName}</td>
                    <td>${o.category}</td>
                    <td>${o.ownerName}</td>
                    <td>${o.phone}</td>
                    <td>${o.email}</td>
                    <td>${o.status}</td>
                </tr>
            `
        });

        renderPagination('ownerList', data, 'loadOwners');
    } catch (e) {
        renderEmptyRow(
            document.getElementById("ownerListTable"),
            6,
            e.message || "사장 목록을 불러오지 못했습니다."
        );
    }
}

/* 리뷰 목록 */
async function loadReviews(page = 0) {
    try {
        const url = `/api/adminPage/reviews?page=${page}&size=${pageSize}`;
        const data = await fetchJson(url);

        renderTable({
            tbodyId: "reviewListTable",
            colSpan: 6,
            data: data.content,
            emptyMessage: "리뷰 목록이 없습니다.",
            rowRenderer: r => `
                <tr>
                    <td>${r.reviewId}</td>
                    <td>${r.memberUsername}</td>
                    <td>${r.dinerName}</td>
                    <td class = "text-warning">${"⭐".repeat(r.rating)}</td>
                    <td>${r.comment}</td>
                    <td>${formatDate(r.createTime)}</td>
                </tr>
            `
        });

        renderPagination('reviewList', data, 'loadReviews');
    } catch (e) {
        renderEmptyRow(
            document.getElementById("reviewListTable"),
            6,
            e.message || "리뷰 목록을 불러오지 못했습니다."
        );
    }
}

/* 대시보드 */
async function loadDashboard() {
    try {
        const url = `/api/adminPage/dashboard`;
        const data = await fetchJson(url);

        document.getElementById("dashboardDinerCount").innerText = data.dinerCount;
        document.getElementById("dashboardTodayBooking").innerText = data.todayBookingCount;
        document.getElementById("dashboardMemberCount").innerText = data.memberCount;
        document.getElementById("dashboardUserCount").innerText = data.userCount;
        document.getElementById("dashboardOwnerCount").innerText = data.ownerCount;

        renderTable({
            tbodyId: "dashboardReviewTable",
            colSpan: 6,
            data: data.todayReviews,
            emptyMessage: "오늘 작성된 리뷰가 없습니다.",
            rowRenderer: r => `
                <tr>
                    <td>${r.reviewId}</td>
                    <td>${r.memberUsername}</td>
                    <td>${r.dinerName}</td>
                    <td class = "text-warning">${"⭐".repeat(r.rating)}</td>
                    <td>${r.comment}</td>
                    <td>${formatDate(r.createTime)}</td>
                </tr>
            `
        });


    } catch {
        console.error("대시보드 로딩 실패");
    }
}

/* Rendering pagination */
function renderPagination(targetTab, data, loadFunction) {
    const paginationId = targetTab + "Pagination";

    let nav = document.getElementById(paginationId);

    if (!nav) {
        const tabPane = document.getElementById(targetTab);
        nav = document.createElement("nav");
        nav.id = paginationId;
        nav.className = "d-flex justify-content-center mt-3";
        tabPane.appendChild(nav);
    }

    const { totalPages, number } = data;
    let html = `<ul class="pagination pagination-sm">`;

    // 이전 버튼
    html += `<li class="page-item ${number === 0 ? 'disabled' : ''}">
                <a class="page-link" href="javascript:void(0)" onclick="${loadFunction}(${number - 1})">이전</a>
             </li>`;

    // 페이지 번호 (최대 5개 표시 예시)
    for (let i = 0; i < totalPages; i++) {
        if (i >= number - 2 && i <= number + 2) {
            html += `<li class="page-item ${i === number ? 'active' : ''}">
                        <a class="page-link" href="javascript:void(0)" onclick="${loadFunction}(${i})">${i + 1}</a>
                     </li>`;
        }
    }

    // 다음 버튼
    html += `<li class="page-item ${number >= totalPages - 1 ? 'disabled' : ''}">
                <a class="page-link" href="javascript:void(0)" onclick="${loadFunction}(${number + 1})">다음</a>
             </li>`;

    html += `</ul>`;
    nav.innerHTML = html;
}

function switchToReviewTab() {
    const reviewTabLink = document.querySelector('a[href="#reviewList"]');

    if(reviewTabLink) {
        reviewTabLink.click();
    }
    else {
        console.error("리뷰 목록 탭을 찾을 수 없습니다.");
    }
}