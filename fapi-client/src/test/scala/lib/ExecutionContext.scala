package lib

trait ExecutionContext {
  implicit val executionContext = scala.concurrent.ExecutionContext.global
}
