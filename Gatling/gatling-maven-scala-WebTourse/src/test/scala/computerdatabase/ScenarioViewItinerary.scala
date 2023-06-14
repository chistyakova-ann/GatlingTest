package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Random


object ScenarioViewItinerary {
  private val headers_0 = Map(
    "Cache-Control" -> "max-age=0",
    "If-Modified-Since" -> "Tue, 28 Feb 2017 19:10:32 GMT",
    "Sec-Fetch-Dest" -> "document",
    "Sec-Fetch-Mode" -> "navigate",
    "Sec-Fetch-Site" -> "same-origin",
    "Sec-Fetch-User" -> "?1",
    "sec-ch-ua" -> """Not.A/Brand";v="8", "Chromium";v="114", "Google Chrome";v="114""",
    "sec-ch-ua-mobile" -> "?0",
    "sec-ch-ua-platform" -> "Windows"
  )

  private val headers_1 = Map(
    "Sec-Fetch-Dest" -> "frame",
    "Sec-Fetch-Mode" -> "navigate",
    "Sec-Fetch-Site" -> "same-origin",
    "sec-ch-ua" -> """Not.A/Brand";v="8", "Chromium";v="114", "Google Chrome";v="114""",
    "sec-ch-ua-mobile" -> "?0",
    "sec-ch-ua-platform" -> "Windows"
  )

  private val headers_3 = Map(
    "Cache-Control" -> "max-age=0",
    "Origin" -> "http://localhost:8090",
    "Sec-Fetch-Dest" -> "frame",
    "Sec-Fetch-Mode" -> "navigate",
    "Sec-Fetch-Site" -> "same-origin",
    "Sec-Fetch-User" -> "?1",
    "sec-ch-ua" -> """Not.A/Brand";v="8", "Chromium";v="114", "Google Chrome";v="114""",
    "sec-ch-ua-mobile" -> "?0",
    "sec-ch-ua-platform" -> "Windows"
  )

  private val headers_4 = Map(
    "Sec-Fetch-Dest" -> "frame",
    "Sec-Fetch-Mode" -> "navigate",
    "Sec-Fetch-Site" -> "same-origin",
    "Sec-Fetch-User" -> "?1",
    "sec-ch-ua" -> """Not.A/Brand";v="8", "Chromium";v="114", "Google Chrome";v="114""",
    "sec-ch-ua-mobile" -> "?0",
    "sec-ch-ua-platform" -> "Windows"
  )

  val feeder = csv("login.csv").random
  val numFeeder = Iterator.continually(Map("num" -> Random.between(1, 3)))
  val departFeeder = Iterator.continually(Map("departDate" -> LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))))
  val returnFeeder = Iterator.continually(Map("returnDate" -> LocalDate.now().plusDays(30).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))))

  def scn6 = scenario("ScenarioViewItinerary")
    .forever(
      pace(150)
        .exitBlockOnFail {
          exec(
            http("UC06_ViewItinerary_HomePage_0")
              .get("/WebTours/")
              .headers(headers_0)
              .resources(
                //         http("request_1")
                //           .get("/WebTours/welcome.pl?signOff=true")
                //            .headers(headers_1),
                http("UC06_ViewItinerary_HomePage_2")
                  .get("/WebTours/nav.pl?in=home")
                  .headers(headers_1)
                  //    .check(regex("""<input type=hidden name=userSession value=(.*)>""").saveAs("userSession"))
                  .check(regex(
                    """<form method=post action=login.pl target=body>
                      |<input type=hidden name=userSession value=(.*)>
                      |<table border=0><tr><td>&nbsp;</td>""".stripMargin).saveAs("userSession"))

                  .check(status.is(200))
              )
          )
        }
        .pause(5)
        .feed(feeder)
        .exitBlockOnFail {
          exec(
            http("UC06_ViewItinerary_Login_0")
              .post("/WebTours/login.pl")
              .headers(headers_3)

              .formParam("userSession", "#{userSession}")
              .formParam("username", "#{login}")
              .formParam("password", "#{password}")
              .formParam("login.x", "59")
              .formParam("login.y", "8")
              .formParam("JSFormSubmit", "off")
              .check(status.is(200))
              .check(regex("""<title>Web Tours</title>"""))
              .resources(
                http("UC06_ViewItinerary_Login_1")
                  .get("/WebTours/nav.pl?page=menu&in=home")
                  .headers(headers_4)
                  .check(status.is(200)),
                http("UC06_ViewItinerary_Login_2")
                  .get("/WebTours/login.pl?intro=true")
                  .headers(headers_4)
                  .check(status.is(200))
                  .check(regex("""<blockquote>Welcome, <b>#{login}</b>"""))

              )
          )
        }
        .pause(5)
        .exitBlockOnFail {
          exec(
            http("UC06_ViewItinerary_ClickItinerary_0")
              .get("/WebTours/welcome.pl?page=itinerary")
              .headers(headers_4)
              .check(status.is(200))
              .resources(
                http("UC06_ViewItinerary_ClickItinerary_1")
                  .get("/WebTours/nav.pl?page=menu&in=itinerary")
                  .headers(headers_4)
                  .check(status.is(200)),
                http("UC06_ViewItinerary_ClickItinerary_2")
                  .get("/WebTours/itinerary.pl")
                  .headers(headers_4)
                  .check(status.is(200))
                  .check(regex("""name="flightID" value="(.*)"  />""").count.saveAs("flightCount"))
                  .check(checkIf("#{flightCount}" != 0)(regex("""name="flightID" value="(.*)"  />""").exists))
                  .check(checkIf("#{flightCount}" == 0)(regex("""No flights have been reserved.""")))
              )
          )
        }
        .pause(5)
        .exitBlockOnFail {
          exec(
            http("UC06_ViewItinerary_Logout_0")
              .get("/WebTours/welcome.pl?signOff=1")
              .headers(headers_4)
              .check(status.is(200))
              .resources(
                http("UC06_ViewItinerary_Logout_1")
                  .get("/WebTours/nav.pl?in=home")
                  .headers(headers_4)
                  .check(status.is(200))
              )
          )
        }
    )
}

