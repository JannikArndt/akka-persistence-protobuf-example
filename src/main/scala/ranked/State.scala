package ranked

import akka.persistence.fsm.PersistentFSM.FSMState

sealed trait State extends FSMState

case object NotStarted extends State {
  override def identifier: String = "Empty"
}

case object Running extends State {
  override def identifier: String = "Running"
}

case object Finished extends State {
  override def identifier: String = "Finished"
}
