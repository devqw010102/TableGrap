import pandas as pd
import plotly.graph_objects as go

def generate(data, kiwi=None):
    try:
        if not data or len(data) == 0:
            return {}

        df = pd.DataFrame(data)
        if df.empty:
            return {}

        df = df.sort_values(by='averageRating', ascending=False).head(5)

        fig = go.Figure(data=[go.Bar(
            x=df['averageRating'].tolist(),
            y=df['dinerName'].tolist(),
            orientation='h',
            marker=dict(color=['#FF7E00', '#FF9E33', '#FFBD66', '#FFDC99', '#FFEBCC']),
            customdata=df['dinerId'].tolist(),
            text=df['averageRating'].tolist(),
            textposition='outside',
            cliponaxis=False
        )])

        fig.update_layout(
            xaxis=dict(range=[0, 5.5], showgrid=False, showticklabels=False, zeroline=False),
            yaxis=dict(autorange="reversed"),
            margin=dict(l=10, r=50, t=10, b=10),
            paper_bgcolor='rgba(0,0,0,0)',
            plot_bgcolor='rgba(0,0,0,0)',
            height=280,
            dragmode=False,
            hovermode='closest',
            clickmode='event+select'
        )

        return fig.to_dict()

    except Exception as e:
        import traceback
        print(f"Python Error: {traceback.format_exc()}")
        return {"error": str(e)}