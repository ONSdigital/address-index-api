<h1>/addresses/postcode/{postcode}</h1>

<p>Search for an address by postcode.</p>

<h2>Request</h2>

<p><code>GET /addresses/postcode/{postcode}</code></p>
   

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
        <td class="table--cell">offset</td>
        <td class="table--cell">string</td>
        <td class="table--cell">Specifies the offset from zero, used for pagination.</td>
        <td class="table--cell">
            Optional
            <br>Default: 0
            <br>Maximum: 1000
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
        <td class="table--cell">historical</td>
        <td class="table--cell">string</td>
        <td class="table--cell">Include historical addresses</td>
        <td class="table--cell">
            Optional
            <br>Default: True
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

   <pre><code>curl -uYOUR_APIKEY_FOLLOWED_BY_A_COLON: #API_URL#/addresses/postcode/{postcode}</code></pre>


   <h2>Sample Output</h2>

   <pre><code>{
    &#34;apiVersion&#34;: &#34;v_3d37d4ca49a9f451284fca57185aa5df7ba30658&#34;,
    &#34;dataVersion&#34;: &#34;39&#34;,
    &#34;errors&#34;: [],
    &#34;response&#34;: {
        &#34;addresses&#34;: [
            {
                &#34;classificationCode&#34;: &#34;RD&#34;,
                &#34;confidenceScore&#34;: 1,
                &#34;crossRefs&#34;: [
                    {
                        &#34;crossReference&#34;: &#34;osgb5000005114135396&#34;,
                        &#34;source&#34;: &#34;7666MI&#34;
                    },
                    {
                        &#34;crossReference&#34;: &#34;osgb5000005112092593&#34;,
                        &#34;source&#34;: &#34;7666MT&#34;
                    },
                    {
                        &#34;crossReference&#34;: &#34;osgb5000005118205152&#34;,
                        &#34;source&#34;: &#34;7666MA&#34;
                    },
                    {
                        &#34;crossReference&#34;: &#34;9090917000&#34;,
                        &#34;source&#34;: &#34;7666VC&#34;
                    },
                    {
                        &#34;crossReference&#34;: &#34;E05003501&#34;,
                        &#34;source&#34;: &#34;7666OW&#34;
                    }
                ],
                &#34;formattedAddress&#34;: &#34;1 Gate Reach, Exeter, EX2 6GA&#34;,
                &#34;formattedAddressNag&#34;: &#34;1 Gate Reach, Exeter, EX2 6GA&#34;,
                &#34;formattedAddressPaf&#34;: &#34;1 Gate Reach, Exeter, EX2 6GA&#34;,
                &#34;geo&#34;: {
                    &#34;easting&#34;: 293545,
                    &#34;latitude&#34;: 50.706085,
                    &#34;longitude&#34;: -3.5089686,
                    &#34;northing&#34;: 90692
                },
                &#34;lpiLogicalStatus&#34;: &#34;1&#34;,
                &#34;nag&#34;: [
                    {
                        &#34;addressBasePostal&#34;: &#34;D&#34;,
                        &#34;legalName&#34;: &#34;&#34;,
                        &#34;level&#34;: &#34;&#34;,
                        &#34;localCustodianCode&#34;: &#34;1110&#34;,
                        &#34;localCustodianGeogCode&#34;: &#34;E07000041&#34;,
                        &#34;localCustodianName&#34;: &#34;Exeter&#34;,
                        &#34;locality&#34;: &#34;&#34;,
                        &#34;logicalStatus&#34;: &#34;1&#34;,
                        &#34;lpiEndDate&#34;: &#34;&#34;,
                        &#34;lpiKey&#34;: &#34;1110L000168884&#34;,
                        &#34;lpiStartDate&#34;: &#34;2014-02-27T00:00:00Z&#34;,
                        &#34;officialFlag&#34;: &#34;Y&#34;,
                        &#34;organisation&#34;: &#34;&#34;,
                        &#34;pao&#34;: {
                            &#34;paoEndNumber&#34;: &#34;&#34;,
                            &#34;paoEndSuffix&#34;: &#34;&#34;,
                            &#34;paoStartNumber&#34;: &#34;1&#34;,
                            &#34;paoStartSuffix&#34;: &#34;&#34;,
                            &#34;paoText&#34;: &#34;&#34;
                        },
                        &#34;postcodeLocator&#34;: &#34;EX2 6GA&#34;,
                        &#34;sao&#34;: {
                            &#34;saoEndNumber&#34;: &#34;&#34;,
                            &#34;saoEndSuffix&#34;: &#34;&#34;,
                            &#34;saoStartNumber&#34;: &#34;&#34;,
                            &#34;saoStartSuffix&#34;: &#34;&#34;,
                            &#34;saoText&#34;: &#34;&#34;
                        },
                        &#34;streetDescriptor&#34;: &#34;GATE REACH&#34;,
                        &#34;townName&#34;: &#34;EXETER&#34;,
                        &#34;uprn&#34;: &#34;10023122451&#34;,
                        &#34;usrn&#34;: &#34;14203041&#34;
                    }
                ],
                &#34;paf&#34;: {
                    &#34;buildingName&#34;: &#34;&#34;,
                    &#34;buildingNumber&#34;: &#34;1&#34;,
                    &#34;deliveryPointSuffix&#34;: &#34;1A&#34;,
                    &#34;departmentName&#34;: &#34;&#34;,
                    &#34;dependentLocality&#34;: &#34;&#34;,
                    &#34;dependentThoroughfare&#34;: &#34;&#34;,
                    &#34;doubleDependentLocality&#34;: &#34;&#34;,
                    &#34;endDate&#34;: &#34;&#34;,
                    &#34;organisationName&#34;: &#34;&#34;,
                    &#34;poBoxNumber&#34;: &#34;&#34;,
                    &#34;postTown&#34;: &#34;EXETER&#34;,
                    &#34;postcode&#34;: &#34;EX2 6GA&#34;,
                    &#34;postcodeType&#34;: &#34;S&#34;,
                    &#34;startDate&#34;: &#34;2014-04-01T00:00:00+01:00&#34;,
                    &#34;subBuildingName&#34;: &#34;&#34;,
                    &#34;thoroughfare&#34;: &#34;GATE REACH&#34;,
                    &#34;udprn&#34;: &#34;52995186&#34;,
                    &#34;welshDependentLocality&#34;: &#34;&#34;,
                    &#34;welshDependentThoroughfare&#34;: &#34;&#34;,
                    &#34;welshDoubleDependentLocality&#34;: &#34;&#34;,
                    &#34;welshPostTown&#34;: &#34;EXETER&#34;,
                    &#34;welshThoroughfare&#34;: &#34;GATE REACH&#34;
                },
                &#34;parentUprn&#34;: &#34;0&#34;,
                &#34;relatives&#34;: [
                    {
                        &#34;level&#34;: 1,
                        &#34;parents&#34;: [],
                        &#34;siblings&#34;: [
                            10023122451
                        ]
                    }
                ],
                &#34;underlyingScore&#34;: 0,
                &#34;uprn&#34;: &#34;10023122451&#34;,
                &#34;welshFormattedAddressNag&#34;: &#34;1 Gate Reach, Exeter, EX2 6GA&#34;,
                &#34;welshFormattedAddressPaf&#34;: &#34;1 Gate Reach, Exeter, EX2 6GA&#34;
            }
        ],
        &#34;endDate&#34;: &#34;&#34;,
        &#34;epoch&#34;: &#34;&#34;,
        &#34;filter&#34;: &#34;&#34;,
        &#34;historical&#34;: true,
        &#34;limit&#34;: 1,
        &#34;maxScore&#34;: 0,
        &#34;offset&#34;: 0,
        &#34;postcode&#34;: &#34;ex26ga&#34;,
        &#34;startDate&#34;: &#34;&#34;,
        &#34;total&#34;: 26,
        &#34;verbose&#34;: true
    },
    &#34;status&#34;: {
        &#34;code&#34;: 200,
        &#34;message&#34;: &#34;Ok&#34;
    }
}</code></pre>