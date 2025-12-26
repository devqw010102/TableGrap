document.addEventListener("DOMContentLoaded", () => {
    const registerForm = document.getElementById("registerForm");
    const ownerRegisterForm = document.getElementById("ownerRegisterForm");

    const form = registerForm || ownerRegisterForm;
    if(!form) return;

    const currentPathType = window.location.pathname.includes('owner') ? 'owner' : 'member';
    let isBusinessNumberValid = false;


    form.addEventListener("submit", (e) => {
        e.preventDefault();

        // 모든 검증
        checkName(document.getElementById("registerName"));
        checkUsername(document.getElementById("registerId"));
        EmailValidation();
        checkPhone(document.getElementById("registerPhone"));
        checkPwd(document.getElementById("registerPwd"));
        checkPwdConfirm();

        if (currentPathType === 'owner') {
            checkBusinessNum(document.getElementById("businessNum"));
        }


        setTimeout(() => {
            const invalidInputs = form.querySelectorAll(".is-invalid");
            if (invalidInputs.length > 0) {
                alert("입력 항목을 다시 확인해주세요.");
                return;
            }
            if(currentPathType === 'owner' && !isBusinessNumberValid){
                alert("사업자 번호 조회를 진행해주세요.");
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

            if(currentPathType === 'owner') {
                data.businessNum = document.getElementById("businessNum").value;
                data.dinerName =  document.getElementById("ownerDinerName").value;
            }
            fetch(`/api/${currentPathType}/register`, {
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
                alert("올바른 사업자 번호 형식이 아닙니다. \n(-)을 제외한 숫자 10자리로 입력해주세요.");
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
                const diner = await res.json();

                if(diner && diner.dinerName){
                    document.getElementById("ownerDinerName").value = diner.dinerName;
                    isBusinessNumberValid = true;
                    document.getElementById("businessNum").readOnly = true;
                    alert("사업자 번호가 조회되었습니다");
                } else {
                    alert("식당 데이터가 올바르지 않습니다.")
                }
            } else{
                const error = await res.text();
                alert("사업자 정보 조회에 실패했습니다." + error);

                // 사업자 번호 입력창 초기화
                document.getElementById("ownerDinerName").value = "";
                isBusinessNumberValid = false;
            }
        } catch(err){
            console.error("사업자 정보 조회 오류:", err);
            alert("사업자 정보 조회 중 오류가 발생했습니다.");
        }
    }
});