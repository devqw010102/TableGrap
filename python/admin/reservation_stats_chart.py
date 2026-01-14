import sys, io, json
import pandas as pd
import plotly.graph_objects as go

# 인코딩 강제 설정
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
sys.stdin = io.TextIOWrapper(sys.stdin.buffer, encoding='utf-8')

def main():
    try:
        raw_input = sys.stdin.read().strip()
        # 데이터가 아예 없거나 빈 배열일 때 대응
        data = json.loads(raw_input) if raw_input else []

        # 1. 기준 날짜 설정 (오늘부터 7일전까지)
        end_date = pd.Timestamp.now().normalize()
        start_date = end_date - pd.Timedelta(days=6)
        all_dates = pd.date_range(start=start_date, end=end_date).strftime('%Y-%m-%d').tolist()

        # 2. 데이터 프레임 생성 및 정제
        if data:
            df = pd.DataFrame(data)
            # 날짜 형식을 YYYY-MM-DD로 통일
            df['date'] = pd.to_datetime(df['date']).dt.strftime('%Y-%m-%d')
            # 날짜별 합계 계산 (중복 날짜 방지)
            df = df.groupby('date')['count'].sum().reset_index()

            full_df = pd.DataFrame({'date': all_dates})
            df = pd.merge(full_df, df, on='date', how='left').fillna(0)
        else:
            df = pd.DataFrame({'date': all_dates, 'count': 0})

        # 3. 데이터 추출 (JSON 안정성을 위해 리스트 변환)
        dates = df['date'].tolist()
        counts = [int(x) for x in df['count'].tolist()] # 확실하게 정수 리스트로 변환

        # 4. 차트 생성
        fig = go.Figure()
        fig.add_trace(go.Bar(x=dates, y=counts, name='예약 건수', marker_color='rgba(55, 128, 191, 0.7)'))
        fig.add_trace(go.Scatter(x=dates, y=counts, name='추세', line=dict(color='firebrick', width=2)))

        fig.update_layout(
            title={'text': '최근 1주일 예약 현황', 'x': 0.5},
            xaxis={'type': 'category', 'title': "날짜"},
            yaxis={'title': "예약 건수", 'dtick': 1},
            template='plotly_white',
            margin=dict(l=50, r=50, t=80, b=50)
        )

        # 5. [핵심] JSON 직렬화 시 에러 방지 옵션 추가
        chart_dict = fig.to_dict()
        # 데이터 수동 재할당 (형식 깨짐 방지)
        chart_dict['data'][0]['x'], chart_dict['data'][0]['y'] = dates, counts
        chart_dict['data'][1]['x'], chart_dict['data'][1]['y'] = dates, counts

        # ignore_nan 옵션은 없지만, 위에서 int 변환으로 해결함
        print(json.dumps(chart_dict, ensure_ascii=False), flush=True)

    except Exception as e:
        # 에러 발생 시 유효한 JSON 에러 객체 반환
        error_msg = {"error": str(e)}
        print(json.dumps(error_msg))
        sys.exit(1)

if __name__ == "__main__":
    main()