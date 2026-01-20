import pandas as pd
import plotly.graph_objects as go

# 데이터 타입 경고 방지 및 미래 설정 적용
pd.set_option('future.no_silent_downcasting', True)

def generate(data, kiwi=None):
    try:
        # 데이터 가져오기 (기존 'visitor' 키 유지)
        raw_data = data.get('visitor', [])

        # 데이터가 없거나 필터링 후 결과가 없을 때 사용할 공통 빈 레이아웃 설정 함수
        def get_empty_state():
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

        # 1. 초기 데이터가 없는 경우
        if not raw_data:
            return get_empty_state()

        df = pd.DataFrame(raw_data)

        # DB의 시간(KST)을 정확히 읽기 위해 타임존 제거
        df['date'] = pd.to_datetime(df['date']).dt.tz_localize(None)

        # 분석용 요일 및 시간 정보 추출
        df['hour_int'] = df['date'].dt.hour
        df['day_of_week_en'] = df['date'].dt.day_name().str[:3].str.upper()

        # 분석할 영업시간 설정 (11시 ~ 20시)
        target_hours_int = [11, 12, 13, 17, 18, 19, 20]
        target_hours_str = [str(h) for h in target_hours_int]

        # 타겟 시간대 데이터만 필터링
        df = df[df['hour_int'].isin(target_hours_int)]

        # 2. 필터링 후 데이터가 없는 경우
        if df.empty:
            return get_empty_state()

        df['hour'] = df['hour_int'].astype(str)

        day_map = {
            'MON': '월요일', 'TUE': '화요일', 'WED': '수요일',
            'THU': '목요일', 'FRI': '금요일', 'SAT': '토요일', 'SUN': '일요일'
        }
        day_order_display = ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN']
        day_order_reversed = day_order_display[::-1]

        # 요일(Y)과 시간(X) 기준으로 예약 건수(밀집도) 계산
        density_matrix = df.groupby(['day_of_week_en', 'hour']).size().unstack(fill_value=0)
        density_matrix = density_matrix.reindex(index=day_order_reversed, columns=target_hours_str, fill_value=0)

        # 히트맵 데이터 준비
        z_data = density_matrix.values.tolist()
        y_labels = [day_map[d] for d in day_order_reversed]
        x_labels = [f"{h}시" for h in target_hours_str]

        # 3. 히트맵(밀집도 그래프) 생성
        fig = go.Figure(data=go.Heatmap(
            z=z_data,
            x=x_labels,
            y=y_labels,
            colorscale='YlOrRd',
            showscale=True,
            text=z_data,
            texttemplate="%{text}",
            textfont={"size": 12, "color": "black"},
            hoverongaps=False,
            hovertemplate='<b>%{y} %{x}</b><br>예약 밀집도: <b>%{z}건</b><extra></extra>'
        ))

        # 통계 요약 텍스트
        most_frequent_hour = df['hour'].value_counts().idxmax()
        most_frequent_day = day_map[df['day_of_week_en'].value_counts().idxmax()]
        stats_text = f"최대 밀집 시간: {most_frequent_hour}시 | 최대 밀집 요일: {most_frequent_day}"

        # 레이아웃 설정
        fig.update_layout(
            title={
                'text': f"<b>시간대별 예약 밀집도 분석</b><br><span style='font-size:12px; color:#666;'>{stats_text}</span>",
                'x': 0.5,
                'xanchor': 'center',
                'font': {'size': 18}
            },
            xaxis=dict(title="예약 시간", fixedrange=True),
            yaxis=dict(title="요일", fixedrange=True),
            plot_bgcolor='white',
            margin=dict(t=100, l=80, r=40, b=60)
        )

        return fig.to_dict()

    except Exception as e:
        return {"error": str(e)}