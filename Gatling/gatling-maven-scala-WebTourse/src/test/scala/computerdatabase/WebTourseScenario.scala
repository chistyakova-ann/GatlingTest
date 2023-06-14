package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import ScenarioBuyTicket._

import scala.concurrent.duration.DurationInt

class WebTourseScenario extends Simulation {
  private val httpProtocol = http
    .baseUrl("http://localhost:8090")
    .inferHtmlResources()
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
    .acceptEncodingHeader("gzip, deflate, br")
    .acceptLanguageHeader("ru-RU,ru;q=0.9")
    .upgradeInsecureRequestsHeader("1")
    .userAgentHeader("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")


  // setUp(ScenarioDeleteTicket.scn.inject(rampUsers(1).during(10)),
//  setUp( ScenarioBuyTicket.scn1.inject(rampUsers(1).during(2.minutes))).protocols(httpProtocol)

setUp(
    ScenarioBuyTicket.scn1.inject(incrementConcurrentUsers((1).toInt).times(1).eachLevelLasting(30.minutes).separatedByRampsLasting(1.minutes).startingFrom(0),
    ),
    ScenarioDeleteTicket.scn2.inject(incrementConcurrentUsers((1).toInt).times(1).eachLevelLasting(30.minutes).separatedByRampsLasting(1.minutes).startingFrom(0)
    ),
    ScenarioChooseTicket.scn3.inject(incrementConcurrentUsers((1).toInt).times(1).eachLevelLasting(30.minutes).separatedByRampsLasting(1.minutes).startingFrom(0)
    ),
    ScenarioFillData.scn4.inject(incrementConcurrentUsers((1).toInt).times(1).eachLevelLasting(30.minutes).separatedByRampsLasting(1.minutes).startingFrom(0)
    ),
    ScenarioFormTicket.scn5.inject(incrementConcurrentUsers((4).toInt).times(1).eachLevelLasting(30.minutes).separatedByRampsLasting(1.minutes).startingFrom(0)
    ),
    ScenarioViewItinerary.scn6.inject(
      // Интенсивность на ступень
      incrementConcurrentUsers((2).toInt)
        // Количество ступеней
        .times(1)
        // Длительность полки
        .eachLevelLasting(30.minutes)
        // Длительность разгона
        .separatedByRampsLasting(1.minutes)
        // Начало нагрузки с 0 rps
        .startingFrom(0)
    )
  )
    .protocols(httpProtocol)
    // Общая длительность теста
    .maxDuration(31.minutes)

}
