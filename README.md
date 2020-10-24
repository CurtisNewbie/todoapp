# Todo-App

App for managing TO-DOs. Supports both Windows and Linux platforms. Download it in RELEASE.

### Pre-requisite

- JDK-11

## Features

1. **Adding** and **deleting** TODO.
2. **Redo** using `Ctrl+Z`, currently it only supports recovering the TODO that is deleted.
3. **Copying** a TODO's text.
4. **Sorting** TODO, that is currently based on whether the todo is "done" and when the todo is created.
5. **Backup**, which can be loaded later on.
6. **Exporting a human-readable form**, but it can't be loaded by the application.
7. **Hotkey for saving**, which is `Ctrl+S` that manually triggers an async saving.
8. Two languages (**English** and **Chinese**), which can be configured in menu (A popup menu shown when you do right-click).
9. **External configuration** (through reading/writing json file).

   - `"settings.json"`: a configuration file that allows you to configure where the todo-list should be saved, and which language to use (Currently, it only supports English and Chinese). It's generated on application startup if the file is not found or corrupted.

10. **Persistence** on disk (through reading/writing json file).

- `"save.json"`: where the todo list is actually saved. It's read and written on application startup and shutdown.

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
        "endDate": 1602404024165
    },
    {
        "name": "Eat an apple",
        "done": false,
        "startDate": 1602403968498,
        "endDate": 1602403968498
    },
    {
        "name": "Eat a banana",
        "done": false,
        "startDate": 1602403963418,
        "endDate": 1602403963418
    }
]
```
