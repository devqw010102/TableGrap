let validationTimer;

const showError = (errorId, message) => {
    const errorDiv = document.getElementById(`error-${errorId}`);
    const inputId = document.getElementById(errorId);

    if(errorDiv) {
        errorDiv.innerText = message;
        errorDiv.style.color = "red";
    }
    if(inputId) {
        inputId.classList.add('is-invalid');
    }
};

const clearError = (elementId, successMsg = "") => {
    const errorDiv = document.getElementById(`error-${elementId}`);
    const inputEl = document.getElementById(elementId);

    if (errorDiv) {
        errorDiv.innerText = successMsg;
        errorDiv.style.color = "green";
    }
    if (inputEl) {
        inputEl.classList.remove('is-invalid');
        inputEl.classList.add('is-valid');
    }
};


function checkName(name) {
    if (name.value.trim().length > 0 && name.value.length <= 10) {
        clearError("registerName");
    } else {
        showError("registerName", "이름은 1~10자 이내여야 합니다.")
    }
}

// 아이디 중복 체크 (Register)
function checkUsername(id) {
    const username = id.value.trim();
    const usernameId = "registerId";

    clearTimeout(validationTimer);

    const idReg = /^[a-z0-9]*$/;
    if (username.length < 4 || username.length > 20 || !idReg.test(username)) {
        showError(usernameId, "아이디는 4~20자의 영문 소문자와 숫자만 가능합니다.");
        return;
    }

    validationTimer = setTimeout(() => {
        const xhr = new XMLHttpRequest();
        xhr.open("GET", `/api/member/check-username?username=${encodeURIComponent(username)}`, true);
        xhr.onreadystatechange = function () {
            if(xhr.readyState === 4 && xhr.status === 200) {
                const isDuplicate = JSON.parse(xhr.responseText);
                if (isDuplicate) {
                    showError(usernameId, "이미 사용 중인 아이디입니다.");
                } else {
                    clearError(usernameId, "사용 가능한 아이디입니다.");
                }
            }
        };
        xhr.send();
    }, 500);
}

// 이메일 통합 검증 (합쳐서 서버 체크)
function EmailValidation() {
    // register(registerEmail) or myPage(emailId)
    const emailInput = document.getElementById("registerEmail") || document.getElementById("emailId");
    const domainInput = document.getElementById("registerDomain") || document.getElementById("emailDomainInput");
    //const resultShow = document.getElementById("emailCheckResult");
    const totalEmailField = document.getElementById("totalEmail");
    const emailId = emailInput.id;

    const emailPrefix = emailInput.value.trim();
    const domain = domainInput.value.trim();


    // 공백, 형식 체크
    if(emailPrefix === "" || domain === "") {
        showError(emailId, "이메일 주소를 입력해주세요.");
        return;

        const fullEmail = `${emailPrefix}@${domain}`;
        const emailReg = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

        if(!emailReg.test(fullEmail)) {
            showError(emailId, "유효한 이메일 형식이 아닙니다.");
            return;
        }
        if(totalEmailField) totalEmailField.value = fullEmail;

        clearTimeout(validationTimer);
        validationTimer = setTimeout(() => {
            const xhr = new XMLHttpRequest();
            // MemberController의 /api/member/check-email 호출
            xhr.open("GET", `/api/member/check-email?email=${encodeURIComponent(fullEmail)}`);
            xhr.onreadystatechange = function () {
                if(xhr.readyState === 4 && xhr.status === 200) {
                    const response = xhr.responseText;
                    if(response.includes("가능")){
                        clearError(emailId, response);
                        domainInput.classList.add("is-valid");
                    }else {
                        showError(emailId, response);
                        domainInput.classList.add("is-invalid")
                    }
                }
            };
            xhr.send();
        }, 500);
    }
}

// 도메인 선택 핸들러
function selectDomain() {
    const domainInput = document.getElementById("registerDomain") || document.getElementById("emailDomainInput");
    const domainSelect = document.getElementById("emailDomainSelect");

    if (domainSelect.value === "") {
        domainInput.value = "";
        domainInput.readOnly = false;
        domainInput.focus();
    } else {
        domainInput.value = domainSelect.value;
        domainInput.readOnly = true;
    }
    EmailValidation();
}

// 전화번호 검사 (Regex)
function checkPhone(phone) {
    // MemberDto 패턴: 01012345678 형식
    const regPhone = /^01(?:0|1|[6-9])(?:\d{3}|\d{4})\d{4}$/;
    if(regPhone.test(phone.value)) {
        clearError(phone.id, "올바른 전화번호 형식입니다.");
    } else {
        showError(phone.id, "(-) 제외 10~11자리 번호만 입력해주세요.")
    }
}

// 비밀번호 검사 (8자 이상)
function checkPwd(pwd) {
    if(pwd.value.length >= 8) {
        clearError(pwd.id, "사용 가능한 비밀번호 입니다. ")
    } else {
        showError(pwd.id, "비밀번호는 8자 이상이어야 합니다.")
    }
    checkPwdConfirm(); // 비번 바뀔 때 확인란도 재체크
}

// 비밀번호 일치 확인
function checkPwdConfirm() {
    const pwdInput = document.getElementById("registerPwd") || document.getElementById("newPassword");
    const confirmInput = document.getElementById("registerPwdConfirm") || document.getElementById("pwdConfirm");
    const pwdId = confirmInput.id;
    //const resultShow = document.getElementById("serverPwdConfirmError") || document.getElementById("pwdConfirmResult");

    if (!pwdInput || !confirmInput) return;

    if (pwdInput.value === confirmInput.value && confirmInput.value !== "") {
        clearError(pwdId, "비밀번호가 일치합니다.")
    } else {
        showError(pwdId, "비밀번호가 일치하지 않습니다. ")
    }
}