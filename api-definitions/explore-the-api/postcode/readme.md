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

<h2>Sample Queries</h2>

<p><pre>EX4 3ET</pre></p>
<p><pre>ex26ga</pre></p>
<p><pre>EX1 1LL</pre></p>

   <h2>Sample Output</h2>

   <pre><code>{
     &quot;apiVersion&quot;: &quot;1.0.0&quot;,
      &quot;dataVersion&quot;: &quot;39&quot;,
      &quot;errors&quot;: [],
      &quot;response&quot;: {
          &quot;addresses&quot;: [
              {
                  &quot;classificationCode&quot;: &quot;CR08&quot;,
                  &quot;confidenceScore&quot;: 1,
                  &quot;formattedAddress&quot;: &quot;Body Language, 9 Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;formattedAddressNag&quot;: &quot;Body Language, 9 Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;formattedAddressNisra&quot;: &quot;&quot;,
                  &quot;formattedAddressPaf&quot;: &quot;9, Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;fromSource&quot;: &quot;EW&quot;,
                  &quot;geo&quot;: {
                      &quot;easting&quot;: 291760,
                      &quot;latitude&quot;: 50.72395,
                      &quot;longitude&quot;: -3.534838,
                      &quot;northing&quot;: 92717
                  },
                  &quot;lpiLogicalStatus&quot;: &quot;1&quot;,
                  &quot;parentUprn&quot;: &quot;0&quot;,
                  &quot;underlyingScore&quot;: 0,
                  &quot;uprn&quot;: &quot;100040222182&quot;,
                  &quot;welshFormattedAddressNag&quot;: &quot;&quot;,
                  &quot;welshFormattedAddressPaf&quot;: &quot;9, Lower North Street, Exeter, EX4 3ET&quot;
              },
              {
                  &quot;classificationCode&quot;: &quot;RD06&quot;,
                  &quot;confidenceScore&quot;: 1,
                  &quot;formattedAddress&quot;: &quot;Flat 1, 9 Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;formattedAddressNag&quot;: &quot;Flat 1, 9 Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;formattedAddressNisra&quot;: &quot;&quot;,
                  &quot;formattedAddressPaf&quot;: &quot;Flat 1, 9, Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;fromSource&quot;: &quot;EW&quot;,
                  &quot;geo&quot;: {
                      &quot;easting&quot;: 291760,
                      &quot;latitude&quot;: 50.72395,
                      &quot;longitude&quot;: -3.534838,
                      &quot;northing&quot;: 92717
                  },
                  &quot;lpiLogicalStatus&quot;: &quot;1&quot;,
                  &quot;parentUprn&quot;: &quot;100040222182&quot;,
                  &quot;underlyingScore&quot;: 0,
                  &quot;uprn&quot;: &quot;10013038165&quot;,
                  &quot;welshFormattedAddressNag&quot;: &quot;&quot;,
                  &quot;welshFormattedAddressPaf&quot;: &quot;Flat 1, 9, Lower North Street, Exeter, EX4 3ET&quot;
              },
              {
                  &quot;classificationCode&quot;: &quot;C&quot;,
                  &quot;confidenceScore&quot;: 1,
                  &quot;formattedAddress&quot;: &quot;10 Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;formattedAddressNag&quot;: &quot;10 Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;formattedAddressNisra&quot;: &quot;&quot;,
                  &quot;formattedAddressPaf&quot;: &quot;10, Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;fromSource&quot;: &quot;EW&quot;,
                  &quot;geo&quot;: {
                      &quot;easting&quot;: 291754,
                      &quot;latitude&quot;: 50.723988,
                      &quot;longitude&quot;: -3.5349243,
                      &quot;northing&quot;: 92721
                  },
                  &quot;lpiLogicalStatus&quot;: &quot;1&quot;,
                  &quot;parentUprn&quot;: &quot;0&quot;,
                  &quot;underlyingScore&quot;: 0,
                  &quot;uprn&quot;: &quot;100041045316&quot;,
                  &quot;welshFormattedAddressNag&quot;: &quot;&quot;,
                  &quot;welshFormattedAddressPaf&quot;: &quot;10, Lower North Street, Exeter, EX4 3ET&quot;
              },
              {
                  &quot;classificationCode&quot;: &quot;RD&quot;,
                  &quot;confidenceScore&quot;: 1,
                  &quot;formattedAddress&quot;: &quot;11A Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;formattedAddressNag&quot;: &quot;11A Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;formattedAddressNisra&quot;: &quot;&quot;,
                  &quot;formattedAddressPaf&quot;: &quot;11A, Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;fromSource&quot;: &quot;EW&quot;,
                  &quot;geo&quot;: {
                      &quot;easting&quot;: 291754,
                      &quot;latitude&quot;: 50.723988,
                      &quot;longitude&quot;: -3.5349243,
                      &quot;northing&quot;: 92721
                  },
                  &quot;lpiLogicalStatus&quot;: &quot;1&quot;,
                  &quot;parentUprn&quot;: &quot;0&quot;,
                  &quot;underlyingScore&quot;: 0,
                  &quot;uprn&quot;: &quot;100040222183&quot;,
                  &quot;welshFormattedAddressNag&quot;: &quot;&quot;,
                  &quot;welshFormattedAddressPaf&quot;: &quot;11A, Lower North Street, Exeter, EX4 3ET&quot;
              },
              {
                  &quot;classificationCode&quot;: &quot;RD&quot;,
                  &quot;confidenceScore&quot;: 1,
                  &quot;formattedAddress&quot;: &quot;11B Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;formattedAddressNag&quot;: &quot;11B Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;formattedAddressNisra&quot;: &quot;&quot;,
                  &quot;formattedAddressPaf&quot;: &quot;11B, Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;fromSource&quot;: &quot;EW&quot;,
                  &quot;geo&quot;: {
                      &quot;easting&quot;: 291754,
                      &quot;latitude&quot;: 50.723988,
                      &quot;longitude&quot;: -3.5349243,
                      &quot;northing&quot;: 92721
                  },
                  &quot;lpiLogicalStatus&quot;: &quot;1&quot;,
                  &quot;parentUprn&quot;: &quot;0&quot;,
                  &quot;underlyingScore&quot;: 0,
                  &quot;uprn&quot;: &quot;10013047887&quot;,
                  &quot;welshFormattedAddressNag&quot;: &quot;&quot;,
                  &quot;welshFormattedAddressPaf&quot;: &quot;11B, Lower North Street, Exeter, EX4 3ET&quot;
              },
              {
                  &quot;classificationCode&quot;: &quot;RD&quot;,
                  &quot;confidenceScore&quot;: 1,
                  &quot;formattedAddress&quot;: &quot;12 Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;formattedAddressNag&quot;: &quot;12 Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;formattedAddressNisra&quot;: &quot;&quot;,
                  &quot;formattedAddressPaf&quot;: &quot;12, Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;fromSource&quot;: &quot;EW&quot;,
                  &quot;geo&quot;: {
                      &quot;easting&quot;: 291748,
                      &quot;latitude&quot;: 50.724037,
                      &quot;longitude&quot;: -3.5350108,
                      &quot;northing&quot;: 92727
                  },
                  &quot;lpiLogicalStatus&quot;: &quot;1&quot;,
                  &quot;parentUprn&quot;: &quot;0&quot;,
                  &quot;underlyingScore&quot;: 0,
                  &quot;uprn&quot;: &quot;100040222185&quot;,
                  &quot;welshFormattedAddressNag&quot;: &quot;&quot;,
                  &quot;welshFormattedAddressPaf&quot;: &quot;12, Lower North Street, Exeter, EX4 3ET&quot;
              },
              {
                  &quot;classificationCode&quot;: &quot;RD06&quot;,
                  &quot;confidenceScore&quot;: 1,
                  &quot;formattedAddress&quot;: &quot;The Flat, 12 Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;formattedAddressNag&quot;: &quot;The Flat, 12 Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;formattedAddressNisra&quot;: &quot;&quot;,
                  &quot;formattedAddressPaf&quot;: &quot;Flat, 12, Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;fromSource&quot;: &quot;EW&quot;,
                  &quot;geo&quot;: {
                      &quot;easting&quot;: 291748,
                      &quot;latitude&quot;: 50.724037,
                      &quot;longitude&quot;: -3.5350108,
                      &quot;northing&quot;: 92727
                  },
                  &quot;lpiLogicalStatus&quot;: &quot;1&quot;,
                  &quot;parentUprn&quot;: &quot;100040222185&quot;,
                  &quot;underlyingScore&quot;: 0,
                  &quot;uprn&quot;: &quot;100041142136&quot;,
                  &quot;welshFormattedAddressNag&quot;: &quot;&quot;,
                  &quot;welshFormattedAddressPaf&quot;: &quot;Flat, 12, Lower North Street, Exeter, EX4 3ET&quot;
              },
              {
                  &quot;classificationCode&quot;: &quot;CR08&quot;,
                  &quot;confidenceScore&quot;: 1,
                  &quot;formattedAddress&quot;: &quot;Verve Hairdressing, 13 Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;formattedAddressNag&quot;: &quot;Verve Hairdressing, 13 Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;formattedAddressNisra&quot;: &quot;&quot;,
                  &quot;formattedAddressPaf&quot;: &quot;13, Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;fromSource&quot;: &quot;EW&quot;,
                  &quot;geo&quot;: {
                      &quot;easting&quot;: 291740,
                      &quot;latitude&quot;: 50.724064,
                      &quot;longitude&quot;: -3.535112,
                      &quot;northing&quot;: 92729
                  },
                  &quot;lpiLogicalStatus&quot;: &quot;1&quot;,
                  &quot;parentUprn&quot;: &quot;0&quot;,
                  &quot;underlyingScore&quot;: 0,
                  &quot;uprn&quot;: &quot;10013043834&quot;,
                  &quot;welshFormattedAddressNag&quot;: &quot;&quot;,
                  &quot;welshFormattedAddressPaf&quot;: &quot;13, Lower North Street, Exeter, EX4 3ET&quot;
              },
              {
                  &quot;classificationCode&quot;: &quot;RD04&quot;,
                  &quot;confidenceScore&quot;: 1,
                  &quot;formattedAddress&quot;: &quot;The Maisonette, 13A Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;formattedAddressNag&quot;: &quot;The Maisonette, 13A Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;formattedAddressNisra&quot;: &quot;&quot;,
                  &quot;formattedAddressPaf&quot;: &quot;13A, Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;fromSource&quot;: &quot;EW&quot;,
                  &quot;geo&quot;: {
                      &quot;easting&quot;: 291743,
                      &quot;latitude&quot;: 50.72404,
                      &quot;longitude&quot;: -3.5350752,
                      &quot;northing&quot;: 92727
                  },
                  &quot;lpiLogicalStatus&quot;: &quot;1&quot;,
                  &quot;parentUprn&quot;: &quot;0&quot;,
                  &quot;underlyingScore&quot;: 0,
                  &quot;uprn&quot;: &quot;100041142220&quot;,
                  &quot;welshFormattedAddressNag&quot;: &quot;&quot;,
                  &quot;welshFormattedAddressPaf&quot;: &quot;13A, Lower North Street, Exeter, EX4 3ET&quot;
              },
              {
                  &quot;classificationCode&quot;: &quot;CI&quot;,
                  &quot;confidenceScore&quot;: 1,
                  &quot;formattedAddress&quot;: &quot;Premier Developments, 14A Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;formattedAddressNag&quot;: &quot;Premier Developments, 14A Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;formattedAddressNisra&quot;: &quot;&quot;,
                  &quot;formattedAddressPaf&quot;: &quot;Kirkham Gould, 14A, Lower North Street, Exeter, EX4 3ET&quot;,
                  &quot;fromSource&quot;: &quot;EW&quot;,
                  &quot;geo&quot;: {
                      &quot;easting&quot;: 291738,
                      &quot;latitude&quot;: 50.7241,
                      &quot;longitude&quot;: -3.535154,
                      &quot;northing&quot;: 92733
                  },
                  &quot;lpiLogicalStatus&quot;: &quot;1&quot;,
                  &quot;parentUprn&quot;: &quot;0&quot;,
                  &quot;underlyingScore&quot;: 0,
                  &quot;uprn&quot;: &quot;100041045317&quot;,
                  &quot;welshFormattedAddressNag&quot;: &quot;&quot;,
                  &quot;welshFormattedAddressPaf&quot;: &quot;Kirkham Gould, 14A, Lower North Street, Exeter, EX4 3ET&quot;
              }
          ],
          &quot;epoch&quot;: &quot;&quot;,
          &quot;filter&quot;: &quot;&quot;,
          &quot;historical&quot;: true,
          &quot;limit&quot;: 10,
          &quot;maxScore&quot;: 0,
          &quot;offset&quot;: 0,
          &quot;postcode&quot;: &quot;EX4 3ET&quot;,
          &quot;total&quot;: 74,
          &quot;verbose&quot;: false
      },
      &quot;status&quot;: {
          &quot;code&quot;: 200,
          &quot;message&quot;: &quot;Ok&quot;
      }
}</code></pre>