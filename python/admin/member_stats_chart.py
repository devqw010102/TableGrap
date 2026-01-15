import json
import plotly.graph_objects as go

def generate(data, kiwi=None):
    try:
        # data는 이미 JSON 객체(List<Map>)로 전달됨
        stats_list = data

        user_cnt = next((item['count'] for item in stats_list if item['role'] == 'USER'), 0)
        owner_cnt = next((item['count'] for item in stats_list if item['role'] == 'OWNER'), 0)

        fig = go.Figure()
        fig.add_trace(go.Bar(
            y=['회원 구성'], x=[user_cnt], name='일반회원(USER)',
            orientation='h', marker_color='#0d6efd'
        ))
        fig.add_trace(go.Bar(
            y=['회원 구성'], x=[owner_cnt], name='사장님(OWNER)',
            orientation='h', marker_color='#198754'
        ))

        fig.update_layout(
            barmode='stack', height=60,
            margin=dict(l=5, r=5, t=5, b=5),
            showlegend=False,
            paper_bgcolor='rgba(0,0,0,0)',
            plot_bgcolor='rgba(0,0,0,0)',
            xaxis=dict(visible=False),
            yaxis=dict(visible=False),
            dragmode=False
        )

        return fig.to_dict()
    except Exception as e:
        return {"error": str(e)}