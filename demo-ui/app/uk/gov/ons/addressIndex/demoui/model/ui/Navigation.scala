package uk.gov.ons.addressIndex.demoui.model.ui

import play.api.i18n.Messages

case class Navigation(links: Seq[Link])

case class Link(href: String, label: String, section: String)

object Links {
  def single(implicit messages: Messages): Link = {
    Link(
      href = uk.gov.ons.addressIndex.demoui.controllers.routes.SingleMatchController.showSingleMatchPage().toString,
      label = messages("navbar.singlematch"),
      section = "single"
    )
  }

  def postcode(implicit messages: Messages): Link = {
    Link(
      href = uk.gov.ons.addressIndex.demoui.controllers.routes.PostcodeController.showPostcodeMatchPage().toString,
      label = messages("navbar.postcode"),
      section = "postcode"
    )
  }

  def multi(implicit messages: Messages): Link = {
    Link(
      href = uk.gov.ons.addressIndex.demoui.controllers.routes.BulkMatchController.bulkMatchPage().toString,
      label = messages("navbar.multimatch"),
      section = "multi"
    )
  }

  def clerical(implicit messages: Messages): Link = {
    Link(
      href = uk.gov.ons.addressIndex.demoui.controllers.routes.ClericalToolController.showSingleMatchPage().toString,
      label = messages("navbar.clerical"),
      section = "clerical"
    )
  }

  def query(implicit messages: Messages): Link = {
    Link(
      href = uk.gov.ons.addressIndex.demoui.controllers.routes.ClericalToolController.showQuery().toString,
      label = messages("navbar.query"),
      section = "debug"
    )
  }

  def help(implicit messages: Messages): Link = {
    Link(
      href = uk.gov.ons.addressIndex.demoui.controllers.routes.StaticController.help().toString,
      label = messages("navbar.help"),
      section = "help"
    )
  }

  def radius(implicit messages: Messages): Link = {
    Link(
      href = uk.gov.ons.addressIndex.demoui.controllers.routes.RadiusController.showRadiusMatchPage().toString,
      label = messages("navbar.radius"),
      section = "radius"
    )
  }

  def developers(implicit messages: Messages): Link = {
    Link(
      href = uk.gov.ons.addressIndex.demoui.controllers.routes.StaticController.devLanding().toString,
      label = messages("navbar.developers"),
      section = "developers"
    )
  }
}

object Navigation {
  def default(implicit messages: Messages): Navigation = {
    Navigation(
      links = Seq(
        Links.single,
        Links.postcode,
        Links.multi
      )
    )
  }

  def footer(implicit messages: Messages): Navigation = {
    Navigation(
      links = Seq(
        Links.radius,
        Links.clerical,
        Links.query,
        Links.help,
        Links.developers
      )
    )
  }
}
