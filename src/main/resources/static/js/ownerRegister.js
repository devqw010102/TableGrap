document.addEventListener("DOMContentLoaded", () => {
  const ownerRegisterForm = document.getElementById("ownerRegisterForm");

  if (ownerRegisterForm) {
    ownerRegisterForm.addEventListener("submit", (e) => {
      e.preventDefault();

      // 모든 검증
      checkName(document.getElementById("ownerRegisterName"));
      checkUsername(document.getElementById("ownerRegisterId"));
      EmailValidation();
      checkPhone(document.getElementById("ownerRegisterPhone"));
      checkPwd(document.getElementById("ownerRegisterPwd"));
      checkPwdConfirm();

      setTimeout(() => {
        const invalidInputs = ownerRegisterForm.querySelectorAll(".is-invalid");
        if (invalidInputs.length > 0) {
          alert("입력 항목을 다시 확인해주세요.");
          return;
        }

        const data = {
          name: document.getElementById("ownerRegisterName").value,
          username: document.getElementById("ownerRegisterId").value,
          email: document.getElementById("totalEmail").value,
          phone: document.getElementById("ownerRegisterPhone").value,
          password: document.getElementById("ownerRegisterPwd").value,
          passwordConfirm: document.getElementById("ownerRegisterPwdConfirm").value
        };
        fetch("/api/owner/register", {
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