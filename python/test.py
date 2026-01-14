import sys
import os

# 현재 실행 중인 파이썬의 경로 출력
print("현재 파이썬 경로:", sys.executable)

# 가상환경 안에 있는지 확인
if 'venv' in sys.executable:
    print("✅ 가상환경 연결 성공!")
else:
    print("❌ 기본 파이썬이 실행 중입니다.")