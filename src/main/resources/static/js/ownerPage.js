let currentTab = "pending";     // pending, today, approve
let currentPage = 0;
let totalPages = 0;
const pageSize = 10;

document.addEventListener("DOMContentLoaded", async () => {
    await loadOwnerDiners();
    await loadPendingBookings(0);
})

const tabMenu = {
    '#tab1': 'pending',
    '#tab2': 'today',
    '#tab3': 'approve',
    '#tab4': 'reviews'
}

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

document.getElementById("dinerSelect")?.addEventListener("change", () => {
        currentPage = 0;
        tabLoaders[currentTab](0);
});

/* 페이지 버튼 */
const tabLoaders = {
    pending: (page) => loadBookings({ pending: true, page }),
    today: (page) => loadBookings({
        pending: false,
        date: new Date().toISOString().substring(0, 10),
        page
    }),
    approve: (page) => loadBookings({ pending: false, page }),
    reviews: (page) => loadReviews(page)
};


document.getElementById("prevPageBtn")?.addEventListener("click", () => movePage(-1));
document.getElementById("nextPageBtn")?.addEventListener("click", () => movePage(1));

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

async function loadOwnerDiners() {
    const select = document.getElementById("dinerSelect");
    const diners = await fetchJson("/api/owner/diners");

    select.innerHTML = `<option value="">전체</option>`;
    diners.forEach(d =>
        select.innerHTML += `<option value="${d.id}">${d.dinerName}</option>`
    );

}

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

async function loadPendingBookings(page = 0) {
    currentTab = "pending";
    await loadBookings({pending: true, page});
}
async function loadTodayBookings(page = 0) {
    currentTab = "today";
    const today = new Date().toISOString().substring(0, 10);
    await loadBookings({pending: false, date: today, page});
}
async function loadApproveBookings(page = 0) {
    currentTab = "approve";
    await loadBookings({pending: false, page});
}

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

function updatePaginationInfo(data) {
    currentPage = data.number;
    totalPages = data.totalPages;
    renderPagination();
}
