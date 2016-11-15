@(id : String, action : String, method : String = "GET", search: String, reset: String)

<form id="@id" action="@action" method="@method">
  <input name="search" />
  <button type="submit">@search</button>
  <button type="reset">@reset</button>
</form>