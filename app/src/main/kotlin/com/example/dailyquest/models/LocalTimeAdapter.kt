import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class LocalTimeAdapter : TypeAdapter<LocalTime>() {
    @RequiresApi(Build.VERSION_CODES.O)
    private val formatter = DateTimeFormatter.ISO_LOCAL_TIME

    @RequiresApi(Build.VERSION_CODES.O)
    override fun write(out: JsonWriter, value: LocalTime?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value.format(formatter))
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun read(`in`: JsonReader): LocalTime? {
        return if (`in`.peek() == com.google.gson.stream.JsonToken.NULL) {
            `in`.nextNull()
            null
        } else {
            LocalTime.parse(`in`.nextString(), formatter)
        }
    }
}
