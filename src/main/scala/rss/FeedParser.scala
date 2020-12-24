package rss

import cats.effect.{IO, Resource}
import com.rometools.rome.io.{SyndFeedInput, XmlReader}

import java.net.URL
import scala.jdk.CollectionConverters._

object FeedParser {


  def top10NewsEntries(url: String): IO[List[NewsEntry]] = {

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
            NewsEntry(title = entry.getTitle, link = entry.getLink)
          }
      }
    }
  }

  case class NewsEntry(title: String, link: String)
}


