import pandas as pd
import re
import requests
import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()

# 8080 포트에서 이 파이썬 서버에 접근할 수 있도록 허용
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/api/owner-response/{owner_id}")
async def get_owner_response(owner_id: int):
    try:
        # API 호출
        try:
            response = requests.get("http://localhost:8080/api/notifications/response-data", timeout=5)
            response.raise_for_status()
            raw_data = response.json()
        except Exception as conn_err:
            return {"error": f"Spring Boot 연결 실패: {conn_err}"}

        if not raw_data:
            return {"ownerId": owner_id, "avg_response_min": 0, "msg": "데이터 없음"}

        df = pd.DataFrame(raw_data)

        # 메시지에서 [식당명]과 예약시간 추출
        def extract_info(msg):
            if not isinstance(msg, str):
                return pd.Series([None, None])

            # 데이터 형식: [그리즐리버거 삼산점] 2025-12-08 12:00:00 ...
            # 정규식: 대괄호 안 내용 추출 + 날짜/시간 형식 추출
            match = re.search(r'\[(.*?)\]\s+(\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2})', msg)
            if match:
                return pd.Series([match.group(1), match.group(2)])
            return pd.Series([None, None])

        # 정보 추출 및 컬럼 생성
        df[['diner_name', 'book_time']] = df['message'].apply(extract_info)

        # 유효하지 않은 데이터 제거
        df = df.dropna(subset=['diner_name', 'book_time'])

        if df.empty:
            return {"ownerId": owner_id, "avg_response_min": 0, "msg": "매칭되는 예약 정보 없음"}

        # 고유 키 생성 (식당이름 + 예약시간)
        df['common_key'] = df['diner_name'] + "_" + df['book_time']

        # 데이터 분리 및 병합 (신청 vs 승인/거절)
        req_df = df[df['type'] == 'RESERVATION_CREATE'][['common_key', 'createdAt', 'ownerId']]
        res_df = df[df['type'].isin(['RESERVATION_APPROVE', 'RESERVATION_REJECT'])][['common_key', 'createdAt']]

        # 두 데이터를 키값 기준으로
        merged = pd.merge(req_df, res_df, on='common_key', suffixes=('_req', '_res'))

        if merged.empty:
            return {"ownerId": owner_id, "avg_response_min": 0, "msg": "신청-결과 쌍이 존재하지 않음"}

        # 시간 차이 계산 (분 단위)
        merged['diff_min'] = (pd.to_datetime(merged['createdAt_res']) -
                              pd.to_datetime(merged['createdAt_req'])).dt.total_seconds() / 60

        # 특정 사장님 ID로 필터링
        owner_data = merged[merged['ownerId'] == owner_id]

        if owner_data.empty:
            return {"ownerId": owner_id, "avg_response_min": 0, "msg": "이 사장님의 응답 데이터가 없음"}

        # 평균값 계산
        avg_min = owner_data['diff_min'].mean()

        return {
            "ownerId": owner_id,
            "avg_response_min": round(float(avg_min), 1)
        }

    except Exception as e:
        print(f"Server Error: {e}")
        return {"error": str(e)}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)