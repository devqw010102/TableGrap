import pandas as pd
import sys
import json
import io
import plotly.graph_objs as go
from plotly.subplots import make_subplots
import plotly.io as pio

sys.stdout = io.TextIOWrapper(sys.stdout.detach(), encoding='utf-8')
sys.stderr = io.TextIOWrapper(sys.stderr.detach(), encoding='utf-8')

# [디버깅] 이 로그가 자바 콘솔에 떠야 파일이 바뀐 겁니다!
print("--- [PYTHON START] Ver.최종수정본 ---", file=sys.stderr)

# spring에서 데이터 가져오기
try:
    input_str = sys.stdin.read()
    params = json.loads(input_str)
    raw_data = params['chartData']
except Exception as e:
    print(json.dumps({"error": str(e)}))
    sys.exit(1)

df = pd.DataFrame(raw_data)
# [디버깅용] 자바 콘솔에 데이터프레임 내용을 출력합니다. (JSON 통신 방해 X)
# print("========== [PYTHON DEBUG] DataFrame ==========", file=sys.stderr)
# print(df, file=sys.stderr)
# print("==============================================", file=sys.stderr)

dinerIdToName = {1:'그리즐리버거 삼산점', 6:'시리어스피자', 11:'남가족발', 21:'판(Pann)',
                 30:'주효', 49:'와이오비(YOB)', 53:'빠이타이'}
if not df.empty:
    df['dinerId'] = df['dinerId'].replace(dinerIdToName)
    df['dinerId'] = df['dinerId'].astype(str)
    # json으로 전송할 때 암호화(bdata)방지하기 위해  파이썬 리스트로 변환
    x_data = df['dinerId'].tolist()
    y_reviews = df['reviewCount'].tolist()
    y_ratings = df['averageRating'].tolist()

    # 축 범위 계산
    max_review = max(y_reviews) if y_reviews else 5

    # 차트 그리기
    # 이중축
    fig = make_subplots(specs=[[{"secondary_y": True}]])
    # 막대 그래프
    fig.add_trace(
        go.Bar(
            x=x_data,
            y=y_reviews,
            name='리뷰 수',
            marker_color='lightblue',
            opacity=0.7,
        ), secondary_y=False
    )
    fig.add_trace(
        go.Scatter(
            x=x_data,
            y=y_ratings,
            name='평균 별점',
            mode='lines+markers',
            marker=dict(size=10, color='orange'),
            line=dict(width=3, color='orange')
        ), secondary_y=True
    )
    fig.update_layout(
        title_text="식당별 리뷰 수 및 평균 별점 비교",
        width=700,
        height=500,
    )
    # X축을 무조건 '항목'으로 인식하게 강제함 (숫자로 인식 X)
    fig.update_xaxes(type='category')
    fig.update_yaxes(title_text='리뷰 갯수', range=[0, max_review + 2], secondary_y=False)
    fig.update_yaxes(title_text='평균 별점', range=[0, 5.5], showgrid=False, secondary_y=True)


    print(pio.to_json(fig))
else:
    # 데이터가 없을 때 빈 차트라도 보내야 에러가 안 납니다.
    print(json.dumps({"data": [], "layout": {}}))