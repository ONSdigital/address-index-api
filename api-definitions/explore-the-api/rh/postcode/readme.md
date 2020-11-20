<h1>/addresses/rh/postcode/{postcode}</h1>

<p>Search for an address by postcode. RH Version</p>

<h2>Request</h2>

<p><code>GET /addresses/rh/postcode/{postcode}</code></p>
   

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
        <td class="table--cell">Classification code filter. Can be pattern match (ZW*), exact match (RD06), multiple exact match (RD02,RD04) or a preset keyword such as residential, commercial, workplace or educational</td>
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
            <br>Default: False
        </td>
    </tr>
    <tr class="table--row">
       <td class="table--cell">favourpaf</td>
       <td class="table--cell">string</td>
       <td class="table--cell">paf beats nag on draw for best match</td>
       <td class="table--cell">
            Optional
       <br>Default: True
       </td>
    </tr>
    <tr class="table--row">
       <td class="table--cell">favourwelsh</td>
       <td class="table--cell">string</td>
       <td class="table--cell">Welsh beats English on draw for best match</td>
       <td class="table--cell">
          Optional
          <br>Default: False
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
                   <td class="table--cell">eboost</td>
                   <td class="table--cell">string</td>
                   <td class="table--cell">Set to 0 to exclude addresses in England</td>
                   <td class="table--cell">
                        Optional
                        <br>Default: 1.0
                    </td>
                </tr>
                <tr class="table--row">
                    <td class="table--cell">nboost</td>
                    <td class="table--cell">string</td>
                    <td class="table--cell">Set to 0 to exclude addresses in Northern Ireland</td>
                    <td class="table--cell">
                        Optional
                        <br>Default: 1.0
                    </td>
                </tr>
                <tr class="table--row">
                    <td class="table--cell">sboost</td>
                    <td class="table--cell">string</td>
                    <td class="table--cell">Set to 0 to exclude addresses in Scotland</td>
                    <td class="table--cell">
                        Optional
                        <br>Default: 1.0
                    </td>
                </tr>
                <tr class="table--row">
                    <td class="table--cell">wboost</td>
                    <td class="table--cell">string</td>
                    <td class="table--cell">Set to 0 to exclude addresses in Wales</td>
                    <td class="table--cell">
                         Optional
                        <br>Default: 1.0
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

<h2>Sample Queries</h2>

<p><pre>EX4 3ET</pre></p>
<p><pre>ex26ga</pre></p>
<p><pre>EX1 1LL</pre></p>

   <h2>Sample Output</h2>

   <pre><code>
 {
   "apiVersion": "1.0.0-SNAPSHOT",
   "dataVersion": "NA",
   "response": {
     "postcode": "EX4 3ET",
     "addresses": [
       {
         "uprn": "100040222182",
         "formattedAddress": "9 Lower North Street, Exeter, EX4 3ET",
         "addressType": "PAF",
         "censusAddressType": "NA",
         "censusEstabType": "NA",
         "countryCode": "E"
       },
       {
         "uprn": "10013038165",
         "formattedAddress": "Flat 1, 9 Lower North Street, Exeter, EX4 3ET",
         "addressType": "PAF",
         "censusAddressType": "HH",
         "censusEstabType": "Household",
         "countryCode": "E"
       },
       {
         "uprn": "100041045316",
         "formattedAddress": "10 Lower North Street, Exeter, EX4 3ET",
         "addressType": "PAF",
         "censusAddressType": "NA",
         "censusEstabType": "NA",
         "countryCode": "E"
       },
       {
         "uprn": "100040222183",
         "formattedAddress": "11A Lower North Street, Exeter, EX4 3ET",
         "addressType": "PAF",
         "censusAddressType": "HH",
         "censusEstabType": "Household",
         "countryCode": "E"
       },
       {
         "uprn": "10013047887",
         "formattedAddress": "11B Lower North Street, Exeter, EX4 3ET",
         "addressType": "PAF",
         "censusAddressType": "HH",
         "censusEstabType": "Household",
         "countryCode": "E"
       }
     ],
     "filter": "",
     "historical": false,
     "epoch": "",
     "limit": 5,
     "offset": 0,
     "total": 74,
     "maxScore": 0,
     "verbose": false
   },
   "status": {
     "code": 200,
     "message": "Ok"
   },
   "errors": [
     
   ]
 }
</code></pre>