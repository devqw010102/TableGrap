document.addEventListener("DOMContentLoaded", () => {
  const ownerRegisterForm = document.getElementById("ownerRegisterForm");
  let isBusinessNumberValid = false;

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
      checkBusinessNum(document.getElementById("businessNum"));

      setTimeout(() => {
        const invalidInputs = ownerRegisterForm.querySelectorAll(".is-invalid");
        if (invalidInputs.length > 0) {
          alert("입력 항목을 다시 확인해주세요.");
          return;
        }
        if(!isBusinessNumberValid){
        alert("사업자 번호 조회를 진행해주세요.");
        return;
        }
        const data = {
          //Dto 필드명에 맞추기
          name: document.getElementById("ownerRegisterName").value,
          username: document.getElementById("ownerRegisterId").value,
          email: document.getElementById("totalEmail").value,
          phone: document.getElementById("ownerRegisterPhone").value,
          password: document.getElementById("ownerRegisterPwd").value,
          passwordConfirm: document.getElementById("ownerRegisterPwdConfirm").value,
          businessNum: document.getElementById("businessNum").value,
          dinerName: document.getElementById("ownerDinerName").value
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

  //사업자 번호로 조회
  const businessNumberBtn = document.getElementById("businessNumBtn");
  if (businessNumberBtn) {
    businessNumberBtn.addEventListener("click", () => {
      const businessNumber = document.getElementById("businessNum").value.trim();
      const reg = /^\d{10}$/;
      if (businessNumber === "") {
        alert("사업자 번호를 입력해주세요.");
        return;
      } else if(businessNumber.length !== 10 || !reg.test(businessNumber)){
        alert("올바른 사업자 번호 형식이 아닙니다. 숫자 10자리로 입력해주세요.");
        return;
      } else {
        fetchBusinessInfo(businessNumber);
      }
      });
    }

      async function fetchBusinessInfo(businessNumber) {

      try{
        // Controller의 경로(@RequestMapping + @GetMapping)에 맞춰 수정
        const url = `/api/owner/proxy/business-info?query=${businessNumber}`;
        const res = await fetch(url);
        if(res.ok){
          const data = await res.json();
          console.log(data);
          const items = data.items || data.data;
          if(items && items.length > 0){
            const info = items[0];

            // 받아온 상호명을 공백 제거
            const cleanDinerName = info.company.replace(/\s+/g, '');
            document.getElementById("ownerDinerName").value = cleanDinerName;
            isBusinessNumberValid = true;
            document.getElementById("businessNum").readOnly = true;
            alert("사업자 정보가 정상적으로 조회되었습니다.");
          } else {
            alert("유효한 사업자 번호가 아닙니다.");
          }
        } else{
          const error = await res.text();
          alert("사업자 정보 조회에 실패했습니다." + error);
        }
      } catch(err){
          console.error("사업자 정보 조회 오류:", err);
          alert("사업자 정보 조회 중 오류가 발생했습니다.");
        }
      }
});