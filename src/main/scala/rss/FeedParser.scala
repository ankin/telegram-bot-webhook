package rss

import cats.effect.{IO, Resource}
import com.github.blemale.scaffeine.{LoadingCache, Scaffeine}
import com.rometools.rome.io.{SyndFeedInput, XmlReader}
import com.typesafe.scalalogging.StrictLogging

import java.net.URL
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters._

object FeedParser extends StrictLogging {


  val cache: LoadingCache[String, IO[List[RssEntry]]] =
    Scaffeine()
      .expireAfterWrite(10.minutes)
      .maximumSize(10)
      .build[String, IO[List[RssEntry]]] { url : String =>

        def acquire = IO(new XmlReader(new URL(url)))
        def release(reader: XmlReader) = IO(reader.close())
        val readerRes = Resource.make(acquire)(release)

        readerRes.use { xmlReader =>
          IO {
            new SyndFeedInput()
              .build(xmlReader)
              .getEntries
              .asScala
              .toList
              .take(10)
              .map { entry =>
                RssEntry(title = entry.getTitle, link = entry.getLink)
              }
          }
        }
      }

  def top10RssEntries(url: String): IO[List[RssEntry]] = {
    cache.get(url)
  }

  case class RssEntry(title: String, link: String)
}


