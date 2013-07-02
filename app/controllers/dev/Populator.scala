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

object Populator extends Common {

	private def lorem = new LoremIpsum()
	private val namegen = randomnames.NameGenerators.standardGenerator()

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

	def buildCategories = Action {
		val is = play.api.Play.getFile("conf/categories.json")
		val jsonString = io.Source.fromFile(is).getLines.mkString("\n")
		val root = Json.parse(jsonString)
		transaction {
			ItemCategory.table.deleteWhere(i => i.id <> -1)
			for((k, v) <- root.as[JsObject].value) {
				cons(ItemCategory.table.insert(ItemCategory(k, None)), v)
			}
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
