def generate(json_data, kiwi) :
    try :
        content = json_data.get('content', "")

        if not content or len(content.strip()) == 0 :
            return {"keywords": []}

        # 불용어, 추가해야할듯..
        EXCLUDE_WORDS = {
            '진짜', '너무', '오늘', '어제', '방문', '의사', '추천',
            '생각', '사람', '우리', '그냥', '많이', '조금', '정말',
            '때문', '정도', '하나', '무조건', '예약', '시간', '이번', '자리', '매장', '잡내', '울산', '요리'
        }
        # kiwi AI 모델 사용
        analysis_result = kiwi.analyze(content)
        tokens = analysis_result[0][0]

        extracted_keywords = []
        for token in tokens :
            if token.tag in ['NNG', 'NNP'] and len(token.form) >= 2 and token.form not in EXCLUDE_WORDS :
                extracted_keywords.append(token.form)

        unique_keywords = list(dict.fromkeys(extracted_keywords))
        result_keywords = unique_keywords[:5]

        return {
            "status" : "success",
            "keywords" : result_keywords
        }

    except Exception as e:
        return {
            "status": "error",
            "message": str(e),
            "keywords": []
        }