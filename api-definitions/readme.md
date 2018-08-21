<h1>ONS Address Index API Developers Guide</h1>

<h2>Introduction</h2>

<p>The Office for National Statistics (ONS) Address Index API provides public sector organisations with address lookup functions, via HTTP. It allows users to look-up addresses in England and Wales, and to select and retrieve the official address.</p>

<p>The API is currently only available to a limited audience. Access can be requested by contacting <a href="mailto:ai.users@ons.gov.uk">Address Index</a></p>

<p>API access is restricted. API keys are required, and will be provided for those granted access.</p>

<p>This API is currently in Beta and still being developed. Please be aware that as a result there may occasionally be breaking changes as we enhance functionality and respond to feedback.</p>

<h2>Getting data from ONS Address Index API</h2>

<p>The Address Index API provides a number of end points to enable address look-up. Each end point and its basic purpose are defined below. A <a href="ai-swagger.json">full swagger definition</a> is also provided.</p>

<h3>Address</h3>

<p>The addresses end point is the primary address search option. The API tokenises the given term, and returns a ranked list of responses.</p>

<p><strong>Query form:</strong> /addresses?input=&lt;search term&gt;<br>
<strong>With parameters:</strong> /addresses?input=&lt;search term&gt;&classificationfilter=RD</p>

<p>Address allows for the following parameters: offset, limit, classificationfilter, rangekm, lat, lon, historical, matchthreshold</p>

<h3>UPRN</h3>

<p>The uprn end point will return a single address entry when provided with a valid UPRN.</p>

<p><strong>Query form:</strong> /addresses/uprn/&lt;uprn&gt;<br>
<strong>With parameters:</strong> /addresses/uprn/&lt;uprn&gt;?historical=false</p>

<p>UPRN allows for the following parameters: historical</p>

<h3>Postcode</h3>

<p>The postcode end point will return all addresses matched to a valid, given postcode in an ordered response. This is generally in numerical order. Where a postcode includes multiple streets, the street entries should be grouped together.</p>

<p>This end point allows for postcodes with and without spaces</p>

<p><strong>Query form:</strong> /addresses/postcode/&lt;postcode&gt;<br>
<strong>With parameters:</strong> /addresses/postcode/&lt;postcode&gt;?classificationfilter=RD</p>

<p>Postcode allows for the following parameters: offset, limit, classificationfilter, historical</p>

<h3>Partial</h3>

<p>The partial end point is designed for typeahead/'search as you type' style queries, returning 20 results by default. It is currently configured for matching of the terms in any order, and can match against partial words. Results will become more accurate the more is typed. Test results are good for number or house name and postcode (postcode must include a space in this instance).</p>

<p>The partial end point returns only a subset of the address information for each result: formattedAddress, formattedAddressNag, formattedAddressPaf, WelshFormattedAddressNag, WelshFormattedAddressPaf, uprn, underlyingScore

<p><strong>This end point is still being refined. Unusual/unexpected results should be fed back to improve the service.</strong></p>

<p><strong>Query form:</strong> /addresses/partial/&lt;partial&gt;<br>
<strong>With parameters:</strong> /addresses/partial/&lt;partial&gt;?classificationfilter=RD</p>

<p>Partial allows for the following parameters: offset, limit, classificationfilter, historical</p>

<h3>Bulk</h3>

<p>The bulk end points are not currently available via the external API</p>

<h3>Parameters</h3>

<h4>offset</h4>
<p>Optional<br>Specifies the offset from zero, used for pagination (default: 0, maximum: 1000)</p>

<h4>limit</h4>
<p>Optional<br>Specifies the number of addresses to return (default: 10, maximum: 100)</p>

<h4>classificationfilter</h4>
<p>Optional<br>Classification code filter. Can be pattern match e.g. ZW*, exact match e.g. RD06, or a preset keyword e.g. residential; commercial</p>

<h4>rangekm</h4>
<p>Optional<br>Limit results to those within this number of kilometers of point (decimal: e.g. 0.1)</p>

<h4>lat</h4>
<p>Optional<br>Latitude of point in decimal format (e.g. 50.705948)</p>

<h4>lon</h4>
<p>Optional<br>Longitude of point in decimal format (e.g. -3.5091076)</p>

<h4>historical</h4>
<p>Optional<br>Include historical addresses (default: true)</p>

<h4>matchthreshold</h4>
<p>Optional<br>Minimum confidence score (percentage) for match to be included in results (default: 5.0)</p>


<h2>Address Index Simple Tester</h2>

The Address Index Simple Tester allows users to use the various functions available via the API, providing an input request and receiving a response from the API.

<strong>Note that for any option other than 'Version' or 'Swagger' an API key must be provided to get a response.</strong>

<h3>Functionality</h3>

The Tester has 8 functions: 

<ol>
<li><strong>Version:</strong> Returns version information for the API and the data (AddressBase).</li>
<li><strong>Swagger:</strong> Returns the swagger definition.</li>
<li><strong>Single Match:</strong> Returns a ranked list of addresses matching the search query in the specified format.</li>
<li><strong>Debug:</strong> Returns query that is sent to Elastic (for debug purposes).</li>
<li><strong>UPRN:</strong> Returns a single address, identified by its UPRN.</li>
<li><strong>Bulk:</strong> Will process all BulkQuery items in the BulkBody returns reduced information on found addresses (UPRN, formatted address).</li>
<li><strong>Bulk Full:</strong> Will process all BulkQuery items in the BulkBody this version is slower and more memory-consuming since all the information on found addresses is returned.</li>
<li><strong>Bulk Debug:</strong> Bulk end point that accepts tokens instead of input texts for each address.</li>
</ol>

<h3>Using the Simple Tester</h3>

The Simple Tester is a self-contained HTML file located at api-definitions/ai-demo.html, and can be run as a local file in Internet Explorer.

Each function has its own tab, and is accessed by clicking the tab.

Your API key needs to be entered in the input field in the page banner to return any results other than 'Version' or 'Swagger'.

Where an input is required from the user, a default sample input is provided which will return a response, however the input can be changed.