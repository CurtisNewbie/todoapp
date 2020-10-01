# Todo-app

Download it in RELEASE

1. Supports external configuration
   - `"settings.conf"`: a configuration file that allows you to configure where the todo-list should be saved. It's generated on application startup if not found.
2. Supports persistence on disk (through reading/writing json file)
   - `"save.json"`: where the todo list is actually saved. It's read and written on application startup and shutdonw.
3. Supports todo-list ordering, it's currently based on whether the todo is "done" and when the todo is created.

These configuration files are created at home.user directory. If you are using Linux, it will be at `/home/somebody/` directory. If you are using Windows, it will be at your `users/` directory.
