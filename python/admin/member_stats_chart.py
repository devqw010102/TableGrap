import sys
import json
import plotly.graph_objects as go
import plotly.io as pio

def generate():
    try:
        # Java에서 보낸 List<Map> 데이터 파싱
        input_data = sys.stdin.read()
        stats_list = json.loads(input_data)

        # 리스트에서 필요한 값 추출
        user_cnt = next((item['count'] for item in stats_list if item['role'] == 'USER'), 0)
        owner_cnt = next((item['count'] for item in stats_list if item['role'] == 'OWNER'), 0)

        fig = go.Figure()

        # 가로형 누적 막대 설정
        fig.add_trace(go.Bar(
            y=['회원 구성'], x=[user_cnt], name='일반회원(USER)',
            orientation='h', marker_color='#0d6efd'
        ))
        fig.add_trace(go.Bar(
            y=['회원 구성'], x=[owner_cnt], name='사장님(OWNER)',
            orientation='h', marker_color='#198754'
        ))

        fig.update_layout(
            barmode='stack',
            height=60,  # 높이를 슬림하게 조절
            margin=dict(l=5, r=5, t=5, b=5), # 여백 최소화
            showlegend=False, # 공간 확보를 위해 범례 숨김 (HTML 라벨로 대체)
            paper_bgcolor='rgba(0,0,0,0)',
            plot_bgcolor='rgba(0,0,0,0)',
            xaxis=dict(visible=False),
            yaxis=dict(visible=False),
            dragmode=False
        )

        print(pio.to_json(fig))
    except Exception as e:
        print(json.dumps({"error": str(e)}))

if __name__ == "__main__":
    generate()