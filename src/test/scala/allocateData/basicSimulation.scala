package allocateData

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import java.util.concurrent.ThreadLocalRandom

class basicSimulation extends Simulation{

    val httpProtocol = http
        .baseUrl("https://reqres.in")

    val userRecords = csv("data/userList.csv").readRecords
    val allocateCount = 5 // this number denotes the number of data allocated to each user
    val isCircular = true // true -> if the data should be cycled at EOF, and turn isRandom to false if you need queue or circular
    val isRandom = true // true -> pick random values from the allocated range. If this is true, then isCircular is ignored

    def dataController() = {
        exec(session => {
                if(isRandom == true){
                    session
                    .set("p_user", userRecords(ThreadLocalRandom.current().nextInt(((session.userId).toInt * allocateCount) - allocateCount, ((session.userId).toInt * allocateCount))).get("p_user").get)
                }
                else{
                    if(session("feedCount").as[Int] == allocateCount){
                        if(isCircular == true){
                            session
                            .set("p_user", userRecords(((session.userId).toInt * allocateCount) - allocateCount).get("p_user").get)
                            .set("feedCount", 1)
                        }
                        else session.markAsFailed
                    }    
                    else
                        session
                        .set("p_user", userRecords(((session.userId).toInt * allocateCount) - allocateCount + session("feedCount").as[Int]).get("p_user").get)
                        .set("feedCount", (session("feedCount").as[Int] + 1))
                }
                
        })
        // .exec(session=>{
        //     println("vuser: " + session.userId + ", data: " + session("p_user").as[String])
        //     session
        // })
        .exitHereIfFailed
    }

    val scn = scenario("basic")
        .exec(session => session.set("feedCount", 0))
        .repeat(8){
            exec(dataController())
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