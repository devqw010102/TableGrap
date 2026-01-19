import json
import plotly.graph_objects as go
from datetime import datetime
from dateutil.relativedelta import relativedelta

def generate(data, kiwi=None):
    if isinstance(data, str):
        input_data = json.loads(data)
    else:
        input_data = data

    my_history = input_data.get('myHistory', [])

    # --- 데이터가 없을 경우 예외 처리 추가 ---
    if not my_history:
        empty_layout = {
            "title": {
                "text": "현재 데이터 수집 중입니다.",
                "x": 0.5,
                "y": 0.5,
                "xanchor": "center",
                "yanchor": "middle",
                "font": {"size": 18}
            },
            "xaxis": {"visible": False},
            "yaxis": {"visible": False},
            "annotations": [
                {
                    "text": "데이터가 충분히 쌓이면 이곳에 차트가 표시됩니다.",
                    "x": 0.5,
                    "y": 0.4,
                    "xref": "paper",
                    "yref": "paper",
                    "showarrow": False,
                    "font": {"size": 12, "color": "gray"}
                }
            ],
            "template": "plotly_white",
            "margin": dict(l=20, r=20, t=60, b=40),
            "paper_bgcolor": 'rgba(0,0,0,0)',
            "plot_bgcolor": 'rgba(0,0,0,0)',
        }

        return json.dumps({
            "data": [],
            "layout": empty_layout,
            "percentile": input_data.get('percentile', 0),
            "avgAll": round(input_data.get('avgAll', 0), 1),
            "totalCount": input_data.get('totalCount', 0)
        }, ensure_ascii=False)
    # ---------------------------------------

    # 1. 최근 6개월 달력 생성 (정확한 월 단위 계산)
    now = datetime.now()
    all_months = []
    for i in range(5, -1, -1):
        # 현재 기준 i개월 전의 연-월 계산
        target_date = now - relativedelta(months=i)
        all_months.append(target_date.strftime('%Y-%m'))

    history_map = {item['month']: item['count'] for item in my_history}
    counts = [history_map.get(m, 0) for m in all_months]

    # 2. 차트 생성
    fig = go.Figure()
    fig.add_trace(go.Bar(
        x=all_months,
        y=counts,
        name='나의 방문',
        marker_color=['#FF7E00', '#FF9E33', '#FFBD66', '#FFDC99', '#FFEBCC', '#FFBD66'],
        text=[str(c) for c in counts],
        textposition='auto',
    ))

    fig.update_layout(
        title=None,
        autosize=True,
        margin=dict(l=40, r=20, t=20, b=40),
        paper_bgcolor='rgba(0,0,0,0)',
        plot_bgcolor='rgba(0,0,0,0)',
        yaxis=dict(fixedrange=True),
        xaxis=dict(fixedrange=True)
    )

    # 3. 결과 리턴 (ensure_ascii=False로 한글 깨짐 방지)
    return json.dumps({
        "data": fig.to_dict()['data'],
        "layout": fig.to_dict()['layout'],
        "percentile": input_data.get('percentile', 0),
        "avgAll": round(input_data.get('avgAll', 0), 1),
        "totalCount": input_data.get('totalCount', 0)
    }, ensure_ascii=False)