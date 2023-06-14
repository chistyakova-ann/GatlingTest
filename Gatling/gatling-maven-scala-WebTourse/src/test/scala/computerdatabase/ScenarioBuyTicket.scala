package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.util.Random


object ScenarioBuyTicket {
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
  val numFeeder = Iterator.continually(Map("num" -> Random.between(1, 4)))
  val departFeeder = Iterator.continually(Map("departDate" -> LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))))
  val returnFeeder = Iterator.continually(Map("returnDate" -> LocalDate.now().plusDays(30).format(DateTimeFormatter.ofPattern("MM/dd/yyyy"))))

  def scn1 = scenario("ScenarioBuyTicket")

    .forever(
      pace(80)
        .exitBlockOnFail {
          exec(
            http("UC01_BuyTicket_HomePage_0")
              .get("/WebTours/")
              .headers(headers_0)
              .resources(
                //         http("request_1")
                //           .get("/WebTours/welcome.pl?signOff=true")
                //            .headers(headers_1),
                http("UC01_BuyTicket_HomePage_2")
                  .get("/WebTours/nav.pl?in=home")
                  .headers(headers_1)
                  //.check(regex("""<input type=hidden name=userSession value=(.*)>""").saveAs("userSession"))
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
            http("UC01_BuyTicket_Login_0")
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
                http("UC01_BuyTicket_Login_1")
                  .get("/WebTours/nav.pl?page=menu&in=home")
                  .headers(headers_4)
                  .check(status.is(200)),
                http("UC01_BuyTicket_Login_2")
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
            http("UC01_BuyTicket_ClickFlights_0")
              .get("/WebTours/welcome.pl?page=search")
              .headers(headers_4)
              .check(status.is(200))
              .resources(
                http("UC01_BuyTicket_ClickFlights_1")
                  .get("/WebTours/reservations.pl?page=welcome")
                  .headers(headers_4)
                  .check(status.is(200))
                  .check(regex("""<option value=".*">(.*)</option>""").findRandom.saveAs("CityDepart"))
                  .check(regex("""<option value=".*">(.*)</option>""").findRandom.saveAs("CityArrive"))
                  .check(regex("""input type="radio" name="seatType" value="(.*?)"""").findRandom.saveAs("seatType"))
                  .check(regex("""input type="radio" name="seatPref" value="(.*?)"""").findRandom.saveAs("seatPref"))
                ,
                http("UC01_BuyTicket_ClickFlights_2")
                  .get("/WebTours/nav.pl?page=menu&in=flights")
                  .headers(headers_4)
                  .check(status.is(200))
              )
          )
        }
        .pause(5)
        .feed(numFeeder)
        .feed(departFeeder)
        .feed(returnFeeder)
        .exitBlockOnFail {
          exec(
            http("UC01_BuyTicket_FillDataTicket_0")
              .post("/WebTours/reservations.pl")
              .headers(headers_3)
              .formParam("advanceDiscount", "0")
              .formParam("depart", "#{CityDepart}")
              .formParam("departDate", "#{departDate}")
              .formParam("arrive", "#{CityArrive}")
              .formParam("returnDate", "#{returnDate}")
              .formParam("numPassengers", "#{num}")
              .formParam("seatPref", "#{seatPref}")
              .formParam("seatType", "#{seatType}")
              .formParam("findFlights.x", "69")
              .formParam("findFlights.y", "11")
              .formParam(".cgifields", "roundtrip")
              .formParam(".cgifields", "seatType")
              .formParam(".cgifields", "seatPref")
              .check(status.is(200))
              .check(regex("""input type = radio name=outboundFlight value=(.*?)>""").findRandom.saveAs("outboundFlight"))
          )
        }
        .pause(5)
        .exitBlockOnFail {
          exec(
            http("UC01_BuyTicket_ChooseTicket_0")
              .post("/WebTours/reservations.pl")
              .headers(headers_3)
              .formParam("outboundFlight", "#{outboundFlight}")
              .formParam("numPassengers", "#{num}")
              .formParam("advanceDiscount", "0")
              .formParam("seatType", "#{seatPref}")
              .formParam("seatPref", "#{seatType}")
              .formParam("reserveFlights.x", "22")
              .formParam("reserveFlights.y", "13")
              .check(status.is(200))
              .check(regex("""name="firstName" value="(.*?)"""").saveAs("firstName"))
              .check(regex("""name="lastName" value="(.*?)&#10;"""").saveAs("lastName"))
              .check(regex("""name="address1" value="(.*?)"""").saveAs("address1"))
              .check(regex("""name="address2" value="(.*?)"""").saveAs("address2"))
              .check(regex("""name="pass1" value="(.*?)&#10;"""").saveAs("pass1"))
          )
        }
        .pause(5)
        .doIfEqualsOrElse("#{num}", 1) {
          exitBlockOnFail {
            exec(
              http("UC01_BuyTicket_Payment_0")
                .post("/WebTours/reservations.pl")
                .headers(headers_3)
                .formParam("firstName", "#{firstName}")
                .formParam("lastName", "#{lastName}")
                .formParam("address1", "#{address1}")
                .formParam("address2", "#{address2}")
                .formParam("pass1", "#{pass1}")
                .formParam("creditCard", "123456")
                .formParam("expDate", "03/24")
                .formParam("oldCCOption", "")
                .formParam("numPassengers", "#{num}")
                .formParam("seatType", "#{seatType}")
                .formParam("seatPref", "#{seatPref}")
                .formParam("outboundFlight", "#{outboundFlight}")
                .formParam("advanceDiscount", "0")
                .formParam("returnFlight", "")
                .formParam("JSFormSubmit", "off")
                .formParam("buyFlights.x", "10")
                .formParam("buyFlights.y", "6")
                .formParam(".cgifields", "saveCC")
                .check(status.is(200))
                .check(regex("Thank you for booking through Web Tours."))
            )
          }
        } {
          doIfEqualsOrElse("#{num}", 2) {
            exitBlockOnFail {
              exec(
                http("UC01_BuyTicket_Payment_0")
                  .post("/WebTours/reservations.pl")
                  .headers(headers_3)
                  .formParam("firstName", "#{firstName}")
                  .formParam("lastName", "#{lastName}")
                  .formParam("address1", "#{address1}")
                  .formParam("address2", "#{address2}")
                  .formParam("pass1", "#{pass1}")
                  .formParam("pass2", "Jonny Depp")
                  .formParam("creditCard", "123456")
                  .formParam("expDate", "03/24")
                  .formParam("oldCCOption", "")
                  .formParam("numPassengers", "#{num}")
                  .formParam("seatType", "#{seatType}")
                  .formParam("seatPref", "#{seatPref}")
                  .formParam("outboundFlight", "#{outboundFlight}")
                  .formParam("advanceDiscount", "0")
                  .formParam("returnFlight", "")
                  .formParam("JSFormSubmit", "off")
                  .formParam("buyFlights.x", "10")
                  .formParam("buyFlights.y", "6")
                  .formParam(".cgifields", "saveCC")
                  .check(status.is(200))
                  .check(regex("Thank you for booking through Web Tours."))
              )
            }
          } {
            exitBlockOnFail {
              exec(
                http("UC01_BuyTicket_Payment_0")
                  .post("/WebTours/reservations.pl")
                  .headers(headers_3)
                  .formParam("firstName", "#{firstName}")
                  .formParam("lastName", "#{lastName}")
                  .formParam("address1", "#{address1}")
                  .formParam("address2", "#{address2}")
                  .formParam("pass1", "#{pass1}")
                  .formParam("pass2", "Jonny Depp")
                  .formParam("pass3", "Cris Pratt")
                  .formParam("creditCard", "123456")
                  .formParam("expDate", "03/24")
                  .formParam("oldCCOption", "")
                  .formParam("numPassengers", "#{num}")
                  .formParam("seatType", "#{seatType}")
                  .formParam("seatPref", "#{seatPref}")
                  .formParam("outboundFlight", "#{outboundFlight}")
                  .formParam("advanceDiscount", "0")
                  .formParam("returnFlight", "")
                  .formParam("JSFormSubmit", "off")
                  .formParam("buyFlights.x", "10")
                  .formParam("buyFlights.y", "6")
                  .formParam(".cgifields", "saveCC")
                  .check(status.is(200))
                  .check(regex("Thank you for booking through Web Tours."))
              )
            }
          }
        }
        .pause(5)
        .exitBlockOnFail {
          exec(
            http("UC01_BuyTicket_ClickItinerary_0")
              .get("/WebTours/welcome.pl?page=itinerary")
              .headers(headers_4)
              .check(status.is(200))
              .resources(
                http("UC01_BuyTicket_ClickItinerary_1")
                  .get("/WebTours/nav.pl?page=menu&in=itinerary")
                  .headers(headers_4)
                  .check(status.is(200)),
                http("UC01_BuyTicket_ClickItinerary_2")
                  .get("/WebTours/itinerary.pl")
                  .headers(headers_4)
                  .check(status.is(200))
                  .check(regex("""name="flightID" value="(.*)"  />""").exists)
              )
          )
        }
        .pause(5)
        .exitBlockOnFail {
          exec(
            http("UC01_BuyTicket_Logout_0")
              .get("/WebTours/welcome.pl?signOff=1")
              .headers(headers_4)
              .check(status.is(200))
              .resources(
                http("UC01_BuyTicket_Logout_1")
                  .get("/WebTours/nav.pl?in=home")
                  .headers(headers_4)
                  .check(status.is(200))
              )
          )
        }
    )
}

