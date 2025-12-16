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
    getOwnerBtn.addEventListener("click", () => {
        // 1. 체크된 박스 가져오기
        const checkedBoxes = document.querySelectorAll('input[name="selectDiner"]:checked');

        if (checkedBoxes.length === 0) {
            alert("선택된 식당이 없습니다.");
            return;
        }

        if (!confirm(`${checkedBoxes.length}개의 식당의 사장님이신가요?`)) {
            return;
        }

        // 2. 체크된 박스에서 value(dinerId)만 뽑아서 배열로 만듦
        const selectedDinerIds = Array.from(checkedBoxes).map(cb => cb.value);

        // 3. 서버 전송 (Fetch API)
        fetch("/api/diners/ownerRequest", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ dinerIds: selectedDinerIds })
        })
        .then(res => {
            if (res.ok) {
                alert("신청이 완료되었습니다.");
            } else {
                return res.text().then(text => { throw new Error(text) });
            }
        })
        .catch(error => {
            console.error("신청 실패:", err);
        });
    });
});
