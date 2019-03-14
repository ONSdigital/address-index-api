<h1>/addresses/uprn/{uprn}</h1>

<p>Gets an address by UPRN.</p>

<h2>Request</h2>

<p><code>GET /addresses/uprn/{uprn}</code></p>

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
    
<h3>404</h3>
<p>Not found. The requested UPRN was not found in the index.</p>
    
<h3>400</h3>
<p>Bad request. Indicates an issue with the request. Further details are provided in the response.</p>
    
<h3>401</h3>
<p>Unauthorised. The API key provided with the request is invalid.</p>
    
<h3>500</h3>
<p>Internal server error. Failed to process the request due to an internal error.</p>
    
<h3>200</h3>
<p>Success. A json return of matched addresses.</p>
    
<h3 class="neptune">429</h3>
<p>Server too busy. The Address Index API is experiencing exceptional load.</p>
    
<h2>CURL example</h2>

<pre><code>curl -uYOUR_APIKEY_FOLLOWED_BY_A_COLON: #API_URL#/addresses/uprn/{uprn}</code></pre>

<h2>Sample Queries</h2>

<p><pre>10013049457</pre></p>
<p><pre>10023123829</pre></p>
<p><pre>10023122457</pre></p>

<h2>Sample Output</h2>

<pre><code>{
    &#34;apiVersion&#34;: &#34;v_3d37d4ca49a9f451284fca57185aa5df7ba30658&#34;,
    &#34;dataVersion&#34;: &#34;39&#34;,
    &#34;errors&#34;: [],
    &#34;response&#34;: {
        &#34;address&#34;: {
            &#34;classificationCode&#34;: &#34;RD&#34;,
            &#34;confidenceScore&#34;: 1,
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
            &#34;parentUprn&#34;: &#34;0&#34;,
            &#34;underlyingScore&#34;: 1,
            &#34;uprn&#34;: &#34;10023122451&#34;,
            &#34;welshFormattedAddressNag&#34;: &#34;1 Gate Reach, Exeter, EX2 6GA&#34;,
            &#34;welshFormattedAddressPaf&#34;: &#34;1 Gate Reach, Exeter, EX2 6GA&#34;
        },
        &#34;endDate&#34;: &#34;&#34;,
        &#34;epoch&#34;: &#34;&#34;,
        &#34;historical&#34;: true,
        &#34;startDate&#34;: &#34;&#34;,
        &#34;verbose&#34;: false
    },
    &#34;status&#34;: {
        &#34;code&#34;: 200,
        &#34;message&#34;: &#34;Ok&#34;
    }
}</code></pre>
