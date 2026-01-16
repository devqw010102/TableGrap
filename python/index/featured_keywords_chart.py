import pandas as pd
import plotly.graph_objects as go
from collections import Counter

def generate(data, kiwi):
    try:
        if not data or 'comments' not in data:
            return {"error": "데이터가 없습니다."}

        comments = data.get('comments', [])
        diner_name = data.get('dinerName', '알 수 없는 식당')

        words = []
        for text in comments:
            if not text: continue
            tokens = kiwi.tokenize(text)
            for t in tokens:
                # 명사(NN)
                if t.tag.startswith('NN') and len(t.form) > 1:
                    words.append(t.form)

        count_data = Counter(words).most_common(5)
        if not count_data:
            return {"error": "분석할 수 있는 키워드가 부족합니다."}

        count_data.reverse()
        labels = [item[0] for item in count_data]
        values = [item[1] for item in count_data]

        fig = go.Figure(data=[go.Bar(
            x=values,
            y=labels,
            orientation='h',
            marker=dict(color=['#FF7E00', '#FF9E33', '#FFBD66', '#FFDC99', '#FFEBCC']),
            text=values,
            textposition='auto'
        )])

        fig.update_layout(
            title={'text': f"<b>{diner_name}</b> 핵심 키워드 TOP 5", 'x': 0.5},
            margin=dict(l=50, r=20, t=60, b=20),
            height=350,
            paper_bgcolor='rgba(0,0,0,0)',
            plot_bgcolor='rgba(0,0,0,0)',
            font=dict(size=12),
            dragmode=False,
            hovermode=False,
            xaxis=dict(fixedrange=True),
            yaxis=dict(fixedrange=True)
        )

        return fig.to_dict()
    except Exception as e:
        return {"error": str(e)}