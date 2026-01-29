import pandas as pd
import plotly.express as px
import plotly.graph_objs as go
from plotly.subplots import make_subplots


def generate(data, kiwi=None):
    try:
        raw_data = data.get('chartData', [])
        diner_name = data.get('dinerName')
        if not raw_data:
            empty_layout = {
                "title": {
                    "text": "현재 데이터 수집 중입니다.",
                    "x": 0.5,          # 가로 중앙 정렬
                    "y": 0.5,          # 세로 중앙 정렬
                    "xanchor": "center",
                    "yanchor": "middle",
                    "font": {"size": 18} # 글자 크기 조절
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
                "template": "plotly_white" # 배경색 등 스타일 유지
            }
            # data는 빈 리스트로 두어 차트가 그려지지 않게 함
            return {'data': [], 'layout': empty_layout}

        df = pd.DataFrame(raw_data)

        if not df.empty:
            df['createTime'] = pd.to_datetime(df['createTime'])
            x_values = df['createTime'].dt.strftime('%Y-%m').tolist()
            y_reviews = df['reviewCount'].tolist()
            y_ratings = df['averageRating'].tolist()
            max_review = max(y_reviews) if y_reviews else 5
            fig = make_subplots(specs=[[{"secondary_y": True}]])

            # 막대 그래프
            fig.add_trace(
                go.Bar(
                    x=x_values,
                    y=y_reviews,
                    name = '리뷰수',
                    marker_color='lightblue',
                ),  secondary_y=False
            )
            fig.add_trace(
                go.Scatter(
                    x=x_values,
                    y=y_ratings,
                    name = '평점 평균',
                    mode = 'lines+markers',
                    marker=dict(size=10, color='orange'),
                    line=dict(width=3, color='orange')
                ), secondary_y=True
            )

            fig.update_layout(
                title = f'{diner_name}의 월간 리뷰',
                xaxis=dict(type='date', tickformat='%Y-%m', dtick="M1"),
                legend=dict(orientation="h", yanchor="bottom", y=1.02, xanchor="right", x=1),
                dragmode=False,
                template="plotly_white",
            )
            fig.update_xaxes(type='category')
            fig.update_yaxes(title_text='리뷰 갯수', range=[0, max_review + 2], secondary_y=False)
            fig.update_yaxes(title_text='평균 별점', range=[0, 5.5], showgrid=False, secondary_y=True)

            return fig.to_dict()
        else:
            return {"data": [], "layout": {"title": "표시할 데이터가 없습니다."}}
    except Exception as e:
        return {"error": str(e)}