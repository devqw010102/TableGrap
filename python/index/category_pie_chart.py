import pandas as pd
import plotly.graph_objects as go

def generate(data, kiwi=None):
    try:
        if not data: return {}

        df = pd.DataFrame(data)
        if df.empty: return {}

        df = df.sort_values(by='count', ascending=False).head(5)

        fig = go.Figure(data=[go.Pie(
            labels=df['category'].tolist(),
            values=df['count'].tolist(),
            hole=.5,
            marker=dict(colors=['#FF7E00', '#FF9E33', '#FFBD66', '#FFDC99', '#FFEBCC']),
            textinfo='label+percent'
        )])

        fig.update_layout(
            margin=dict(l=10, r=10, t=10, b=10),
            showlegend=False,
            paper_bgcolor='rgba(0,0,0,0)',
            height=280,
            dragmode=False
        )

        return fig.to_dict()
    except Exception as e:
        return {"error": str(e)}