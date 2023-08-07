package repositories

trait State

case object SuccessState extends State

trait ErrorState extends State {
  val message: String
}

case class FailedToDeleteOldImports(message: String) extends ErrorState
case class FailedToInsertList(message: String) extends ErrorState
case class FailedToSave(message: String) extends ErrorState
