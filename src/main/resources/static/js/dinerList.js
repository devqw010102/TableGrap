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
    getOwnerBtn.addEventListener("click", async () => {
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
        //async fetch메소드 선언
        async function ownerRequest(){

        try{
        //await서버가 응답하면 다음 단계로 이동
         const res = await fetch("/diner/ownerRequest", {
                     method: "POST",
                     headers: {
                         "Content-Type": "application/json"
                     },
                     body: JSON.stringify({ dinerIds: selectedDinerIds })
          });
         // 신청 성공할 경우
         if(res.status === 200){
          alert("신청이 완료되었습니다.");
          return;
         }
         // 신청과정에서 오류 발생
         //에러 메세지를 읽어올 때까지 대기, 메시지를 가져오면 switch문 실행
         const errorMsg = await res.text();
         //에러 처리
         switch (res.status){
          case 401:
            alert("로그인이 필요한 서비스입니다.");
            location.href="/login";
          break;
          //중복 신청 할 경우
          case 409:
            alert("이미 신청하셨습니다." + errorMsg);
          break;
          //잘 못된 데이터
          case 400:
            alert("잘 못된 요청입니다." + errorMsg);
          break;
          case 500:
            alert("서버에 오류가 발생했습니다.")
          break;
          default: alert(errorMsg);
        }
        }catch(error){
          console.log(error);
          }
        };
    });
});

      /*
       fetch("/diner/ownerRequest", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({ dinerIds: selectedDinerIds })
        })
        //화살표 함수라서 async...?
        .then(async res => {
            //신청 성공
            if (res.status === 200) {
              alert("신청이 완료되었습니다.");
              return;
            }
        })

        //2 오류 처리
        const errorMsg = await res.text();//서버의 에러메세지
        Switch (res.status){
          //로그인하지 않았을 경우
          case 401:
            alert("로그인이 필요한 서비스입니다.");
            location.href="/login";
          break;
          //중복 신청 할 경우
          case 409:
            alert("이미 신청하셨습니다." + errorMsg);
          break;
          //잘 못된 데이터
          case 400:
            alert("잘 못된 요청입니다." + errorMsg);
          break;
          case 500:
            alert("서버에 오류가 발생했습니다.")
          break;

          default: alert(errorMsg);
        }
        .catch(error => {
            console.error("신청 실패:", error);
        });*/

