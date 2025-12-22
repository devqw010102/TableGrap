/* Tab event */
document.addEventListener("DOMContentLoaded", () => {
    const tabActions = {
        "#list": loadDiners,
        "#stats": loadMembers,
        "#ownerRequestList": loadOwnerRequests,
        "#reservationList": loadReservations,
        "#ownerList": loadOwners,
        "#reviewList": loadReviews,
        "#dashboard": loadDashboard
    };

    Object.entries(tabActions).forEach(([hash, handler]) => {
        document.querySelector(`a[href="${hash}"]`)
            ?.addEventListener("click", handler);
    });
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
async function loadDiners() {
    try {
        const data = await fetchJson("/api/adminPage/diners");

        renderTable({
            tbodyId: "dinerTable",
            colSpan: 7,
            data,
            emptyMessage: "등록된 식당이 없습니다.",
            rowRenderer: d => `
                <tr>
                    <td>${d.id}</td>
                    <td>${d.category}</td>
                    <td>${d.dinerName}</td>
                    <td>${d.location}</td>
                    <td>${d.tel ?? "-"}</td>
                    <td>${d.dx ?? "-"}</td>
                    <td>${d.dy ?? "-"}</td>
                </tr>
            `
        });
    } catch {
        renderEmptyRow(
            document.getElementById("dinerTable"),
            7,
            "식당 목록을 불러오지 못했습니다."
        );
    }
}

/* 회원 목록 */
async function loadMembers() {
    try {
        const data = await fetchJson("/api/adminPage/members");

        renderTable({
            tbodyId: "memberTable",
            colSpan: 5,
            data,
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
    } catch {
        renderEmptyRow(
            document.getElementById("memberTable"),
            5,
            "회원 정보를 불러오지 못했습니다."
        );
    }
}

/* 권한 신청 상태 뱃지 */
function statusBadge(status) {
    const map = {
        PENDING: "warning",
        APPROVED: "success",
        REJECTED: "secondary"
    };
    return `<span class="badge bg-${map[status] ?? "secondary"}">${status}</span>`;
}

/* 권한 신청 목록 */
async function loadOwnerRequests() {
    try {
        const data = await fetchJson("/api/adminPage/owner-requests");

        renderTable({
            tbodyId: "ownerRequestTable",
            colSpan: 6,
            data,
            emptyMessage: "권한 신청 내역이 없습니다.",
            rowRenderer: r => `
                <tr>
                    <td>${r.id}</td>
                    <td>${r.memberUsername}</td>
                    <td>${r.dinerName}</td>
                    <td>${statusBadge(r.status)}</td>
                    <td>${r.createdAt.substring(0, 10)}</td>
                    <td>
                        ${r.status === "PENDING" ? `
                            <button class="btn btn-success btn-sm"
                                onclick="approveRequest(${r.id})">승인</button>
                            <button class="btn btn-danger btn-sm"
                                onclick="rejectRequest(${r.id})">반려</button>
                        ` : "-"}
                    </td>
                </tr>
            `
        });
    } catch (e) {
        renderEmptyRow(
            document.getElementById("ownerRequestTable"),
            6,
            e.message || "권한 신청 목록을 불러오지 못했습니다."
        );
    }
}

/* 예약 목록 */
async function loadReservations() {
    try {
        const data = await fetchJson("/api/adminPage/books");

        renderTable({
            tbodyId: "reservationTable",
            colSpan: 7,
            data,
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
    } catch (e) {
        renderEmptyRow(
            document.getElementById("reservationTable"),
            7,
            e.message || "예약 목록을 불러오지 못했습니다."
        );
    }
}

/* 사장 목록 */
async function loadOwners() {
    try {
        const data = await fetchJson("/api/adminPage/owners");

        renderTable({
            tbodyId: "ownerListTable",
            colSpan: 6,
            data,
            emptyMessage: "사장 목록이 없습니다.",
            rowRenderer: o => `
                <tr>
                    <td>${o.ownerName}</td>
                    <td>${o.email}</td>
                    <td>${o.phone}</td>
                    <td>${o.dinerName}</td>
                    <td>${o.category}</td>
                    <td>${o.status}</td>
                </tr>
            `
        });
    } catch (e) {
        renderEmptyRow(
            document.getElementById("ownerListTable"),
            6,
            e.message || "사장 목록을 불러오지 못했습니다."
        );
    }
}

/* 리뷰 목록 */
async function loadReviews() {
    try {
        const data = await fetchJson("/api/adminPage/reviews");

        renderTable({
            tbodyId: "reviewListTable",
            colSpan: 6,
            data,
            emptyMessage: "리뷰 목록이 없습니다.",
            rowRenderer: r => `
                <tr>
                    <td>${r.reviewId}</td>
                    <td>${r.memberUsername}</td>
                    <td>${r.dinerName}</td>
                    <td>${r.rating}</td>
                    <td>${r.comment}</td>
                    <td>${formatDate(r.createTime)}</td>
                </tr>
            `
        });
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
        const data = await fetchJson("/api/adminPage/dashboard");

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
                    <td>${r.rating}</td>
                    <td>${r.comment}</td>
                    <td>${formatDate(r.createTime)}</td>
                </tr>
            `
        });
    } catch {
        console.error("대시보드 로딩 실패");
    }
}


// 승인, 반려 실행시킬 메소드
// + url 주소, 성공 메시지를 제외한 나머지코드가 동일하므로 객체화 시킴
// ++ 승인, 반려 외 대기(인증확인) 등 여러 상태가 추가되도 url, message 만 바꾸어서 이 메소드에 넣으면 정상 작동

/*  승인 / 반려 */
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