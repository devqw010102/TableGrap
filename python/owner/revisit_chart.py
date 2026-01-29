import pandas as pd
import plotly.express as px
import plotly.graph_objs as go

def generate(data, kiwi=None):
    try:
        raw_data = data.get('chartData', [])
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
            #  재방문율 계산
            # 가계별 멤버별 방문 횟수
            visit_count = df.groupby(['dinerName', 'memberId']).size().reset_index(name='counts')
            # 2회 이상 방문 추출
            revisit_count = visit_count[visit_count['counts'] >= 2]
            # 가계별 재방문 횟수 추출
            revisit_count_diner = revisit_count.groupby('dinerName').size()
            total_count_diner = visit_count.groupby('dinerName').size()

            revisit_rate = (revisit_count_diner / total_count_diner).fillna(0) * 100 # fillna(0)은 재방문객이 0일 때, 오류 방지

            chart_data = revisit_rate.reset_index(name='rate')

            x_values = chart_data['dinerName'].tolist()
            y_values = chart_data['rate'].tolist()

            # 식당별 막대 color 설정
            color_list = px.colors.qualitative.Pastel1
            idx_colors = color_list * (len(x_values) // len(color_list) + 1)
            final_colors = idx_colors[:len(x_values)]

            fig = go.Figure()
            fig.add_trace(
                go.Bar(x=x_values,
                       y=y_values,
                       name='재방문율',
                       marker_color=final_colors,
                )
            )

            fig.update_layout(
                title_text='나의 식당 재방문율',
                yaxis_title='재방문율(%)',
                yaxis=dict(range=[0, 105], dtick=20),
                template='plotly_white',
            )
            return fig.to_dict()
        else:
            return {"data": [], "layout": {}}
    except Exception as e:
        return {"error": str(e)}