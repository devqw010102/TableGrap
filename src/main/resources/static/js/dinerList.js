/*
document.addEventListener("DOMContentLoaded", () => {
    const toggleBtn = document.getElementById("toggleBtn");
    const getOwnerBtn = document.getElementById("getOwnerBtn");

    //[혹시 사장님이신가요?] 버튼 토글 기능
    toggleBtn.addEventListener("click", () => {

        const hiddenCols = document.querySelectorAll(".owner-check");

        //  체크박스 열 보이기/숨기기 (이건 반복문이 필요함)
        hiddenCols.forEach(col => {
            col.style.display = (col.style.display === "none") ? "table-cell" : "none";
        });

        // 버튼 텍스트 및 신청 버튼 표시는 한 번만 하면 됨 (반복문 밖으로 뺌)
        const isHidden = (toggleBtn.innerText === "혹시 사장님이신가요?");

        if (isHidden) {
            toggleBtn.innerText = "취소";
            toggleBtn.classList.replace("btn-secondary", "btn-outline-secondary"); // 스타일 팁
            getOwnerBtn.style.display = "inline-block";
        } else {
            toggleBtn.innerText = "혹시 사장님이신가요?";
            toggleBtn.classList.replace("btn-outline-secondary", "btn-secondary");
            getOwnerBtn.style.display = "none";

            // 취소 누르면 체크박스 초기화
            document.querySelectorAll('input[name="selectDiner"]').forEach(cb => cb.checked = false);
        }
    });

    // 선택한 식당 신청
    //async와 await는 한 몸이다.(async가 없으면 69행 실행x)
    getOwnerBtn.addEventListener("click", async () => {await ownerRequest()});
});

// 3. 서버 전송 (Fetch API)
//async fetch메소드 선언
async function ownerRequest() {

    // 체크된 식당 리스트 select
    const checkedBoxes = document.querySelectorAll('input[name="selectDiner"]:checked');

    if (checkedBoxes.length === 0) {
        alert("선택된 식당이 없습니다.");
        return;
    }

    if (!confirm(`${checkedBoxes.length}개의 식당에 대해 사장 권한을 신청하시겠습니까?`)) {
        return;
    }

    const selectedDinerIds = Array.from(checkedBoxes).map(cb => Number(cb.value));

    // fetch
    try {
        const res = await fetch("/api/diner/ownerRequest", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ dinerIds: selectedDinerIds })
        });

        // 성공 (200, 201[create] 모두 허용)
        if (res.ok) {
            alert("사장 권한 신청이 접수되었습니다.");

            // 신청한 식당 체크 해제 (UX)
            checkedBoxes.forEach(cb => cb.checked = false);
            return;
        }

        const errorMsg = await res.text();

        switch (res.status) {
            case 401:
                alert("로그인이 필요한 서비스입니다.");
                location.href = "/login";
                break;
            case 403:
                alert("접근 권한이 없습니다.");
                break;
            case 409:
                // 중복 신청 / 이미 owner 존재 / 승인 대기
                // Service에서 처리
                alert(errorMsg);
                break;
            case 400:
                alert("잘못된 요청입니다.\n" + errorMsg);
                break;
            case 500:
                alert("서버 오류가 발생했습니다.\n잠시 후 다시 시도해주세요.");
                break;

            default:
                alert(errorMsg || "알 수 없는 오류가 발생했습니다.");
        }

    } catch (error) {
        console.error(error);
        alert("네트워크 오류가 발생했습니다.");
    }
}

 */