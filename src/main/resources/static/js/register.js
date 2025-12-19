document.addEventListener("DOMContentLoaded", () => {
    const registerForm = document.getElementById("registerForm");

    if (registerForm) {
        registerForm.addEventListener("submit", (e) => {
            e.preventDefault();

            // 최종 전송용 데이터 조립
            const data = {
                name: document.getElementById("registerName").value,
                username: document.getElementById("registerId").value,
                email: document.getElementById("totalEmail").value,
                phone: document.getElementById("registerPhone").value,
                password: document.getElementById("registerPwd").value,
                passwordConfirm: document.getElementById("registerPwdConfirm").value
            };

            fetch("/api/member/register", { // MemberController의 POST 경로
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(data)
            })
                .then(async res => {
                    if (res.ok) {
                        alert("회원가입이 완료되었습니다!");
                        window.location.href = "/"; // 성공 시 홈으로
                    } else {
                        const errorMsg = await res.text();
                        alert("가입 실패: " + errorMsg);
                    }
                })
                .catch(err => console.error("Register Error:", err));
        });
    }
});