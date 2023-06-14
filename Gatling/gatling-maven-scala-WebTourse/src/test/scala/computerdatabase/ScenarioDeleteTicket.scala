package computerdatabase

import io.gatling.core.Predef._
import io.gatling.http.Predef._

object ScenarioDeleteTicket {

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

  def scn2 = scenario("ScenarioDeleteTicket ")
    .forever(
      pace(49)
        .exitBlockOnFail {
          exec(
            http("UC02_DeleteTicket_HomePage_0")
              .get("/WebTours/")
              .headers(headers_0)
              .resources(
                //       http("request_1")
                //       .get("/WebTours/welcome.pl?signOff=true")
                //     .headers(headers_1),
                http("UC02_DeleteTicket_HomePage_1")
                  .get("/WebTours/nav.pl?in=home")
                  .headers(headers_1)
                  // .check(regex("""<input type=hidden name=userSession value=(.*)>""").saveAs("userSession"))
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
            http("UC02_DeleteTicket_Login_0")
              .post("/WebTours/login.pl")
              .headers(headers_3)
              .formParam("userSession", "#{userSession}")
              .formParam("username", "#{login}")
              .formParam("password", "#{password}")
              .formParam("login.x", "78")
              .formParam("login.y", "8")
              .formParam("JSFormSubmit", "off")
              .check(status.is(200))
              .check(regex("""<title>Web Tours</title>"""))
              .resources(
                http("UC02_DeleteTicket_Login_1")
                  .get("/WebTours/nav.pl?page=menu&in=home")
                  .headers(headers_4)
                  .check(status.is(200)),
                http("UC02_DeleteTicket_Login_2")
                  .get("/WebTours/login.pl?intro=true")
                  .headers(headers_4)
                  .check(status.is(200))
                  .check(regex("""<blockquote>Welcome, <b>#{login}</b>"""))
              )
          )
        }
        .pause(5)
        //Тут можно сделать, чтобы выходил в случае, если не найдет билетов (нужно будет изменить check)
        .tryMax(3) {
          exec(
            http("UC02_DeleteTicket_ClickItinerary_0")
              .get("/WebTours/welcome.pl?page=itinerary")
              .headers(headers_4)
              .check(status.is(200))
              .resources(
                http("UC02_DeleteTicket_ClickItinerary_1")
                  .get("/WebTours/nav.pl?page=menu&in=itinerary")
                  .headers(headers_4)
                  .check(status.is(200)),
                http("UC02_DeleteTicket_ClickItinerary_2")
                  .get("/WebTours/itinerary.pl")
                  .headers(headers_4)
                  .check(status.is(200))
                  .check(regex("""name="flightID" value="(.*)"  />""").count.saveAs("flightCount"))
                  //   .check(checkIf((r: Response, s: Session) => !=(r.body.string.contains("""No flights have been reserved.""")))(regex("""name="flightID" value="(.*)"  />""").findAll.saveAs("flightMass")))
                  .check(checkIf("#{flightCount}" != 0)(regex("""name="flightID" value="(.*)"  />""").findAll.saveAs("flightMass")))
                //.check(regex("""No flights have been reserved.""").notExists)
              )
          )
        }
        .pause(5)
        .exitHereIf("#{flightCount}" == 0) //выход из сценария если нет билетов, по идее должен работать, но что-то не так
        //Другая проверка
        .doIfEqualsOrElse("#{flightCount}", 0) {
          //TODO ДОРАБОТАТЬ ЧТОБЫ ВЫДАВАЛ WARNING и падал
          exec { session =>
            println("Нет билетов")
            session
          }

        } {
          exitBlockOnFail {
            //Проблема, что  здесь итерация в случае ошибки должна прерываться, но этого не происходит. Пока в процессе
            exec(
              http("UC02_DeleteTicket_DeleteTicket_0")
                .post("/WebTours/itinerary.pl")
                .headers(headers_3)
                .formParam("1", "on")
                .multivaluedFormParam("flightID", "#{flightMass}")
                .formParam("removeFlights.x", "42")
                .formParam("removeFlights.y", "8")
                .formParam(".cgifields", "4")
                .formParam(".cgifields", "1")
                .formParam(".cgifields", "3")
                .formParam(".cgifields", "2")
                .formParam(".cgifields", "5")
                .check(regex("""name="flightID" value="(.*)"  />""").count.saveAs("flightCountAfterDelete"))
                .check(checkIf("#{flightCount}" == 1)(regex("""No flights have been reserved.""").exists))
                //Данная проверка иногда ложносрабатывает,т.к могла создаться либо удалить еще заявка
                .check(checkIf("#{flightCount}" != 1)(regex("""A total of #{flightCountAfterDelete} scheduled flights.""")))
            )
              .doIfOrElse("#{flightCount}" > "#{flightCountAfterDelete}") {
                exec { session =>
                  println("Билет успешно удален")
                  session
                }
              } {
                exec { session =>
                  println("Что-то пошло не так")
                  session
                }
              }
            //
            //         .doIfEquals("#{flightCount}", "#{flightCountAfterDelete}") {
            //           exec { session =>
            //              println("Что-то пошло не так")
            //              session
            //            }
            //          }

          }
        }
        .pause(5)
        .exitBlockOnFail {
          exec(
            http("UC02_DeleteTicket_Logout_0")
              .get("/WebTours/welcome.pl?signOff=1")
              .headers(headers_4)
              .check(status.is(200))
              .resources(
                http("UC02_DeleteTicket_Logout_1")
                  .get("/WebTours/nav.pl?in=home")
                  .headers(headers_4)
                  .check(status.is(200)))
          )
        }
    )

}

