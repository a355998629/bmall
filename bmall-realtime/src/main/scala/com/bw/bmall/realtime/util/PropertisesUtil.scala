package com.bw.bmall.realtime.util

import java.io.InputStreamReader
import java.util.Properties

object PropertisesUtil {
  def main(args: Array[String]): Unit = {
    val properties:Properties = PropertisesUtil.load("config.properties")
    println(properties.getProperty("kafka.broker.list"))
  }
  def load(PropertiesName:String):Properties={
    val props = new Properties()
    props.load(new InputStreamReader(Thread.currentThread()
      .getContextClassLoader.getResourceAsStream(PropertiesName),"utf-8"))
    props
  }
}
