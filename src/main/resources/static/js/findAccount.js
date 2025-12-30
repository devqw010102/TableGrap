document.addEventListener("DOMContentLoaded", () => {
    // 아이디 찾기
    const findIdForm = document.getElementById("findIdForm");
    if (findIdForm) {
        findIdForm.addEventListener("submit", async (e) => {
            e.preventDefault();
            const name = document.getElementById("registerName").value;
            // 폼 제출 시점에 이메일 합치기
            const email = document.getElementById("registerEmail").value + "@" + document.getElementById("registerDomain").value;

            if(!name || email.length < 5) { alert(" 정보를 올바르게 입력해주세요."); return; }

            try {
                const response = await fetch(`/api/member/find-id?name=${encodeURIComponent(name)}&email=${encodeURIComponent(email)}`);
                if (response.ok) {
                    const username = await response.text();
                    document.getElementById("findIdResult").classList.remove("d-none");
                    document.getElementById("displayUsername").innerText = username;
                    findIdForm.classList.add("d-none"); // 폼 숨기기
                } else {
                    alert("일치하는 회원 정보가 없습니다.");
                }
            } catch (error) {
                console.error("Error:", error);
            }
        });
    }

    // 비밀번호 재설정
    const verifyPwdForm = document.getElementById("verifyPwdForm");
    const newPwdForm = document.getElementById("newPwdForm");

    if (verifyPwdForm) {
        verifyPwdForm.addEventListener("submit", async (e) => {
            e.preventDefault();

            // 제출 시점에 이메일 최종 확인
            const email = document.getElementById("registerEmail").value + "@" + document.getElementById("registerDomain").value;
            const username = document.getElementById("registerId").value;

            try {
                const response = await fetch(`/api/member/verify-identity?username=${username}&email=${email}`, {
                    method: 'POST'
                });

                if (response.ok) {
                    alert("본인 확인 완료!");
                    document.getElementById("verifiedUsername").value = username;
                    verifyPwdForm.classList.add("d-none");
                    newPwdForm.classList.remove("d-none");
                } else {
                    alert("일치하는 정보가 없습니다.");
                }
            } catch (error) { console.error(error); }
        });
    }

    // 비밀번호 재설정
    if (newPwdForm) {
        newPwdForm.addEventListener("submit", async (e) => {
            e.preventDefault();

            if (document.querySelectorAll(".is-invalid").length > 0) {
                alert("비밀번호 형식을 확인해주세요.");
                return;
            }

            const data = {
                username: document.getElementById("verifiedUsername").value,
                password: document.getElementById("registerPwd").value,
                passwordConfirm: document.getElementById("registerPwdConfirm").value
            };

            try {
                const res = await fetch("/api/member/reset-password", {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(data)
                });

                if (res.ok) {
                    alert("비밀번호가 성공적으로 변경되었습니다. 다시 로그인해주세요.");
                    window.location.href = "/login";
                } else {
                    const msg = await res.text();
                    alert("변경 실패: " + msg);
                }
            } catch (e) { console.error(e); }
        });
    }
});