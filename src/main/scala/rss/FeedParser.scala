package rss

import cats.effect.IO
import com.rometools.rome.feed.synd.{SyndEntry, SyndFeed}
import com.rometools.rome.io.{SyndFeedInput, XmlReader}

import java.net.URL
import scala.jdk.CollectionConverters._

object FeedParser {


  def getNewsEntries(url: String): IO[List[NewsEntry]] = {

    def buildWithUrl(feedInput: SyndFeedInput, url: String): IO[SyndFeed] = IO {
      feedInput
        .build(new XmlReader(new URL(url)))
    }

    def mapEntries(entries: java.util.List[SyndEntry]) = IO {
      entries.asScala.toList.map(se => NewsEntry(se.getTitle, se.getLink))
    }

    for {
      feedInput <- IO(new SyndFeedInput())
      feed <- buildWithUrl(feedInput, url)
      entries <- mapEntries(feed.getEntries)
    } yield entries
  }

  case class NewsEntry(title: String, link: String)
}


