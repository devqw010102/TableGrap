import pandas as pd
import plotly.graph_objects as go

def generate(data, kiwi=None):
    try:
        # 기존 코드의 'visitor' 키를 유지합니다.
        # (만약 데이터 소스 키가 'chartData'로 변경되었다면 아래 줄을 data.get('chartData', [])로 수정하세요)
        raw_data = data.get('visitor', [])

        # 1. 데이터가 없을 때: 사용자 요청에 따른 안내용 레이아웃 반환
        if not raw_data:
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
                "template": "plotly_white"
            }
            return {'data': [], 'layout': empty_layout}

        # 2. 데이터가 있는 경우의 정상 로직 시작
        df = pd.DataFrame(raw_data)
        df['date'] = pd.to_datetime(df['date'])

        # 요일 및 시간 추출
        df['day_of_week_en'] = df['date'].dt.day_name().str[:3].str.upper()
        df['hour'] = df['date'].dt.hour

        # 요일 한글 매핑
        day_map = {
            'MON': '월요일', 'TUE': '화요일', 'WED': '수요일',
            'THU': '목요일', 'FRI': '금요일', 'SAT': '토요일', 'SUN': '일요일'
        }

        # 통계 계산
        most_frequent_day_en = df['day_of_week_en'].value_counts().idxmax()
        most_frequent_day_kr = day_map.get(most_frequent_day_en, most_frequent_day_en)
        most_frequent_hour = df['hour'].value_counts().idxmax()

        # 요일별 데이터 정리
        day_order = ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN']
        day_total = df.groupby('day_of_week_en').size().reindex(day_order, fill_value=0)

        x_values = [day_map[d] for d in day_order]
        y_values = day_total.values.tolist()

        # 차트 생성
        fig = go.Figure()

        # 막대 그래프
        fig.add_trace(
            go.Bar(
                x=x_values,
                y=y_values,
                name="예약 건수",
                marker_color='rgba(52, 152, 219, 0.5)',
                hovertemplate='%{x}: <b>%{y}건</b><extra></extra>'
            )
        )

        # 꺾은선 그래프
        fig.add_trace(
            go.Scatter(
                x=x_values,
                y=y_values,
                name="추세",
                mode='lines+markers',
                line=dict(color='#e74c3c', width=3),
                marker=dict(size=10, color='#c0392b', symbol='circle'),
                hovertemplate='%{x} 추세: <b>%{y}건</b><extra></extra>'
            )
        )

        # 레이아웃 설정
        stats_text = f"인기 시간대: {most_frequent_hour}시 | 인기 요일: {most_frequent_day_kr}"

        fig.update_layout(
            title={
                'text': f"<b>요일별 예약 현황 분석</b><br><span style='font-size:13px; color:#666;'>{stats_text}</span>",
                'x': 0.5,
                'xanchor': 'center',
                'font': {'size': 20}
            },
            xaxis=dict(title="요일", showgrid=False, fixedrange=True),
            yaxis=dict(
                title="예약 건수(건)",
                gridcolor='rgba(0,0,0,0.05)',
                rangemode='tozero',
                zeroline=True,
                zerolinecolor='rgba(0,0,0,0.1)',
                fixedrange=True
            ),
            plot_bgcolor='white',
            margin=dict(t=120, l=40, r=40, b=60),
            legend=dict(
                orientation="h",
                yanchor="bottom",
                y=1.02,
                xanchor="right",
                x=1
            ),
            hovermode="x unified"
        )

        return fig.to_dict()

    except Exception as e:
        return {"error": str(e)}