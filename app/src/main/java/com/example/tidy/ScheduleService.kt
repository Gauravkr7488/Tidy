package com.example.tidy

import android.content.Context
import com.example.tidy.constants.RepeatTypes
import com.example.tidy.constants.TaskActions
import com.example.tidy.constants.WeekDays
import com.tidy.sqldelight.Task
import java.util.Calendar

class ScheduleService(private val context: Context) {

    fun scheduleTask(task: Task): Boolean {
        val scheduleDate: Long? = if (task.repeatType != RepeatTypes.NONE) {
            val t = getScheduleDate(
                frequencyNumber = task.frequencyNumber?.toInt() ?: 1,
                repeatType = task.repeatType,
                repeatDays = task.repeatDays.split(",")
            )
            var k = ""
            if (t != null) k = Utils.changeDateFormat(t, "dd-MM-yy hh mm a")
            println("schedule = $k")
            if (task.repeatType == RepeatTypes.MINUTE || task.repeatType == RepeatTypes.HOUR) t else
                Utils.combineDateAndTimeMillis(t, task.dueDateAndTime)
        } else {
            task.dueDateAndTime
        }
        if (scheduleDate == null) return true
        if (task.endDate == null || task.endDate > scheduleDate) {
            Utils.scheduleAlarm(
                context = context,
                taskId = task.id,
                scheduleTime = scheduleDate,
                action = TaskActions.UNARCHIVE
            )
        }
        return false
    }


    private fun getScheduleDate(
        frequencyNumber: Int,
        repeatType: String,
        repeatDays: List<String>,
    ): Long? {
        val c = Calendar.getInstance()
        c.set(Calendar.SECOND, 0)
        val scheduleTime = when (repeatType) {
            RepeatTypes.MINUTE -> {
                c.add(Calendar.MINUTE, frequencyNumber)
                c.timeInMillis
            }

            RepeatTypes.HOUR -> {
                c.set(Calendar.MINUTE, 0)
                c.add(Calendar.HOUR_OF_DAY, frequencyNumber)
                c.timeInMillis
            }

            RepeatTypes.DAY -> {
                c.set(Calendar.MINUTE, 0)
                c.set(Calendar.HOUR_OF_DAY, 0)
                c.add(Calendar.DAY_OF_YEAR, frequencyNumber)
                c.timeInMillis
            }

            RepeatTypes.WEEK -> {
                c.set(Calendar.MINUTE, 0)
                c.set(Calendar.HOUR_OF_DAY, 0)
                val today = c.get(Calendar.DAY_OF_WEEK)
                val listScheduleDays: List<Int> = repeatDays.mapNotNull {
                    getWeekDayNum(it)
                }
                if (listScheduleDays.any { it > today }) {
                    c.set(Calendar.DAY_OF_WEEK, listScheduleDays.first { it > today })
                } else {
                    c.set(Calendar.DAY_OF_WEEK, listScheduleDays.first())
                    c.add(Calendar.WEEK_OF_YEAR, frequencyNumber)
                }
                c.timeInMillis
            }

            RepeatTypes.MONTH -> {
                c.set(Calendar.MINUTE, 0)
                c.set(Calendar.HOUR_OF_DAY, 0)
                val today = c.get(Calendar.DAY_OF_MONTH)
                val listScheduleDays = repeatDays.map { it.toInt() }
                if (listScheduleDays.any { it > today }) {
                    c.set(Calendar.DAY_OF_MONTH, listScheduleDays.first { it > today })
                } else {
                    c.set(Calendar.DAY_OF_MONTH, listScheduleDays.first())
                    c.add(Calendar.MONTH, frequencyNumber)
                }
                c.timeInMillis
            }

            RepeatTypes.YEAR -> {
                c.set(Calendar.MINUTE, 0)
                c.set(Calendar.HOUR_OF_DAY, 0)
                val today = c.timeInMillis
                val listScheduleDays = repeatDays.map { it.toLong() }
                if (listScheduleDays.any { it > today }) {
                    val next = listScheduleDays.first { it > today }
                    c.timeInMillis = next
                } else {
                    c.add(Calendar.YEAR, frequencyNumber)
                }
                c.timeInMillis
            }

            else -> null
        }
        return scheduleTime
    }

    private fun getWeekDayNum(string: String): Int? = when (string) {
        WeekDays.SUN -> Calendar.SUNDAY
        WeekDays.MON -> Calendar.MONDAY
        WeekDays.TUE -> Calendar.TUESDAY
        WeekDays.WED -> Calendar.WEDNESDAY
        WeekDays.THU -> Calendar.THURSDAY
        WeekDays.FRI -> Calendar.FRIDAY
        WeekDays.SAT -> Calendar.SATURDAY
        else -> null
    }
}