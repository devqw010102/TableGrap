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
                tbody.innerHTML += `
                    <tr>
                        <td>${book.dinerName}</td>
                        <td>${book.bookingDate}</td>
                        <td>${book.personnel}</td>
                        <td>${book.memberName}</td>
                        <td>${book.success ? "확정" : "대기"}</td>
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

fetch("/api/myPage/info")
    .then(res => res.json())
    .then(data => {
        document.getElementById("myUsername").innerText = data.username;
        document.getElementById("myName").innerText = data.name;
        document.getElementById("myEmail").innerText = data.email;
        document.getElementById("myPhone").innerText = data.phone;
    });
