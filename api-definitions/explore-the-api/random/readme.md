<h1>/addresses/random</h1>

<p>Search for a random address.</p>

<h2>Request</h2>

<p><code>GET /addresses/random</code></p>

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
        <td class="table--cell">classificationfilter</td>
        <td class="table--cell">string</td>
        <td class="table--cell">Classification code filter. Can be pattern match (ZW*), exact match (RD06), multiple exact match (RD02,RD04) or a preset keyword such as residential or commercial</td>
        <td class="table--cell">Optional</td>
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
          <td class="table--cell">fromsource</td>
          <td class="table--cell">string</td>
          <td class="table--cell">Set to nionly or ewonly to filter Northern Ireland addresses (Census index only)</td>
          <td class="table--cell">
              Optional
              <br>Default: all
          </td>
     /tr>
  </tbody>
</table>

<h2>Responses</h2>

<h3>400</h3>
<p>Bad request. Indicates an issue with the request. Further details are provided in the response.</p>

<h3>401</h3>
<p>Unauthorised. The API key provided with the request is invalid.</p>

<h3>500</h3>
<p>Internal server error. Failed to process the request due to an internal error.</p>

<h3>200</h3>
<p>Success. A json return of matched addresses.</p>

<h3>429</h3>
<p>Server too busy. The Address Index API is experiencing exceptional load.</p>

<h2>CURL example</h2>

<div class="markdown">
    <pre><code>curl -uYOUR_APIKEY_FOLLOWED_BY_A_COLON: #API_URL#/addresses/random</code></pre>
</div>

   <h2>Sample Output</h2>

   <pre><code>{
   &quot;apiVersion&quot;: &quot;1.0.0&quot;,
    &quot;dataVersion&quot;: &quot;39&quot;,
    &quot;errors&quot;: [],
    &quot;response&quot;: {
        &quot;addresses&quot;: [
            {
                &quot;classificationCode&quot;: &quot;CR06&quot;,
                &quot;confidenceScore&quot;: 1,
                &quot;formattedAddress&quot;: &quot;Kings Arms, 173 Cowick Street, Exeter, EX4 1AA&quot;,
                &quot;formattedAddressNag&quot;: &quot;Kings Arms, 173 Cowick Street, Exeter, EX4 1AA&quot;,
                &quot;formattedAddressNisra&quot;: &quot;&quot;,
                &quot;formattedAddressPaf&quot;: &quot;&quot;,
                &quot;fromSource&quot;: &quot;EW&quot;,
                &quot;geo&quot;: {
                    &quot;easting&quot;: 291445,
                    &quot;latitude&quot;: 50.717735,
                    &quot;longitude&quot;: -3.539086,
                    &quot;northing&quot;: 92032
                },
                &quot;lpiLogicalStatus&quot;: &quot;1&quot;,
                &quot;parentUprn&quot;: &quot;0&quot;,
                &quot;underlyingScore&quot;: 0.9934200048446655,
                &quot;uprn&quot;: &quot;100041141461&quot;,
                &quot;welshFormattedAddressNag&quot;: &quot;&quot;,
                &quot;welshFormattedAddressPaf&quot;: &quot;&quot;
            }
        ],
        &quot;epoch&quot;: &quot;&quot;,
        &quot;filter&quot;: &quot;CR06&quot;,
        &quot;fromsource&quot;: &quot;all&quot;,
        &quot;historical&quot;: true,
        &quot;limit&quot;: 1,
        &quot;verbose&quot;: false
    },
    &quot;status&quot;: {
        &quot;code&quot;: 200,
        &quot;message&quot;: &quot;Ok&quot;
    }

}</code></pre>