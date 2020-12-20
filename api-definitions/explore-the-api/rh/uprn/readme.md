<h1 class="jupiter">/addresses/rh/uprn/{uprn}</h1>

<p>Fetch single address by UPRN with named address type (custom output for RH).</p>

<h2>Request</h2>

<p><code>GET /addresses/rh/uprn/{uprn}</code></p>



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
            <td class="table--cell">addresstype</td>
            <td class="table--cell">string</td>
            <td class="table--cell">Version of the address to return - paf, welshpaf, nag, welshnag, nisra</td>
            <td class="table--cell">
                 Optional
                <br>Default: paf
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
    

   <h2>CURL example</h2>

   <pre><code>curl -uYOUR_APIKEY_FOLLOWED_BY_A_COLON: #API_URL#/addresses/eq/uprn</code></pre>

<h2>Sample Queries</h2>

<p><pre>/10013049457?addresstype=nag</pre></p>

<h2>Sample Output</h2>

<pre><code>
{
  "apiVersion": "1.0.0-SNAPSHOT",
  "dataVersion": "NA",
  "response": {
    "address": {
      "uprn": "10013049457",
      "formattedAddress": "Costa Coffee, 12 Bedford Street, Exeter, EX1 1LL",
      "addressLine1": "Costa Coffee",
      "addressLine2": "12 Bedford Street",
      "addressLine3": "",
      "townName": "Exeter",
      "postcode": "EX1 1LL",
      "censusAddressType": "NA",
      "censusEstabType": "NA",
      "countryCode": "E",
      "organisationName: "Costa Coffee"
    },
    "addressType": "NAG",
    "epoch": ""
  },
  "status": {
    "code": 200,
    "message": "Ok"
  },
  "errors": [
    
  ]
}
</code></pre>