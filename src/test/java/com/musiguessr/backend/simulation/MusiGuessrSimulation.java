package com.musiguessr.backend.simulation;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class MusiGuessrSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
            .baseUrl("https://www.musiguessr.app")
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Gatling Load Test / 3.13.5");

    Iterator<Map<String, Object>> feeder = Stream.generate((Supplier<Map<String, Object>>) () -> {
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return Map.of(
                "username", "test_" + uuid,
                "email", "test_" + uuid + "@loadtest.com",
                "password", "Pass123!",
                "name", "Load User " + uuid
        );
    }).iterator();

    ChainBuilder gameChain =
            exec(http("Create Game")
                    .post("/api/games")
                    .header("Authorization", "Bearer #{jwtToken}")
                    .check(status().is(201))
                    .check(jsonPath("$.id").saveAs("gameId"))
            )
                    .pause(2)
                    .exec(http("Start Game")
                            .post("/api/games/#{gameId}/start")
                            .header("Authorization", "Bearer #{jwtToken}")
                            .check(status().is(200))
                    )
                    .repeat(5, "roundIndex").on(
                            pause(Duration.ofSeconds(1))
                                    .exec(http("Make a Guess - Round #{roundIndex}")
                                            .post("/api/games/#{gameId}/guess")
                                            .header("Authorization", "Bearer #{jwtToken}")
                                            .body(StringBody("{ \"musicId\": 1, \"elapsedMs\": 1000 }"))
                                            .check(status().in(200, 400))
                                    )
                    )
                    .pause(1)
                    .exec(http("Finish Game")
                            .post("/api/games/#{gameId}/finish")
                            .header("Authorization", "Bearer #{jwtToken}")
                            .check(status().is(200))
                    );

    ChainBuilder authChain =
            feed(feeder)
                    .exec(http("Register Request")
                            .post("/api/auth/register")
                            .body(StringBody("{ \"username\": \"#{username}\", \"email\": \"#{email}\", \"password\": \"#{password}\", \"name\": \"#{name}\" }"))
                            .check(status().in(201, 200))
                    )
                    .pause(1)
                    .exec(http("Login Request")
                            .post("/api/auth/login")
                            .body(StringBody("{ \"username\": \"#{username}\", \"password\": \"#{password}\" }"))
                            .check(status().is(200))
                            .check(bodyString().saveAs("loginResponseBody"))
                            .check(jsonPath("$.accessToken").saveAs("jwtToken"))
                    )
                    .exec(session -> {
                        if (!session.contains("jwtToken")) {
                            System.err.println("!!! TOKEN NOT FOUND !!!");
                            System.err.println("User: " + session.getString("username"));
                            System.err.println("Response: " + session.getString("loginResponseBody"));
                            return session.markAsFailed();
                        }
                        return session;
                    });

    ScenarioBuilder scn = scenario("MusiGuessr Load Test")
            .exec(authChain)
            .exitHereIfFailed()
            .pause(2)
            .exec(gameChain);

    {
        setUp(
                scn.injectOpen(
                        rampUsers(20).during(10),

                        nothingFor(5),

                        constantUsersPerSec(10).during(30),

                        rampUsers(100).during(20)
                )
        ).protocols(httpProtocol);
    }
}