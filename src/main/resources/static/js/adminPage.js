document.querySelector('a[href="#list"]').addEventListener('click', loadDiners);
document.querySelector('a[href="#stats"]').addEventListener('click', loadMembers);
document.querySelector('a[href="#ownerList"]').addEventListener('click', loadOwnerRequests);

function loadDiners() {
    fetch("api/adminPage/diners")
        .then(res => res.json())
        .then(data => {
            const tbody = document.getElementById('dinerTable');

            if (!data || data.length === 0) {
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
                `
            });
        });
}

function loadMembers() {
    fetch("api/adminPage/members")
        .then(res => res.json())
        .then(data => {
            const tbody = document.getElementById('memberTable');

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
        });
}

function loadOwnerRequests() {
    fetch("/api/adminPage/owner-requests")
        .then(res => res.json())
        .then(data => {
           const tbody = document.getElementById('ownerRequestTable');

            if (!data || data.length === 0) {
                renderEmptyRow(tbody, 5, "권한 신청 내역이 없습니다.");
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
                        <td>${r.status === 'PENDING' ? 
                            `
                                <button class="btn btn-success btn-sm"
                                    onclick="approveRequest(${r.id})">승인</button>
                                <button class="btn btn-danger btn-sm"
                                    onclick="rejectRequest(${r.id})">반려</button>
                            ` : '-'}
                        </td>
                    </tr>
                `;
            });
        });
}

function statusBadge(status) {
    if (status === "PENDING") {
        return `<span class="badge bg-warning">대기</span>`;
    }
    if (status === "APPROVED") {
        return `<span class="badge bg-success">승인</span>`;
    }
    return `<span class="badge bg-secondary">반려</span>`;
}

function renderEmptyRow(tbody, colSpan, message) {
    tbody.innerHTML = `
        <tr>
            <td colspan="${colSpan}" class="text-center text-muted">
                ${message}
            </td>
        </tr>
    `;
}

function approveRequest(id) {
    if(!confirm("승인하시겠습니까?")) return;

    fetch(`api/adminPage/owner-requests/${id}/approve`, {method : "put"})
        .then(() => {
            alert("승인 완료");
            loadOwnerRequests();
        });
}

function rejectRequest(id) {
    if (!confirm("반려하시겠습니까?")) return;

    fetch(`/api/adminPage/owner-requests/${id}/reject`, {
        method: "PUT"
    }).then(() => {
        alert("반려 처리 완료");
        loadOwnerRequests();
    });
}