package controllers

import play.api._
import play.api.mvc._
import models.Address
import play.api.data._
import play.api.data.Forms._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready.", findRoutesForm))
  }

  def addressMapping: Mapping[Address] = mapping(
    "street" -> text,
    "number" -> text,
    "city" -> text,
    "zipCode" -> text,
    "county" -> text,
    "country" -> text
  )(Address.apply)(Address.unapply)

  lazy val findRoutesForm = {
    Form(
      tuple(
        "from" -> addressMapping,
        "to" -> addressMapping
      )
    )
  }

  def findRoutes = Action { implicit request => {

    findRoutesForm.bindFromRequest().fold(
      formWithErrors  => BadRequest(views.html.index("Your new application is ready.", formWithErrors)),
      addresses => {
        val (from, to) = addresses
        val (fromLatLong, toLatLong) = (from.latLong, to.latLong)

        //todo find a route between fromLatLong and toLatLong

        Ok("todo")
      }
    )

  }
  }

}