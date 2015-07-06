import java.time.format.DateTimeFormatter
import java.util.Date
import javax.xml.bind.DatatypeConverter
import java.time.ZonedDateTime
val format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
val x = ZonedDateTime.parse("2015-07-05T17:12:25+0000",format)//"T06:49:17+0000")
x.toEpochSecond
new Date(x.toEpochSecond*1000)