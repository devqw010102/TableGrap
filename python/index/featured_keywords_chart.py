# featured_keywords_chart.py
import pandas as pd
import plotly.graph_objects as go
from collections import Counter

def generate(data, kiwi):
    try:
        if not data: return {}

        comments = data.get('comments', [])
        if not comments: return {}

        # 명사 추출 및 빈도 계산
        words = []
        for text in comments:
            if not text: continue
            tokens = kiwi.tokenize(text)
            for t in tokens:
                if t.tag.startswith('NN') and len(t.form) > 1:
                    words.append(t.form)

        count_data = Counter(words).most_common(5)
        if not count_data: return {}

        # 데이터 역순 정렬 (상위 키워드가 위로 오게)
        count_data.reverse()
        labels = [item[0] for item in count_data]
        values = [item[1] for item in count_data]

        # 가로 막대 차트(go.Bar)로 생성
        fig = go.Figure(data=[go.Bar(
            x=values,
            y=labels,
            orientation='h',
            marker_color='#FF7E00', # 주황색 설정
            text=values,
            textposition='auto'
        )])

        fig.update_layout(
            title={'text': f"'{data.get('dinerName', '')}' 인기 키워드", 'x': 0.5},
            margin=dict(l=20, r=20, t=60, b=20),
            height=300,
            paper_bgcolor='rgba(0,0,0,0)',
            plot_bgcolor='rgba(0,0,0,0)',
            dragmode=False
        )

        return fig.to_dict()
    except Exception as e:
        return {"error": str(e)}