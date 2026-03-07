package com.example.tidy.viewModels

class AddTaskViewModel {
    private var flag: String = ""
    private var taskId: Long? = null

    fun getFlag(): String {
        val f = flag
        flag = ""
        return f
    }

    fun setId(id: Long){
        taskId = id
    }

    fun getId(): Long? {
        val id = taskId
        taskId = null
        return id
    }

    fun setChildFlag(){
        flag = "child"
    }

    fun setParentFlag(){
        flag = "parent"
    }

}