package com.example.dailyquest.database

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val dueDate: String? = null,
    val priority: Int? = null,
    var isCompleted: Boolean = false,
    var completedDate: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readByte() != 0.toByte(),
        parcel.readString()
    ) {
    }

    override fun toString() : String{
        return "[DataContainer]: $name \n" +
                "  Description: $description.toString() \n" +
                "  DueDate: $dueDate \n" +
                "  Priority: $priority \n" +
                "  IsCompleted: $completedDate + \n" +
                "  CompletedDate: $completedDate \n"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(dueDate)
        parcel.writeValue(priority)
        parcel.writeByte(if (isCompleted) 1 else 0)
        parcel.writeString(completedDate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Task> {
        override fun createFromParcel(parcel: Parcel): Task {
            return Task(parcel)
        }

        override fun newArray(size: Int): Array<Task?> {
            return arrayOfNulls(size)
        }
    }
}
