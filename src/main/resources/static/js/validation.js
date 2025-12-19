let validationTimer;

function checkName(el) {
    if (el.value.trim().length > 0 && el.value.length <= 10) { // MemberDto @Size(max=10)
        el.classList.add("is-valid");
        el.classList.remove("is-invalid");
    } else {
        el.classList.add("is-invalid");
        el.classList.remove("is-valid");
    }
}

// 아이디 중복 체크 (Register)
function checkUsername(el) {
    const username = el.value;
    const resultShow = document.getElementById("serverUsernameError");

    clearTimeout(validationTimer);
    if (username.length < 4 || username.length > 20) { // MemberDto @Size
        if(resultShow) resultShow.innerHTML = "4~20자 사이로 입력해주세요.";
        el.classList.add("is-invalid");
        return;
    }

    validationTimer = setTimeout(() => {
        const xhr = new XMLHttpRequest();
        xhr.open("GET", `/api/member/check-username?username=${encodeURIComponent(username)}`);
        xhr.onreadystatechange = function () {
            if(xhr.readyState === 4 && xhr.status === 200) {
                const isDuplicate = JSON.parse(xhr.responseText);
                if (isDuplicate) {
                    if(resultShow) resultShow.innerHTML = "이미 사용 중인 아이디입니다.";
                    el.classList.add("is-invalid");
                    el.classList.remove("is-valid");
                } else {
                    if(resultShow) resultShow.innerHTML = "사용 가능한 아이디입니다.";
                    el.classList.add("is-valid");
                    el.classList.remove("is-invalid");
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
    const resultShow = document.getElementById("emailCheckResult");
    const totalEmailField = document.getElementById("totalEmail");

    if (emailInput.value.trim() !== "" && domainInput.value.trim() !== "") {
        const fullEmail = `${emailInput.value}@${domainInput.value}`;
        if(totalEmailField) totalEmailField.value = fullEmail;

        clearTimeout(validationTimer);
        validationTimer = setTimeout(() => {
            const xhr = new XMLHttpRequest();
            // MemberController의 /api/member/check-email 호출
            xhr.open("GET", `/api/member/check-email?email=${encodeURIComponent(fullEmail)}`);
            xhr.onreadystatechange = function () {
                if(xhr.readyState === 4 && xhr.status === 200) {
                    if(resultShow) {
                        resultShow.innerHTML = xhr.responseText;
                        resultShow.style.color = xhr.responseText.includes("가능") ? "green" : "red";
                    }
                    // 성공/실패 여부
                    if (xhr.responseText.includes("가능")) {
                        emailInput.classList.add("is-valid");
                        domainInput.classList.add("is-valid");
                    } else {
                        emailInput.classList.add("is-invalid");
                        domainInput.classList.add("is-invalid");
                    }
                }
            };
            xhr.send();
        }, 500);
    } else {
        if(totalEmailField) totalEmailField.value = "";
        const resultShow = document.getElementById("emailCheckResult");
        if(resultShow) resultShow.innerHTML = "";
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
function checkPhone(el) {
    // MemberDto 패턴: 01012345678 형식
    const regPhone = /^01(?:0|1|[6-9])(?:\d{3}|\d{4})\d{4}$/;
    if(regPhone.test(el.value)) {
        el.classList.replace("is-invalid", "is-valid");
    } else {
        el.classList.add("is-invalid");
    }
}

// 비밀번호 검사 (8자 이상)
function checkPwd(el) {
    if(el.value.length >= 8) { // MemberDto @Size(min=8)
        el.classList.replace("is-invalid", "is-valid");
    } else {
        el.classList.add("is-invalid");
    }
    checkPwdConfirm(); // 비번 바뀔 때 확인란도 재체크
}

// 비밀번호 일치 확인
function checkPwdConfirm() {
    const pwdInput = document.getElementById("registerPwd") || document.getElementById("newPassword");
    const confirmInput = document.getElementById("registerPwdConfirm") || document.getElementById("pwdConfirm");
    const resultShow = document.getElementById("serverPwdConfirmError") || document.getElementById("pwdConfirmResult");

    if (!pwdInput || !confirmInput) return;

    if (pwdInput.value === confirmInput.value && confirmInput.value !== "") {
        if(resultShow) resultShow.innerHTML = "비밀번호가 일치합니다.";
        confirmInput.classList.replace("is-invalid", "is-valid");
    } else {
        if(resultShow) resultShow.innerHTML = "비밀번호가 일치하지 않습니다.";
        confirmInput.classList.add("is-invalid");
    }
}