package uk.gov.ons.addressIndex.model.db.index


import scala.util.Try

/**
  * NAG Address DTO
  */
case class NationalAddressGazetteerAddress (
  uprn: String,
  postcodeLocator: String,
  addressBasePostal: String,
  latitude: String,
  longitude: String,
  easting: String,
  northing: String,
  organisation: String,
  legalName: String,
  classificationCode: String,
  usrn: String,
  lpiKey: String,
  paoText: String,
  paoStartNumber: String,
  paoStartSuffix: String,
  paoEndNumber: String,
  paoEndSuffix: String,
  saoText: String,
  saoStartNumber: String,
  saoStartSuffix: String,
  saoEndNumber: String,
  saoEndSuffix: String,
  level: String,
  officialFlag: String,
  streetDescriptor: String,
  townName: String,
  locality: String,
  lpiLogicalStatus: String,
  blpuLogicalStatus: String,
  source: String,
  usrnMatchIndicator: String,
  parentUprn: String,
  crossReference: String,
  streetClassification: String,
  multiOccCount: String,
  language: String,
  classScheme: String,
  localCustodianCode: String,
  localCustodianName: String,
  localCustodianGeogCode: String,
  rpc: String,
  nagAll: String,
  lpiEndDate: String
)

/**
  * NAF Address DTO companion object that also contains implicits needed for Elastic4s
  */
object NationalAddressGazetteerAddress {

  object Fields {

    /**
      * Document Fields
      */
    val uprn: String = "uprn"
    val postcodeLocator: String = "postcodeLocator"
    val addressBasePostal: String = "addressBasePostal"
    val easting: String = "easting"
    val northing: String = "northing"
    val organisation: String = "organisation"
    val legalName: String = "legalName"
    val classificationCode: String = "classificationCode"
    val usrn: String = "usrn"
    val lpiKey: String = "lpiKey"
    val paoText: String = "paoText"
    val paoStartNumber: String = "paoStartNumber"
    val paoStartSuffix: String = "paoStartSuffix"
    val paoEndNumber: String = "paoEndNumber"
    val paoEndSuffix: String = "paoEndSuffix"
    val saoText: String = "saoText"
    val saoStartNumber: String = "saoStartNumber"
    val saoStartSuffix: String = "saoStartSuffix"
    val saoEndNumber: String = "saoEndNumber"
    val saoEndSuffix: String = "saoEndSuffix"
    val level: String = "level"
    val officialFlag: String = "officialFlag"
    val streetDescriptor: String = "streetDescriptor"
    val townName: String = "townName"
    val locality: String = "locality"
    val lpiLogicalStatus: String = "lpiLogicalStatus"
    val blpuLogicalStatus: String = "blpuLogicalStatus"
    val source: String = "source"
    val usrnMatchIndicator: String = "usrnMatchIndicator"
    val parentUprn: String = "parentUprn"
    val crossReference: String = "crossReference"
    val streetClassification: String = "streetClassification"
    val multiOccCount: String = "multiOccCount"
    val location: String = "location"
    val language: String = "language"
    val classScheme: String = "classScheme"
    val localCustodianCode: String = "localCustodianCode"
    val localCustodianName: String = "localCustodianName"
    val localCustodianGeogCode: String = "localCustodianGeogCode"
    val rpc: String = "rpc"
    val nagAll: String = "nagAll"
    val lpiEndDate: String = "lpiEndDate"
  }

  object Languages {
    val english: String = "ENG"
    val welsh: String = "CYM"
  }

  def fromEsMap (nag: Map[String, AnyRef]): NationalAddressGazetteerAddress = {
    val filteredNag = nag.filter{ case (_, value) => value != null }

    val matchLocationRegex = """-?\d+\.\d+""".r
    val location = filteredNag.getOrElse(Fields.location, "").toString

    val Array(longitude, latitude) = Try(matchLocationRegex.findAllIn(location).toArray).getOrElse(Array("0", "0"))

    NationalAddressGazetteerAddress (
      uprn = filteredNag.getOrElse(Fields.uprn, "").toString,
      postcodeLocator = filteredNag.getOrElse(Fields.postcodeLocator, "").toString,
      addressBasePostal = filteredNag.getOrElse(Fields.addressBasePostal, "").toString,
      latitude = latitude,
      longitude = longitude,
      easting = filteredNag.getOrElse(Fields.easting, "").toString,
      northing = filteredNag.getOrElse(Fields.northing, "").toString,
      organisation = filteredNag.getOrElse(Fields.organisation, "").toString,
      legalName = filteredNag.getOrElse(Fields.legalName, "").toString,
      classificationCode = filteredNag.getOrElse(Fields.classificationCode, "").toString,
      usrn = filteredNag.getOrElse(Fields.usrn, "").toString,
      lpiKey = filteredNag.getOrElse(Fields.lpiKey, "").toString,
      paoText = filteredNag.getOrElse(Fields.paoText, "").toString,
      paoStartNumber = filteredNag.getOrElse(Fields.paoStartNumber, "").toString,
      paoStartSuffix = filteredNag.getOrElse(Fields.paoStartSuffix, "").toString,
      paoEndNumber = filteredNag.getOrElse(Fields.paoEndNumber, "").toString,
      paoEndSuffix = filteredNag.getOrElse(Fields.paoEndSuffix, "").toString,
      saoText = filteredNag.getOrElse(Fields.saoText, "").toString,
      saoStartNumber = filteredNag.getOrElse(Fields.saoStartNumber, "").toString,
      saoStartSuffix = filteredNag.getOrElse(Fields.saoStartSuffix, "").toString,
      saoEndNumber = filteredNag.getOrElse(Fields.saoEndNumber, "").toString,
      saoEndSuffix = filteredNag.getOrElse(Fields.saoEndSuffix, "").toString,
      level = filteredNag.getOrElse(Fields.level, "").toString,
      officialFlag = filteredNag.getOrElse(Fields.officialFlag, "").toString,
      streetDescriptor = filteredNag.getOrElse(Fields.streetDescriptor, "").toString,
      townName = filteredNag.getOrElse(Fields.townName, "").toString,
      locality = filteredNag.getOrElse(Fields.locality, "").toString,
      lpiLogicalStatus = filteredNag.getOrElse(Fields.lpiLogicalStatus, "").toString,
      blpuLogicalStatus = filteredNag.getOrElse(Fields.blpuLogicalStatus, "").toString,
      source = filteredNag.getOrElse(Fields.source, "").toString,
      usrnMatchIndicator = filteredNag.getOrElse(Fields.usrnMatchIndicator, "").toString,
      parentUprn = filteredNag.getOrElse(Fields.parentUprn, "").toString,
      crossReference = filteredNag.getOrElse(Fields.crossReference, "").toString,
      streetClassification = filteredNag.getOrElse(Fields.streetClassification, "").toString,
      multiOccCount = filteredNag.getOrElse(Fields.multiOccCount, "").toString,
      language = filteredNag.getOrElse(Fields.language, "").toString,
      classScheme = filteredNag.getOrElse(Fields.classScheme, "").toString,
      localCustodianCode = filteredNag.getOrElse(Fields.localCustodianCode, "").toString,
      localCustodianName = LocalCustodian.getLAName(filteredNag.getOrElse(Fields.localCustodianCode, "").toString),
      localCustodianGeogCode = LocalCustodian.getLACode(filteredNag.getOrElse(Fields.localCustodianCode, "").toString),
      rpc = filteredNag.getOrElse(Fields.rpc, "").toString,
      nagAll = filteredNag.getOrElse(Fields.nagAll, "").toString,
      lpiEndDate = filteredNag.getOrElse(Fields.lpiEndDate, "").toString
    )
  }
}

case class LocalCustodian (custodians: Map[String,String])


object LocalCustodian {

  def getLAName(code: String): String = {

    val custKey = "N" + code
    if(custodians.contains(custKey)) {
      custodians(custKey)
    } else {
      custKey
    }

  }

  def getLACode(code: String): String = {

    val custKey = "C" + code
    if(custodians.contains(custKey)) {
      custodians(custKey)
    } else {
      custKey
    }

  }

  val custodians = Map(
    "N114" -> "BATH AND NORTH EAST SOMERSET",
    "C114" -> "E06000022",
    "N116" -> "BRISTOL CITY",
    "C116" -> "E06000023",
    "N119" -> "SOUTH GLOUCESTERSHIRE",
    "C119" -> "E06000025",
    "N121" -> "NORTH SOMERSET",
    "C121" -> "E06000024",
    "N230" -> "LUTON",
    "C230" -> "E06000032",
    "N235" -> "BEDFORD",
    "C235" -> "E06000055",
    "N240" -> "CENTRAL BEDFORDSHIRE",
    "C240" -> "E06000056",
    "N335" -> "BRACKNELL FOREST",
    "C335" -> "E06000036",
    "N340" -> "WEST BERKSHIRE",
    "C340" -> "E06000037",
    "N345" -> "READING",
    "C345" -> "E06000038",
    "N350" -> "SLOUGH",
    "C350" -> "E06000039",
    "N355" -> "WINDSOR AND MAIDENHEAD",
    "C355" -> "E06000040",
    "N360" -> "WOKINGHAM",
    "C360" -> "E06000041",
    "N405" -> "AYLESBURY VALE",
    "C405" -> "E07000004",
    "N410" -> "SOUTH BUCKS",
    "C410" -> "E07000006",
    "N415" -> "CHILTERN",
    "C415" -> "E07000005",
    "N425" -> "WYCOMBE",
    "C425" -> "E07000007",
    "N435" -> "MILTON KEYNES",
    "C435" -> "E06000042",
    "N505" -> "CAMBRIDGE",
    "C505" -> "E07000008",
    "N510" -> "EAST CAMBRIDGESHIRE",
    "C510" -> "E07000009",
    "N515" -> "FENLAND",
    "C515" -> "E07000010",
    "N520" -> "HUNTINGDONSHIRE",
    "C520" -> "E07000011",
    "N530" -> "SOUTH CAMBRIDGESHIRE",
    "C530" -> "E07000012",
    "N540" -> "CITY OF PETERBOROUGH",
    "C540" -> "E06000031",
    "N650" -> "HALTON",
    "C650" -> "E06000006",
    "N655" -> "WARRINGTON",
    "C655" -> "E06000007",
    "N660" -> "CHESHIRE EAST",
    "C660" -> "E06000049",
    "N665" -> "CHESHIRE WEST AND CHESTER",
    "C665" -> "E06000050",
    "N724" -> "HARTLEPOOL",
    "C724" -> "E06000001",
    "N728" -> "REDCAR AND CLEVELAND",
    "C728" -> "E06000003",
    "N734" -> "MIDDLESBROUGH",
    "C734" -> "E06000002",
    "N738" -> "STOCKTON-ON-TEES",
    "C738" -> "E06000004",
    "N835" -> "ISLES OF SCILLY",
    "C835" -> "E06000053",
    "N840" -> "CORNWALL",
    "C840" -> "E06000052",
    "N905" -> "ALLERDALE",
    "C905" -> "E07000026",
    "N910" -> "BARROW-IN-FURNESS",
    "C910" -> "E07000027",
    "N915" -> "CARLISLE",
    "C915" -> "E07000028",
    "N920" -> "COPELAND",
    "C920" -> "E07000029",
    "N925" -> "EDEN",
    "C925" -> "E07000030",
    "N930" -> "SOUTH LAKELAND",
    "C930" -> "E07000031",
    "N1005" -> "AMBER VALLEY",
    "C1005" -> "E07000032",
    "N1010" -> "BOLSOVER",
    "C1010" -> "E07000033",
    "N1015" -> "CHESTERFIELD",
    "C1015" -> "E07000034",
    "N1025" -> "EREWASH",
    "C1025" -> "E07000036",
    "N1030" -> "HIGH PEAK",
    "C1030" -> "E07000037",
    "N1035" -> "NORTH EAST DERBYSHIRE",
    "C1035" -> "E07000038",
    "N1040" -> "SOUTH DERBYSHIRE",
    "C1040" -> "E07000039",
    "N1045" -> "DERBYSHIRE DALES",
    "C1045" -> "E07000035",
    "N1055" -> "CITY OF DERBY",
    "C1055" -> "E06000015",
    "N1105" -> "EAST DEVON",
    "C1105" -> "E07000040",
    "N1110" -> "EXETER",
    "C1110" -> "E07000041",
    "N1115" -> "NORTH DEVON",
    "C1115" -> "E07000043",
    "N1125" -> "SOUTH HAMS",
    "C1125" -> "E07000044",
    "N1130" -> "TEIGNBRIDGE",
    "C1130" -> "E07000045",
    "N1135" -> "MID DEVON",
    "C1135" -> "E07000042",
    "N1145" -> "TORRIDGE",
    "C1145" -> "E07000046",
    "N1150" -> "WEST DEVON",
    "C1150" -> "E07000047",
    "N1160" -> "CITY OF PLYMOUTH",
    "C1160" -> "E06000026",
    "N1165" -> "TORBAY",
    "C1165" -> "E06000027",
    "N1210" -> "CHRISTCHURCH",
    "C1210" -> "E07000048",
    "N1215" -> "NORTH DORSET",
    "C1215" -> "E07000050",
    "N1225" -> "PURBECK",
    "C1225" -> "E07000051",
    "N1230" -> "WEST DORSET",
    "C1230" -> "E07000052",
    "N1235" -> "WEYMOUTH AND PORTLAND",
    "C1235" -> "E07000053",
    "N1240" -> "EAST DORSET",
    "C1240" -> "E07000049",
    "N1250" -> "BOURNEMOUTH",
    "C1250" -> "E06000028",
    "N1255" -> "POOLE",
    "C1255" -> "E06000029",
    "N1350" -> "DARLINGTON",
    "C1350" -> "E06000005",
    "N1355" -> "DURHAM",
    "C1355" -> "E06000047",
    "N1410" -> "EASTBOURNE",
    "C1410" -> "E07000061",
    "N1415" -> "HASTINGS",
    "C1415" -> "E07000062",
    "N1425" -> "LEWES",
    "C1425" -> "E07000063",
    "N1430" -> "ROTHER",
    "C1430" -> "E07000064",
    "N1435" -> "WEALDEN",
    "C1435" -> "E07000065",
    "N1445" -> "BRIGHTON AND HOVE",
    "C1445" -> "E06000043",
    "N1505" -> "BASILDON",
    "C1505" -> "E07000066",
    "N1510" -> "BRAINTREE",
    "C1510" -> "E07000067",
    "N1515" -> "BRENTWOOD",
    "C1515" -> "E07000068",
    "N1520" -> "CASTLE POINT",
    "C1520" -> "E07000069",
    "N1525" -> "CHELMSFORD",
    "C1525" -> "E07000070",
    "N1530" -> "COLCHESTER",
    "C1530" -> "E07000071",
    "N1535" -> "EPPING FOREST",
    "C1535" -> "E07000072",
    "N1540" -> "HARLOW",
    "C1540" -> "E07000073",
    "N1545" -> "MALDON",
    "C1545" -> "E07000074",
    "N1550" -> "ROCHFORD",
    "C1550" -> "E07000075",
    "N1560" -> "TENDRING",
    "C1560" -> "E07000076",
    "N1570" -> "UTTLESFORD",
    "C1570" -> "E07000077",
    "N1590" -> "SOUTHEND-ON-SEA",
    "C1590" -> "E06000033",
    "N1595" -> "THURROCK",
    "C1595" -> "E06000034",
    "N1605" -> "CHELTENHAM",
    "C1605" -> "E07000078",
    "N1610" -> "COTSWOLD",
    "C1610" -> "E07000079",
    "N1615" -> "FOREST OF DEAN",
    "C1615" -> "E07000080",
    "N1620" -> "GLOUCESTER",
    "C1620" -> "E07000081",
    "N1625" -> "STROUD",
    "C1625" -> "E07000082",
    "N1630" -> "TEWKESBURY",
    "C1630" -> "E07000083",
    "N1705" -> "BASINGSTOKE AND DEANE",
    "C1705" -> "E07000084",
    "N1710" -> "EAST HAMPSHIRE",
    "C1710" -> "E07000085",
    "N1715" -> "EASTLEIGH",
    "C1715" -> "E07000086",
    "N1720" -> "FAREHAM",
    "C1720" -> "E07000087",
    "N1725" -> "GOSPORT",
    "C1725" -> "E07000088",
    "N1730" -> "HART",
    "C1730" -> "E07000089",
    "N1735" -> "HAVANT",
    "C1735" -> "E07000090",
    "N1740" -> "NEW FOREST",
    "C1740" -> "E07000091",
    "N1750" -> "RUSHMOOR",
    "C1750" -> "E07000092",
    "N1760" -> "TEST VALLEY",
    "C1760" -> "E07000093",
    "N1765" -> "WINCHESTER",
    "C1765" -> "E07000094",
    "N1775" -> "CITY OF PORTSMOUTH",
    "C1775" -> "E06000044",
    "N1780" -> "CITY OF SOUTHAMPTON",
    "C1780" -> "E06000045",
    "N1805" -> "BROMSGROVE",
    "C1805" -> "E07000234",
    "N1820" -> "MALVERN HILLS",
    "C1820" -> "E07000235",
    "N1825" -> "REDDITCH",
    "C1825" -> "E07000236",
    "N1835" -> "WORCESTER",
    "C1835" -> "E07000237",
    "N1840" -> "WYCHAVON",
    "C1840" -> "E07000238",
    "N1845" -> "WYRE FOREST",
    "C1845" -> "E07000239",
    "N1850" -> "HEREFORDSHIRE",
    "C1850" -> "E06000019",
    "N1905" -> "BROXBOURNE",
    "C1905" -> "E07000095",
    "N1910" -> "DACORUM",
    "C1910" -> "E07000096",
    "N1915" -> "EAST HERTFORDSHIRE",
    "C1915" -> "E07000097",
    "N1920" -> "HERTSMERE",
    "C1920" -> "E07000098",
    "N1925" -> "NORTH HERTFORDSHIRE",
    "C1925" -> "E07000099",
    "N1930" -> "ST ALBANS",
    "C1930" -> "E07000100",
    "N1935" -> "STEVENAGE",
    "C1935" -> "E07000101",
    "N1940" -> "THREE RIVERS",
    "C1940" -> "E07000102",
    "N1945" -> "WATFORD",
    "C1945" -> "E07000103",
    "N1950" -> "WELWYN HATFIELD",
    "C1950" -> "E07000104",
    "N2001" -> "EAST RIDING OF YORKSHIRE",
    "C2001" -> "E06000011",
    "N2002" -> "NORTH EAST LINCOLNSHIRE",
    "C2002" -> "E06000012",
    "N2003" -> "NORTH LINCOLNSHIRE",
    "C2003" -> "E06000013",
    "N2004" -> "CITY OF KINGSTON UPON HULL",
    "C2004" -> "E06000010",
    "N2114" -> "ISLE OF WIGHT",
    "C2114" -> "E06000046",
    "N2205" -> "ASHFORD",
    "C2205" -> "E07000105",
    "N2210" -> "CANTERBURY",
    "C2210" -> "E07000106",
    "N2215" -> "DARTFORD",
    "C2215" -> "E07000107",
    "N2220" -> "DOVER",
    "C2220" -> "E07000108",
    "N2230" -> "GRAVESHAM",
    "C2230" -> "E07000109",
    "N2235" -> "MAIDSTONE",
    "C2235" -> "E07000110",
    "N2245" -> "SEVENOAKS",
    "C2245" -> "E07000111",
    "N2250" -> "SHEPWAY",
    "C2250" -> "E07000112",
    "N2255" -> "SWALE",
    "C2255" -> "E07000113",
    "N2260" -> "THANET DISTRICT",
    "C2260" -> "E07000114",
    "N2265" -> "TONBRIDGE AND MALLING",
    "C2265" -> "E07000115",
    "N2270" -> "TUNBRIDGE WELLS",
    "C2270" -> "E07000116",
    "N2280" -> "MEDWAY",
    "C2280" -> "E06000035",
    "N2315" -> "BURNLEY",
    "C2315" -> "E07000117",
    "N2320" -> "CHORLEY",
    "C2320" -> "E07000118",
    "N2325" -> "FYLDE",
    "C2325" -> "E07000119",
    "N2330" -> "HYNDBURN",
    "C2330" -> "E07000120",
    "N2335" -> "LANCASTER CITY",
    "C2335" -> "E07000121",
    "N2340" -> "PENDLE",
    "C2340" -> "E07000122",
    "N2345" -> "PRESTON",
    "C2345" -> "E07000123",
    "N2350" -> "RIBBLE VALLEY",
    "C2350" -> "E07000124",
    "N2355" -> "ROSSENDALE",
    "C2355" -> "E07000125",
    "N2360" -> "SOUTH RIBBLE",
    "C2360" -> "E07000126",
    "N2365" -> "WEST LANCASHIRE",
    "C2365" -> "E07000127",
    "N2370" -> "WYRE",
    "C2370" -> "E07000128",
    "N2372" -> "BLACKBURN",
    "C2372" -> "E06000008",
    "N2373" -> "BLACKPOOL",
    "C2373" -> "E06000009",
    "N2405" -> "BLABY",
    "C2405" -> "E07000129",
    "N2410" -> "CHARNWOOD",
    "C2410" -> "E07000130",
    "N2415" -> "HARBOROUGH",
    "C2415" -> "E07000131",
    "N2420" -> "HINCKLEY AND BOSWORTH",
    "C2420" -> "E07000132",
    "N2430" -> "MELTON",
    "C2430" -> "E07000133",
    "N2435" -> "NORTH WEST LEICESTERSHIRE",
    "C2435" -> "E07000134",
    "N2440" -> "OADBY AND WIGSTON",
    "C2440" -> "E07000135",
    "N2465" -> "CITY OF LEICESTER",
    "C2465" -> "E06000016",
    "N2470" -> "RUTLAND",
    "C2470" -> "E06000017",
    "N2505" -> "BOSTON",
    "C2505" -> "E07000136",
    "N2510" -> "EAST LINDSEY",
    "C2510" -> "E07000137",
    "N2515" -> "LINCOLN",
    "C2515" -> "E07000138",
    "N2520" -> "NORTH KESTEVEN",
    "C2520" -> "E07000139",
    "N2525" -> "SOUTH HOLLAND",
    "C2525" -> "E07000140",
    "N2530" -> "SOUTH KESTEVEN",
    "C2530" -> "E07000141",
    "N2535" -> "WEST LINDSEY",
    "C2535" -> "E07000142",
    "N2605" -> "BRECKLAND",
    "C2605" -> "E07000143",
    "N2610" -> "BROADLAND",
    "C2610" -> "E07000144",
    "N2615" -> "GREAT YARMOUTH",
    "C2615" -> "E07000145",
    "N2620" -> "NORTH NORFOLK",
    "C2620" -> "E07000147",
    "N2625" -> "NORWICH",
    "C2625" -> "E07000148",
    "N2630" -> "SOUTH NORFOLK",
    "C2630" -> "E07000149",
    "N2635" -> "KINGS LYNN AND WEST NORFOLK",
    "C2635" -> "E07000146",
    "N2705" -> "CRAVEN",
    "C2705" -> "E07000163",
    "N2710" -> "HAMBLETON",
    "C2710" -> "E07000164",
    "N2715" -> "HARROGATE",
    "C2710" -> "E07000165",
    "N2720" -> "RICHMONDSHIRE",
    "C2720" -> "E07000166",
    "N2725" -> "RYEDALE",
    "C2725" -> "E07000167",
    "N2730" -> "SCARBOROUGH",
    "C2730" -> "E07000168",
    "N2735" -> "SELBY",
    "C2735" -> "E07000169",
    "N2741" -> "YORK",
    "C2741" -> "E06000014",
    "N2805" -> "CORBY",
    "C2805" -> "E07000150",
    "N2810" -> "DAVENTRY",
    "C2810" -> "E07000151",
    "N2815" -> "EAST NORTHAMPTONSHIRE",
    "C2815" -> "E07000152",
    "N2820" -> "KETTERING",
    "C2820" -> "E07000153",
    "N2825" -> "NORTHAMPTON",
    "C2825" -> "E07000154",
    "N2830" -> "SOUTH NORTHAMPTONSHIRE",
    "C2830" -> "E07000155",
    "N2835" -> "WELLINGBOROUGH",
    "C2835" -> "E07000156",
    "N2935" -> "NORTHUMBERLAND",
    "C2935" -> "E06000048",
    "N3005" -> "ASHFIELD",
    "C3005" -> "E07000170",
    "N3010" -> "BASSETLAW",
    "C3010" -> "E07000171",
    "N3015" -> "BROXTOWE",
    "C3015" -> "E07000172",
    "N3020" -> "GEDLING",
    "C3020" -> "E07000173",
    "N3025" -> "MANSFIELD",
    "C3025" -> "E07000174",
    "N3030" -> "NEWARK AND SHERWOOD",
    "C3030" -> "E07000175",
    "N3040" -> "RUSHCLIFFE",
    "C3040" -> "E07000176",
    "N3060" -> "NOTTINGHAM CITY",
    "C3060" -> "E06000018",
    "N3105" -> "CHERWELL",
    "C3105" -> "E07000177",
    "N3110" -> "OXFORD",
    "C3110" -> "E07000178",
    "N3115" -> "SOUTH OXFORDSHIRE",
    "C3115" -> "E07000179",
    "N3120" -> "VALE OF WHITE HORSE",
    "C3120" -> "E07000180",
    "N3125" -> "WEST OXFORDSHIRE",
    "C3125" -> "E07000181",
    "N3240" -> "TELFORD",
    "C3240" -> "E06000020",
    "N3245" -> "SHROPSHIRE",
    "C3245" -> "E06000051",
    "N3305" -> "MENDIP",
    "C3305" -> "E07000187",
    "N3310" -> "SEDGEMOOR",
    "C3310" -> "E07000188",
    "N3315" -> "TAUNTON DEANE",
    "C3315" -> "E07000190",
    "N3320" -> "WEST SOMERSET",
    "C3320" -> "E07000191",
    "N3325" -> "SOUTH SOMERSET",
    "C3325" -> "E07000189",
    "N3405" -> "CANNOCK CHASE",
    "C3405" -> "E07000192",
    "N3410" -> "EAST STAFFORDSHIRE",
    "C3410" -> "E07000193",
    "N3415" -> "LICHFIELD",
    "C3415" -> "E07000194",
    "N3420" -> "NEWCASTLE-UNDER-LYME",
    "C3420" -> "E07000195",
    "N3425" -> "STAFFORD",
    "C3425" -> "E07000197",
    "N3430" -> "SOUTH STAFFORDSHIRE",
    "C3430" -> "E07000196",
    "N3435" -> "STAFFORDSHIRE MOORLANDS",
    "C3435" -> "E07000198",
    "N3445" -> "TAMWORTH",
    "C3445" -> "E07000199",
    "N3455" -> "CITY OF STOKE-ON-TRENT",
    "C3455" -> "E06000021",
    "N3505" -> "BABERGH",
    "C3505" -> "E07000200",
    "N3510" -> "FOREST HEATH",
    "C3510" -> "E07000201",
    "N3515" -> "IPSWICH",
    "C3515" -> "E07000202",
    "N3520" -> "MID SUFFOLK",
    "C3520" -> "E07000203",
    "N3525" -> "ST EDMUNDSBURY",
    "C3525" -> "E07000204",
    "N3530" -> "SUFFOLK COASTAL",
    "C3530" -> "E07000205",
    "N3535" -> "WAVENEY",
    "C3535" -> "E07000206",
    "N3605" -> "ELMBRIDGE",
    "C3605" -> "E07000207",
    "N3610" -> "EPSOM AND EWELL",
    "C3610" -> "E07000208",
    "N3615" -> "GUILDFORD",
    "C3615" -> "E07000209",
    "N3620" -> "MOLE VALLEY",
    "C3620" -> "E07000210",
    "N3625" -> "REIGATE AND BANSTEAD",
    "C3625" -> "E07000211",
    "N3630" -> "RUNNYMEDE",
    "C3630" -> "E07000212",
    "N3635" -> "SPELTHORNE",
    "C3635" -> "E07000213",
    "N3640" -> "SURREY HEATH",
    "C3640" -> "E07000214",
    "N3645" -> "TANDRIDGE",
    "C3645" -> "E07000215",
    "N3650" -> "WAVERLEY",
    "C3650" -> "E07000216",
    "N3655" -> "WOKING",
    "C3655" -> "E07000217",
    "N3705" -> "NORTH WARWICKSHIRE",
    "C3705" -> "E07000218",
    "N3710" -> "NUNEATON AND BEDWORTH",
    "C3710" -> "E07000219",
    "N3715" -> "RUGBY",
    "C3715" -> "E07000220",
    "N3720" -> "STRATFORD-ON-AVON",
    "C3720" -> "E07000221",
    "N3725" -> "WARWICK",
    "C3725" -> "E07000222",
    "N3805" -> "ADUR",
    "C3805" -> "E07000223",
    "N3810" -> "ARUN",
    "C3810" -> "E07000224",
    "N3815" -> "CHICHESTER",
    "C3815" -> "E07000225",
    "N3820" -> "CRAWLEY",
    "C3820" -> "E07000226",
    "N3825" -> "HORSHAM",
    "C3825" -> "E07000227",
    "N3830" -> "MID SUSSEX",
    "C3830" -> "E07000228",
    "N3835" -> "WORTHING",
    "C3835" -> "E07000229",
    "N3935" -> "SWINDON",
    "C3935" -> "E06000030",
    "N3940" -> "WILTSHIRE",
    "C3940" -> "E06000054",
    "N4205" -> "BOLTON",
    "C4205" -> "E08000001",
    "N4210" -> "BURY",
    "C4210" -> "E08000002",
    "N4215" -> "MANCHESTER",
    "C4215" -> "E08000003",
    "N4220" -> "OLDHAM",
    "C4220" -> "E08000004",
    "N4225" -> "ROCHDALE",
    "C4225" -> "E08000005",
    "N4230" -> "SALFORD",
    "C4230" -> "E08000006",
    "N4235" -> "STOCKPORT",
    "C4235" -> "E08000007",
    "N4240" -> "TAMESIDE",
    "C4240" -> "E08000008",
    "N4245" -> "TRAFFORD",
    "C4245" -> "E08000009",
    "N4250" -> "WIGAN",
    "C4250" -> "E08000010",
    "N4305" -> "KNOWSLEY",
    "C4305" -> "E08000011",
    "N4310" -> "LIVERPOOL",
    "C4310" -> "E08000012",
    "N4315" -> "ST HELENS",
    "C4315" -> "E08000013",
    "N4320" -> "SEFTON",
    "C4320" -> "E08000014",
    "N4325" -> "WIRRAL",
    "C4325" -> "E08000015",
    "N4405" -> "BARNSLEY",
    "C4405" -> "E08000016",
    "N4410" -> "DONCASTER",
    "C4410" -> "E08000017",
    "N4415" -> "ROTHERHAM",
    "C4415" -> "E08000018",
    "N4420" -> "SHEFFIELD",
    "C4420" -> "E08000019",
    "N4505" -> "GATESHEAD",
    "C4505" -> "E08000020",
    "N4510" -> "NEWCASTLE CITY",
    "C4510" -> "E08000021",
    "N4515" -> "NORTH TYNESIDE",
    "C4515" -> "E08000022",
    "N4520" -> "SOUTH TYNESIDE",
    "C4520" -> "E08000023",
    "N4525" -> "SUNDERLAND",
    "C4525" -> "E08000024",
    "N4605" -> "BIRMINGHAM",
    "C4605" -> "E08000025",
    "N4610" -> "COVENTRY",
    "C4610" -> "E08000026",
    "N4615" -> "DUDLEY",
    "C4615" -> "E08000027",
    "N4620" -> "SANDWELL",
    "C4620" -> "E08000028",
    "N4625" -> "SOLIHULL",
    "C4625" -> "E08000029",
    "N4630" -> "WALSALL",
    "C4630" -> "E08000030",
    "N4635" -> "WOLVERHAMPTON",
    "C4635" -> "E08000031",
    "N4705" -> "BRADFORD",
    "C4705" -> "E08000032",
    "N4710" -> "CALDERDALE",
    "C4710" -> "E08000033",
    "N4715" -> "KIRKLEES",
    "C4715" -> "E08000034",
    "N4720" -> "LEEDS",
    "C4720" -> "E08000035",
    "N4725" -> "WAKEFIELD",
    "C4725" -> "E08000036",
    "N5030" -> "CITY OF LONDON",
    "C5030" -> "E09000001",
    "N5060" -> "BARKING AND DAGENHAM",
    "C5060" -> "E09000002",
    "N5090" -> "BARNET",
    "C5090" -> "E09000003",
    "N5120" -> "BEXLEY",
    "C5120" -> "E09000004",
    "N5150" -> "BRENT",
    "C5150" -> "E09000005",
    "N5180" -> "BROMLEY",
    "C5180" -> "E09000006",
    "N5210" -> "CAMDEN",
    "C5210" -> "E09000007",
    "N5240" -> "CROYDON",
    "C5240" -> "E09000008",
    "N5270" -> "EALING",
    "C5270" -> "E09000009",
    "N5300" -> "ENFIELD",
    "C5300" -> "E09000010",
    "N5330" -> "GREENWICH",
    "C5330" -> "E09000011",
    "N5360" -> "HACKNEY",
    "C5360" -> "E09000012",
    "N5390" -> "HAMMERSMITH AND FULHAM",
    "C5390" -> "E09000013",
    "N5420" -> "HARINGEY",
    "C5420" -> "E09000014",
    "N5450" -> "HARROW",
    "C5450" -> "E09000015",
    "N5480" -> "HAVERING",
    "C5480" -> "E09000016",
    "N5510" -> "HILLINGDON",
    "C5510" -> "E09000017",
    "N5540" -> "HOUNSLOW",
    "C5540" -> "E09000018",
    "N5570" -> "ISLINGTON",
    "C5570" -> "E09000019",
    "N5600" -> "KENSINGTON AND CHELSEA",
    "C5600" -> "E09000020",
    "N5630" -> "KINGSTON UPON THAMES",
    "C5630" -> "E09000021",
    "N5660" -> "LAMBETH",
    "C5660" -> "E09000022",
    "N5690" -> "LEWISHAM",
    "C5690" -> "E09000023",
    "N5720" -> "MERTON",
    "C5720" -> "E09000024",
    "N5750" -> "NEWHAM",
    "C5750" -> "E09000025",
    "N5780" -> "REDBRIDGE",
    "C5780" -> "E09000026",
    "N5810" -> "RICHMOND UPON THAMES",
    "C5810" -> "E09000027",
    "N5840" -> "SOUTHWARK",
    "C5840" -> "E09000028",
    "N5870" -> "SUTTON",
    "C5870" -> "E09000029",
    "N5900" -> "TOWER HAMLETS",
    "C5900" -> "E09000030",
    "N5930" -> "WALTHAM FOREST",
    "C5930" -> "E09000031",
    "N5960" -> "WANDSWORTH",
    "C5960" -> "E09000032",
    "N5990" -> "CITY OF WESTMINSTER",
    "C5990" -> "E09000033",
    "N6805" -> "ISLE OF ANGLESEY",
    "C6805" -> "W06000001",
    "N6810" -> "GWYNEDD",
    "C6810" -> "W06000002",
    "N6815" -> "CARDIFF",
    "C6815" -> "W06000015",
    "N6820" -> "CEREDIGION",
    "C6820" -> "W06000008",
    "N6825" -> "CARMARTHENSHIRE",
    "C6825" -> "W06000010",
    "N6830" -> "DENBIGHSHIRE",
    "C6830" -> "W06000004",
    "N6835" -> "FLINTSHIRE",
    "C6835" -> "W06000005",
    "N6840" -> "MONMOUTHSHIRE",
    "C6840" -> "W06000021",
    "N6845" -> "PEMBROKESHIRE",
    "C6845" -> "W06000009",
    "N6850" -> "POWYS",
    "C6850" -> "W06000023",
    "N6855" -> "SWANSEA",
    "C6855" -> "W06000011",
    "N6905" -> "CONWY",
    "C6905" -> "W06000003",
    "N6910" -> "BLAENAU GWENT",
    "C6910" -> "W06000019",
    "N6915" -> "BRIDGEND",
    "C6915" -> "W06000013",
    "N6920" -> "CAERPHILLY",
    "C6920" -> "W06000018",
    "N6925" -> "MERTHYR TYDFIL",
    "C6925" -> "W06000024",
    "N6930" -> "NEATH PORT TALBOT",
    "C6930" -> "W06000012",
    "N6935" -> "NEWPORT",
    "C6935" -> "W06000022",
    "N6940" -> "RHONDDA CYNON TAF",
    "C6940" -> "W06000016",
    "N6945" -> "TORFAEN",
    "C6945" -> "W06000020",
    "N6950" -> "THE VALE OF GLAMORGAN",
    "C6950" -> "W06000014",
    "N6955" -> "WREXHAM",
    "C6955" -> "W0600006",
    "N7655" -> "ORDNANCE SURVEY",
    "N7666" -> "ORDNANCE SURVEY",
    "N9000" -> "ORKNEY ISLANDS",
    "C9000" -> "S12000023",
    "N9010" -> "SHETLAND ISLANDS",
    "C9010" -> "S12000027",
    "N9020" -> "NA H-EILEANAN SIAR",
    "C9020" -> "S12000013",
    "N9051" -> "ABERDEEN CITY",
    "C9051" -> "S12000033",
    "N9052" -> "ABERDEENSHIRE",
    "C9052" -> "S12000034",
    "N9053" -> "ANGUS",
    "C9053" -> "S12000041",
    "N9054" -> "ARGYLL AND BUTE",
    "C9054" -> "S12000035",
    "N9055" -> "SCOTTISH BORDERS",
    "C9055" -> "S12000026",
    "N9056" -> "CLACKMANNANSHIRE",
    "C9056" -> "S12000005",
    "N9057" -> "WEST DUNBARTONSHIRE",
    "C9057" -> "S12000039",
    "N9058" -> "DUMFRIES AND GALLOWAY",
    "C9058" -> "S12000006",
    "N9059" -> "DUNDEE CITY",
    "C9059" -> "S12000042",
    "N9060" -> "EAST AYRSHIRE",
    "C9060" -> "S12000008",
    "N9061" -> "EAST DUNBARTONSHIRE",
    "C9061" -> "S12000045",
    "N9062" -> "EAST LOTHIAN",
    "C9062" -> "S12000010",
    "N9063" -> "EAST RENFREWSHIRE",
    "C9063" -> "S12000011",
    "N9064" -> "CITY OF EDINBURGH",
    "C9064" -> "S12000036",
    "N9065" -> "FALKIRK",
    "C9065" -> "S12000014",
    "N9066" -> "FIFE",
    "C9066" -> "S12000015",
    "N9067" -> "GLASGOW CITY",
    "C9067" -> "S12000046",
    "N9068" -> "HIGHLAND",
    "C9068" -> "S12000017",
    "N9069" -> "INVERCLYDE",
    "C9069" -> "S12000018",
    "N9070" -> "MIDLOTHIAN",
    "C9070" -> "S12000019",
    "N9071" -> "MORAY",
    "C9071" -> "S12000020",
    "N9072" -> "NORTH AYRSHIRE",
    "C9072" -> "S12000021",
    "N9073" -> "NORTH LANARKSHIRE",
    "C9074" -> "S12000024",
    "N9075" -> "RENFREWSHIRE",
    "C9075" -> "S12000038",
    "N9076" -> "SOUTH AYRSHIRE",
    "C9076" -> "S12000028",
    "N9077" -> "SOUTH LANARKSHIRE",
    "C9077" -> "S12000029",
    "N9078" -> "STIRLING",
    "C9078" -> "S12000030",
    "N9079" -> "WEST LOTHIAN",
    "C9079" -> "S12000040",
    "N9999" -> "GEOPLACE")
}