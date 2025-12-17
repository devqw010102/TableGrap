document.addEventListener("DOMContentLoaded", () => {
    const bookLink = document.querySelector('a[href="#books"]');
    const infoLink = document.querySelector('a[href="#info"]');
    if (bookLink) bookLink.addEventListener('click', loadBooks);
    if (infoLink) infoLink.addEventListener('click', loadMyInfo);

    // 회원정보 수정/저장/취소 버튼 이벤트 연결
    const btnEdit = document.getElementById("btnEdit");
    const btnSave = document.getElementById("btnSave");
    const btnCancel = document.getElementById("btnCancel");

    // 수정 버튼 클릭 시 수정 모드
    if (btnEdit) {
        btnEdit.addEventListener("click", () => toggleEditMode(true));
    }

    // 저장 버튼 클릭 시 저장 로직
    if (btnSave) {
        btnSave.addEventListener("click", saveMember);
    }

    // 취소 버튼 클릭 시 수정 모드 x
    if (btnCancel) {
        btnCancel.addEventListener("click", () => {
            toggleEditMode(false);
            loadMyInfo(); // 취소하면 원래 데이터로 원복
        });
    }

    // 회원 탈퇴 버튼 이벤트 연결 <button id="btnDeleteMember
    const btnDeleteMember = document.getElementById("btnDeleteMember");
    if (btnDeleteMember) {
        btnDeleteMember.addEventListener("click", deleteMember);
    }
    // 초기 화면 로드
    loadBooks();
});

// 예약 내역 불러오기
function loadBooks() {
    fetch("/api/myPage/books")
        .then(res => res.json())
        .then(data => {
            const tbody = document.getElementById("bookTable");
            if (!data || data.length === 0) {
                renderEmptyRow(tbody, 6, "예약 내역이 없습니다.");
                return;
            }

            tbody.innerHTML = '';
            data.forEach(book => {
                let modifyDate = book.bookingDate.replace("T", " ").substring(0, 16);
                const myBookingLink = `/reservation?id=${book.dinerId}&bookId=${book.bookId}`;

                tbody.innerHTML += `
                    <tr>
                        <td><a href="${myBookingLink}" class="text-primary text-decoration-underline">${book.dinerName}</a></td>
                        <td>${modifyDate}</td>
                        <td>${book.personnel}</td>
                        <td>${book.memberName}</td>
                        <td>${book.success ? "확정" : "대기"}</td>
                        <td><button class="btn btn-danger btn-sm btn-cancel-booking" data-id="${book.bookId}">취소</button></td>
                    </tr>
                `;
            });

            // 이벤트 리스너 다시 걸기
            document.querySelectorAll(".btn-cancel-booking").forEach(btn => {
                btn.addEventListener("click", function() {
                    cancelBooking(this.getAttribute("data-id"));
                });
            });
        })
        .catch(err => console.error("예약 조회 실패:", err));
}

// 회원 정보 불러오기
function loadMyInfo() {
    fetch("/api/myPage/info")
        .then(res => res.json())
        .then(data => {
            document.getElementById("myUsername").value = data.username;
            document.getElementById("myName").value = data.name;
            document.getElementById("myEmail").value = data.email;
            document.getElementById("myPhone").value = data.phone;

            toggleEditMode(false); // 로드 시엔 보기 모드
        })
        .catch(err => console.error("회원정보 로드 실패:", err));
}

// 수정 모드 토글
function toggleEditMode(isMemberEdit) {
    const inputs = ['myEmail', 'myPhone'];
    inputs.forEach(id => {
        const el = document.getElementById(id);
        if (!el) return;

        if (isMemberEdit) {
            el.removeAttribute("readonly");
            el.classList.replace("form-control-plaintext", "form-control");
        } else {
            el.setAttribute("readonly", true);
            el.classList.replace("form-control", "form-control-plaintext");
        }
    });

    document.querySelectorAll(".edit-mode-row").forEach(row => {
        row.style.display = isMemberEdit ? "table-row" : "none";
    });

    const btnEdit = document.getElementById("btnEdit");
    const btnSave = document.getElementById("btnSave");
    const btnCancel = document.getElementById("btnCancel");

    if (btnEdit) btnEdit.style.display = isMemberEdit ? "none" : "inline-block";
    if (btnSave) btnSave.style.display = isMemberEdit ? "inline-block" : "none";
    if (btnCancel) btnCancel.style.display = isMemberEdit ? "inline-block" : "none";

    // 수정 취소 혹은 완료 시 비밀번호 필드 초기화
    if (!isMemberEdit) {
        const newPw = document.getElementById("newPassword");
        const confPw = document.getElementById("confirmPassword");
        if(newPw) newPw.value = "";
        if(confPw) confPw.value = "";
    }
}

// 회원 정보 저장
function saveMember() {
    const newPassword = document.getElementById("newPassword").value;
    const confirmPassword = document.getElementById("confirmPassword").value;

    const data = {
        email: document.getElementById("myEmail").value,
        phone: document.getElementById("myPhone").value,
        password: newPassword,
        passwordConfirm: confirmPassword
    };

    if (newPassword && newPassword !== confirmPassword) {
        alert("비밀번호가 일치하지 않습니다.");
        return;
    }

    fetch("/api/myPage/update", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data)
    })
        .then(res => {
            if (res.ok) {
                alert("수정되었습니다.");
                loadMyInfo(); // 저장 성공 시 최신 정보 다시 로드
            } else {
                alert("수정 실패: 입력 정보를 확인해주세요.");
            }
        })
        .catch(err => console.error("Update Error:", err));
}

// 회원 탈퇴
function deleteMember() {
    const passwordInput = document.getElementById("deletePassword");

    const password = passwordInput.value;
    if (!password) {
        alert("비밀번호를 입력해주세요.");
        return;
    }

    if (!confirm("정말 탈퇴하시겠습니까?")) return;

    fetch("/api/myPage/delete", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ password: password })
    })
        .then(res => {
            if (res.ok) {
                alert("정상적으로 탈퇴되었습니다. 메인으로 이동합니다.");
                location.href = "/logout";
            } else {
                alert("비밀번호가 일치하지 않거나 오류가 발생했습니다.");
            }
        })
        .catch(err => console.error("Delete Error:", err));
}

// 예약 취소
function cancelBooking(bookId) {
    if (!confirm("정말로 예약을 취소하시겠습니까?")) return;

    fetch(`/api/myPage/book/delete/${bookId}`, { method: 'DELETE' })
        .then(res => {
            if (res.ok) {
                alert("예약이 취소되었습니다.");
                loadBooks(); // 목록 갱신
            } else {
                alert("취소 실패: 이미 취소되었거나 오류가 있습니다.");
            }
        })
        .catch(() => alert("서버 통신 오류"));
}

// 빈 테이블 렌더링
function renderEmptyRow(tbody, colSpan, message) {
    tbody.innerHTML = `
        <tr>
            <td colspan="${colSpan}" class="text-center text-muted py-4">
                ${message}
            </td>
        </tr>
    `;
}