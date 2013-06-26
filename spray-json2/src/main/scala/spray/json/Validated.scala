package spray.json

object Validated {
  def apply[T](body: ⇒ T): Validated[T] = {
    try {
      Success(body)
    } catch {
      case ex: Exception ⇒ Failure(ex)
    }
  }
}

sealed abstract class Validated[+T] {
  def isSuccess: Boolean
  def isFailure: Boolean

  def get: T
  def getOrElse[R >: T](default: ⇒ R): R
  def orElse[R >: T](alternative: ⇒ Validated[R]): Validated[R]

  def toEither: Either[Throwable, T]
  def toOption: Option[T]

  def map[R](f: T ⇒ R): Validated[R]
  def flatMap[R](f: T ⇒ Validated[R]): Validated[R]
  def foreach(f: T ⇒ Unit)
  def filter(p: T ⇒ Boolean): Validated[T]
  def withFilter(p: T ⇒ Boolean): Validated[T] = filter(p)

  def exists(p: T ⇒ Boolean): Boolean

  def dyn(implicit ev: T <:< JsValue): DynamicValidatedJsValue =
    DynamicValidatedJsValue(this.map(ev))
}

case class Failure[+T](throwable: Throwable) extends Validated[T] {
  def isSuccess = false
  def isFailure = true

  def get = throw throwable
  def getOrElse[R >: T](default: ⇒ R) = default
  def orElse[R >: T](alternative: ⇒ Validated[R]) = alternative

  def toEither = Left(throwable)
  def toOption = None

  def map[R](f: T ⇒ R) = this.asInstanceOf[Validated[R]]
  def flatMap[R](f: T ⇒ Validated[R]) = this.asInstanceOf[Validated[R]]
  def foreach(f: T ⇒ Unit) {}
  def filter(p: T ⇒ Boolean) = this

  def exists(p: T ⇒ Boolean): Boolean = false
}

case class Success[+T](value: T) extends Validated[T] {
  def isSuccess = true
  def isFailure = false

  def get = value
  def getOrElse[R >: T](default: ⇒ R) = value
  def orElse[R >: T](alternative: ⇒ Validated[R]) = this

  def toEither = Right(value)
  def toOption = Some(value)

  def map[R](f: T ⇒ R) = Validated(f(value))
  def flatMap[R](f: T ⇒ Validated[R]) = {
    try {
      f(value)
    } catch {
      case ex: Exception ⇒ Failure(ex)
    }
  }
  def foreach(f: T ⇒ Unit) { f(value) }
  def filter(p: T ⇒ Boolean) = {
    try {
      if (p(value)) this
      else Failure(new UnsatisfiedFilterException())
    } catch {
      case ex: Exception ⇒ Failure(ex)
    }
  }

  def exists(p: T ⇒ Boolean): Boolean = p(value)
}

class UnsatisfiedFilterException(msg: String = "") extends RuntimeException(msg)