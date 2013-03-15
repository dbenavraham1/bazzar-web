package controllers

import play.api.mvc._
import play.api.data.Forms._
import java.io.FileReader
import java.io.InputStreamReader
import java.io.FileInputStream
import java.io.BufferedReader
import java.io.File
import play.Play
import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer
import scala.collection.immutable.Set
import play.Logger
import play.api.libs.ws._
import play.api.libs.json._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object ProductUpload extends Controller with ProvidesHeader {
  //I have hard code here the file path
  val filePath = "/conf/data/"

  //Handling default requests. to load product form
  def index = Action { implicit request =>
    Ok(views.html.productUpload(""))
  }

  //
  def upload = Action(parse.multipartFormData) { request =>
    request.body.file("fileupload").map { file =>
      //moving file to application
      file.ref.moveTo(new File(Play.application.path +
        filePath + file.filename), true)

      //uploading products
      val job: JsValue = uploadProducts(file.filename)

      val message: String = "Products uploaded successfully !!! with json response: " + job.toString();
      //send message
      Redirect(routes.ProductUpload.index).
        flashing("message" -> message)
    }.getOrElse {
      //send error message
      Redirect(routes.ProductUpload.index).
        flashing("errormessage" -> "File Missing")
    }
  }

  //function to upload products
  def uploadProducts(fileName: String) = {
    // Submit products import job
    var jobParams = JsObject(
      "importItemDownlaodUrl" -> JsString("http://localhost:9000/downloadProducts/" + fileName) ::
        Nil)
    val result: Future[JsValue] =
      WS.url("http://localhost:8080/bazzar_online/item/import/job")
        .withTimeout(2000)
        .post(jobParams)
        .map { response => response.json }
    Await.result(result, Duration.Inf)
  }

  //Handling product file downloads
  def downloadProducts(fileName: String) = Action { implicit request =>
    Ok.sendFile(
      content = Play.application.getFile(filePath + fileName),
      fileName = _ => fileName)
  }

}
