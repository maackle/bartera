package controllers.dev

import controllers.Common
import play.api.mvc.Action
import play.api.libs.ws.WS
import scala.concurrent.{Await, Future, ExecutionContext}
import ExecutionContext.Implicits.global
import org.jsoup.Jsoup
import scala.collection.JavaConversions
import JavaConversions._
import models.{ItemImage, Have, User}
import de.svenjacobs.loremipsum.LoremIpsum
import scala.concurrent.duration._
import scala.util.{Success, Failure}

import uk.co.halfninja.randomnames
import uk.co.halfninja.randomnames.Gender
import java.util.concurrent.TimeoutException

object Populator extends Common {


	private def lorem = new LoremIpsum()
	private val namegen = randomnames.NameGenerators.standardGenerator()

	private def getImage(channel:String):Future[ItemImage] = {
		if( ! Set("cats", "animals", "technics", "nature", "food").contains(channel)) throw new Exception("Invalid image channel")
		WS.url("http://lorempixel.com/400/400/%s".format(channel)).get().map { response =>
			val bytes = response.getAHCResponse.getResponseBodyAsBytes()
			val contentType = response.getAHCResponse.getContentType
			println(bytes.mkString(" "))
			ItemImage(bytes, null, contentType)
		}
	}

	private def randomDescription = {
//		val rs = WS.url("http://www.jameydeorio.com/randsense/generate/").get().map { response =>
//			(response.json \ "sentence").as[String]
//		}
//		lazy val sentence = try {
//			Await.result(rs, 100 milliseconds) + "  "
//		} catch {
//			case e:TimeoutException => ""
//		}
		lorem.getWords(100, (math.random * 50).toInt)
	}


	private def createHave(user:User, what:String, numImages:Int, channel:String) = {
		transaction {
			val have = Have.table.insert(Have(what, randomDescription, user.id))
			for (_ <- 1 to numImages) {
				getImage(channel).onComplete {
					case Success(im) =>
						inTransaction {
							val inserted = ItemImage.table.insert(im)
							val haveimage = have.images.associate(inserted)
						}
					case Failure(t) =>
						throw t
				}
			}
			have
		}
	}

	def populate(numHaves:Int, numImages:Int = 3) = Action {
		def anyGender = {
			if(System.currentTimeMillis() % 2 == 0)
				Gender.male
			else
				Gender.female
		}

		val haves = transaction {
			val user = User.table.get(1L)
			for(_ <- 1 to numHaves) yield {
				val name = namegen.generate(anyGender).toString
				createHave(user, "%s Cat".format(name), numImages, "cats")
			}
		}
		Ok(haves.mkString(", "))
	}

	def scrape = Action {
		val key = "dogs"
		val url = "https://www.google.com/search?hl=en&authuser=0&site=imghp&tbm=isch&source=hp&q=%s&btnG=Search+by+image&gbv=1&sei=TPm7UcfsGIWeqQH34YGwBA".format(key)
		val page = Jsoup.connect(url)
			.userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
			.referrer("http://www.google.com")
			.get()
		val imgs = page.getElementsByTag("img")
		val srcs = for(img <- imgs.iterator) yield {
			img.attr("src")
		}
		Ok(srcs.mkString(", "))
//		Async {
//			WS.url(url).get().map { response =>
//
//				val images = (response.xml \ "img")
//				Ok(images)
//			}
//		}
	}
}
