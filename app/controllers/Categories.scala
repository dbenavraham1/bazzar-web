package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.ws.WS
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.templates.Html
import models.MenuCategory

class Categories extends Controller with ProvidesHeader {

  def detail(id: Long) = Action { implicit request =>
    Async {
      WS.url("http://localhost:8080/bazzar_online/menu/category/" + id + "/").get().map { response =>
        val category = Json.fromJson[MenuCategory](response.json \ "category").get
        val sidebar = views.html.category.sidebar(category)
        val breadcrumb = views.html.category.breadcrumb(category)
        Ok(views.html.category.detail(category, sidebar, breadcrumb))
      }
    }
  }

  def list(id: Long) = Action {
    val categories = (clientCategories \ "clients")(0) \ id.toString \ "categories"
    Ok(categories)
  }

  var clientCategories = JsObject(
    "clients" -> JsArray(
      JsObject(
        "1" ->
          JsObject(
            "categories" -> JsArray(
              JsObject(
                "name" -> JsString("Appliances") ::
                  "categories" -> JsArray(
                    JsObject(
                      "name" -> JsString("Kitchen") ::
                        "categories" -> JsArray(
                          JsObject(
                            "name" -> JsString("Appliance Packages") ::
                              Nil) ::
                            JsObject(
                              "name" -> JsString("Cooking Products") ::
                                Nil) ::
                              JsObject(
                                "name" -> JsString("Dishwashers") ::
                                  Nil) ::
                                JsObject(
                                  "name" -> JsString("Garbage Disposals") ::
                                    Nil) :: Nil) ::
                          Nil) ::
                      JsObject(
                        "name" -> JsString("Laundry") ::
                          "categories" -> JsArray(
                            JsObject(
                              "name" -> JsString("Laundry Packages") ::
                                Nil) ::
                              JsObject(
                                "name" -> JsString("Washers") ::
                                  Nil) ::
                                JsObject(
                                  "name" -> JsString("Dryers") ::
                                    Nil) ::
                                  JsObject(
                                    "name" -> JsString("Stack Washers and Dryers") ::
                                      Nil) :: Nil) ::
                            Nil) ::
                        JsObject(
                          "name" -> JsString("Home Comfort") ::
                            "categories" -> JsArray(
                              JsObject(
                                "name" -> JsString("Air Conditioners") ::
                                  Nil) ::
                                JsObject(
                                  "name" -> JsString("Air Purifiers") ::
                                    Nil) ::
                                  JsObject(
                                    "name" -> JsString("Dehumidifiers") ::
                                      Nil) ::
                                    JsObject(
                                      "name" -> JsString("Fans And Space Heaters") ::
                                        Nil) :: Nil) ::
                              Nil) ::
                          JsObject(
                            "name" -> JsString("Accessories") ::
                              "categories" -> JsArray(
                                JsObject(
                                  "name" -> JsString("Appliance Accessories") ::
                                    Nil) :: Nil) ::
                                Nil) :: Nil) :: Nil) ::
                JsObject(
                  "name" -> JsString("TV & Video") ::
                    "categories" -> JsArray(
                      JsObject(
                        "name" -> JsString("Televisions") ::
                          "categories" -> JsArray(
                            JsObject(
                              "name" -> JsString("LED TV") ::
                                Nil) ::
                              JsObject(
                                "name" -> JsString("LCD TV") ::
                                  Nil) ::
                                JsObject(
                                  "name" -> JsString("Plasma TV") ::
                                    Nil) ::
                                  JsObject(
                                    "name" -> JsString("Projectors") ::
                                      Nil) :: Nil) ::
                            Nil) ::
                        JsObject(
                          "name" -> JsString("Video Players") ::
                            "categories" -> JsArray(
                              JsObject(
                                "name" -> JsString("Blu-ray & DVD Players") ::
                                  Nil) ::
                                JsObject(
                                  "name" -> JsString("Digital Media Devices") ::
                                    Nil) ::
                                  JsObject(
                                    "name" -> JsString("DVD/VCR Combos") ::
                                      Nil) ::
                                    JsObject(
                                      "name" -> JsString("DVD Recorders") ::
                                        Nil) :: Nil) ::
                              Nil) ::
                          JsObject(
                            "name" -> JsString("DVR / Tivo") ::
                              "categories" -> JsArray(
                                JsObject(
                                  "name" -> JsString("Digital Video (DVR) Recorders") ::
                                    Nil) :: Nil) ::
                                Nil) ::
                            JsObject(
                              "name" -> JsString("Satellite / HDTV Receivers") ::
                                "categories" -> JsArray(
                                  JsObject(
                                    "name" -> JsString("Satellite Receivers") ::
                                      Nil) ::
                                    JsObject(
                                      "name" -> JsString("Digital Converters") ::
                                        Nil) :: Nil) ::
                                  Nil) :: Nil) :: Nil) ::
                  JsObject(
                    "name" -> JsString("Communications") ::
                      "categories" -> JsArray(
                        JsObject(
                          "name" -> JsString("Phones") ::
                            "categories" -> JsArray(
                              JsObject(
                                "name" -> JsString("Phones") ::
                                  Nil) ::
                                JsObject(
                                  "name" -> JsString("Cellular Phones") ::
                                    Nil) :: Nil) ::
                              Nil) ::
                          JsObject(
                            "name" -> JsString("Accessories") ::
                              "categories" -> JsArray(
                                JsObject(
                                  "name" -> JsString("Communication Accessories") ::
                                    Nil) ::
                                  JsObject(
                                    "name" -> JsString("Cellular Accessories") ::
                                      Nil) ::
                                    JsObject(
                                      "name" -> JsString("Cordless Phone Accessories") ::
                                        Nil) ::
                                      JsObject(
                                        "name" -> JsString("Fax Accessories") ::
                                          Nil) ::
                                        JsObject(
                                          "name" -> JsString("Batteries") ::
                                            Nil) :: Nil) ::
                                Nil) ::
                            JsObject(
                              "name" -> JsString("Buying Guides") ::
                                "categories" -> JsArray(
                                  JsObject(
                                    "name" -> JsString("Bluetooth") ::
                                      Nil) ::
                                    JsObject(
                                      "name" -> JsString("Cell Phones") ::
                                        Nil) ::
                                      JsObject(
                                        "name" -> JsString("Telephones (Land lines)") ::
                                          Nil) :: Nil) ::
                                  Nil) ::
                              JsObject(
                                "name" -> JsString("Trade In Your Gear") ::
                                  "categories" -> JsArray(
                                    JsObject(
                                      "name" -> JsString("Trade-In Program") ::
                                        Nil) :: Nil) ::
                                    Nil) :: Nil) :: Nil) :: Nil) :: Nil) :: Nil) :: Nil) :: Nil)

}