import sys
import io
import json
import pandas as pd
import plotly.express as px

# 인코딩 설정
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
sys.stdin = io.TextIOWrapper(sys.stdin.buffer, encoding='utf-8')

def main():
    try:
        raw_input = sys.stdin.read()
        if not raw_input: return

        data = json.loads(raw_input)
        df = pd.DataFrame(data)

        # 1. 데이터를 순수 리스트로 추출 (가장 중요)
        # .tolist()를 하면 numpy 데이터 타입이 완전히 제거된 순수 숫자 리스트가 됩니다.
        counts = pd.to_numeric(df['count']).tolist()
        categories = df['category'].tolist()

        # 2. 차트 생성
        fig = px.pie(
            values=counts,
            names=categories,
            hole=0.4,
            color_discrete_sequence=px.colors.qualitative.Pastel
        )

        fig.update_layout(
            font=dict(family="Malgun Gothic, Dotum, sans-serif", size=12),
            title={'text': f"카테고리별 식당 분포 (총 {int(sum(counts))}개)", 'x': 0.5},
            margin=dict(l=20, r=20, t=60, b=20),
            template="plotly_white",
            dragmode=False
        )

        # [최후의 수정] dict 변환 후 values와 labels를 강제로 순수 리스트로 덮어씁니다.
        chart_dict = fig.to_dict()

        # Plotly가 몰래 끼워넣은 압축 데이터(bdata 등)를 우리가 만든 순수 리스트로 교체합니다.
        chart_dict['data'][0]['values'] = counts
        chart_dict['data'][0]['labels'] = categories

        # 표준 json.dumps로 출력 (이러면 bdata가 끼어들 틈이 없습니다)
        output_json = json.dumps(chart_dict, ensure_ascii=False)
        sys.stdout.buffer.write(output_json.encode('utf-8'))
        sys.stdout.flush()

    except Exception as e:
        sys.stderr.write(f"Python Error: {str(e)}")
        sys.exit(1)

if __name__ == "__main__":
    main()