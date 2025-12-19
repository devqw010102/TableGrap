document.addEventListener("DOMContentLoaded", () => {
    const bookLink = document.querySelector('a[href="#books"]');
    const infoLink = document.querySelector('a[href="#info"]');
    const reviewLink = document.querySelector('a[href="#review"]');
    if (bookLink) bookLink.addEventListener('click', loadBooks);
    if (infoLink) infoLink.addEventListener('click', loadMyInfo);
    if (reviewLink) reviewLink.addEventListener('click', loadMyReview);

    // 회원정보 수정/저장/취소 버튼 이벤트 연결
    const btnEdit = document.getElementById("btnEdit");
    const btnSave = document.getElementById("btnSave");
    const btnCancel = document.getElementById("btnCancel");
    const btnDeleteMember = document.getElementById("btnDeleteMember");

    if (btnEdit) btnEdit.addEventListener("click", () => toggleEditMode(true));
    if (btnSave) btnSave.addEventListener("click", saveMember);
    if (btnCancel) {
        btnCancel.addEventListener("click", () => {
            toggleEditMode(false);
            loadMyInfo();
        });
    }
    if (btnDeleteMember) btnDeleteMember.addEventListener("click", deleteMember);
    loadMyInfo();
});

// 수정 모드 토글
function toggleEditMode(isMemberEdit) {
    // 이 부분만 on/off - 이름, 아이디는 x
    const emailDisplay = document.getElementById("emailDisplay");
    const emailEditGroup = document.getElementById("emailEditGroup");
    const phoneDisplay = document.getElementById("phoneDisplay");
    const phoneEditGroup = document.getElementById("phoneEditGroup");

    const otherFields = ['newPassword', 'pwdConfirm','myPhone', 'emailId', 'emailDomainInput'];
    const emailSelect = document.getElementById("emailDomainSelect");

    if (isMemberEdit) {
        if (emailDisplay) emailDisplay.style.display = "none";
        if (emailEditGroup) emailEditGroup.style.display = "flex";
        if (phoneDisplay) phoneDisplay.style.display = "none";
        if (phoneEditGroup) phoneEditGroup.style.display = "flex";

    otherFields.forEach(id => {
        const editValue = document.getElementById(id);
        if (isMemberEdit) {
            editValue.readOnly = false;
            editValue.className = "form-control";
        }
    });
    if (emailSelect) emailSelect.disabled = false;

    } else{
    if (emailDisplay) emailDisplay.style.display = "block";
    if (emailEditGroup) emailEditGroup.style.display = "none";
    if (phoneDisplay) phoneDisplay.style.display = "block";
    if (phoneEditGroup) phoneEditGroup.style.display = "none";

    otherFields.forEach(id => {
        const editValue = document.getElementById(id);
        if(editValue){
            editValue.readOnly = true;
            editValue.className = "form-control-plaintext";
            editValue.classList.remove("is-valid", "is-invalid");
        }
    });
    if(emailSelect) emailSelect.disabled = true;

    document.querySelectorAll(".small").forEach(div => div.innerHTML = "");
}
document.querySelectorAll(".edit-mode-row").forEach(row=> {
    row.style.display = isMemberEdit ? "table-row" : "none";
});

    document.getElementById("btnEdit").style.display = isMemberEdit ? "none" : "inline-block";
    document.getElementById("btnSave").style.display = isMemberEdit ? "inline-block": "none"
    document.getElementById("btnCancel").style.display = isMemberEdit ? "inline-block" : "none";
};



// 회원 정보 저장 - /api/member/update
function saveMember() {
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

    fetch("/api/member/update", {
        method: "POST",
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
                //예약 확정 상태에 따라 리뷰버튼 활성화 여부 결정
                const reviewBtn = book.success
                    ? `<button class="btn btn-success btn-sm btn-review" data-diner-id="${book.dinerId}">후기 작성</button>`
                    : `<span class="text-muted">-</span>`;
                tbody.innerHTML += `
                    <tr>
                        <td><a href="${myBookingLink}" class="text-primary text-decoration-underline">${book.dinerName}</a></td>
                        <td>${modifyDate}</td>
                        <td>${book.personnel}</td>
                        <td>${book.memberName}</td>
                        <td>${book.success ? "확정" : "대기"}</td>
                        <td><button class="btn btn-danger btn-sm btn-cancel-booking" data-id="${book.bookId}">취소</button></td>
                        <td>${reviewBtn}</td>
                    </tr>
                `;
            });
            // 리뷰작성 이벤트 리스너
            document.querySelectorAll(".btn-review").forEach(btn => {
                btn.addEventListener("click", (e) => {
                    const dinerId = e.target.getAttribute("data-diner-id");
                    openModal(dinerId);
                })
            })


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
    fetch("/api/member/info")
        .then(res => res.json())
        .then(data => {
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
            toggleEditMode(false);
        })
        .catch(err => console.error("회원정보 로드 실패:", err));
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

    fetch("/api/member/delete", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ password: password })
    })
        .then(res => {
            if (res.ok) {
                alert("정상적으로 탈퇴되었습니다. 메인으로 이동합니다.");
                // location.reload();
                location.href="/";
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

// 후기 불러오기
function loadMyReview(){
    fetch("/api/review/list")
        .then(res => res.json())
        .then(data => {
            const reviewTable = document.getElementById("reviewTable");
            if(!data || data.length === 0){
                reviewTable.innerHTML = '<tr><td colspan="2" class="text-center">작성한 후기가 없습니다.</td></tr>';
                return;
            }
            reviewTable.innerHTML=``;
            data.forEach(review => {
                reviewTable.innerHTML += `
                    <tr>
                        <td>${review.rating}</td>
                        <td>${review.comment}</td>
                    </tr>`
            });
        });
}

//모달 출력함수
function openModal(dinerId) {
    //dinerId 저장
    document.getElementById("modalDinerId").value = dinerId;
    //모달 띄우기
    const reviewModal = new bootstrap.Modal(document.getElementById('reviewModal'));
    reviewModal.show();
}

//리뷰 작성 메소드
function createReview(dinerId) {
    const reviewDinerId = document.getElementById("modalDinerId").value;
    const rating = document.getElementById("modalRating").value;
    const comment = document.getElementById("modalComment").value;

    // 후기 모달 데이터
    const reviewData = {
        dinerId: parseInt(reviewDinerId),
        rating: parseInt(rating),
        comment: comment
    };

    fetch("/api/mypage/review/create", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(reviewData)
    })
        .then(res => {
            if (res.ok) {
                alert("후기가 DB에 성공적으로 저장되었습니다.");
            } else {
                alert("후기 저장 실패!");
            }
        })
        .catch(err => console.error("에러 발생:", err));
}

// 빈 테이블 렌더링
function renderEmptyRow(tbody, colSpan, message) {
    tbody.innerHTML = `
        <tr>
            <td colspan="${colSpan}" class="text-center text-muted py-4">
                ${message}
            </td>
        </tr>
    `}

