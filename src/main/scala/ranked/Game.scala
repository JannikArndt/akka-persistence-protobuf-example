package ranked

import akka.persistence._
import akka.persistence.fsm._
import ranked.GameProtocol.Team._
import ranked.GameProtocol._

import scala.reflect._

class Game extends PersistentFSM[State, Score, DomainEvent] {

  override def persistenceId: String = context.self.path.toString

  override def onRecoveryCompleted(): Unit = log.info("Recovery completed!")

  override def domainEventClassTag: ClassTag[DomainEvent] = classTag[DomainEvent]

  startWith(NotStarted, Score())

  when(NotStarted) {
    case Event(StartGame(), _) =>
      log.info("Game started!")
      goto(Running)
    case Event(_, _) =>
      log.error("Please start the game first, using `game ! StartGame()` !")
      stay
  }

  when(Running) {
    case Event(NewGoal(team), score: Score) =>
      log.info(s"$team scored a goal!")
      val newScore = team match {
        case TEAM_A              => score.withTeamA(score.teamA + 1)
        case TEAM_B              => score.withTeamB(score.teamB + 1)
        case Unrecognized(value) => throw new MatchError(s"Can't match $value")
      }
      if (newScore.teamA == 10 || newScore.teamB == 10) {
        log.info(s"Game finished! $team has won with $newScore!")
        sender() ! newScore
        goto(Finished) applying Goal(team)
      } else
        stay applying Goal(team)

    case Event(RequestCorrection(team), score: Score) =>
      log.info(s"Correcting '$team' scored a goal!")
      val newScore = team match {
        case TEAM_A              => score.withTeamA(score.teamA - 1)
        case TEAM_B              => score.withTeamB(score.teamB - 1)
        case Unrecognized(value) => throw new MatchError(s"Can't match $value")
      }
      if (score.teamA >= 0 && score.teamB >= 0) {
        sender() ! newScore
        stay applying Correction(team)
      } else
        stay

    case Event(GetScore(), score: Score) =>
      log.info(s"Score: Team A has ${score.teamA} goals and Team B has ${score.teamB} goals.")
      sender() ! score
      stay

    case Event(AbortGame(), score: Score) =>
      log.info(s"Aborting game, score was $score")
      goto(Finished)

    case Event(StartGame(), _) =>
      log.error("Game is already started!")
      stay

    case Event(SaveSnapshotSuccess(metadata), data) =>
      log.info(s"Snapshot saved: $metadata with $data")
      stay

    case Event(SaveSnapshotFailure(metadata, cause), data) =>
      log.error(s"Error while saving snapshot $metadata and data $data: $cause")
      stay
  }

  when(Finished) {
    case Event(RequestCorrection(team), score: Score) =>
      val newScore = team match {
        case TEAM_A              => score.withTeamA(score.teamA - 1)
        case TEAM_B              => score.withTeamB(score.teamB - 1)
        case Unrecognized(value) => throw new MatchError(s"Can't match $value")
      }
      log.info(s"Resuming game with score $newScore")
      goto(Running) applying Correction(team)

    case Event(GetScore(), score: Score) =>
      log.info(s"Score: Team A has ${score.teamA} goals and Team B has ${score.teamB} goals.")
      sender() ! score
      stay

    case Event(_, _) =>
      log.error("This game is finished. Start a new game to continue!")
      stay
  }

  override def applyEvent(domainEvent: DomainEvent, score: Score): Score = {
    domainEvent match {
      case Goal(team) =>
        team match {
          case TEAM_A              => score.withTeamA(score.teamA + 1)
          case TEAM_B              => score.withTeamB(score.teamB + 1)
          case Unrecognized(value) => throw new MatchError(s"Can't match $value")
        }
      case Correction(team) =>
        team match {
          case TEAM_A              => score.withTeamA(score.teamA - 1)
          case TEAM_B              => score.withTeamB(score.teamB - 1)
          case Unrecognized(value) => throw new MatchError(s"Can't match $value")
        }
    }
  }
}
