import urllib.request
import pandas as pd
import numpy as np
import pickle
import os
from konlpy.tag import Mecab
from sklearn.model_selection import train_test_split
from tensorflow.keras.preprocessing.text import Tokenizer
from tensorflow.keras.preprocessing.sequence import pad_sequences
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Embedding, Dropout, Conv1D, MaxPooling1D, LSTM, Dense, Activation
from tensorflow.keras.callbacks import EarlyStopping, ModelCheckpoint

# ✅ mecabrc 환경변수 설정
os.environ["MECABRC"] = "/opt/homebrew/etc/mecabrc"

# 📁 경로 설정
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
MODEL_DIR = os.path.join(BASE_DIR, "../models")
os.makedirs(MODEL_DIR, exist_ok=True)

# 1. 데이터 다운로드
url = "https://raw.githubusercontent.com/bab2min/corpus/master/sentiment/naver_shopping.txt"
urllib.request.urlretrieve(url, filename="ratings_total.txt")
data = pd.read_table("ratings_total.txt", names=["ratings", "reviews"])

# 2. 레이블 생성
data['label'] = np.select([data.ratings > 3], [1], default=0)
data.drop_duplicates(subset=['reviews'], inplace=True)
data['reviews'] = data['reviews'].replace('', np.nan)  # FutureWarning 해결
data.dropna(how='any', inplace=True)

# 3. 학습/검증 데이터 분할
train_data, test_data = train_test_split(data, test_size=0.25, random_state=42)

# 4. 형태소 분석 + 불용어 제거
try:
    possible_paths = [
        '/opt/homebrew/lib/mecab/dic/mecab-ko-dic',
        '/usr/local/lib/mecab/dic/mecab-ko-dic',
        '/usr/local/Cellar/mecab-ko-dic/2.1.1-20180720/lib/mecab/dic/mecab-ko-dic',
        '/opt/homebrew/Cellar/mecab-ko-dic/2.1.1-20180720/lib/mecab/dic/mecab-ko-dic'
    ]

    for path in possible_paths:
        if os.path.exists(path):
            mecab = Mecab(dicpath=path)
            print(f"사용 중인 MeCab 사전 경로: {path}")
            break
    else:
        try:
            mecab = Mecab()
            print("기본 MeCab 설정 사용")
        except:
            import subprocess
            result = subprocess.run(['find', '/', '-name', 'mecab-ko-dic', '-type', 'd'],
                                    stdout=subprocess.PIPE, stderr=subprocess.DEVNULL, text=True)
            paths = result.stdout.strip().split('\n')
            if paths and paths[0]:
                mecab = Mecab(dicpath=paths[0])
                print(f"찾은 MeCab 사전 경로: {paths[0]}")
            else:
                raise RuntimeError("MeCab-ko-dic를 찾을 수 없습니다. 직접 설치해주세요.")
except Exception as e:
    print(f"MeCab 초기화 오류: {e}")
    print("\n아래 명령어로 MeCab과 한국어 사전을 설치해보세요:")
    print("brew install mecab mecab-ko-dic")
    print("pip install mecab-python3")
    raise

stopwords = ['도', '는', '다', '의', '가', '이', '은', '한', '에', '하', '고', '을', '를', '인', '듯', '과', '와', '네', '들', '듯', '지', '임', '게']

def tokenize(text):
    return [t for t in mecab.morphs(text) if t not in stopwords]

train_data['tokenized'] = train_data['reviews'].apply(tokenize)
test_data['tokenized'] = test_data['reviews'].apply(tokenize)

# 5. 토크나이저 학습 및 저장
X_train = train_data['tokenized'].values
X_test = test_data['tokenized'].values
y_train = train_data['label'].values
y_test = test_data['label'].values

tokenizer = Tokenizer()
tokenizer.fit_on_texts(X_train)

# 희귀 단어 제외
total_cnt = len(tokenizer.word_index)
rare_cnt = 0
threshold = 3
for key, value in tokenizer.word_counts.items():
    if value < threshold:
        rare_cnt += 1
vocab_size = total_cnt - rare_cnt + 2

# 토크나이저 다시 생성 + 저장
tokenizer = Tokenizer(vocab_size, oov_token="OOV")
tokenizer.fit_on_texts(X_train)
with open(os.path.join(MODEL_DIR, "tokenizer.pickle"), "wb") as f:
    pickle.dump(tokenizer, f)

# 정수 인코딩 & 패딩
X_train = tokenizer.texts_to_sequences(X_train)
X_test = tokenizer.texts_to_sequences(X_test)

max_len = 80
X_train = pad_sequences(X_train, maxlen=max_len)
X_test = pad_sequences(X_test, maxlen=max_len)

# 6. 모델 구성
model = Sequential()
model.add(Embedding(vocab_size, 100))
model.add(Dropout(0.5))
model.add(Conv1D(64, 5, activation='relu'))
model.add(MaxPooling1D(pool_size=4))
model.add(LSTM(250, return_sequences=True))
model.add(LSTM(250, return_sequences=True))
model.add(LSTM(250))
model.add(Dense(1))
model.add(Activation('sigmoid'))

model.compile(optimizer='adam', loss='binary_crossentropy', metrics=['accuracy'])

es = EarlyStopping(monitor='val_loss', mode='min', patience=4)
mc = ModelCheckpoint(os.path.join(MODEL_DIR, 'best_model.h5'), monitor='val_accuracy', mode='max', save_best_only=True)

model.fit(X_train, y_train, epochs=10, batch_size=128, validation_data=(X_test, y_test), callbacks=[es, mc])