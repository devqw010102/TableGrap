import uvicorn
from fastapi import FastAPI
from pydantic import BaseModel
import importlib
import base64
import json
from kiwipiepy import Kiwi
import sys

app = FastAPI()

# 1. 서버 시작 시 Kiwi 객체를 한 번만 생성
kiwi = Kiwi()

class ChartRequest(BaseModel):
    data: str

@app.post("/{domain}/{file_name}")
async def dynamic_chart_handler(domain: str, file_name: str, request: ChartRequest):
    try:
        # 데이터 복호화 로직 (Base64와 일반 JSON 모두 대응)
        try:
            decoded_bytes = base64.b64decode(request.data)
            json_data = json.loads(decoded_bytes.decode('utf-8'))
        except Exception:
            json_data = json.loads(request.data)

        # 모듈 동적 로드
        module_path = f"{domain}.{file_name}"
        if module_path in sys.modules:
            del sys.modules[module_path]

            # 새로 모듈 로드
        chart_module = importlib.import_module(module_path)

        if hasattr(chart_module, 'generate'):
            # 키워드 차트인 경우 kiwi 전달, 아니면 data만 전달하거나
            # 이전에 맞춘대로 (data, kiwi) 전달
            result = chart_module.generate(json_data, kiwi)
            return result
        else:
            return {"error": "generate function not found"}

    except Exception as e:
        # 로깅 에러 방지를 위해 print 대신 에러 객체 반환
        return {"error": str(e)}

if __name__ == "__main__":
    # access_log=False 설정을 통해 I/O operation on closed file 에러 방지
    uvicorn.run("main:app", host="127.0.0.1", port=8000, reload=True, access_log=False)