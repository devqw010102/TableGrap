// 이메일 드롭다운
function selectDomain() {
    const domainInput = document.getElementById('emailDomainInput');
    const domainSelect = document.getElementById('emailDomainSelect');
    
    const selectedValue = domainSelect.value;

    if (selectedValue === "") {
        // 직접 입력창 비우고 쓰기 가능하게 함
        domainInput.value = "";
        domainInput.readOnly = false;
        domainInput.focus(); 
    } else {
        // 도메인 선택 시 입력창에 값 넣고 수정 불가능하게 막음
        domainInput.value = selectedValue;
        domainInput.readOnly = true;
    }
    // 합치기
    combineEmail();
}

// 이메일 합치기
function combineEmail() {
    const emailId = document.getElementById('emailId').value;
    const emailDomain = document.getElementById('emailDomainInput').value;
    const totalEmail = document.getElementById('totalEmail');

    // totalEmail 확인
    if(totalEmail) {
        if(emailId && emailDomain) {
            totalEmail.value = emailId + '@' + emailDomain;
        } else {
            totalEmail.value = '';
        }
    }
}

// 전화번호 합치기
function combinePhone() {
    const p1 = document.getElementById('phone1').value;
    const p2 = document.getElementById('phone2').value;
    const p3 = document.getElementById('phone3').value;
    const totalPhone = document.getElementById('totalPhone');

    // 모두 입력되었을 때만 합침
    if (p1.length >= 2 && p2.length >= 3 && p3.length === 4) {
        totalPhone.value = p1 + '-' + p2 + '-' + p3;
    } else {
        totalPhone.value = result;
    }
}