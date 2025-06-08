import os

def print_structure(path, indent=0):
    for item in sorted(os.listdir(path)):
        full_path = os.path.join(path, item)
        if item.startswith('.'):
            continue  # 숨김파일 생략
        print('  ' * indent + '|-- ' + item)
        if os.path.isdir(full_path):
            print_structure(full_path, indent + 1)

if __name__ == "__main__":
    root_path = "."  # 현재 디렉토리 기준
    print_structure(root_path)