package foreverExitBlock

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class exitBlockSimulation extends Simulation {

    val httpProtocol = http
        .baseUrl("https://reqres.in")
    
    val userBodyFeeder = tsv("data/createUser.tsv").circular

    val scn = scenario("exitBlock")
    //.exitBlockOnFail{ //if you add exitBlock here, and if iteration fails inside forever loop, then Vuser will go out of simulation
        .exec(http("getUsers")
            .get("/api/users?page=2"))
        .forever(
            pace(5)
            .exitBlockOnFail{ //if you add exitBlock here, and if iteration fails inside block, then Vuser will start the loop again
                feed(userBodyFeeder)
                .exec(http("createUser")
                    .post("/api/users")
                    .body(StringBody("${p_userBody}")).asJson
                    .check(status.is(201)))
            }
        )
    //}
    
    setUp(
        scn.inject(
            atOnceUsers(1)
        ).protocols(httpProtocol)
    ).maxDuration(60)

}
