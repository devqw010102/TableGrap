let validationTimer;
const currentPathType = window.location.pathname.includes('owner') ? 'owner' : 'member';

// error
const showError = (errorId, message) => {
    const errorDiv = document.getElementById(`error-${errorId}`);
    const inputId = document.getElementById(errorId);

    if(errorDiv) {
        errorDiv.innerText = message;
        errorDiv.style.color = "red";
    }
    if(inputId) {
        inputId.classList.remove('is-valid');
        inputId.classList.add('is-invalid');
    }
};

const clearError = (elementId, successMsg = "") => {
    const errorDiv = document.getElementById(`error-${elementId}`);
    const inputValue = document.getElementById(elementId);

    if (errorDiv) {
        errorDiv.innerText = successMsg;
        errorDiv.style.color = "green";
    }
    if (inputValue) {
        inputValue.classList.remove('is-invalid');
        inputValue.classList.add('is-valid');
    }
};

// 이름 (한글/영문 10자까지)
function checkName(name) {
    const nameVal = name.value.trim();
    const nameReg = /^[가-힣]{2,10}$/;

    if (nameVal.length > 1 && nameVal.length <= 10 && nameReg.test(nameVal)) {
        clearError("registerName");
    } else {
        showError("registerName", "이름은 한글 2~10자 이내여야 합니다.");
    }
}

// 아이디 중복 체크 (Register)
function checkUsername(id) {
    const username = id.value.trim();
    const usernameId = "registerId";

    clearTimeout(validationTimer);

    const idReg = /^[a-zA-Z0-9]{3,12}$/;
    if (username.length < 3 || username.length > 12 || !idReg.test(username)) {
        showError(usernameId, "아이디는 영문 소문자로 시작하고 숫자를 포함한 3~12자여야 합니다.");
        return;
    }

    validationTimer = setTimeout(() => {
        const xhr = new XMLHttpRequest();
        xhr.open("GET", `/api/${currentPathType}/check-username?username=${encodeURIComponent(username)}`, true);

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
    const emailInput = document.getElementById("registerEmail") || document.getElementById("emailId");
    const domainInput = document.getElementById("registerDomain") || document.getElementById("emailDomainInput");
    const totalEmailInput = document.getElementById("totalEmail");

    if (!emailInput || !domainInput) return;

    const emailId = emailInput.id;
    const emailPrefix = emailInput.value.trim();
    const domain = domainInput.value.trim();

    emailInput.classList.remove('is-valid', 'is-invalid');
    domainInput.classList.remove('is-valid', 'is-invalid');


    if(emailPrefix.length < 2 || domain === "") {
        showError(emailId, "이메일 앞자리는 2자 이상, 도메인은 필수입니다.");
        domainInput.classList.add('is-invalid');
        return false;
    }

    const fullEmail = `${emailPrefix}@${domain}`;
    const emailReg = /^[a-zA-Z0-9]{2,}@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

    if(!emailReg.test(fullEmail)) {
        showError(emailId, "유효한 이메일 형식이 아닙니다.");
        if(totalEmailInput) totalEmailInput.value = "";
        return false;
    }

    if(totalEmailInput) totalEmailInput.value = fullEmail;

    const originEmail = emailInput.dataset.origin;
    if(originEmail && fullEmail === originEmail) {
        clearError(emailInput.id, "현재 사용 중인 이메일입니다.");
        domainInput.classList.add('is-valid');
    return;
    }

    clearTimeout(validationTimer);
    validationTimer = setTimeout(() => {
        const xhr = new XMLHttpRequest();
        xhr.open("GET", `/api/${currentPathType}/check-email?email=${encodeURIComponent(fullEmail)}`);
        xhr.onreadystatechange = function () {
            if(xhr.readyState === 4 && xhr.status === 200) {
                const response = xhr.responseText;
                if(response.includes("가능")){
                    clearError(emailId, response);
                    domainInput.classList.remove('is-invalid');
                    domainInput.classList.add('is-valid');
                } else {
                    showError(emailId, response);
                    domainInput.classList.remove('is-valid');
                    domainInput.classList.add('is-invalid');
                }
            }
        };
        xhr.send();
    }, 500);
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

// 전화번호 검사
function checkPhone(phone) {
    const phoneVal = phone.value.trim();
    const regPhone = /^01(?:0|1|[6-9])(?:\d{3}|\d{4})\d{4}$/;

    if (phoneVal === "") {
        const errorDiv = document.getElementById(`error-${phone.id}`);
        if (errorDiv) errorDiv.innerText = "";
        phone.classList.remove('is-invalid', 'is-valid');
        return;
    }

    if(regPhone.test(phoneVal)) {
        clearError(phone.id, "올바른 전화번호 형식입니다.");
    } else {
        showError(phone.id, "(-) 제외 10~11자리 번호만 입력해주세요.");
    }
}

// 비밀번호 검사
function checkPwd(pwd) {
    const pwdVal = pwd.value;
    const pwdReg = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\d~!@#$%^&*()+|=]{8,16}$/;

    if(pwdReg.test(pwdVal)) {
        clearError(pwd.id, "사용 가능한 비밀번호입니다.");
    } else {
        showError(pwd.id, "영문, 숫자, 특수문자(~!@#$%^&*)를 포함하여 8자 이상 입력해주세요.");
    }
    checkPwdConfirm(); // 비번 바뀔 때 확인란도 재체크
}

// 비밀번호 일치 확인
function checkPwdConfirm() {
    const pwdInput = document.getElementById("registerPwd") || document.getElementById("newPassword");
    const confirmInput = document.getElementById("registerPwdConfirm") || document.getElementById("pwdConfirm");
    const pwdId = confirmInput.id;

    if (!pwdInput || !confirmInput) return;

    if (pwdInput.value === confirmInput.value && confirmInput.value !== "") {
        clearError(pwdId, "비밀번호가 일치합니다.")
    } else {
        showError(pwdId, "비밀번호가 일치하지 않습니다. ")
    }
}
    //사업자 번호 유효성 검사
    function checkBusinessNum(businessNumber) {
        const bizNumVal = businessNumber.value.trim();
        const bizNumReg = /^\d{10}$/;

        if (bizNumReg.test(bizNumVal)) {
            clearError(businessNumber.id);
        } else {
            showError(businessNumber.id, "사업자 번호는 (-)를 제외한 숫자 10자리여야 합니다.");
        }
    }

        // 아이디 찾기, 비밀번호 재설정
        // 아이디 형식만 검사 - resetPwd
        function checkUsernameFormat(idInput) {
            const username = idInput.value.trim();
            const idReg = /^[a-zA-Z0-9]{3,12}$/;

            if (username.length >= 3 && username.length <= 12 && idReg.test(username)) {
                clearError(idInput.id);
                return true;
            } else {
                showError(idInput.id, "아이디는 영문/숫자 3~12자여야 합니다.");
                return false;
            }
        }

        // 이메일 형식만 검사 - findId, resetPwd
        function checkEmailFormat() {
            const emailInput = document.getElementById("registerEmail");
            const domainInput = document.getElementById("registerDomain");
            const totalEmailInput = document.getElementById("totalEmail");

            const emailPrefix = emailInput.value.trim();
            const domain = domainInput.value.trim();
            const fullEmail = `${emailPrefix}@${domain}`;
            const emailReg = /^[a-zA-Z0-9]{2,}@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

            if (emailReg.test(fullEmail)) {
                clearError(emailInput.id);
                domainInput.classList.remove('is-invalid');
                domainInput.classList.add('is-valid');
                if (totalEmailInput) totalEmailInput.value = fullEmail;
                return true;
            } else {
                // 형식이 안 맞을 때만 에러 표시
                showError(emailInput.id, "유효한 이메일 형식이 아닙니다.");
                domainInput.classList.add('is-invalid');
                return false;
            }
        }
        // 이메일 선택 시
    function selectDomainSimple() {
        const domainInput = document.getElementById("registerDomain");
        const domainSelect = document.getElementById("emailDomainSelect");

        if (domainInput && domainSelect) {
            domainInput.value = domainSelect.value;
            domainInput.readOnly = (domainSelect.value !== "");
            checkEmailFormat();
        }
}