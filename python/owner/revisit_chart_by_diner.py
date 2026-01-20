import pandas as pd
import json
import plotly.graph_objs as go
import traceback

def generate (data, kiwi=None):
    try:
        # 1. 데이터 타입 방어 로직 (가장 유력한 원인 해결)
        # Java에서 보낸 JSON이 파이썬에 '문자열(String)'로 도착했을 경우, 딕셔너리로 변환해야 합니다.
        # if isinstance(data, str):
        #     data = json.loads(data)

        raw_data = data.get('chartData', [])
        if not raw_data:
            # 차트 축(axis)을 숨기고, 중앙에 메시지만 표시하는 레이아웃 설정
            empty_layout = {
                "title": {
                    "text": "현재 데이터 수집 중입니다.",
                    "x": 0.5,          # 가로 중앙 정렬
                    "y": 0.5,          # 세로 중앙 정렬
                    "xanchor": "center",
                    "yanchor": "middle",
                    "font": {"size": 18} # 글자 크기 조절
                },
                # X축과 Y축을 숨겨서 빈 좌표평면이 안 보이게 함
                "xaxis": {"visible": False},
                "yaxis": {"visible": False},
                # 필요하다면 부가 설명을 추가할 수 있음 (선택 사항)
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

        # target_diner_id = data.get('dinerId')
        # diner_name = dinerIdToName.get(target_diner_id)
        diner_name = data.get('dinerName')
        if not df.empty:
            df['bookingDate'] = pd.to_datetime(df['bookingDate'])
            df = df.sort_values('bookingDate')

            # 회원 별 누적 방문 횟수 카운트
            df["visit_count"] = df.groupby('memberId').cumcount()
            # 재방문 여부 칼럼 생성
            df['revisit'] = df['visit_count'] > 0
            # 월별 그룹화(년, 월 정보 추출)
            df['month'] = df['bookingDate'].dt.to_period('M')

            monthly_stats = df.groupby('month').agg(
                total_bookings=('memberId', 'count'),
                revisit=('revisit', 'sum'),
            )

            # 재방문율 계산
            monthly_stats['revisit_rate'] = (monthly_stats['revisit'] / monthly_stats['total_bookings']) * 100
            # 소수점 1자리까지 반올림
            monthly_stats['revisit_rate'] = monthly_stats['revisit_rate'].fillna(0).round(1)

            x_values = monthly_stats.index.to_timestamp().tolist()
            y_values = monthly_stats['revisit_rate'].tolist()
            fig = go.Figure()
            fig.add_trace(
                go.Scatter(x=x_values,
                           y=y_values,
                           name='재방문율',
                           mode='lines+markers+text',
                           text=[f"{x}%" for x in y_values],
                           textposition="top center",
                           marker=dict(size=10, color='orange'),
                           line=dict(width=3, color='orange'),
                           hovertemplate="<b>%{x|%Y-%m}</b><br>재방문율: %{y}%<extra></extra>"
                )
            )

            fig.update_layout(
                title_text=f'{diner_name} 재방문율',
                xaxis=dict(type='date', tickformat='%Y-%m', dtick="M1"),
                xaxis_title='월별',
                yaxis_title='재방문율(%)',
                yaxis=dict(range=[0, 105], dtick=20),
                template='plotly_white',
            )
            return json.loads(fig.to_json())
        else:
            return {'data': [], 'layout': {}}
    except Exception as e:
        error_msg = traceback.format_exc()
        print("Python Error Log:", error_msg) # 파이썬 콘솔에 출력
        return {"error": f"Python Processing Error: {str(e)}", "trace": error_msg}