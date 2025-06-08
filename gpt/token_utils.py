import tiktoken

def count_tokens(text: str, model: str = "gpt-4o") -> int:
    if not isinstance(text, str):
        text = str(text)
    enc = tiktoken.encoding_for_model(model)
    return len(enc.encode(text))