import cats.data.Kleisli
import org.http4s._
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import zio.{Task, ZIO, ExitCode}
import zio.interop.catz._
import zio.interop.catz.implicits._
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import doobie._
import doobie.implicits._
import io.circe.generic.auto._
import org.http4s.circe.CirceEntityCodec._

import scala.concurrent.ExecutionContext.global
//implicit def countriesEncoder: EntityEncoder[Task, List[Country]] = ???
object Main extends CatsApp {

  //private val bookRepo: BookRepo = new BookRepo.DummyImpl

  def bookRoutes(repo: CountriesPersistenceService): HttpRoutes[Task] = {
    val dsl = new Http4sDsl[Task]{}
    val allCountries: Task[List[Country]] = repo.getAll()
    import dsl._
    HttpRoutes.of[Task] {
      case _@GET -> Root / "books" =>
        Ok(allCountries)
    }
  }

  //maps from HttpRoutes[F] -> HttpApp: Kleisli[Task, Request[Task], Response[Task]]
  //to compose an entire HttpApp from a bunch of routes

  def run(args: List[String]) =
    streamz.compile.drain.fold(_ => ExitCode.failure, _ => ExitCode.success)

  def streamz() = {
    val xa = Transactor.fromDriverManager[Task](
      "org.postgresql.Driver",     // driver classname
      "jdbc:postgresql:world",     // connect URL (driver-specific)
      "postgres",                  // user
      "",                          // password
    )
    for {
      client <- BlazeClientBuilder[Task](global).stream
      bookRepo = new CountriesPersistenceService(xa)

      httpRoutes = Router[Task](
        "/" -> bookRoutes(bookRepo)
      ).orNotFound

      httpApp = httpRoutes
      finalHttpApp = Logger.httpApp(true, true)(httpApp)
      exitCode <- BlazeServerBuilder[Task](global)
          .bindHttp(9000, "0.0.0.0")
          .withHttpApp(finalHttpApp)
          .serve
    } yield exitCode
  }.drain
}

case class Country(code: String, name: String, pop: Int, gnp: Option[Double])
final class CountriesPersistenceService(tnx: Transactor[Task]) {

  import DAO._
  def getAll(): Task[List[Country]] =
    SQL
      .getAll
      .stream
      .take(15)
      .compile.toList
      .transact(tnx)
}
//queries separated so they can be typechecked with doobie-specs2 or doobie-scalatest
object DAO {
  object SQL {
    def getAll = {
      sql"select code, name, population, gnp from country"
        .query[Country]
    }
  }
}
