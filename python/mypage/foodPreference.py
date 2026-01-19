import pandas as pd
import plotly.graph_objects as go

def generate(data, kiwi=None):
    try:
        # 1. 데이터 프레임 생성
        if isinstance(data, list):
            df = pd.DataFrame(data)
        elif isinstance(data, dict) and 'categories' in data:
            df = pd.DataFrame(data['categories'])
        else:
            df = pd.DataFrame() # 빈 데이터프레임 생성

        # --- 데이터가 없을 경우 예외 처리 추가 ---
        if df.empty or (not df.empty and df['category'].str.strip().eq("").all()):
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
                "margin": dict(l=10, r=10, t=10, b=10),
                "paper_bgcolor": 'rgba(0,0,0,0)',
                "plot_bgcolor": 'rgba(0,0,0,0)'
            }
            # 이 파일의 리턴 형식인 dict에 맞춰서 반환합니다.
            return {'data': [], 'layout': empty_layout}
        # ---------------------------------------

        df = df[df['category'].str.strip() != ""]
        df = df.sort_values(by='count', ascending=False).head(5)

        fig = go.Figure(data=[go.Pie(
            labels=df['category'].tolist(),
            values=df['count'].tolist(),
            hole=.5,
            domain={'x': [0, 1], 'y': [0, 1]},
            marker=dict(colors=['#FF7E00', '#FF9E33', '#FFBD66', '#FFDC99', '#FFEBCC']),
            textinfo='label+percent',
            textposition='inside'
        )])

        fig.update_layout(
            autosize=True,
            margin=dict(l=30, r=30, t=30, b=30),
            showlegend=False,
            dragmode=False,
            paper_bgcolor='rgba(0,0,0,0)',
            plot_bgcolor='rgba(0,0,0,0)',
        )

        fig.update_traces(
            textposition='inside',
            textinfo='label+percent'
        )

        return fig.to_dict()
    except Exception as e:
        return{"error": str(e)}