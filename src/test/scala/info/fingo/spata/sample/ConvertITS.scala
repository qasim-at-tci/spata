package info.fingo.spata.sample

import java.time.LocalDate

import cats.effect.IO
import fs2.Stream
import org.scalatest.funsuite.AnyFunSuite
import info.fingo.spata.CSVReader

/* Samples which convert CSV records to case classes. */
class ConvertITS extends AnyFunSuite {

  test("spata allows manipulate data and convert it to case classes using stream functionality") {
    // class to convert data to - class fields have to match CSV header fields
    case class DayTemp(date: LocalDate, minTemp: Double, maxTemp: Double)
    val mh = Map("terrestrial_date" -> "date", "min_temp" -> "minTemp", "max_temp" -> "maxTemp")
    val reader = CSVReader.config.mapHeader(mh).get // reader with default configuration
    val stream = Stream
      .bracket(IO { SampleTH.sourceFromResource(SampleTH.dataFile) })(source => IO { source.close() }) // ensure resource cleanup
      .through(reader.pipe) // get stream of CSV records
      .map(_.to[DayTemp]()) // convert records to DayTemps
      .rethrow // get data out of Either and let stream fail on error
      .filter(_.date.getYear == 2016) // filter data for specific year
      .handleErrorWith(ex => fail(ex.getMessage)) // fail test on any stream error
    val result = stream.compile.toList.unsafeRunSync()
    assert(result.length > 300 && result.length < 400)
    assert(result.forall(_.date.getYear == 2016))
  }
}