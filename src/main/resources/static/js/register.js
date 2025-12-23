document.addEventListener("DOMContentLoaded", () => {
    const registerForm = document.getElementById("registerForm");

    if (registerForm) {
        registerForm.addEventListener("submit", (e) => {
            e.preventDefault();

            // 모든 검증
            checkName(document.getElementById("registerName"));
            checkUsername(document.getElementById("registerId"));
            EmailValidation();
            checkPhone(document.getElementById("registerPhone"));
            checkPwd(document.getElementById("registerPwd"));
            checkPwdConfirm();

            setTimeout(() => {
                const invalidInputs = registerForm.querySelectorAll(".is-invalid");
                if (invalidInputs.length > 0) {
                    alert("입력 항목을 다시 확인해주세요.");
                    return;
                }

                const data = {
                    name: document.getElementById("registerName").value,
                    username: document.getElementById("registerId").value,
                    email: document.getElementById("totalEmail").value,
                    phone: document.getElementById("registerPhone").value,
                    password: document.getElementById("registerPwd").value,
                    passwordConfirm: document.getElementById("registerPwdConfirm").value
                };
                fetch("/api/member/register", {
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
            }, 600);
        });
    }
});