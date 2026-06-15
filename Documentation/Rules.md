### Rules

1. When `Clear` is called, a completed task is deleted unless it has an incomplete parent or is a repeating task.
2. A parent task is only `done` when all its children are `done`.
3. When `Clear` is called on a child task whose parent is not `done`, the child task is unaffected.
4. When `Clear` is called on a `done` repeating task, `hide` is set to `true` and `done` is set to `false`.
5. When `Skip` is called on a task it is `Archived` by setting its `hide` to `true`.
6. If an `Archived` Task is repetitive then it will get `Unarchived` on the next instance of the repeat.
7. If an `Archived` Task is non-repetitive then it will `Unarchived` on the next day.