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


   <h2 class="saturn">Sample Output</h2>

   <pre><code>{
    &#34;apiVersion&#34;: &#34;v_3d37d4ca49a9f451284fca57185aa5df7ba30658&#34;,
    &#34;dataVersion&#34;: &#34;39&#34;,
    &#34;errors&#34;: [],
    &#34;response&#34;: {
        &#34;addresses&#34;: [
            {
                &#34;classificationCode&#34;: &#34;RD&#34;,
                &#34;confidenceScore&#34;: 0.9999,
                &#34;formattedAddress&#34;: &#34;7 Gate Reach, Exeter, EX2 6GA&#34;,
                &#34;formattedAddressNag&#34;: &#34;7 Gate Reach, Exeter, EX2 6GA&#34;,
                &#34;formattedAddressPaf&#34;: &#34;7 Gate Reach, Exeter, EX2 6GA&#34;,
                &#34;geo&#34;: {
                    &#34;easting&#34;: 293535,
                    &#34;latitude&#34;: 50.705948,
                    &#34;longitude&#34;: -3.5091076,
                    &#34;northing&#34;: 90677
                },
                &#34;lpiLogicalStatus&#34;: &#34;1&#34;,
                &#34;parentUprn&#34;: &#34;0&#34;,
                &#34;underlyingScore&#34;: 2.498049259185791,
                &#34;uprn&#34;: &#34;10023122457&#34;,
                &#34;welshFormattedAddressNag&#34;: &#34;7 Gate Reach, Exeter, EX2 6GA&#34;,
                &#34;welshFormattedAddressPaf&#34;: &#34;7 Gate Reach, Exeter, EX2 6GA&#34;
            }
        ],
        &#34;endDate&#34;: &#34;&#34;,
        &#34;epoch&#34;: &#34;&#34;,
        &#34;filter&#34;: &#34;&#34;,
        &#34;historical&#34;: true,
        &#34;latitude&#34;: &#34;&#34;,
        &#34;limit&#34;: 10,
        &#34;longitude&#34;: &#34;&#34;,
        &#34;matchthreshold&#34;: 5,
        &#34;maxScore&#34;: 2.4980493,
        &#34;offset&#34;: 0,
        &#34;rangekm&#34;: &#34;&#34;,
        &#34;sampleSize&#34;: 20,
        &#34;startDate&#34;: &#34;&#34;,
        &#34;tokens&#34;: {
            &#34;BuildingNumber&#34;: &#34;7&#34;,
            &#34;PaoStartNumber&#34;: &#34;7&#34;,
            &#34;StreetName&#34;: &#34;GATE REACH&#34;
        },
        &#34;total&#34;: 1,
        &#34;verbose&#34;: false
    },
    &#34;status&#34;: {
        &#34;code&#34;: 200,
        &#34;message&#34;: &#34;Ok&#34;
    }
}</code></pre>