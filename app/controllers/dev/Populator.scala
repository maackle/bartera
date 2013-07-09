package controllers.dev

import controllers.Common
import play.api.mvc.Action
import play.api.libs.ws.WS
import scala.concurrent.{Await, Future, ExecutionContext}
import ExecutionContext.Implicits.global
import org.jsoup.Jsoup
import scala.collection.JavaConversions
import JavaConversions._
import models._
import de.svenjacobs.loremipsum.LoremIpsum

import uk.co.halfninja.randomnames
import uk.co.halfninja.randomnames.Gender

import SQ._
import play.api.libs.json.{JsObject, JsValue, Json}
import scala.util.Failure
import scala.Some
import scala.util.Success
import play.api.Play

object Populator extends Common {

	private def lorem = new LoremIpsum()
	private val namegen = randomnames.NameGenerators.standardGenerator()
	def anyGender = {
		if(System.currentTimeMillis() % 2 == 0)
			Gender.male
		else
			Gender.female
	}
	private def getImage(channel:String):Future[ItemImage] = {
		if( ! Set("cats", "animals", "technics", "nature", "food").contains(channel)) throw new Exception("Invalid image channel")
		WS.url("http://lorempixel.com/400/400/%s".format(channel)).get().map { response =>
			val bytes = response.getAHCResponse.getResponseBodyAsBytes()
			val contentType = response.getAHCResponse.getContentType
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

		val zips = Settings.zipcodeSubset.toIndexedSeq

		def randomZip = zips((math.random * zips.length).toInt)

		transaction {
			val t = Have(what, randomDescription, user.id)
			t.setZipcode(randomZip)
			val have = Have.table.insert(t)
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

		val haves = transaction {
			val user = User.table.get(1L)
			for(_ <- 1 to numHaves) yield {
				val name = namegen.generate(anyGender).toString
				createHave(user, "%s Cat".format(name), numImages, "cats")
			}
		}
		Ok(haves.map(h => h.location.zipcode).mkString(", "))
	}


	def scrapeCraigslist = Action {

		val url = {
			val locations = Set("portland", "sfbay", "newyork", "losangeles").toVector
			def randoLoco = locations((math.random  * locations.size).toInt)
			"http://%s.craigslist.org/sss".format(randoLoco)
		}

		val page = Jsoup.connect(url)
			.userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
			.referrer("http://www.google.com")
			.get()

		val user = transaction {
			val email = namegen.generate(anyGender).toString.toLowerCase.replaceAll("""\s""", ".") + "@gmail.com"
			User.register(User(email, "1234"))
		}

		val toc = page.getElementById("toc_rows")
		val rows = toc.getElementsByClass("row")
		val items = transaction {
			{
				for(row <- rows.iterator) yield {
					val title = row.getElementsByClass("pl").head.getElementsByTag("a").head.text()
					val pid = row.attr("data-pid")
					val lat = row.attr("data-latitude")
					val lng = row.attr("data-longitude")
					if(title.toLowerCase.matches(".*(buying|i buy|looking|want|searching).*")) {
						Want.table.insert(Want(title, title, user.id).withZipcode(Location.randomZipcode))
					} else {
						Have.table.insert(Have(title, title, user.id).withZipcode(Location.randomZipcode))
					}
				}
			}.toList
		}
		Ok(items.mkString("\n"))
	}


	def buildCategories = Action {
		val jsonString = io.Source.fromInputStream(Play.classloader.getResourceAsStream("conf/categories.json")).getLines.mkString("\n")
		val root = Json.parse(jsonString)
		transaction {
			ItemCategory.table.deleteWhere(i => i.id <> -1)
			cons(ItemCategory.table.insert(ItemCategory("ROOT", None)), root)
//			for((k, v) <- root.as[JsObject].value) {
//				cons(ItemCategory.table.insert(ItemCategory(k, None)), v)
//			}
		}

		def cons(cat:ItemCategory, v:JsValue):Unit = {
			val m = v.as[JsObject].value
			if(m.isEmpty) {

			} else {
				for((k, v) <- m) {
					val newCat = ItemCategory(k, Some(cat.id))
					cons(ItemCategory.table.insert(newCat), v)
				}
			}
		}

		Ok("categories built")
	}
}
