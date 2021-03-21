# Todo-App

JavaFX app for managing TO-DOs. Supports both Windows and Linux platforms. Download it in RELEASE.

### Pre-requisite

- JDK-11

## Features

- **Add**, **Delete**, **Update**, **Copy**, **Append (into current list)**, **Load (read-only)**, **Backup**, and many more features that you will normally expect.
- **Configuration via UI or external configuration file**
  - `$HOME/todo-app/settings.json`
- **Persistence** on disk using **SQLite**.

Note: These external configuration files are created at home.user directory under the folder named `/todo-app`. If you are using Linux, it will be under `/home/somebody/` directory. If you are using Windows, it will be at your `...\users\yourName\` directory.
