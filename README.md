# Todo-App

App for managing TO-DOs. Supports both Windows and Linux platforms. Download it in RELEASE.

### Pre-requisite

- JDK-11

## Features

- **Add**, **Delete**, **Update**, **Copy**, **Append (into current list)**, **Load (read-only)**, **Backup**, and many more features that you will normally expect.
- **Configuration via UI or external configuration file**
    - `$HOME/todo-app/settings.json`
- **Persistence** on disk.
    - `$HOME/todo-app/save.json`

Note: These external configuration files are created at home.user directory under the folder named `/todo-app`. If you are using Linux, it will be under `/home/somebody/` directory. If you are using Windows, it will be at your `...\users\yourName\` directory.

### Example of settings.json

`"language"` only supports `"chn"` (for Chinese) or `"eng"` (for English, which is the default language).

```
{
    "savePath": "/home/zhuangyongj/save.json",
    "language": "eng"
}
```

### Example of save.json

```
[
    {
        "name": "Do something important",
        "done": false,
        "startDate": 1602404024165,
    },
    {
        "name": "Eat an apple",
        "done": false,
        "startDate": 1602403968498,
    },
    {
        "name": "Eat a banana",
        "done": false,
        "startDate": 1602403963418,
    }
]
```

### Example of Exported File In A Human-Readable Form 

```
[IN PROGRESS] 31/10/2020 'Eat a banana'
[IN PROGRESS] 31/10/2020 'Eat an apple'
[IN PROGRESS] 31/10/2020 'Do something important'
```
