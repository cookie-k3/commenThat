import re
import torch
import torch.nn as nn
from bs4 import BeautifulSoup
from konlpy.tag import Okt
from transformers import AutoTokenizer, BertConfig, BertModel, BertPreTrainedModel

print("[INFO] 라이브러리 임포트 완료")

# 전처리: 텍스트 정제 및 Okt 토큰화
okt = Okt()
stopwords = ['의', '가', '이', '은', '들', '는', '좀', '잘', '걍', '과', '도', '를',
             '으로', '자', '에', '와', '한', '하다', '되다', '있다', '이다']

def clean_text(text):
    return re.sub(r"[^가-힣ㄱ-ㅎㅏ-ㅣa-zA-Z0-9\s]", "", BeautifulSoup(text, "html.parser").get_text()).strip()

def tokenize(text):
    tokens = okt.pos(text, stem=True)
    allowed_pos = ['Noun', 'Verb', 'Adjective']
    return " ".join([word for word, tag in tokens if tag in allowed_pos and word not in stopwords])

def preprocess(text: str) -> str:
    cleaned = clean_text(text)
    print(f"[DEBUG] Cleaned: {cleaned}")
    tokenized = tokenize(cleaned)
    print(f"[DEBUG] Tokenized: {tokenized}")
    return tokenized

# KoBERT 모델 정의
class CustomBERTClassifier(BertPreTrainedModel):
    def __init__(self, config):
        super().__init__(config)
        self.bert = BertModel(config)
        self.dropout = nn.Dropout(0.3)
        self.classifier = nn.Linear(config.hidden_size, config.num_labels)
        self.init_weights()

    def forward(self, input_ids, attention_mask=None, token_type_ids=None):
        outputs = self.bert(input_ids=input_ids,
                            attention_mask=attention_mask,
                            token_type_ids=token_type_ids)
        pooled_output = outputs.pooler_output
        pooled_output = self.dropout(pooled_output)
        return self.classifier(pooled_output)

# 모델 및 토크나이저 로드
MODEL_PATH = "/Users/mihye/analysis_spring/data/python/models/best_kobert_model_2.pt"
print("[INFO] 모델 및 토크나이저 로딩 중...")

tokenizer = AutoTokenizer.from_pretrained("monologg/kobert", trust_remote_code=True)
config = BertConfig.from_pretrained("monologg/kobert", num_labels=2)
model = CustomBERTClassifier.from_pretrained("monologg/kobert", config=config)
model.load_state_dict(torch.load(MODEL_PATH, map_location="cpu"))
model.eval()

print("[INFO] 모델 로드 완료")

# 감정 분류 함수
def predict_sentiment(sentences):
    results = []
    for sentence in sentences:
        # print(f"[INFO] 문장 입력: {sentence}")
        preprocessed = preprocess(sentence)
        inputs = tokenizer(preprocessed,
                           return_tensors="pt",
                           truncation=True,
                           padding="max_length",
                           max_length=64)

        with torch.no_grad():
            outputs = model(
                input_ids=inputs["input_ids"],
                attention_mask=inputs["attention_mask"],
                token_type_ids=inputs["token_type_ids"]
            )
            pred = torch.argmax(outputs, dim=1).item()
            label = "positive" if pred == 1 else "negative"
            # print(f"[RESULT] 예측 결과: {label}")
            results.append(label)
    return results

# 실행 예시
if __name__ == "__main__":
    example_sentences = [
        "정말 감동적이에요. 눈물이 났어요.",
        "이게 뭐야 진짜 별로야",
        "좋아요 너무 재밌어요",
        "짜증나서 끄고 나왔어요"
    ]

    print("[INFO] 예측 시작")
    results = predict_sentiment(example_sentences)

    print("[INFO] 최종 결과 출력")
    for text, label in zip(example_sentences, results):
        print(f"{text} → {label}")