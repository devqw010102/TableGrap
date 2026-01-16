document.addEventListener("DOMContentLoaded", () => {
    const bookLink = document.querySelector('a[href="#books"]');
    const pastBookLink = document.querySelector('a[href="#pastBooks"]')
    const infoLink = document.querySelector('a[href="#info"]');
    const reviewLink = document.querySelector('a[href="#review"]');
    const reportLink = document.querySelector('a[href="#report"]');
    if (bookLink) bookLink.addEventListener('click', loadBooks);
    if (pastBookLink) pastBookLink.addEventListener('click', loadBooks);
    if (infoLink) infoLink.addEventListener('click', loadMyInfo);
    if (reviewLink) reviewLink.addEventListener('click', loadMyReview);
    if (reportLink) {
        reportLink.addEventListener('click', () => {
            loadFoodPreferenceChart();
            loadMonthlyVisitChart();
        });
    }

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

// 수정 모드 토글
function toggleEditMode(isMemberEdit) {
    // 이 부분만 on/off - 이름, 아이디는 x
    const emailDisplay = document.getElementById("emailDisplay");
    const emailEditGroup = document.getElementById("emailEditGroup");
    const phoneDisplay = document.getElementById("phoneDisplay");
    const phoneEditGroup = document.getElementById("phoneEditGroup");

    const pwdViewRow = document.getElementById("pwdViewRow");
    const pwdEditGroup = document.getElementById("pwdEditGroup");

    const otherFields = ['newPassword', 'pwdConfirm','myPhone', 'emailId', 'emailDomainInput'];
    const emailSelect = document.getElementById("emailDomainSelect");

    if (isMemberEdit) {
        if (emailDisplay) emailDisplay.style.display = "none";
        if (emailEditGroup) emailEditGroup.style.display = "flex";
        if (phoneDisplay) phoneDisplay.style.display = "none";
        if (phoneEditGroup) phoneEditGroup.style.display = "flex";

        if(pwdViewRow) pwdViewRow.style.display = "none";
        if(pwdEditGroup) pwdEditGroup.style.display = "block";

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

        if(pwdViewRow) pwdViewRow.style.display = "block";
        if(pwdEditGroup) pwdEditGroup.style.display = "none";

        otherFields.forEach(id => {
            const editValue = document.getElementById(id);
            if(editValue){
                editValue.readOnly = true;
                editValue.className = "form-control-plaintext";
                editValue.classList.remove("is-valid", "is-invalid");
            }
        });
        if(emailSelect) emailSelect.disabled = true;

        document.getElementById("newPassword").value = "";
        document.getElementById("pwdConfirm").value = "";

        document.querySelectorAll("[id^='error-']").forEach(div => div.innerHTML="");
    }
    document.getElementById("btnEdit").style.display = isMemberEdit ? "none" : "inline-block";
    document.getElementById("btnSave").style.display = isMemberEdit ? "inline-block": "none"
    document.getElementById("btnCancel").style.display = isMemberEdit ? "inline-block" : "none";
}


// 회원 정보 저장 - /api/member/update
function saveMember() {
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
            const currentTbody = document.getElementById("bookTable");
            const pastTbody = document.getElementById("pastBookTable");

            if(currentTbody) currentTbody.innerHTML = '';
            if(pastTbody) pastTbody.innerHTML = '';

            if (!data || data.length === 0) {
                renderEmptyRow(currentTbody, 6, "현재 예약 내역이 없습니다.");
                renderEmptyRow(pastTbody, 6, "지난 예약 내역이 없습니다.");
                return;
            }

            data.forEach(book => {
                let modifyDate = book.bookingDate.replace("T", " ").substring(0, 16);
                const myBookingLink = `/reservation?id=${book.dinerId}&bookId=${book.bookId}`;
                // 예약 취소 | 후기 작성 버튼 변환
                const date = new Date();

                //테스트용 시간 설정 (미래) -> 예약 대기 상태에서는 버튼 출력x
                //const date = new Date("2026-01-01");
                //테스트용 시간 설정 (과거)
                //const date = new Date("2025-01-01");
                //const now = new Date();

                const bookDate = new Date(book.bookingDate);
                const isPast = date > bookDate;
                const timeDiff = date - bookDate; //현재 시간과 예약

                const oneDayInMs = 24 * 60 * 60 * 1000;
                // 예약까지 남은시간
                const timeLeft = bookDate - date;
                // 클릭 차단 조건
                const isLinkBlocked = timeDiff >= 0 || timeLeft <= oneDayInMs;

                const canCancel = (timeDiff <= 0) || (book.cancelAllowed === true);

                //버튼 변경 로직
                const changeBtn =
                    (book.reviewId) ? `<button class="btn btn-info btn-sm btn-update-review" 
                    data-review-id="${book.reviewId}"  
                    data-book-id="${book.bookId}" 
                    data-diner-id="${book.dinerId}"
                    >후기 수정</button>` :
                        (timeDiff > 0 && book.success)
                            ? `<button class="btn btn-success btn-sm btn-review" 
                    data-book-id="${book.bookId}"
                    data-diner-id="${book.dinerId}"
                    >후기 작성</button>`
                            // 예약 일자 경과 전
                            //: (timeDiff <= 0 && (book.success || !book.success))
                            : (canCancel && (book.success || !book.success))
                                ? `<button class="btn btn-danger btn-sm btn-cancel-booking" data-id="${book.bookId}">예약 취소</button>`
                                : "";

                //예약 시간이 경과한 후 예약 수정 페이지 진입 차단 + 예약 하루 전 수정 불가능
                const changeUrl = (isLinkBlocked && book.success)
                    ? `<a href="javascript:void(0);"
                          onclick="alert('예약 24시간 전부터는 수정이 불가능합니다. 식당으로 직접 문의해주세요.');"
                          class="text-secondary text-decoration-none"
                          style="cursor: not-allowed;">${book.dinerName}</a>`
                    : `<a href="${myBookingLink}" class="text-primary text-decoration-underline">${book.dinerName}</a>`;

                    const statusBadge = book.success
                        ? `<span class="badge bg-success-subtle text-success border border-success-subtle">확정</span>`
                        : `<span class="badge bg-warning-subtle text-warning border border-warning-subtle">대기</span>`;

                const rowHtml = `
                    <tr>
                        <td>${changeUrl}</td>
                        <td>${modifyDate}</td>
                        <td>${book.personnel}</td>
                        <td>${book.memberName}</td>
                        <td>${statusBadge}</td> <td>${changeBtn}</td>
                    </tr>
                `;

                if (isPast) {
                    if(pastTbody) pastTbody.innerHTML += rowHtml;
                } else {
                    if(currentTbody) currentTbody.innerHTML += rowHtml;
                }
            });

            if (currentTbody && currentTbody.innerHTML === '') renderEmptyRow(currentTbody, 6, "현재 예약 내역이 없습니다.");
            if (pastTbody && pastTbody.innerHTML === '') renderEmptyRow(pastTbody, 6, "지난 예약 내역이 없습니다.");

            addBookingEventListeners();
        })
        .catch(err => console.error("예약 조회 실패:", err));
    }
    function addBookingEventListeners() {
        document.querySelectorAll(".btn-review").forEach(btn => {
            btn.addEventListener("click", (e) => openModal(e.target.dataset.bookId, e.target.dataset.dinerId));
        });
        document.querySelectorAll(".btn-update-review").forEach(btn => {
            btn.addEventListener("click", (e) => openEditModal(e.target.dataset.reviewId, e.target.dataset.bookId, e.target.dataset.dinerId));
        });
        document.querySelectorAll(".btn-cancel-booking").forEach(btn => {
            btn.addEventListener("click", function() { cancelBooking(this.getAttribute("data-id")); });
        });
/*
            // 리뷰작성 이벤트 리스너
            document.querySelectorAll(".btn-review").forEach(btn => {
                btn.addEventListener("click", (e) => {
                    const bookId = e.target.getAttribute("data-book-id");
                    const dinerId = e.target.getAttribute("data-diner-id");
                    openModal(bookId, dinerId);
                })
            })

            //리뷰수정 이벤트 리스너
            document.querySelectorAll(".btn-update-review").forEach(btn => {
                btn.addEventListener("click", (e) => {
                    const reviewId = e.target.getAttribute("data-review-id");
                    const bookId = e.target.getAttribute("data-book-id");
                    const dinerId = e.target.getAttribute("data-diner-id");
                    openEditModal(reviewId, bookId, dinerId)
                })
            })

            // 이벤트 리스너 다시 걸기
            document.querySelectorAll(".btn-cancel-booking").forEach(btn => {
                btn.addEventListener("click", function() {
                    cancelBooking(this.getAttribute("data-id"));
                });
            });
            */
}

// 회원 정보 불러오기
function loadMyInfo() {
    fetch("/api/member/info")
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
        .then(async res => {
            if (res.ok) {
                alert("정상적으로 탈퇴되었습니다. 메인으로 이동합니다.");
                // location.reload();
                location.href="/";
            } else {
                const errorMsg = await res.text();
                alert(errorMsg || "오류가 발생했습니다.");
            }
        })
        .catch(err => {
            console.error("Delete Error:", err);
            alert("서버 통신 오류가 발생했습니다.");
        });
}

// 예약 취소
function cancelBooking(bookId) {
    if (!confirm("정말로 예약을 취소하시겠습니까?")) return;

    fetch(`/api/myPage/book/delete/${bookId}`, { method: 'DELETE' })
        .then(async res => {
            if (res.ok) {
                alert("예약이 취소되었습니다.");
                loadBooks(); // 목록 갱신
            } else {
                const errorMsg = await res.text();
                alert("예약 24시간 전 취소 불가합니다, 가게로 연락 부탁드립니다.");
            }
        })
        .catch(() => alert("서버 통신 오류"));
}

// 후기 불러오기
function loadMyReview(){
    fetch(`/api/review/list`)
        .then(res => res.json())
        .then(data => {


            const reviewTable = document.getElementById("reviewTable");
            if(!data || data.length === 0){
                reviewTable.innerHTML = '<tr><td colspan="5" class="text-center">작성한 후기가 없습니다.</td></tr>';
                return;
            }
            reviewTable.innerHTML=``;
            data.forEach(review => {
                reviewTable.innerHTML += `
                    <tr>
                        <td>${review.dinerName}</a></td>
                        <td>${"⭐".repeat(review.rating)}</td>
                        <td>${review.comment}</td>
                        <td>${review.createTime}</td>
                        <td>${review.updateTime}</td>
                    </tr>`
            });
        });
}

//리뷰 작성 모달 열기
function openModal(bookId, dinerId) {
    document.getElementById("modalBookId").value = bookId;
    document.getElementById("modalDinerId").value = dinerId;
    //모달 띄우기
    const reviewModal = new bootstrap.Modal(document.getElementById('reviewModal'));
    reviewModal.show();
}
//review type=number유지하고 1-5이외의 숫자 or 문자 입력시 빈칸 처리
document.getElementById("modalRating").addEventListener("input", e => {
    //1-5까지의 숫자를 제외하고 빈칸으로 처리
    let rating = e.target.value.replace(/[^1-5]$/g, "");
    //111, 555 같이 범위내 같은 숫자 연속 입력 시, 잘라내기
    if(rating.length > 1){
        rating = rating.slice(0, 1);
    }
    e.target.value = rating;
})

// 리뷰 수정 모달 열기
function openEditModal(reviewId, bookId, dinerId){
    document.getElementById("editReviewId").value = reviewId;
    document.getElementById("editBookId").value = bookId;
    document.getElementById("editDinerId").value = dinerId;

    fetch(`/api/review/${reviewId}`)
        .then(res => res.json())
        .then(data => {
            document.getElementById("editRating").value = data.rating;
            document.getElementById("editComment").value = data.comment;
            const reviewModal = new bootstrap.Modal(document.getElementById("reviewEditModal"));
            reviewModal.show();
        })
        .catch(err => console.error("리뷰를 불러올 수 없습니다.", err))
}

//review type=number유지하고 1-5이외의 숫자 or 문자 입력시 빈칸 처리
document.getElementById("editRating").addEventListener("input", e => {
    //1-5까지의 숫자를 제외하고 빈칸으로 처리
    let rating = e.target.value.replace(/[^1-5]$/g, "");
    //111, 555 같이 범위내 같은 숫자 연속 입력 시, 잘라내기
    if(rating.length > 1){
        rating = rating.slice(0, 1);
    }
    e.target.value = rating;
})

//리뷰 작성 메소드
function createReview() {
    if(!document.getElementById("modalRating").value){
        alert("별점을 입력해주세요.");
        return;
    }

    if(!document.getElementById("modalComment").value){
        alert("후기 내용을 입력해주세요.");
        return;
    }
    const commentValue = document.getElementById("modalComment").value;

    if(commentValue.replace(/\s/g, "").length > 100){
        alert("후기 내용은 100자 이내로 작성해주세요.");
        return;
    }

    //후기에 저장하는 값
    const reviewBookId = document.getElementById("modalBookId").value;
    const reviewDinerId = document.getElementById("modalDinerId").value;
    const rating = document.getElementById("modalRating").value;
    const comment = document.getElementById("modalComment").value;

    // 후기 모달 데이터
    const reviewData = {
        bookId: parseInt(reviewBookId),
        dinerId: parseInt(reviewDinerId),
        rating: parseInt(rating),
        comment: comment
    };

    fetch(`/api/review/create`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(reviewData)
    })
        .then(res => {
            if (res.ok) {
                alert("후기가 성공적으로 저장되었습니다.");
                location.reload();
            } else {
                const errorMsg=res.text()
                    .then(errorMsg => alert("후기 저장에 실패했습니다." + errorMsg));
            }
        })
        .catch(err => console.error("에러 발생:", err));
}

//리뷰 수정
function updateReview() {
    if(!document.getElementById("editRating").value){
        alert("별점을 입력해주세요.");
        return;
    }

    if(!document.getElementById("editComment").value){
        alert("후기 내용을 입력해주세요.");
        return;
    }
    const editCommentValue = document.getElementById("editComment").value;
    if(editCommentValue.replace(/\s/g, "").length > 100){
        alert("후기 내용은 100자 이내로 작성해주세요.");
        return;
    }

    const reviewId = document.getElementById("editReviewId").value;
    const editRating = document.getElementById("editRating").value;
    const editComment = document.getElementById("editComment").value;
    const updateData = {
        rating: parseInt(editRating),
        comment: editComment
    };

    fetch(`/api/review/update/${reviewId}`, {
        method: "PATCH",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(updateData)
    })
        .then(res => {
            if(res.ok){
                alert("후기가 수정되었습니다.");
                location.reload();
            } else {
                const errorMsg=res.text()
                    .then(errorMsg => alert("후기 수정에 실패했습니다." + errorMsg));
            }
        })
        .catch(err => console.error("에러 발생", err));
}

//리뷰 삭제
function deleteReview(){
    const reviewId = parseInt(document.getElementById("editReviewId").value);

    fetch(`api/review/delete/${reviewId}`, {
        method: "DELETE",
        headers: {"Content-Type": "application/json"}
    })
        .then(res => {
            if(res.ok){
                alert("후기가 삭제되었습니다.");
                location.reload();
            } else {
                const errMsg = res.text()
                    .then(errMsg => alert("후기 삭제를 실패했습니다." + errMsg));
            }
        })
        .catch(err => console.error("에러 발생", err));
}

// 빈 테이블 렌더링
function renderEmptyRow(tbody, colSpan, message) {
    tbody.innerHTML = `
        <tr>
            <td colspan="${colSpan}" class="text-center text-muted py-4">
                ${message}
            </td>
        </tr>
    `
}

window.onload = function() {
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('error') === 'closed') {
        alert("해당 식당은 현재 예약 서비스를 일시 중단하였습니다. 관련 문의는 식당으로 직접 연락 부탁드립니다.");
        window.history.replaceState({}, document.title, window.location.pathname);
    }
    if (urlParams.get('error') === 'dinerIdNull') {
        alert("식당 정보를 불러올 수 없습니다.");
        window.history.replaceState({}, document.title, window.location.pathname);
    }
}

function loadFoodPreferenceChart() {
    fetch("/api/member/info")
    .then(res => res.json())
    .then(member => {
        const memberId = member.id;
        return fetch(`/api/reservation/charts/food-preference/${memberId}`);
    })
    .then(res => res.json())
    .then(response => {

        if (response.error) {
            console.error("Python 에러:", response.error);
            return;
        }

        Plotly.newPlot('foodPreferenceChart', response.data, response.layout);

        const analysisTexts = document.getElementById("foodAnalysisText");
        if (analysisTexts && response.data && response.data[0] && response.data[0].labels) {
            const topCategory = response.data[0].labels[0]; // 가장 비율 높은 카테고리
            if (topCategory) {
                analysisTexts.innerText = `사용자님은 '${topCategory}' 음식을 가장 선호하시는군요!`;
            }
        }
    })
    .catch(err => {
        console.error("차트 로드 중 예외 발생:", err);
    });
}

function loadMonthlyVisitChart() {
    fetch("/api/member/info")
    .then(res => res.json())
    .then(member => fetch(`/api/reservation/charts/monthly-visit/${member.id}`))
    .then(res => res.json())
    .then(response => {
        if (response.error) {
            console.error("차트 에러:", response.error);
            return;
        }

        Plotly.newPlot('monthlyVisitChart', response.data, response.layout);

        const analysisText = document.getElementById("visitAnalysisText");
        const myRank = response.percentile;
        const total = response.totalCount;
        const avgAll = response.avgAll;

        let rankMsg = `회원님은 상위 ${myRank}%의 미식가입니다!`; // 이 문구들을 수정
        if(myRank <= 10) rankMsg = `👑 대단해요! 상위 ${myRank}%의 진정한 미식가시네요!`;
        if(total === 0) rankMsg = `아직 방문 확정된 내역이 없네요. 맛집 예약을 시작해보세요!`;

        analysisText.innerHTML = `
            <div>
                <strong>${rankMsg}</strong><br>
                <small>(최근 6개월 총 ${total}회 방문 | 전체 사용자 월평균: ${avgAll}회)</small>
            </div>
        `;
    })
    .catch(err => console.error("방문 차트 로드 실패:", err));
}