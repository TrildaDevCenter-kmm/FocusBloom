package com.joelkanyi.focusbloom.presentation.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ParentDataModifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import com.joelkanyi.focusbloom.domain.model.Task
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

@Composable
fun Dp.dpToPx() = with(LocalDensity.current) { this@dpToPx.toPx() }

@Composable
fun Int.pxToDp() = with(LocalDensity.current) { this@pxToDp.toDp() }

fun differenceBetweenMinutes(
    minTime: LocalTime,
    maxTime: LocalTime,
): Int {
    return (maxTime.hour - minTime.hour) * 60
}

fun differenceBetweenDays(
    minDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
    maxDate: LocalDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
): Int {
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time
    return (maxDate.dayOfMonth - minDate.dayOfMonth)
}

fun differenceBetweenWeeks(
    minDate: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    maxDate: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
): Int {
    return (maxDate.dayOfMonth - minDate.dayOfMonth)
}

fun LocalDate.plusDays(
    days: Int,
): LocalDate {
    return this.plus(days, DateTimeUnit.DAY)
}

fun LocalTime.plusHours(
    hours: Int,
): LocalTime {
    val addedHours = this.hour + hours
    return LocalTime(addedHours, this.minute)
}

fun LocalDateTime.plusWeeks(
    weeks: Int,
): LocalDateTime {
    return this.date.plusDays(weeks * 7).atStartOfDayIn(
        TimeZone.currentSystemDefault()
    ).toLocalDateTime(
        TimeZone.currentSystemDefault()
    )
}

fun LocalDateTime.minusDays(
    days: Int,
): LocalDateTime {
    return this.date.minus(1, DateTimeUnit.DAY).atStartOfDayIn(
        TimeZone.currentSystemDefault()
    ).toLocalDateTime(
        TimeZone.currentSystemDefault()
    )
}

fun LocalTime.truncatedTo(): LocalTime {
    return LocalTime(this.hour, this.minute)
}

fun LocalTime.MIN(): LocalTime {
    return LocalTime(0, 0)
}

fun LocalTime.MAX(): LocalTime {
    return LocalTime(23, 59, 59, 999999999)
}

inline class SplitType private constructor(val value: Int) {
    companion object {
        val None = SplitType(0)
        val Start = SplitType(1)
        val End = SplitType(2)
        val Both = SplitType(3)
    }
}

data class PositionedTask(
    val task: Task,
    val splitType: SplitType,
    val date: LocalDate,
    val start: LocalTime,
    val end: LocalTime,
    val col: Int = 0,
    val colSpan: Int = 1,
    val colTotal: Int = 1,
)

// val TaskTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a")
// val DayFormatter = DateTimeFormatter.ofPattern("EE, MMM d")

sealed class ScheduleSize {
    class FixedSize(val size: Dp) : ScheduleSize()
    class FixedCount(val count: Float) : ScheduleSize() {
        constructor(count: Int) : this(count.toFloat())
    }

    class Adaptive(val minSize: Dp) : ScheduleSize()
}

class TaskDataModifier(
    val positionedTask: PositionedTask,
) : ParentDataModifier {
    override fun Density.modifyParentData(parentData: Any?) = positionedTask
}

fun Modifier.taskData(positionedTask: PositionedTask) =
    this.then(TaskDataModifier(positionedTask))

fun splitTasks(tasks: List<Task>): List<PositionedTask> {
    return tasks
        .map { task ->
            val startDate = task.start.date
            val endDate = task.end.date
            if (startDate == endDate) {
                listOf(
                    PositionedTask(
                        task,
                        SplitType.None,
                        task.start.date,
                        task.start.time,
                        task.end.time,
                    ),
                )
            } else {
                val days = differenceBetweenDays(startDate, endDate)
                val splitTasks = mutableListOf<PositionedTask>()
                for (i in 0..days) {
                    val date = startDate.plusDays(i)
                    splitTasks += PositionedTask(
                        task,
                        splitType = if (date == startDate) SplitType.End else if (date == endDate) SplitType.Start else SplitType.Both,
                        date = date,
                        start = if (date == startDate) {
                            task.start.time
                        } else {
                            Clock.System.now()
                                .toLocalDateTime(TimeZone.currentSystemDefault()).time.MIN()
                        },
                        end = if (date == endDate) {
                            task.end.time
                        } else {
                            Clock.System.now()
                                .toLocalDateTime(TimeZone.currentSystemDefault()).time.MAX()
                        },
                    )
                }
                splitTasks
            }
        }
        .flatten()
}

fun PositionedTask.overlapsWith(other: PositionedTask): Boolean {
    return date == other.date && start < other.end && end > other.start
}

fun List<PositionedTask>.timesOverlapWith(task: PositionedTask): Boolean {
    return any { it.overlapsWith(task) }
}

fun arrangeTasks(tasks: List<PositionedTask>): List<PositionedTask> {
    val positionedTasks = mutableListOf<PositionedTask>()
    val groupTasks: MutableList<MutableList<PositionedTask>> = mutableListOf()

    fun resetGroup() {
        groupTasks.forEachIndexed { colIndex, col ->
            col.forEach { e ->
                positionedTasks.add(e.copy(col = colIndex, colTotal = groupTasks.size))
            }
        }
        groupTasks.clear()
    }

    tasks.forEach { task ->
        var firstFreeCol = -1
        var numFreeCol = 0
        for (i in 0 until groupTasks.size) {
            val col = groupTasks[i]
            if (col.timesOverlapWith(task)) {
                if (firstFreeCol < 0) continue else break
            }
            if (firstFreeCol < 0) firstFreeCol = i
            numFreeCol++
        }

        when {
            // Overlaps with all, add a new column
            firstFreeCol < 0 -> {
                groupTasks += mutableListOf(task)
                // Expand anything that spans into the previous column and doesn't overlap with this task
                for (ci in 0 until groupTasks.size - 1) {
                    val col = groupTasks[ci]
                    col.forEachIndexed { ei, e ->
                        if (ci + e.colSpan == groupTasks.size - 1 && !e.overlapsWith(task)) {
                            col[ei] = e.copy(colSpan = e.colSpan + 1)
                        }
                    }
                }
            }
            // No overlap with any, start a new group
            numFreeCol == groupTasks.size -> {
                resetGroup()
                groupTasks += mutableListOf(task)
            }
            // At least one column free, add to first free column and expand to as many as possible
            else -> {
                groupTasks[firstFreeCol] += task.copy(colSpan = numFreeCol)
            }
        }
    }
    resetGroup()
    return positionedTasks
}
