import io.gatling.core.Predef._
import io.gatling.http.Predef._

class basicSimulation extends Simulation{

    val httpProtocol = http
        .baseUrl("https://reqres.in")

    val userRecords = csv("userList.csv").readRecords
    val allocateCount = 6 // this number denotes the number of data allocated to each user

    val scn = scenario("basic")
        .exec(session => session.set("feedCount", 0))
        .repeat(7){
            exec(session => {
                if(session("feedCount").as[Int] == allocateCount) session.markAsFailed    
                else
                    session
                    .set("p_user", userRecords(((session.userId).toInt * allocateCount) - allocateCount + session("feedCount").as[Int]).get("p_user").get)
                    .set("feedCount", (session("feedCount").as[Int] + 1))
            })
            .exitHereIfFailed
            .exec(http("createUsers")
                .post("/api/users")
                .body(StringBody("""{"name": "${p_user}","job": "leader"}""")))
            .pause(1)
        }
        
    setUp(
        scn.inject(
            atOnceUsers(3)
        ).protocols(httpProtocol)
    )

}