import pandas as pd
import json
import plotly.graph_objs as go

def generate(data, kiwi=None):
    try:
        raw_data = data.get('chartData', [])
        if not raw_data:
            return json.dumps({"data": [], "layout": {"title": "데이터가 없습니다."}}, ensure_ascii=False)

        df = pd.DataFrame(raw_data)

        dinerIdToName = {
            1:'그리즐리버거 삼산점', 6:'시리어스피자', 11:'남가족발', 21:'판(Pann)',
            30:'주효', 49:'와이오비(YOB)', 53:'빠이타이'
        }

        if not df.empty:
            #  재방문율 계산
            # 가계별 멤버별 방문 횟수
            visit_count = df.groupby(['dinerId', 'memberId']).size().reset_index(name='counts')
            # 2회 이상 방문 추출
            revisit_count = visit_count[visit_count['counts'] >= 2]
            # 가계별 재방문 횟수 추출
            revisit_count_diner = revisit_count.groupby('dinerId').size()
            total_count_diner = visit_count.groupby('dinerId').size()

            revisit_rate = (revisit_count_diner / total_count_diner).fillna(0) * 100 # fillna(0)은 재방문객이 0일 때, 오류 방지

            chart_data = revisit_rate.reset_index(name='rate')

            x_values = chart_data['dinerId'].replace(dinerIdToName).astype(str).tolist() # ID가 숫자면 문자로 변환 추천
            y_values = chart_data['rate'].tolist()
            fig = go.Figure()
            fig.add_trace(
                go.Bar(x=x_values,
                       y=y_values,
                       name='재방문율',
                       marker_color='orange',
                       opacity=0.7
                )
            )

            fig.update_layout(
                title_text='나의 식당 재방문율',
                xaxis_title='식당',
                 yaxis_title='재방문율(%)'
            )
            return fig.to_dict() # 딕셔너리 문자열로 치환
        else:
            return {"data": [], "layout": {}}
    except Exception as e:
        return {"error": str(e)}