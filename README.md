# Todo-App

JavaFX app for managing TO-DOs. Supports both Windows and Linux platforms. Download it in RELEASE.

## Pre-requisite

- JDK-11
    - (For Oracle JDK 1.8, comment the openjfx dependencies specified in pom.xml, and build it)

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
    ...
}
```
## Demo

![Screenshot of the app with language set to chinese](demo/demo-cn.png)
![Screenshot of the app with language set to english](demo/demo-en.png)
