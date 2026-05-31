### Task :

1. id
    - Type: Integer
    - Nullable: false
    - Description: Unique identifier of a task
2. title
    - Type: String
    - Nullable: false
    - Description: Display name of a task
3. done
    - Type: Boolean
    - Nullable: false
    - Description: Indicates whether a Task has been completed
4. repeatType
    - Type: String
    - Nullable: true
    - Description: Defines the recurrence behavior of a Task
5. description
    - Type: String
    - Nullable: true
    - Description: Additional information associated with a Task
6. hide
    - Type: Boolean
    - Nullable: false
    - Description: Indicates whether a Task should be visible in Home Screen
7. createdAt
    - Type: Integer
    - Nullable: false
    - Description: Contains the time of creation of the task
8. parentId
    - Type: Integer
    - Nullable: true
    - Description: Identifier of the parent Task