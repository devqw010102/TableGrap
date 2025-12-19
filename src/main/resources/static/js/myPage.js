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
    loadBooks();
});

// 중복 체크
let emailTimer;
function checkEmail(dbcheck){
    const email = dbcheck.value;
    const resultShow = document.getElementById("emailResult");

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    clearTimeout(emailTimer);

    if (!emailRegex.test(email)) {
        resultShow.innerHTML = "올바른 이메일 형식을 입력해주세요.";
        dbcheck.classList.remove("is-valid");
        return;
    }

    emailTimer = setTimeout(() => {
        const xhr = new XMLHttpRequest();
        xhr.open("GET", `/api/myPage/check-email?email=${encodeURIComponent(email)}`)
        xhr.onreadystatechange = function () {
            if(xhr.readyState === 4 && xhr.status === 200) {
                resultShow.innerHTML = xhr.responseText;
                if (xhr.responseText.includes("가능")) {
                    dbcheck.classList.add("is-valid");
                    dbcheck.classList.remove("is-invalid");
                } else {
                    dbcheck.classList.add("is-invalid");
                }
            }
        };
        xhr.send();
    }, 500); // 사용자가 입력을 멈추고 0.5초 뒤에 실행
}


function  checkPhone(dbcheck){
    const regPhone = /^01([0|1|6|7|8|9])-?([0-9]{3,4})-?([0-9]{4})$/;
    const resultShow = document.getElementById("phoneResult")

    if(regPhone.test(dbcheck.value)) {
        dbcheck.classList.replace("is-invalid", "is-valid");
    }else{
        dbcheck.classList.add("is-invalid");
    }
}

function checkPwd(dbcheck){
    const pwd = dbcheck.value;
    const resultShow = document.getElementById("pwdResult")

    if(!pwd) {
        resultShow.innerHTML = "";
        dbcheck.classList.remove("is-valid", "is-invalid");
        return;
    }

    if(pwd.length < 8) {
        resultShow.innerHTML = "8자 이상 입력해주세요.";
        dbcheck.classList.add("is-invalid");
    }else{
        dbcheck.classList.replace("is-invalid", "is-valid");
    }
    checkPwdConfirm();
}

function checkPwdConfirm() {
    const pwd = document.getElementById("newPassword").value;
    const pwdConfirmEl = document.getElementById("pwdConfirm"); // HTML ID와 일치!
    const resultShow = document.getElementById("pwdConfirmResult");

    if (!pwdConfirmEl || !resultShow) return;

    const pwdConfirm = pwdConfirmEl.value;

    if (!pwdConfirmEl.value) {
        resultShow.innerHTML = "";
        pwdConfirmEl.classList.remove("is-valid", "is-invalid");
        return;
    }

    if (pwd === pwdConfirmEl.value) {
        resultShow.innerHTML = "비밀번호가 일치합니다.";
        pwdConfirmEl.classList.replace("is-invalid", "is-valid");
    } else {
        resultShow.innerHTML = "비밀번호가 일치하지 않습니다.";
        pwdConfirmEl.classList.add("is-invalid");
    }
}

// 수정 모드 토글
function toggleEditMode(isMemberEdit) {
    const fields = ['myEmail', 'myPhone', 'newPassword', 'pwdConfirm'];

    fields.forEach(id=> {
        const editValue = document.getElementById(id);
        if (!editValue) return;

        if (isMemberEdit) {
            editValue.readOnly = false;
            editValue.className = "form-control";
        } else {
            editValue.readOnly = true;
            editValue.className = "form-control-plaintext";

            editValue.classList.remove("is-valid", "is-invalid");
        }
    });

    if(!isMemberEdit){
        document.querySelectorAll(".small").forEach(div => {
            div.innerHTML="";
        });
    }
    document.querySelectorAll(".edit-mode-row").forEach(row => {
        row.style.display = isMemberEdit ? "table-row" : "none";
    });

    document.getElementById("btnEdit").style.display = isMemberEdit ? "none" : "inline-block";
    document.getElementById("btnSave").style.display = isMemberEdit ? "inline-block": "none"
    document.getElementById("btnCancel").style.display = isMemberEdit ? "inline-block" : "none";
}



// 회원 정보 저장
function saveMember() {
    const pwd = document.getElementById("newPassword").value;
    const pwdConfirm = document.getElementById("pwdConfirm").value;

    if(pwd && pwd !== pwdConfirm) {
        alert("비밀번호가 일치하지 않습니다.");
        return;
    }

    const data = {
        email: document.getElementById("myEmail").value,
        phone: document.getElementById("myPhone").value,
        password: pwd,
        passwordConfirm: pwdConfirm
    };

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
                location.reload();
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

