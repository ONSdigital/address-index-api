package utils

import javax.inject.Inject
import play.api.http.DefaultHttpErrorHandler
import play.api.routing.Router
import play.api.{Configuration, OptionalSourceMapper}

class ErrorHandler @Inject() (
  env          : play.api.Environment,
  config       : Configuration,
  sourceMapper : OptionalSourceMapper,
  router       : javax.inject.Provider[Router])
  extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {
}