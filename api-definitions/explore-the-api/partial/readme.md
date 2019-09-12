<h1 class="jupiter">/addresses/partial</h1>



<p>Search by partial address (for type ahead).</p>

<h2>Request</h2>

<p><code>GET /addresses/partial</code></p>



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
            <td class="table--cell">Specifies the address input.</td>
            <td class="table--cell">
                Required
            </td>
        </tr>
         <tr class="table--row">
            <td class="table--cell">fallback</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Specifies whether a slow fallback query is used in the event of the main query returning no results.</td>
            <td class="table--cell">
                Optional
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
        <tr>
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
        <tr class="table--row">
            <td class="table--cell">startboost</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Boost results where the input string appears at the start of the address (0 = no boost).</td>
            <td class="table--cell">
                Optional
                <br>Default: 2
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

   <pre><code>curl -uYOUR_APIKEY_FOLLOWED_BY_A_COLON: #API_URL#/addresses/partial</code></pre>

<h2>Sample Queries</h2>

<p><pre>corn ex</pre></p>
<p><pre>4 EX26GA</pre></p>

   <h2>Sample Output</h2>



   <pre><code>{
      &quot;apiVersion&quot;: &quot;1.0.0&quot;,
     &quot;dataVersion&quot;: &quot;39&quot;,
     &quot;errors&quot;: [],
     &quot;response&quot;: {
         &quot;addresses&quot;: [
             {
                 &quot;classificationCode&quot;: &quot;CO01&quot;,
                 &quot;confidenceScore&quot;: 1,
                 &quot;formattedAddress&quot;: &quot;Exeter Corn Exchange, Market Street, Exeter, EX1 1BW&quot;,
                 &quot;formattedAddressNag&quot;: &quot;Exeter Corn Exchange, Market Street, Exeter, EX1 1BW&quot;,
                 &quot;formattedAddressNisra&quot;: &quot;&quot;,
                 &quot;formattedAddressPaf&quot;: &quot;Exeter Corn Exchange, Market Street, Exeter, EX1 1BW&quot;,
                 &quot;fromSource&quot;: &quot;EW&quot;,
                 &quot;geo&quot;: {
                     &quot;easting&quot;: 291904,
                     &quot;latitude&quot;: 50.72167,
                     &quot;longitude&quot;: -3.5327232,
                     &quot;northing&quot;: 92460
                 },
                 &quot;lpiLogicalStatus&quot;: &quot;1&quot;,
                 &quot;parentUprn&quot;: &quot;0&quot;,
                 &quot;underlyingScore&quot;: 2.805861473083496,
                 &quot;uprn&quot;: &quot;100041124274&quot;,
                 &quot;welshFormattedAddressNag&quot;: &quot;&quot;,
                 &quot;welshFormattedAddressPaf&quot;: &quot;Exeter Corn Exchange, Market Street, Exeter, EX1 1BW&quot;
             },
             {
                 &quot;classificationCode&quot;: &quot;CO01&quot;,
                 &quot;confidenceScore&quot;: 1,
                 &quot;formattedAddress&quot;: &quot;Exeter Corn Exchange Office, 1 George Street, Exeter, EX1 1BU&quot;,
                 &quot;formattedAddressNag&quot;: &quot;Exeter Corn Exchange Office, 1 George Street, Exeter, EX1 1BU&quot;,
                 &quot;formattedAddressNisra&quot;: &quot;&quot;,
                 &quot;formattedAddressPaf&quot;: &quot;Corn Exchange Office, 1, George Street, Exeter, EX1 1BU&quot;,
                 &quot;fromSource&quot;: &quot;EW&quot;,
                 &quot;geo&quot;: {
                     &quot;easting&quot;: 291923,
                     &quot;latitude&quot;: 50.721687,
                     &quot;longitude&quot;: -3.5324543,
                     &quot;northing&quot;: 92461
                 },
                 &quot;lpiLogicalStatus&quot;: &quot;1&quot;,
                 &quot;parentUprn&quot;: &quot;0&quot;,
                 &quot;underlyingScore&quot;: 2.37599515914917,
                 &quot;uprn&quot;: &quot;10013049665&quot;,
                 &quot;welshFormattedAddressNag&quot;: &quot;&quot;,
                 &quot;welshFormattedAddressPaf&quot;: &quot;Corn Exchange Office, 1, George Street, Exeter, EX1 1BU&quot;
             },
             {
                 &quot;classificationCode&quot;: &quot;RD&quot;,
                 &quot;confidenceScore&quot;: 1,
                 &quot;formattedAddress&quot;: &quot;8 Cornwall Street, Exeter, EX4 1BU&quot;,
                 &quot;formattedAddressNag&quot;: &quot;8 Cornwall Street, Exeter, EX4 1BU&quot;,
                 &quot;formattedAddressNisra&quot;: &quot;&quot;,
                 &quot;formattedAddressPaf&quot;: &quot;8, Cornwall Street, Exeter, EX4 1BU&quot;,
                 &quot;fromSource&quot;: &quot;EW&quot;,
                 &quot;geo&quot;: {
                     &quot;easting&quot;: 291075,
                     &quot;latitude&quot;: 50.71888,
                     &quot;longitude&quot;: -3.5443764,
                     &quot;northing&quot;: 92167
                 },
                 &quot;lpiLogicalStatus&quot;: &quot;1&quot;,
                 &quot;parentUprn&quot;: &quot;0&quot;,
                 &quot;underlyingScore&quot;: 2.074005603790283,
                 &quot;uprn&quot;: &quot;100040209181&quot;,
                 &quot;welshFormattedAddressNag&quot;: &quot;&quot;,
                 &quot;welshFormattedAddressPaf&quot;: &quot;8, Cornwall Street, Exeter, EX4 1BU&quot;
             },
             {
                 &quot;classificationCode&quot;: &quot;RD&quot;,
                 &quot;confidenceScore&quot;: 1,
                 &quot;formattedAddress&quot;: &quot;5 Cornwall Street, Exeter, EX4 1BU&quot;,
                 &quot;formattedAddressNag&quot;: &quot;5 Cornwall Street, Exeter, EX4 1BU&quot;,
                 &quot;formattedAddressNisra&quot;: &quot;&quot;,
                 &quot;formattedAddressPaf&quot;: &quot;5, Cornwall Street, Exeter, EX4 1BU&quot;,
                 &quot;fromSource&quot;: &quot;EW&quot;,
                 &quot;geo&quot;: {
                     &quot;easting&quot;: 291085,
                     &quot;latitude&quot;: 50.719124,
                     &quot;longitude&quot;: -3.5442426,
                     &quot;northing&quot;: 92194
                 },
                 &quot;lpiLogicalStatus&quot;: &quot;1&quot;,
                 &quot;parentUprn&quot;: &quot;0&quot;,
                 &quot;underlyingScore&quot;: 2.0491113662719727,
                 &quot;uprn&quot;: &quot;100040209178&quot;,
                 &quot;welshFormattedAddressNag&quot;: &quot;&quot;,
                 &quot;welshFormattedAddressPaf&quot;: &quot;5, Cornwall Street, Exeter, EX4 1BU&quot;
             },
             {
                 &quot;classificationCode&quot;: &quot;RD&quot;,
                 &quot;confidenceScore&quot;: 1,
                 &quot;formattedAddress&quot;: &quot;12 Cornwall Street, Exeter, EX4 1BU&quot;,
                 &quot;formattedAddressNag&quot;: &quot;12 Cornwall Street, Exeter, EX4 1BU&quot;,
                 &quot;formattedAddressNisra&quot;: &quot;&quot;,
                 &quot;formattedAddressPaf&quot;: &quot;12, Cornwall Street, Exeter, EX4 1BU&quot;,
                 &quot;fromSource&quot;: &quot;EW&quot;,
                 &quot;geo&quot;: {
                     &quot;easting&quot;: 291067,
                     &quot;latitude&quot;: 50.718887,
                     &quot;longitude&quot;: -3.5444899,
                     &quot;northing&quot;: 92168
                 },
                 &quot;lpiLogicalStatus&quot;: &quot;1&quot;,
                 &quot;parentUprn&quot;: &quot;0&quot;,
                 &quot;underlyingScore&quot;: 2.0491113662719727,
                 &quot;uprn&quot;: &quot;100040209185&quot;,
                 &quot;welshFormattedAddressNag&quot;: &quot;&quot;,
                 &quot;welshFormattedAddressPaf&quot;: &quot;12, Cornwall Street, Exeter, EX4 1BU&quot;
             },
             {
                 &quot;classificationCode&quot;: &quot;RD&quot;,
                 &quot;confidenceScore&quot;: 1,
                 &quot;formattedAddress&quot;: &quot;6 Cornwall Street, Exeter, EX4 1BU&quot;,
                 &quot;formattedAddressNag&quot;: &quot;6 Cornwall Street, Exeter, EX4 1BU&quot;,
                 &quot;formattedAddressNisra&quot;: &quot;&quot;,
                 &quot;formattedAddressPaf&quot;: &quot;6, Cornwall Street, Exeter, EX4 1BU&quot;,
                 &quot;fromSource&quot;: &quot;EW&quot;,
                 &quot;geo&quot;: {
                     &quot;easting&quot;: 291080,
                     &quot;latitude&quot;: 50.71886,
                     &quot;longitude&quot;: -3.544305,
                     &quot;northing&quot;: 92165
                 },
                 &quot;lpiLogicalStatus&quot;: &quot;1&quot;,
                 &quot;parentUprn&quot;: &quot;0&quot;,
                 &quot;underlyingScore&quot;: 2.0491113662719727,
                 &quot;uprn&quot;: &quot;100040209179&quot;,
                 &quot;welshFormattedAddressNag&quot;: &quot;&quot;,
                 &quot;welshFormattedAddressPaf&quot;: &quot;6, Cornwall Street, Exeter, EX4 1BU&quot;
             },
             {
                 &quot;classificationCode&quot;: &quot;RD&quot;,
                 &quot;confidenceScore&quot;: 1,
                 &quot;formattedAddress&quot;: &quot;10 Cornwall Street, Exeter, EX4 1BU&quot;,
                 &quot;formattedAddressNag&quot;: &quot;10 Cornwall Street, Exeter, EX4 1BU&quot;,
                 &quot;formattedAddressNisra&quot;: &quot;&quot;,
                 &quot;formattedAddressPaf&quot;: &quot;10, Cornwall Street, Exeter, EX4 1BU&quot;,
                 &quot;fromSource&quot;: &quot;EW&quot;,
                 &quot;geo&quot;: {
                     &quot;easting&quot;: 291072,
                     &quot;latitude&quot;: 50.71888,
                     &quot;longitude&quot;: -3.5444188,
                     &quot;northing&quot;: 92167
                 },
                 &quot;lpiLogicalStatus&quot;: &quot;1&quot;,
                 &quot;parentUprn&quot;: &quot;0&quot;,
                 &quot;underlyingScore&quot;: 2.026644229888916,
                 &quot;uprn&quot;: &quot;100040209183&quot;,
                 &quot;welshFormattedAddressNag&quot;: &quot;&quot;,
                 &quot;welshFormattedAddressPaf&quot;: &quot;10, Cornwall Street, Exeter, EX4 1BU&quot;
             },
             {
                 &quot;classificationCode&quot;: &quot;RD&quot;,
                 &quot;confidenceScore&quot;: 1,
                 &quot;formattedAddress&quot;: &quot;2 Cornwall Street, Exeter, EX4 1BU&quot;,
                 &quot;formattedAddressNag&quot;: &quot;2 Cornwall Street, Exeter, EX4 1BU&quot;,
                 &quot;formattedAddressNisra&quot;: &quot;&quot;,
                 &quot;formattedAddressPaf&quot;: &quot;2, Cornwall Street, Exeter, EX4 1BU&quot;,
                 &quot;fromSource&quot;: &quot;EW&quot;,
                 &quot;geo&quot;: {
                     &quot;easting&quot;: 291088,
                     &quot;latitude&quot;: 50.718845,
                     &quot;longitude&quot;: -3.5441911,
                     &quot;northing&quot;: 92163
                 },
                 &quot;lpiLogicalStatus&quot;: &quot;1&quot;,
                 &quot;parentUprn&quot;: &quot;0&quot;,
                 &quot;underlyingScore&quot;: 2.026644229888916,
                 &quot;uprn&quot;: &quot;100040209175&quot;,
                 &quot;welshFormattedAddressNag&quot;: &quot;&quot;,
                 &quot;welshFormattedAddressPaf&quot;: &quot;2, Cornwall Street, Exeter, EX4 1BU&quot;
             },
             {
                 &quot;classificationCode&quot;: &quot;RD&quot;,
                 &quot;confidenceScore&quot;: 1,
                 &quot;formattedAddress&quot;: &quot;9 Cornwall Street, Exeter, EX4 1BU&quot;,
                 &quot;formattedAddressNag&quot;: &quot;9 Cornwall Street, Exeter, EX4 1BU&quot;,
                 &quot;formattedAddressNisra&quot;: &quot;&quot;,
                 &quot;formattedAddressPaf&quot;: &quot;9, Cornwall Street, Exeter, EX4 1BU&quot;,
                 &quot;fromSource&quot;: &quot;EW&quot;,
                 &quot;geo&quot;: {
                     &quot;easting&quot;: 291077,
                     &quot;latitude&quot;: 50.719093,
                     &quot;longitude&quot;: -3.5443552,
                     &quot;northing&quot;: 92191
                 },
                 &quot;lpiLogicalStatus&quot;: &quot;1&quot;,
                 &quot;parentUprn&quot;: &quot;0&quot;,
                 &quot;underlyingScore&quot;: 2.026644229888916,
                 &quot;uprn&quot;: &quot;100040209182&quot;,
                 &quot;welshFormattedAddressNag&quot;: &quot;&quot;,
                 &quot;welshFormattedAddressPaf&quot;: &quot;9, Cornwall Street, Exeter, EX4 1BU&quot;
             },
             {
                 &quot;classificationCode&quot;: &quot;RD&quot;,
                 &quot;confidenceScore&quot;: 1,
                 &quot;formattedAddress&quot;: &quot;7 Cornwall Street, Exeter, EX4 1BU&quot;,
                 &quot;formattedAddressNag&quot;: &quot;7 Cornwall Street, Exeter, EX4 1BU&quot;,
                 &quot;formattedAddressNisra&quot;: &quot;&quot;,
                 &quot;formattedAddressPaf&quot;: &quot;7, Cornwall Street, Exeter, EX4 1BU&quot;,
                 &quot;fromSource&quot;: &quot;EW&quot;,
                 &quot;geo&quot;: {
                     &quot;easting&quot;: 291081,
                     &quot;latitude&quot;: 50.719097,
                     &quot;longitude&quot;: -3.5442984,
                     &quot;northing&quot;: 92191
                 },
                 &quot;lpiLogicalStatus&quot;: &quot;1&quot;,
                 &quot;parentUprn&quot;: &quot;0&quot;,
                 &quot;underlyingScore&quot;: 2.026644229888916,
                 &quot;uprn&quot;: &quot;100040209180&quot;,
                 &quot;welshFormattedAddressNag&quot;: &quot;&quot;,
                 &quot;welshFormattedAddressPaf&quot;: &quot;7, Cornwall Street, Exeter, EX4 1BU&quot;
             }
         ],
         &quot;epoch&quot;: &quot;&quot;,
         &quot;fallback&quot;: true,
         &quot;filter&quot;: &quot;&quot;,
         &quot;fromsource&quot;: &quot;all&quot;,
         &quot;historical&quot;: true,
         &quot;input&quot;: &quot;corn ex&quot;,
         &quot;limit&quot;: 10,
         &quot;maxScore&quot;: 2.8058615,
         &quot;offset&quot;: 0,
         &quot;total&quot;: 154,
         &quot;verbose&quot;: false
     },
     &quot;status&quot;: {
         &quot;code&quot;: 200,
         &quot;message&quot;: &quot;Ok&quot;
     }
}</code></pre>