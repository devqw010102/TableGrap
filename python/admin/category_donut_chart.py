
import json
import pandas as pd
import plotly.express as px

def generate(data, kiwi = None):
    try:
        df = pd.DataFrame(data)

        if df.empty: return {"error": "데이터가 비어 있습니다."}

        counts = pd.to_numeric(df['count']).tolist()
        categories = df['category'].tolist()

        # 2. 차트 생성
        fig = px.pie(
            values=counts,
            names=categories,
            hole=0.4,
            color_discrete_sequence=px.colors.qualitative.Pastel
        )

        fig.update_layout(
            font=dict(family="Malgun Gothic, Dotum, sans-serif", size=12),
            title={'text': f"카테고리별 식당 분포 (총 {int(sum(counts))}개)", 'x': 0.5},
            margin=dict(l=20, r=20, t=60, b=20),
            template="plotly_white",
            dragmode=False
        )

        # 딕셔너리 형태로 반환하면 FastAPI가 JSON으로 자동 변환함
        chart_dict = fig.to_dict()

        chart_dict['data'][0]['values'] = counts
        chart_dict['data'][0]['labels'] = categories

        return chart_dict

    except Exception as e:
        return {"error": f"Python Error in category_donut_chart: {str(e)}"}