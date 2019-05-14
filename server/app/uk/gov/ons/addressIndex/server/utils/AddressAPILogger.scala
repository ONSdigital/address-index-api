package uk.gov.ons.addressIndex.server.utils

import play.api.Logger
import uk.gov.ons.addressIndex.server.modules._
import views.html.helper.input

import scala.reflect.ClassTag

class AddressAPILogger(log: String) extends APILogger {

  override def logName: String = log

  // Splunk is the current system logger
  private val splunk = Logger("SPLUNK")

  def systemLog(ip: String = "", url: String = "", responseTimeMillis: String = "",
                uprn: String = "", postcode: String = "", random: String = "", partialAddress: String = "", input: String = "",
                fallback: Boolean = true,
                offset: String = "", limit: String = "", filter: String = "", verbose: Boolean = false,
                historical: Boolean = true,
                epoch: String = "",
                rangekm: String = "", lat: String = "", lon: String = "", bulkSize: String = "",
                batchSize: String = "", badRequestMessage: String = "", isNotFound: Boolean = false,
                formattedOutput: String = "", numOfResults: String = "", score: String = "",
                endpoint: String = "", activity: String = "", uuid: String = "",
                networkid: String = "", organisation: String = "", clusterid: String = ""): Unit = {

    // Note we are using the info level for Splunk
    super.logMessage(splunk.info, AddressLoggerMessage(
      s" IP=$ip url=$url millis=${System.currentTimeMillis()} " +
        s"response_time_millis=$responseTimeMillis is_uprn=${!uprn.isEmpty} " +
        s"is_postcode=${!postcode.isEmpty} is_input=${!input.isEmpty} is_random=${!random.isEmpty} " +
        s"is_bulk=${!bulkSize.isEmpty} is_partial=${!partialAddress.isEmpty} " +
        s"uprn=$uprn postcode=$postcode input=$input " +
        s"fallback=$fallback" +
        s"offset=$offset limit=$limit filter=$filter " + s"verbose=$verbose " +
        s"partialAddress=$partialAddress " + s"historical=$historical " + s"epoch=$epoch " +
        s"rangekm=$rangekm lat=$lat lon=$lon " +
        s"bulk_size=$bulkSize batch_size=$batchSize " +
        s"bad_request_message=$badRequestMessage is_not_found=$isNotFound " +
        s"formattedOutput=${formattedOutput.replaceAll("""\s""", "_")} " +
        s"numOfResults=$numOfResults score=$score endpoint=$endpoint " +
        s"activity=$activity uuid=$uuid networkid=$networkid organisation=$organisation " +
        s"clusterid=$clusterid "))
  }

  def systemLogNew(args: Option[QueryArgs] = None,
                   ip: String = "",
                   url: String = "",
                   responseTimeMillis: String = "",
                   bulkSize: String = "",
                   batchSize: String = "",
                   isNotFound: Boolean = false,
                   endpoint: String = "",
                   uuid: String = "",
                   authVal: String = "",
                   clusterId: String = ""
                  )(
                    badRequestErrorMessage: String = "",
                    formattedOutput: String = "",
                    numOfResults: String = "",
                    score: String = "",
                    activity: String = ""
                  ): Unit = {
    def asArgType[T: ClassTag](args: Option[QueryArgs]): Option[T] = args collect { case m: T => m }

    // Note we are using the info level for Splunk
    // val filterDateRange = asInstanceOfOption[DateFilterable](args)
    val uprnArgs = asArgType[UPRNArgs](args)
    val partialArgs = asArgType[PartialArgs](args)
    val postcodeArgs = asArgType[PostcodeArgs](args)
    val randomArgs = asArgType[RandomArgs](args)
    val addressArgs = asArgType[AddressArgs](args)
    val region = addressArgs.flatMap(_.region)
    val bulkArgs = asArgType[BulkArgs](args)

    val (networkId, organisation) = if (authVal != "") {
      if (authVal.indexOf("+") > 0) {
        val networkId = authVal.split("\\+")(0)
        (networkId, networkId.split("_")(1))
      } else {
        (authVal.split("_")(0), "not set")
      }
    } else ("", "")

    super.logMessage(splunk.info, AddressLoggerMessage(
      s"""
         |IP=$ip
         |url=$url
         |millis=${System.currentTimeMillis()}
         |response_time_millis=$responseTimeMillis
         |is_uprn=${uprnArgs.nonEmpty} uprn=${uprnArgs.map(_.uprn).getOrElse("")}
         |is_partial=${partialArgs.nonEmpty} partialAddress=${partialArgs.map(_.input).getOrElse("")}
         |is_postcode=${postcodeArgs.nonEmpty} postcode=${postcodeArgs.map(_.postcode).getOrElse("")}
         |is_random=${randomArgs.nonEmpty}
         |is_input=${addressArgs.nonEmpty} input=$input tokens=${addressArgs.map(_.tokens).getOrElse("")}
         |rangekm=${region.map(_.range.toString)} lat=${region.map(_.lat.toString)} lon=${region.map(_.lon.toString)}
         |is_bulk=${bulkArgs.nonEmpty} bulk_size=$bulkSize
         |limit=${asArgType[Limitable](args).map(_.limit.toString).getOrElse("")}
         |offset=${asArgType[StartAtOffset](args).map(_.start.toString).getOrElse("")}
         |filter=${asArgType[Filterable](args).map(_.filters).getOrElse("")}
         |verbose=${asArgType[Verboseable](args).exists(_.verbose)}
         |is_skinny=${asArgType[Skinnyable](args).exists(_.skinny)}
         |historical=${args.map(_.historical).getOrElse("")} epoch=${args.map(_.epoch).getOrElse("")}
         |batch_size=$batchSize
         |bad_request_message=$badRequestErrorMessage
         |is_not_found=$isNotFound
         |formattedOutput=${formattedOutput.replaceAll("\\s", "_")}
         |numOfResults=$numOfResults
         |score=$score
         |endpoint=$endpoint
         |activity=$activity
         |uuid=$uuid
         |networkid=$networkId
         |organisation=$organisation
         |clusterid=$clusterId
      """.stripMargin.replaceAll("\n", " ")))
  }
}

object AddressAPILogger {

  def apply(name: String): AddressAPILogger = {
    new AddressAPILogger(name)
  }

}
