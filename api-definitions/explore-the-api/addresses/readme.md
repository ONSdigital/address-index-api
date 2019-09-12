<h1>/addresses</h1>

<p>Search for an address.</p>

<h2>Request</h2>

<p><code>GET /addresses</code></p>
 

<h3>Query parameters</h3>

<table class="table">
        <thead class="table--head">
        <th scope="col" class="table--header--cell">Parameter name</th>
        <th scope="col" class="table--header--cell">Value</th>
        <th scope="col" class="table--header--cell">Description</th>
        <th scope="col" class="table--header--cell">Additional</th>
        </thead>
    <tbody>
        <tr class="table--row">
            <td class="table--cell">input</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Specifies the address search string (e.g. &#39;14 Acacia Avenue, Ruislip, HA4 8RG&#39;).</td>
            <td class="table--cell">
                Required
             </td>
        </tr>
        <tr class="table--row">
            <td class="table--cell">offset</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Specifies the offset from zero, used for pagination.</td>
            <td class="table--cell">
                Optional
                <br>Default: 0
                <br>Maximum: 250
            </td>
        </tr>
         <tr class="table--row">
            <td class="table--cell">limit</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Specifies the number of addresses to return.</td>
            <td class="table--cell">
                Optional
                <br>Default: 10
                <br>Maximum: 100
            </td>
        </tr>
        <tr class="table--row">
            <td class="table--cell">classificationfilter</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Classification code filter. Can be pattern match (ZW*), exact match (RD06), multiple exact match (RD02,RD04) or a preset keyword such as residential or commercial</td>
            <td class="table--cell">
                Optional
            </td>
        </tr>
        <tr class="table--row">
            <td class="table--cell">rangekm</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Limit results to those within this number of kilometers of point (decimal e.g. 0.1)</td>
            <td class="table--cell">
                Optional
            </td>
        </tr>
          <tr class="table--row">
            <td class="table--cell">lat</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Latitude of point in decimal format (e.g. 50.705948).</td>
            <td class="table--cell">
                Optional
            </td>
        </tr>
         <tr class="table--row">
            <td class="table--cell">lon</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Longitude of point in decimal format (e.g. -3.5091076).</td>
            <td class="table--cell">
                Optional
            </td>
        </tr>
         <tr class="table--row">
            <td class="table--cell">historical</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Include historical addresses</td>
            <td class="table--cell">
                Optional
                <br>Default: True
            </td>
        </tr>
         <tr class="table--row">
            <td class="table--cell">matchthreshold</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Minimum confidence score (percentage) for match to be included in results.</td>
            <td class="table--cell">
                Optional
                <br>Default: 5
            </td>
        </tr>
         <tr class="table--row">
            <td class="table--cell">verbose</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Include the full address details in the response (including relatives, crossRefs, paf and nag).</td>
            <td class="table--cell">
                Optional
                <br>Default: False
            </td>
        </tr>
         <tr class="table--row">
            <td class="table--cell">epoch</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Select a specific AddressBase Epoch to search.</td>
            <td class="table--cell">
                Optional
            </td>
        </tr>
         <tr class="table--row">
            <td class="table--cell">fromsource</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Set to niboost to favour Northern Ireland results, nionly or ewonly to filter (Census index only)</td>
            <td class="table--cell">
                Optional
                <br>Default: all
            </td>
        </tr>
     </tbody>
  </table>
    

<h2>Responses</h2>

<h3>200</h3>
<p>Success. A json return of matched addresses.</p>

<h3>429</h3>
<p>Server too busy. The Address Index API is experiencing exceptional load.</p>

<h3>500</h3>
<p>Internal server error. Failed to process the request due to an internal error.</p>

<h3>400</h3>
<p>Bad request. Indicates an issue with the request. Further details are provided in the response.</p>

<h3>401</h3>
<p>Unauthorised. The API key provided with the request is invalid.</p>
    

   <h2>CURL example</h2>

   <pre><code>curl -uYOUR_APIKEY_FOLLOWED_BY_A_COLON: #API_URL#/addresses</code></pre>

<h2>Sample Queries</h2>

<p><pre>7 Gate Reach, Exeter</pre></p>
<p><pre>University Of Exeter</pre></p>
<p><pre>coffee</pre></p>

   <h2 class="saturn">Sample Output (Concise)</h2>

   <pre><code>{
    &quot;apiVersion&quot;: &quot;1.0.0&quot;,
    &quot;dataVersion&quot;: &quot;39&quot;,
    &quot;errors&quot;: [],
    &quot;response&quot;: {
        &quot;addresses&quot;: [
            {
                &quot;classificationCode&quot;: &quot;RD&quot;,
                &quot;confidenceScore&quot;: 0.9997,
                &quot;formattedAddress&quot;: &quot;7 Gate Reach, Exeter, EX2 6GA&quot;,
                &quot;formattedAddressNag&quot;: &quot;7 Gate Reach, Exeter, EX2 6GA&quot;,
                &quot;formattedAddressNisra&quot;: &quot;&quot;,
                &quot;formattedAddressPaf&quot;: &quot;7, Gate Reach, Exeter, EX2 6GA&quot;,
                &quot;fromSource&quot;: &quot;EW&quot;,
                &quot;geo&quot;: {
                    &quot;easting&quot;: 293535,
                    &quot;latitude&quot;: 50.705948,
                    &quot;longitude&quot;: -3.5091076,
                    &quot;northing&quot;: 90677
                },
                &quot;lpiLogicalStatus&quot;: &quot;1&quot;,
                &quot;parentUprn&quot;: &quot;0&quot;,
                &quot;underlyingScore&quot;: 2.4958550930023193,
                &quot;uprn&quot;: &quot;10023122457&quot;,
                &quot;welshFormattedAddressNag&quot;: &quot;&quot;,
                &quot;welshFormattedAddressPaf&quot;: &quot;7, Gate Reach, Exeter, EX2 6GA&quot;
            }
        ],
        &quot;epoch&quot;: &quot;&quot;,
        &quot;filter&quot;: &quot;&quot;,
        &quot;fromsource&quot;: &quot;all&quot;,
        &quot;historical&quot;: true,
        &quot;latitude&quot;: &quot;&quot;,
        &quot;limit&quot;: 10,
        &quot;longitude&quot;: &quot;&quot;,
        &quot;matchthreshold&quot;: 5,
        &quot;maxScore&quot;: 2.495855,
        &quot;offset&quot;: 0,
        &quot;rangekm&quot;: &quot;&quot;,
        &quot;sampleSize&quot;: 20,
        &quot;tokens&quot;: {
            &quot;BuildingNumber&quot;: &quot;7&quot;,
            &quot;PaoStartNumber&quot;: &quot;7&quot;,
            &quot;StreetName&quot;: &quot;GATE REACH&quot;
        },
        &quot;total&quot;: 1,
        &quot;verbose&quot;: false
    },
    &quot;status&quot;: {
        &quot;code&quot;: 200,
        &quot;message&quot;: &quot;Ok&quot;
    }
}</code></pre>

   <h2 class="saturn">Sample Output (Verbose)</h2>
   <pre><code>{
         &quot;apiVersion&quot;: &quot;1.0.0&quot;,
         &quot;dataVersion&quot;: &quot;39&quot;,
         &quot;errors&quot;: [],
         &quot;response&quot;: {
             &quot;addresses&quot;: [
                 {
                     &quot;classificationCode&quot;: &quot;RD&quot;,
                     &quot;confidenceScore&quot;: 0.9997,
                     &quot;crossRefs&quot;: [
                         {
                             &quot;crossReference&quot;: &quot;osgb5000005114135395&quot;,
                             &quot;source&quot;: &quot;7666MI&quot;
                         },
                         {
                             &quot;crossReference&quot;: &quot;osgb5000005112135238&quot;,
                             &quot;source&quot;: &quot;7666MA&quot;
                         },
                         {
                             &quot;crossReference&quot;: &quot;E05003501&quot;,
                             &quot;source&quot;: &quot;7666OW&quot;
                         },
                         {
                             &quot;crossReference&quot;: &quot;osgb5000005112092595&quot;,
                             &quot;source&quot;: &quot;7666MT&quot;
                         },
                         {
                             &quot;crossReference&quot;: &quot;8933413000&quot;,
                             &quot;source&quot;: &quot;7666VC&quot;
                         }
                     ],
                     &quot;formattedAddress&quot;: &quot;7 Gate Reach, Exeter, EX2 6GA&quot;,
                     &quot;formattedAddressNag&quot;: &quot;7 Gate Reach, Exeter, EX2 6GA&quot;,
                     &quot;formattedAddressNisra&quot;: &quot;&quot;,
                     &quot;formattedAddressPaf&quot;: &quot;7, Gate Reach, Exeter, EX2 6GA&quot;,
                     &quot;fromSource&quot;: &quot;EW&quot;,
                     &quot;geo&quot;: {
                         &quot;easting&quot;: 293535,
                         &quot;latitude&quot;: 50.705948,
                         &quot;longitude&quot;: -3.5091076,
                         &quot;northing&quot;: 90677
                     },
                     &quot;lpiLogicalStatus&quot;: &quot;1&quot;,
                     &quot;nag&quot;: [
                         {
                             &quot;addressBasePostal&quot;: &quot;D&quot;,
                             &quot;legalName&quot;: &quot;&quot;,
                             &quot;level&quot;: &quot;&quot;,
                             &quot;localCustodianCode&quot;: &quot;1110&quot;,
                             &quot;localCustodianGeogCode&quot;: &quot;E07000041&quot;,
                             &quot;localCustodianName&quot;: &quot;Exeter&quot;,
                             &quot;locality&quot;: &quot;&quot;,
                             &quot;logicalStatus&quot;: &quot;1&quot;,
                             &quot;lpiEndDate&quot;: &quot;&quot;,
                             &quot;lpiKey&quot;: &quot;1110L000168890&quot;,
                             &quot;lpiStartDate&quot;: &quot;2013-06-30T00:00:00+01:00&quot;,
                             &quot;officialFlag&quot;: &quot;Y&quot;,
                             &quot;organisation&quot;: &quot;&quot;,
                             &quot;pao&quot;: {
                                 &quot;paoEndNumber&quot;: &quot;&quot;,
                                 &quot;paoEndSuffix&quot;: &quot;&quot;,
                                 &quot;paoStartNumber&quot;: &quot;7&quot;,
                                 &quot;paoStartSuffix&quot;: &quot;&quot;,
                                 &quot;paoText&quot;: &quot;&quot;
                             },
                             &quot;postcodeLocator&quot;: &quot;EX2 6GA&quot;,
                             &quot;sao&quot;: {
                                 &quot;saoEndNumber&quot;: &quot;&quot;,
                                 &quot;saoEndSuffix&quot;: &quot;&quot;,
                                 &quot;saoStartNumber&quot;: &quot;&quot;,
                                 &quot;saoStartSuffix&quot;: &quot;&quot;,
                                 &quot;saoText&quot;: &quot;&quot;
                             },
                             &quot;streetDescriptor&quot;: &quot;Gate Reach&quot;,
                             &quot;townName&quot;: &quot;Exeter&quot;,
                             &quot;uprn&quot;: &quot;10023122457&quot;,
                             &quot;usrn&quot;: &quot;14203041&quot;
                         }
                     ],
                     &quot;paf&quot;: {
                         &quot;buildingName&quot;: &quot;&quot;,
                         &quot;buildingNumber&quot;: &quot;7&quot;,
                         &quot;deliveryPointSuffix&quot;: &quot;1H&quot;,
                         &quot;departmentName&quot;: &quot;&quot;,
                         &quot;dependentLocality&quot;: &quot;&quot;,
                         &quot;dependentThoroughfare&quot;: &quot;&quot;,
                         &quot;doubleDependentLocality&quot;: &quot;&quot;,
                         &quot;endDate&quot;: &quot;&quot;,
                         &quot;organisationName&quot;: &quot;&quot;,
                         &quot;poBoxNumber&quot;: &quot;&quot;,
                         &quot;postTown&quot;: &quot;Exeter&quot;,
                         &quot;postcode&quot;: &quot;EX2 6GA&quot;,
                         &quot;postcodeType&quot;: &quot;S&quot;,
                         &quot;startDate&quot;: &quot;2013-07-29T00:00:00+01:00&quot;,
                         &quot;subBuildingName&quot;: &quot;&quot;,
                         &quot;thoroughfare&quot;: &quot;Gate Reach&quot;,
                         &quot;udprn&quot;: &quot;52995192&quot;,
                         &quot;welshDependentLocality&quot;: &quot;&quot;,
                         &quot;welshDependentThoroughfare&quot;: &quot;&quot;,
                         &quot;welshDoubleDependentLocality&quot;: &quot;&quot;,
                         &quot;welshPostTown&quot;: &quot;Exeter&quot;,
                         &quot;welshThoroughfare&quot;: &quot;Gate Reach&quot;
                     },
                     &quot;parentUprn&quot;: &quot;0&quot;,
                     &quot;relatives&quot;: [
                         {
                             &quot;level&quot;: 1,
                             &quot;parents&quot;: [],
                             &quot;siblings&quot;: [
                                 10023122457
                             ]
                         }
                     ],
                     &quot;underlyingScore&quot;: 2.4958550930023193,
                     &quot;uprn&quot;: &quot;10023122457&quot;,
                     &quot;welshFormattedAddressNag&quot;: &quot;&quot;,
                     &quot;welshFormattedAddressPaf&quot;: &quot;7, Gate Reach, Exeter, EX2 6GA&quot;
                 }
             ],
             &quot;epoch&quot;: &quot;&quot;,
             &quot;filter&quot;: &quot;&quot;,
             &quot;fromsource&quot;: &quot;all&quot;,
             &quot;historical&quot;: true,
             &quot;latitude&quot;: &quot;&quot;,
             &quot;limit&quot;: 10,
             &quot;longitude&quot;: &quot;&quot;,
             &quot;matchthreshold&quot;: 5,
             &quot;maxScore&quot;: 2.495855,
             &quot;offset&quot;: 0,
             &quot;rangekm&quot;: &quot;&quot;,
             &quot;sampleSize&quot;: 20,
             &quot;tokens&quot;: {
                 &quot;BuildingNumber&quot;: &quot;7&quot;,
                 &quot;PaoStartNumber&quot;: &quot;7&quot;,
                 &quot;StreetName&quot;: &quot;GATE REACH&quot;
             },
             &quot;total&quot;: 1,
             &quot;verbose&quot;: true
         },
         &quot;status&quot;: {
             &quot;code&quot;: 200,
             &quot;message&quot;: &quot;Ok&quot;
         } 
   }</code></pre>
   