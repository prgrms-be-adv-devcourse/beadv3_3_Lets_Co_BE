# GIT Commands

# 🌲 Git Branch

---

## 🍀 Description

- Used when you need to copy code or develop independently regardless of the original code.
- After using the BRANCH command, you can see at a glance what has changed based on the standard.
- It is easy to restore the original state after creating a BRANCH.

## 🗒️ Type

| Type   | :Comment:                |
| ------ | ------------------------ |
| Feat   | Add new features         |
| Update | Modify existing features |
| Fix    | Fix bugs                 |

```bash
git branch Type/Subject
git branch Type/Page/Subject

# Example
git branch Feat/InsertMember
git branch Update/InsertMember
```

- First letter capitalized.
- Write in English.
- Use clear nouns or verbs.

### Do not use the master branch. (Master serves as the standard for code recovery.)

### It is easier to operate by creating separate branches for dev and production.

## 💬 Commands

| Command                       | function                                                                         |
| ----------------------------- | -------------------------------------------------------------------------------- |
| git branch                    | Shows local branch information.                                                  |
| git branch -v                 | Shows local branch information along with the last commit history.               |
| git branch -r                 | Shows remote repository branch information.                                      |
| git branch -a                 | Shows all branch information (local/remote).                                     |
| git branch [Name]             | Creates a branch with [Name].                                                    |
| git branch —merged            | Shows only branches that have been MERGED in GIT.                                |
| git branch —no-merged         | Shows only branches that have NOT been MERGED in GIT.                            |
| git branch -d                 | Deletes a branch. Will not delete if it contains unmerged commits.               |
| git branch -D                 | Ignores delete warnings and force deletes the branch.                            |
| git branch -m [Name1] [Name2] | Renames branch [Name1] to [Name2]. (Cannot change if the name already exists)    |
| git branch -M [Name1] [Name2] | Force renames branch [Name1] to [Name2]. (Overwrites if the name already exists) |
| git branch -m [Name1]         | Renames the current branch to [Name1].                                           |

# 🏅 Git Add

---

## 🍀 Description

- Applies the status of all things where the source has been changed, including additions, modifications, and deletions.
- When the source is modified, use the add command to stage the modified list.
- Files not targeted by ADD cannot be saved to the GIT SERVER.

## 🗣️ Usage

```html
git add [File Path/File Name] // Used when targeting only one specific file to add.
git add [File Path] // Used to add everything under that file path.
git add . // Used when targeting everything modified, without distinction.
```

### 💣 Do not use git add *

- \* means everything (all files), and . means the current path.
- Since - includes everything without distinction (including ignored files sometimes), it should be avoided if possible.

# 💾 Commit Message

---

## 🍀 Description

- Command to move contents from Git's Staging Area to the Repository.
- When saving git, you can add comments explaining that specific section.

| Type    | Comment                              |
| ------- | ------------------------------------ |
| Fix     | Fixes/Corrections                    |
| Feat    | Add new feature                      |
| Remove  | Deletion                             |
| Update  | Modification                         |
| Move    | Move code files                      |
| Rename  | Change name                          |
| Comment | Add or change comments               |
| Degin   | CSS changes                          |
| Style   | Code formatting, missing semi-colons |

- You can use the Comment to announce what the work is about.

## 🔔 Usage

---

```bash
# Usage 1
git commit -m "Update: Modification details"

# Usage 2
git commit -m "Feat: Add details

- Brief description of
```

- The first line is the Title of the commit.
- From the second line onwards, it is the comment content (body).
- Markdown features are supported. Line break is 'Space twice'.
- No full sentences, no long explanations.
- Write in the style of a Title and Subtitle.

# 📌 Git Push

---

- Command to move the contents of the local git repository to the remote git repository.
- By moving from Local to Git Remote, you can retrieve Git contents saved by others or on other PCs.

```bash
# When creating a git branch for the first time and the branch does not exist on remote
git push --set-upstream origin master

# When local and remote git branches are connected
git push

# When the git push target Local Branch and Remote Branch are different
git push origin <branch1>:<branch2>
```

- To push git, identical Branches must exist on both local and remote.
- When creating a new Branch, since there is no corresponding Branch on Remote, you must create a Branch with the same name on Remote.
- If the same Branch exists, you can simply push.
- It is generally forbidden to treat master as a regular branch, but there are situations where you need to merge (combine source code) into master after a push. Therefore, local and remote branches might differ, which can be solved at once. However, depending on the situation, this may also be forbidden. If merged directly without going through a pipeline, there is a risk of issues arising in the master source.

![Untitled](Untitled.png)

# 💗 Git pull

---

```bash
# Command used when Local Branch and Remote Branch are the same
# Most commonly used command when pulling the master branch.
git pull

# Command used when Local Branch and Remote Branch names are different
# Used when merging master content or viewing a branch created by someone else.
git pull origin <branch_name>
```

- git pull is frequently used in cases where branches need to be pulled regularly, like master.
- git pull origin <branch_name> is frequently used to fetch a teammate's branch.

# ☠️ Git Ignore

---

### Handles exceptions for git updates in the working directory.

- When there are Server configuration files that should not be modified.
- When configuration files (Values) are different for each Branch.
- When Pathing specific IDE files.
- When source modifications should not be shared.

### 🍁 Git Ignore applies only locally. Therefore, if Git Ignore is modified, you should recommend collaborators to pull it.

### 🎵 To use, simply enter the paths shown when typing git status into .gitignore line by line.

- Can specify directory paths or single files.
