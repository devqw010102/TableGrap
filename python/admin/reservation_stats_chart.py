import json
import pandas as pd
import plotly.graph_objects as go

def generate(data, kiwi=None):
    try:
        # 1. 기준 날짜 설정
        end_date = pd.Timestamp.now().normalize()
        start_date = end_date - pd.Timedelta(days=6)
        all_dates = pd.date_range(start=start_date, end=end_date).strftime('%Y-%m-%d').tolist()

        if data:
            df = pd.DataFrame(data)
            df['date'] = pd.to_datetime(df['date']).dt.strftime('%Y-%m-%d')
            df = df.groupby('date')['count'].sum().reset_index()
            full_df = pd.DataFrame({'date': all_dates})
            df = pd.merge(full_df, df, on='date', how='left').fillna(0)
        else:
            df = pd.DataFrame({'date': all_dates, 'count': 0})

        dates = df['date'].tolist()
        counts = [int(x) for x in df['count'].tolist()]

        fig = go.Figure()
        fig.add_trace(go.Bar(x=dates, y=counts, name='예약 건수', marker_color='rgba(55, 128, 191, 0.7)'))
        fig.add_trace(go.Scatter(x=dates, y=counts, name='추세', line=dict(color='firebrick', width=2)))

        fig.update_layout(
            title={'text': '최근 1주일 예약 현황', 'x': 0.5},
            xaxis={'type': 'category'},
            yaxis={'title': "예약 건수", 'dtick': 1},
            template='plotly_white',
            margin=dict(l=50, r=50, t=80, b=50),
            dragmode=False
        )

        chart_dict = fig.to_dict()
        chart_dict['data'][0]['x'], chart_dict['data'][0]['y'] = dates, counts
        chart_dict['data'][1]['x'], chart_dict['data'][1]['y'] = dates, counts

        return chart_dict
    except Exception as e:
        return {"error": str(e)}