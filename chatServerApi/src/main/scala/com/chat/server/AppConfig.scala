package com.chat.server

import com.typesafe.config.{Config, ConfigFactory}

object AppConfig {

  println(System.getProperties)

  private val environment = System.getProperty("APP_ENVIRONMENT", "dev")

  private val config: Config =
    ConfigFactory.load("application.conf").getConfig("app")
  private val EnvConfig =
    config.getConfig(environment).withFallback(config.getConfig("default"))

  val AppName: String = EnvConfig.getString("name")
  val AppVersion: String = EnvConfig.getString("version")
  val AppEnvironment: String = EnvConfig.getString("env")
}
