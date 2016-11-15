@(js : Seq[String], css : Seq[String])

<head>
  @for(j <- js) {
    @helpers.js(j)
  }
  @for(s <- css) {
    @helpers.css(s)
  }
</head>