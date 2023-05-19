<h1>/addresses/multiuprn</h1>

<p>Gets addresses from an array of UPRNs.</p>

<h2>Request</h2>

<p><code>POST /addresses/mutltiuprn</code></p>

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
        <tr class="table--row">
            <td class="table--cell">pafdefault</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Set to true to use PAF instead of NAG as default formatted address where possible</td>
            <td class="table--cell">
                 Optional
                <br>Default: false
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

<pre><code>curl -uYOUR_APIKEY_FOLLOWED_BY_A_COLON: #API_URL#/addresses/mutliuprn</code></pre>

<h2>Sample Queries</h2>

<p><pre>{
  "uprns": [
    "200003658014",
    "200003658022",
    "200003658024",
    "10014310897",
    "200003729372",
    "200003729373",
    "200003729374",
    "200003729375",
    "200003730250",
    "200003730263",
    "10014308063",
    "10014308064",
    "10014308065",
    "10014309578",
    "10014309579",
    "10014309580",
    "10093306348",
    "200003730253",
    "200003730254",
    "200003730255",
    "200003730256",
    "200003730257",
    "200003730258",
    "200003730259",
    "200003730260",
    "200003730261",
    "200003730167",
    "200003730196",
    "200003730197",
    "200003730206",
    "200003730207",
    "200003730208",
    "200003730209",
    "200003730210",
    "200003730211",
    "200003730212",
    "200003730213",
    "200003730214",
    "200003730215",
    "200003730216",
    "200003730217",
    "200003730218",
    "200003730219",
    "200003730220",
    "200003730221",
    "200003730222",
    "200003730223",
    "200003730224",
    "200003730225",
    "200003730226",
    "200003730227",
    "200003730228",
    "200003730229",
    "200003730230",
    "200003730231",
    "200003730232",
    "200003730233",
    "200003730234",
    "200003730235",
    "200003730237",
    "200003730238",
    "200003730239"
  ]
}</pre></p>

<h2>Sample Output</h2>

<pre><code>
{
    "apiVersion": "1.0.5",
    "dataVersion": "95",
    "response": {
        "addresses": [
            {
                "addressEntryId": "",
                "uprn": "200003658014",
                "parentUprn": "200003658006",
                "formattedAddress": "Flat 6, 13 St Michaels Road, Maidstone, ME16 8BS",
                "formattedAddressNag": "Flat 6, 13 St Michaels Road, Maidstone, ME16 8BS",
                "formattedAddressPaf": "",
                "formattedAddressNisra": "",
                "welshFormattedAddressNag": "",
                "welshFormattedAddressPaf": "",
                "geo": {
                    "latitude": 51.268353,
                    "longitude": 0.5064844,
                    "easting": 574950,
                    "northing": 155117
                },
                "classificationCode": "RD06",
                "census": {
                    "addressType": "HH",
                    "estabType": "Household",
                    "countryCode": "E"
                },
                "lpiLogicalStatus": "1",
                "confidenceScore": 100,
                "underlyingScore": 1
            }
        ],
        "historical": true,
        "epoch": "",
        "verbose": false
    },
    "status": {
        "code": 200,
        "message": "Ok"
    },
    "errors": []
}
</code></pre>
