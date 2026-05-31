### Rules

1. When `Clear` is called, a completed task is deleted unless it has an incomplete parent or is a repeating task.
2. A parent task is only `done` when all its children are `done`.
3. When `Clear` is called on a child task whose parent is not `done`, the child task is unaffected.
4. When `Clear` is called on a repeating task, `hide` is set to `true` and `done` is set to `false`.