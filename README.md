# Todo-App

JavaFX app for managing TO-DOs. Supports both Windows and Linux platforms. Download it in RELEASE.

## Pre-requisite

- JDK-11

## Configuration
- **Configuration via UI or external configuration file**
    - `$HOME/todo-app/settings.json`
- **Persistence** using **SQLite**.
    - `$HOME/todo-app/todoapp.db`

Note: These external configuration files are created at home.user directory under the folder named `/todo-app`. If you are using Linux, it will be under `/home/somebody/` directory. If you are using Windows, it will be at your `...\users\yourName\` directory.

### Example of settings.json

`"language"` only supports `"chn"` (for Chinese) or `"eng"` (for English, which is the default language).

```
E.g.,

{
    "savePath": "/home/zhuangyongj/save.json",
    "language": "eng",
    "strikethroughEffectEnabled":false
}
```

### Example of Exported File In A Human-Readable Form 

```
[IN PROGRESS] Expected: 14/07/2021 - Actual: __/__/____ 'Eat a banana'
[IN PROGRESS] Expected: 14/07/2021 - Actual: __/__/____ 'Eat an apple'
[FINISHED]    Expected: 14/07/2021 - Actual: 14/07/2021 'Do something important'
```
