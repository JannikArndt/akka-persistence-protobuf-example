import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import ranked.GameProtocol.Team._
import ranked.GameProtocol._
import ranked._

import scala.util.Random

class GameSpec(_system: ActorSystem) extends TestKit(_system) with FlatSpecLike with Matchers with BeforeAndAfterAll with ImplicitSender {

  def this() = this(ActorSystem("TestSystem"))

  override def afterAll(): Unit = {
    shutdown(system)
  }

  "A new Game" should "work as intended" in {
    val randomIdentifier = Random.nextInt()
    val game: ActorRef = system.actorOf(Props[Game], s"game-$randomIdentifier")

    game ! StartGame()
    game ! NewGoal(TEAM_A)
    game ! NewGoal(TEAM_B)
    game ! NewGoal(TEAM_B)
    game ! NewGoal(TEAM_B)
    game ! NewGoal(TEAM_B)
    game ! NewGoal(TEAM_B)
    game ! NewGoal(TEAM_B)

    game ! GetScore()

    game ! NewGoal(TEAM_B)
    game ! NewGoal(TEAM_B)
    game ! NewGoal(TEAM_B)
    game ! NewGoal(TEAM_B)

    expectMsg(Score(1, 6))
    expectMsg(Score(1, 10))
  }
}
