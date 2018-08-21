# Akka Persistence with Protobuf

This example uses 

* [Akka Persistence](https://doc.akka.io/docs/akka/2.5/persistence.html?language=scala) for persistent actors,
* [Persistent FSM](https://doc.akka.io/docs/akka/2.5/persistence.html?language=scala#persistent-fsm) (finite state machine) for states and transitions,
* [ScalaPB](https://scalapb.github.io) to generate Scala classes from Protobuf messages

## Getting started

* `sbt test`

## Running in the console

```sbtshell
sbt> console
scala> import akka.actor.{ActorRef, ActorSystem, Props}
scala> import ranked.GameProtocol.Team._
scala> import ranked.GameProtocol._
scala> import ranked._
scala> val system = ActorSystem("TestSystem")
scala> val game: ActorRef = system.actorOf(Props[Game], "game1")

scala> game ! StartGame()
scala> game ! NewGoal(TEAM_A)
scala> game ! NewGoal(TEAM_B)

scala> system.terminate()
scala> :quit
sbt> console

scala> import akka.actor.{ActorRef, ActorSystem, Props}
scala> import ranked.GameProtocol.Team._
scala> import ranked.GameProtocol._
scala> import ranked._
scala> val system = ActorSystem("TestSystem")
scala> val game: ActorRef = system.actorOf(Props[Game], "game1")
[INFO] Recovery completed!

scala> scala> game ! GetScore()
[INFO] Score: Team A has 1 goals and Team B has 1 goals.

```