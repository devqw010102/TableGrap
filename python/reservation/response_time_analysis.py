import pandas as pd
import re
import sys
import json

def analyze_response(raw_data, owner_id):
    # --- 데이터가 없을 경우 예외 처리 추가 ---
    if not raw_data:
        empty_layout = {
            "title": {
                "text": "현재 데이터 수집 중입니다.",
                "x": 0.5, "y": 0.5,
                "xanchor": "center", "yanchor": "middle",
                "font": {"size": 18}
            },
            "xaxis": {"visible": False},
            "yaxis": {"visible": False},
            "annotations": [{
                "text": "데이터가 충분히 쌓이면 이곳에 차트가 표시됩니다.",
                "x": 0.5, "y": 0.4,
                "xref": "paper", "yref": "paper",
                "showarrow": False,
                "font": {"size": 12, "color": "gray"}
            }],
            "template": "plotly_white",
            "paper_bgcolor": 'rgba(0,0,0,0)',
            "plot_bgcolor": 'rgba(0,0,0,0)',
        }
        return {
            "ownerId": owner_id,
            "avg_response_min": 0,
            "data": [],
            "layout": empty_layout,
            "msg": "데이터 없음"
        }
    # ---------------------------------------

    df = pd.DataFrame(raw_data)

    def extract_info(msg):
        if not isinstance(msg, str):
            return pd.Series([None, None])

        match = re.search(r'\[(.*?)\]\s*(\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2})', msg)
        if match:
            return pd.Series([match.group(1).strip(), match.group(2).strip()])
        return pd.Series([None, None])

    df[['diner_name', 'book_time']] = df['message'].apply(extract_info)
    df = df.dropna(subset=['diner_name', 'book_time'])

    if df.empty: return {"ownerId": owner_id, "avg_response_min": 0, "msg": "데이터 없음"}

    df['common_key'] = df['diner_name'] + "_" + df['book_time']
    req_df = df[df['type'] == 'RESERVATION_CREATE'][['common_key', 'createdAt', 'ownerId']]
    res_df = df[df['type'].isin(['RESERVATION_APPROVE', 'RESERVATION_REJECT'])][['common_key', 'createdAt']]

    merged = pd.merge(req_df, res_df, on='common_key', suffixes=('_req', '_res'))
    owner_data = merged[merged['ownerId'] == owner_id]

    if owner_data.empty:
        return {"ownerId": owner_id, "avg_response_min": 0, "msg": "이 사장님 데이터 없음"}

    # errors='coerce' 추가하여 잘못된 날짜 형식으로 인한 NaN 발생 방지
    res_time = pd.to_datetime(owner_data['createdAt_res'], errors='coerce')
    req_time = pd.to_datetime(owner_data['createdAt_req'], errors='coerce')

    diff = (res_time - req_time).dt.total_seconds()
    avg_min = diff.mean() / 60

    # 중요: avg_min이 NaN인 경우 0으로 치환
    if pd.isna(avg_min):
        avg_min = 0

    return {"ownerId": owner_id, "avg_response_min": round(float(avg_min), 1)}

def main():
    sys.stdin.reconfigure(encoding='utf-8')
    sys.stdout.reconfigure(encoding='utf-8')

    try:
        input_data = sys.stdin.read()
        if not input_data:
            print(json.dumps({"error" : "No input data provided", "avg_response_min" : 0}))
            return

        data_dict = json.loads(input_data)

        owner_id = int(data_dict.get('ownerId', 0))
        raw_notifications = data_dict.get('notification', [])

        result = analyze_response(raw_notifications, owner_id)

        print(json.dumps(result, ensure_ascii=False))

    except Exception as e:
        error_res = {
            "ownerId":0,
            "avg_response_min" : 0,
            "msg" : f"Python Error : {str(e)}",
            "error" : True
        }
        print(json.dumps(error_res, ensure_ascii=False))

if __name__ == "__main__":
    main()
