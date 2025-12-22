let currentTab = "pending";     // pending, today, approve
let currentPage = 0;
const pageSize = 10;

document.addEventListener("DOMContentLoaded", async () => {
    await loadOwnerDiners();
    await loadPendingBookings(0);
})
document.querySelector('a[href="#tab1"]')?.addEventListener('click', () => loadPendingBookings(0));
document.querySelector('a[href="#tab2"]')?.addEventListener('click', () => loadTodayBookings(0));
document.querySelector('a[href="#tab3"]')?.addEventListener('click', () => loadApproveBookings(0));
document.getElementById("dinerSelect")?.addEventListener("change", () => {
        currentPage = 0;
        currentTab === "pending" ? loadPendingBookings() : loadTodayBookings();
    });

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

    const data = await fetchJson(url);

    if(currentTab === "pending") {
        renderPendingTable(data.content);
    }
    else if(currentTab === "today") {
        renderTodayTable(data.content);
    }
    else {
        renderApprovedTable(data.content);
    }

    currentPage = data.number;
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