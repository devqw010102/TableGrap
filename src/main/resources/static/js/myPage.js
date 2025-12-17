// myPage 이동 후 Ajax 로 예약 목록, 내 정보 불러오기
document.addEventListener("DOMContentLoaded", () => {
    loadBooks();
    loadMyInfo();
})

function loadBooks() {
    fetch("/api/myPage/books")
        .then(res => {
            if (!res.ok) {
                throw new Error("HTTP 상태코드: " + res.status);
            }
            return res.json();
        })
        .then(data => {
            if (!Array.isArray(data)) {
                throw new Error("응답이 배열이 아님");
            }

            const tbody = document.getElementById("bookTableBody");
            tbody.innerHTML = "";

            if (data.length === 0) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="5" class="text-center">
                            예약 내역이 없습니다.
                        </td>
                    </tr>
                `;
                return;
            }

            data.forEach(book => {

                console.log("데이터 확인:", book);
                let modifyDate = book.bookingDate.replace("T", " ").substring(0, 16);
                const myBookingLink = `/reservation?id=${book.dinerId}&bookId=${book.bookId}`; // each member/book

                tbody.innerHTML += `
                    <tr>
                     <td><a href="${myBookingLink}" style="text-decoration: underline; color: blue; cursor: pointer;">
            ${book.dinerName}</a> </td>
                        
                        <td>${modifyDate}</td>
                        <td>${book.personnel}</td>
                        <td>${book.memberName}</td>
                        <td>${book.success ? "확정" : "대기"}</td>
                        <td>
                        <button class="btn btn-danger btn-sm ms-2" 
                        onclick="cancelBooking(${book.bookId})">취소</button>
                        </td>
                    </tr>
                `;
            });
        })
        .catch(err => {
            console.error("예약 조회 실패:", err);
            alert("로그인이 필요합니다.");
            location.href = "/login";
        });
}
function loadMyInfo() {
    fetch("/api/myPage/info")
        .then(res => res.json())
        .then(data => {
            document.getElementById("myUsername").innerText = data.username;
            document.getElementById("myName").innerText = data.name;
            document.getElementById("myEmail").innerText = data.email;
            document.getElementById("myPhone").innerText = data.phone;
        });
}

    // 예약 취소 함수
    function cancelBooking(bookId) {
        if (!confirm("정말로 예약을 취소하시겠습니까? 복구할 수 없습니다.")) {
            return;
        }

        fetch(`/api/myPage/book/delete/${bookId}`, {
            method: 'DELETE'
        })
            .then(response => {
                if (response.ok) {
                    alert("삭제가 왼료되었습니다");
                    loadBooks();
                }else{
                    alert("삭제에 실패했습니다.")
                }
            })
            . catch(error => {
                alert("서버와 통신 중 오류가 발생했습니다.")
            })

}


