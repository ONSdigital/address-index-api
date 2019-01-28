<h1>ONS Address Index API Developers Guide</h1>

<h2>Introduction</h2>

<p>The Office for National Statistics (ONS) Address Index API provides public sector organisations with address lookup functions, via HTTP. It allows users to look-up addresses in England and Wales, and to select and retrieve the official address.</p>

<p>The API is currently only available to a limited audience. Access can be requested by contacting <a href="mailto:ai.users@ons.gov.uk">Address Index</a></p>

<p>API access is restricted. API keys are required, and will be provided for those granted access.</p>

<p>This API is currently in Beta and still being developed. Please be aware that as a result there may occasionally be breaking changes as we enhance functionality and respond to feedback.</p>

<h2>Getting data from ONS Address Index API</h2>

<p>The Address Index API provides a number of end points to enable address look-up.</p>

<p>Each end point and its basic purpose are available from the <a href="explore-the-api/readme.md">Explore the API readme</a>.</p>
 
<p>A <a href="ai-swagger.json">full swagger definition</a> is also provided.</p>

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