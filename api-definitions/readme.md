<h1>ReadMe for ONS Address Index API</h1>

The ONS Address Index API provides public sector organisations with address lookup functions, via an API.

A key is required to access the API. To request a key, contact <a href="mailto:ai.users@ons.gov.uk">Address Index</a>

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