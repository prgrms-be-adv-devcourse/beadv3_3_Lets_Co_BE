# GIT 명령어

# 🌲 Git Branch

---

## 🍀 설명

- 코드를 복사하거나 원래 코드와 상관없이 독립적인 개발이 필요할 때 사용하게 된다.
- BRANCH 명령어를 사용 한 뒤, 무엇을 기준으로 바뀌었는지 한 눈에 알 수 있다.
- BRANCH 만든 뒤, 원래대로 복원이 용이하다.

## 🗒️ Type

| Type | :Comment: |
| --- | --- |
| Feat | 새로운 기능 추가 |
| Update | 기존기능 수정 |
| Fix | 버그 수정 |

```bash
git branch Type/Subject
git branch Type/Page/Subject

#예시
git branch Feat/InsertMember
git branch Update/InsertMember
```

- 첫 글자 대문자
- 영문 작성
- 명확한 명사 혹은 동사 사용

### master branch는 사용하지 말 것.(master는 코드 복구용 기준이 된다.)

### dev용 branch, production용 branch를 별도로 생성 후 운영을 하면 용이하다.

## 💬 명령어

| Command | function |
| --- | --- |
| git branch | local의 branch 정보를 보여준다. |
| git branch -v | 로컬 branch의 정보를 마지막 commit 내역과 함께 보여준다. |
| git branch -r | 리모트 저장소의 branch 정보를 보여준다. |
| git branch -a | 로컬/리모트 저장소의 모든 branch 정보를 보여준다. |
| git branch [이름] | [이름]의 branch를 생성한다. |
| git branch —merged | GIT에서 MERGE가 완료된 BRANCH만 보여준다. |
| git branch —no-merged | GIT에서 MERGE가 되지 않은 BRANCH만 보여준다. |
| git branch -d | branch를 삭제한다. merge하지 않은 commit을 담고 있을 경우 삭제되지 않음. |
| git branch -D | branch의 삭제 경고를 무시하고, 무조건 삭제 |
| git branch -m [이름1] [이름2] | [이름1]의 branch를 [이름2]로 변경한다. (기존 동일 이름이 있을 경우 변경 불가) |
| git branch -M [이름1] [이름2] | 무조건 [이름1]의 branch를 [이름2]로 변경한다. (기존 동일 이름 있을 경우 덮어 씌운다.) |
| git branch -m [이름1] | 현재의 branch를 [이름1]로 변경한다. |

# 🏅 Git Add

---

## 🍀 설명

- 추가, 변경, 삭제 등 기존 파일에서 소스가 변경 된 모든 것들의 적용여부이다.
- 소스 수정이 되었을 경우 add명령어로 수정이 된 목록들을 추가한다.
- ADD에 대상이 되지 않은 파일은 GIT SERVER에 저장 할 수 없다.

## 🗣️ 사용 방법

```html
git add [파일 경로/파일 명]//추가 하고자 하는 파일을 한 개만 대상이 될 경우 사용 된다.
git add [파일 경로]//해당 파일경로 밑의 모든 것을 추가할 때 사용된다.
git add .//구분 없이, 수정 된 모든 것이 대상이 될 때 사용된다.
```

### 💣 git add * 사용하지 말 것

- *는 전체를 의미하고, .은 현재의 경로를 의미한다.
- * 할 경우, 구분 없이 모두 넣을 수 있기 때문에 가급적 지양한다.

# 💾 Commit Message

---

## 🍀 설명

- Git의 Stagin Area의 내용을 Repository로 옮기는 명령어
- git을 저장할 때, 해당 구간에 대한 설명을 comment할 수 있다.

| Type | Comment |
| --- | --- |
| Fix | 수정 |
| Feat | 새로운 기능 추가 |
| Remove | 삭제 |
| Update | 수정 |
| Move | 코드파일 이동 |
| Rename | 이름 변경 |
| Comment | 주석 추가 및 변경 |
| Degin | CSS 변경 |
| Style | 코드 포멧팅, 누락 수정 |
- Comment를 이용해서, 해당 작업이 어떤 것인지를 알릴 수 있다.

## 🔔 사용 방법

---

```bash
#사용방법 1
git commit -m "Update: 수정내용"

#사용방법 2
git commit -m "Feat: 추가 내용
  
- 추가내용 간략하게"
```

- 첫 번째 줄은 commit의 Title이다.
- 두번째 줄부터는 comment내용이다.
- 마크다운 기능을 제공하고 있다. 개행은 ‘스페이스 2번’
- 문장 금지, 설명 금지
- 제목과 소제목 느낌으로 작성하기

# 📌 Git Push

---

- local에 있는 git repository의 내용을 remote의 git repository로 이동하는 명령어다.
- Local에서 Git의 Remote로 이동함으로써, 타인 혹은 다른 PC에서 저장된 Git의 내용을 불러올 수 있다.

```bash
#git의 branch를 처음 생성해서, remote에 해당 branch가 없을 경우
git push --set-upstream origin master

#git branch가 local과 remote가 연결 되었을 경우
git push

#git push 대상이 local Branch와 Remote Branch가 서로 다를 때
git push origin <branch1>:<branch2>
```

- git을 push하기 위해서는 local과 remote에 동일한 Branch가 있어야 한다.
- Branch를 새로 만들 경우, Remote에 해당되는 Branch가 없으므로, Remote에도 동일한 명칭의 Branch를 만들어야 한다.
- 동일한 Branch가 존재할 경우, 단순하게 push만 해도 된다.
- git은 master를 branch로 부르는 것은 금기사항 이지만, push후 master에게 merge(소스코드 합치기)를 해야하는 상황에 있다. 그래서 local branch와 remote branch가 서로 다를 수 있는데, 이를 한 번에 해결 할 수도 있다. 다만, 상황에 따라서 이 또한 금지사항이 될 수 있다. pipeLine을 거치지 않고 바로 merge가 되었을 경우 master의 소스에 문제가 생길 우려가 있기 때문이다.

![Untitled](Untitled.png)

# 💗 Git pull

---

```bash
#local Branch와 Remote Branch가 동일할 경우 사용하는 명령어
#master branch를 불러올 때, 대부분 이용하는 명령어다.
git pull

#local Branch와 Remote Branch명이 다를 때 사용하는 명령어
#master의 내용을 merge하거나 다른 사람이 만든 branch를 조회할 때 이용한다.
git pull origin <branch명>
```

- git pull은 master처럼 branch를 수시적으로 pull 받을 Case에 자주 사용 된다.
- git pull origin <branch명>은 다른 팀원의 branch를 불러올 때 자주 사용 된다.

# ☠️ Git Ignore

---

### working directory에서의 git 최신화를 예외처리를 한다.

- 수정이 되서는 안 되는 Server설정 파일이 있는 경우
- 각 Branch마다 설정 파일(Value)이 다를 경우.
- IDE의 특유의 파일을 Path하는 경우
- 소스 수정을 공유하면 안 되는 경우

### 🍁 Git Ignore는 local에서만 적용이 된다. 따라서, git Ignore가 수정이 될 경우 같이 협업 하는 사람들에게 pull할 것을 권고해야 한다.

### 🎵 사용 방법은 git status 명령어 입력했을 때 나오는 경로 그대로를 git ignore에 한 줄씩 입력하면 된다.

- 디렉토리 경로 혹은 단일 파일로도 지정이 가능하다.
