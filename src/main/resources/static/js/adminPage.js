// Tab-head
document.addEventListener("DOMContentLoaded", () => {
    document.querySelector('a[href="#list"]').addEventListener('click', loadDiners);
    document.querySelector('a[href="#stats"]').addEventListener('click', loadMembers);
    document.querySelector('a[href="#ownerRequestList"]').addEventListener('click', loadOwnerRequests);
    document.querySelector('a[href="#reservationList"]').addEventListener('click', loadReservations);
    document.querySelector('a[href="#ownerList"]').addEventListener('click', loadOwners);
    document.querySelector('a[href="#reviewList"]').addEventListener('click', loadReviews);
    document.querySelector('a[href="#dashboard"]').addEventListener('click', loadDashboard);
})


// async function 으로 수정한 이유(아래 모든 메소드와 동일 사유)
// answer) 실패에 대한 반응이 없음, 이를 추가하기위해 status 기반 분기 가능

// 식당 목록 fetch
async function loadDiners() {

    const tbody = document.getElementById('dinerTable');

    try {
        const res = await fetch("/api/adminPage/diners")
        if(!res.ok) {
            renderEmptyRow(tbody, 7, "식당 목록을 불러오지 못했습니다.");
            return;
        }

        const data = await res.json();
        if(!data || data.length === 0) {
            renderEmptyRow(tbody, 7, "등록된 식당이 없습니다.");
            return;
        }

        tbody.innerHTML = '';
        data.forEach(diner => {
            tbody.innerHTML += `
                <tr>
                    <td>${diner.id}</td>
                    <td>${diner.category}</td>
                    <td>${diner.dinerName}</td>
                    <td>${diner.location}</td>
                    <td>${diner.tel ?? "-"}</td>
                    <td>${diner.dx ?? "-"}</td>
                    <td>${diner.dy ?? "-"}</td>
                </tr>
            `;
        });
    }
    catch(e) {
        console.error(e);
        renderEmptyRow(tbody, 7, "네트워크 오류가 발생했습니다.");
    }
}

// 회원 목록 fetch
async function loadMembers() {

    const tbody = document.getElementById('memberTable');

    try {
        const res = await fetch("/api/adminPage/members");
        if (!res.ok) {
            renderEmptyRow(tbody, 5, "회원 정보를 불러오지 못했습니다.");
            return;
        }

        const data = await res.json();
        if (!data || data.length === 0) {
            renderEmptyRow(tbody, 5, "회원 정보가 없습니다.");
            return;
        }

        tbody.innerHTML = '';
        data.forEach(m => {
            tbody.innerHTML += `
                <tr>
                    <td>${m.id}</td>
                    <td>${m.name}</td>
                    <td>${m.username}</td>
                    <td>${m.phone ?? "-"}</td>
                    <td>${m.email}</td>
                </tr>
            `;
        });
    } catch (e) {
        console.error(e);
        renderEmptyRow(tbody, 5, "네트워크 오류가 발생했습니다.");
    }
}

// 권한 신청 목록 fetch
async function loadOwnerRequests() {

    const tbody = document.getElementById('ownerRequestTable');

    try {
        const res = await fetch("/api/adminPage/owner-requests")
        if (!res.ok) {
            const errorMsg = await res.text();
            renderEmptyRow(tbody, 6, errorMsg || "권한 신청 목록을 불러오지 못했습니다.");
            return;
        }

        const data = await res.json();
        if (!data || data.length === 0) {
            renderEmptyRow(tbody, 6, "권한 신청 내역이 없습니다.");
            return;
        }

        tbody.innerHTML = '';
        data.forEach(r => {
            tbody.innerHTML += `
                <tr>
                    <td>${r.id}</td>
                    <td>${r.memberUsername}</td>
                    <td>${r.dinerName}</td>
                    <td>${statusBadge(r.status)}</td>
                    <td>${r.createdAt.substring(0, 10)}</td>
                    <td>
                        ${r.status === 'PENDING' ? `
                            <button class="btn btn-success btn-sm"
                                onclick="approveRequest(${r.id})">승인</button>
                            <button class="btn btn-danger btn-sm"
                                onclick="rejectRequest(${r.id})">반려</button>
                        ` : '-'}
                    </td>
                </tr>
            `;
        });
    } catch (e) {
        console.error(e);
        renderEmptyRow(tbody, 6, "네트워크 오류가 발생했습니다.");
    }
}

// 현재 신청 상태 column design
function statusBadge(status) {
    if (status === "PENDING") {
        return `<span class="badge bg-warning">대기</span>`;
    }
    if (status === "APPROVED") {
        return `<span class="badge bg-success">승인</span>`;
    }
    return `<span class="badge bg-secondary">반려</span>`;
}

// 예약 목록 fetch
async function loadReservations() {
    const tbody = document.getElementById('reservationTable');

    try {
        const res = await fetch("/api/adminPage/books")
        if(!res.ok) {
            const errorMsg = await res.text();
            renderEmptyRow(tbody, 7, errorMsg || "예약 목록을 불러오지 못했습니다.");
            return
        }

        const data = await res.json();
        if (!data || data.length === 0) {
            renderEmptyRow(tbody, 7, "예약 목록이 없습니다.");
            return;
        }

        tbody.innerHTML = '';
        data.forEach(b => {
            tbody.innerHTML += `
                <tr>
                    <td>${b.id}</td>
                    <td>${formatDate(b.createDate)}</td>
                    <td>${b.dinerName}</td>
                    <td>${formatDate(b.bookingDate)}</td>
                    <td>${b.personnel}</td>
                    <td>${b.memberName}</td>
                    <td>${b.success}</td>
                </tr>
            `;
        })
    }
    catch (e) {
        console.error(e);
        renderEmptyRow(tbody, 7, "네트워크 오류가 발생했습니다.");
    }
}

// 식당 사장 fetch
async function loadOwners() {
    const tbody = document.getElementById('ownerListTable');

    try {
        const res = await fetch("/api/adminPage/owners")
        if(!res.ok) {
            const errorMsg = await res.text();
            renderEmptyRow(tbody, 6, errorMsg || "사장 목록을 불러오지 못했습니다.");
            return;
        }

        const data = await res.json();
        if (!data || data.length === 0) {
            renderEmptyRow(tbody, 6, "사장 목록이 없습니다.");
            return;
        }

        tbody.innerHTML = '';
        data.forEach(o => {
            tbody.innerHTML += `
                <tr>
                    <td>${o.ownerName}</td>
                    <td>${o.email}</td>
                    <td>${o.phone}</td>
                    <td>${o.dinerName}</td>
                    <td>${o.category}</td>
                    <td>${o.status}</td>
                </tr>
            `;
        })
    }
    catch(e) {
        console.error(e);
        renderEmptyRow(tbody, 6, "네트워크 오류가 발생했습니다.");
    }
}

// 리뷰 fetch
async function loadReviewsCommon({ url, tbodyId, emptyMessage }) {

    const tbody = document.getElementById(tbodyId);

    try {
        const res = await fetch(url);

        if (!res.ok) {
            const errorMsg = await res.text();
            renderEmptyRow(tbody, 6, errorMsg || "리뷰 목록을 불러오지 못했습니다.");
            return;
        }

        const data = await res.json();

        if (!data || data.length === 0) {
            renderEmptyRow(tbody, 6, emptyMessage);
            return;
        }

        tbody.innerHTML = '';
        data.forEach(r => {
            tbody.innerHTML += `
                <tr>
                    <td>${r.reviewId}</td>
                    <td>${r.memberUsername}</td>
                    <td>${r.dinerName}</td>
                    <td>${r.rating}</td>
                    <td>${r.comment}</td>
                    <td>${formatDate(r.createTime)}</td>
                </tr>
            `;
        });

    } catch (e) {
        console.error(e);
        renderEmptyRow(tbody, 6, "네트워크 오류가 발생했습니다.");
    }
}

async function loadReviews() {
    await loadReviewsCommon({
        url: "/api/adminPage/reviews",
        tbodyId: "reviewListTable",
        emptyMessage: "리뷰 목록이 없습니다."
    });
}

// Dashboard fetch
async function loadDashboard() {
    try {
        const res = await fetch("/api/adminPage/dashboard");

        if(!res.ok) {
            console.error("대시보드 요약 실패");
            return;
        }

        const data = await res.json();
        renderDashboardSummary(data);
        renderDashboardReviews(data.todayReviews);
    }
    catch(e) {
        console.error("대시보드 요약 오류");
    }
}

function renderDashboardSummary(data) {
    document.getElementById("dashboardDinerCount").innerText = data.dinerCount;
    document.getElementById("dashboardTodayBooking").innerText = data.todayBookingCount;
    document.getElementById("dashboardMemberCount").innerText = data.memberCount;
    document.getElementById("dashboardUserCount").innerText = data.userCount;
    document.getElementById("dashboardOwnerCount").innerText = data.ownerCount;
}

function renderDashboardReviews(reviews) {
    const tbody = document.getElementById("dashboardReviewTable");
    tbody.innerHTML = "";

    if (!reviews || reviews.length === 0) {
        renderEmptyRow(tbody, 6, "오늘 작성된 리뷰가 없습니다.");
        return;
    }

    reviews.forEach(r => {
        tbody.innerHTML += `
            <tr>
                <td>${r.reviewId}</td>
                <td>${r.memberUsername}</td>
                <td>${r.dinerName}</td>
                <td>${r.rating}</td>
                <td>${r.comment}</td>
                <td>${formatDate(r.createTime)}</td>
            </tr>
        `;
    });

}

// load 시키는 메소드에서 만약 fetch 과정에서 문제가 있거나 데이터가 없을경우 실행
function renderEmptyRow(tbody, colSpan, message) {
    if(!tbody) return;

    tbody.innerHTML = `
        <tr>
            <td colspan="${colSpan}" class="text-center text-muted">
                ${message}
            </td>
        </tr>
    `;
}

// 승인, 반려 실행시킬 메소드
// + url 주소, 성공 메시지를 제외한 나머지코드가 동일하므로 객체화 시킴
// ++ 승인, 반려 외 대기(인증확인) 등 여러 상태가 추가되도 url, message 만 바꾸어서 이 메소드에 넣으면 정상 작동
async function processOwnerRequest({url, successMessage}) {
    try {
        const res = await fetch(url, {method : "put"});
        if(res.ok) {
            alert(successMessage);
            await loadOwnerRequests();
            return;
        }

        const errorMsg = await res.text();

        switch (res.status) {
            case 400:
            case 409:
                alert(errorMsg);
                break;
            case 403:
                alert("권한이 없습니다.");
                break;
            default:
                alert("처리 중 오류가 발생했습니다.");
        }
    }
    catch(e) {
        console.error(e);
        alert("서버 통신 오류");
    }
}

// 승인 url, message
async function approveRequest(id) {
    if(!confirm("승인하시겠습니까?")) return;
    await processOwnerRequest({url: `/api/adminPage/owner-requests/${id}/approve`, successMessage: "승인 완료"});
}

// 반려 url, message
async function rejectRequest(id) {
    if (!confirm("반려하시겠습니까?")) return;
    await processOwnerRequest({url: `/api/adminPage/owner-requests/${id}/reject`, successMessage: "반려 처리 완료"});
}

function formatDate(dt) {
    return dt.replace("T", " ").substring(0, 16);
}