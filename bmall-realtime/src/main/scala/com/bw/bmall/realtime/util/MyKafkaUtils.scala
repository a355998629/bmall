package com.bw.bmall.realtime.util

import java.util.Properties

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}

object MyKafkaUtils {
  private val properties:Properties = PropertisesUtil.load("config.properties")
  val broker = properties.getProperty("kafka.broker.list")
  val kafkaParams = Map(
    "bootstrap.servers" -> broker,//用于初始化连接到集群的地址
    "key.deserializer" -> classOf[StringDeserializer],
    "value.deserializer" -> classOf[StringDeserializer],
    //消费者组
    "group.id" -> "bmall_consumer_group1",
    //更新偏移量
    "auto.offset.reset" -> "latest",
    //如果是true 则这个消费者的偏移量会在后台自动提交，但是kafka宕机容易丢数据
    //如果是false 需要手动维护kafka偏移量
    "enble.auto.commit" -> (false:java.lang.Boolean)
  )
  def getKafkaStream(topic:String,ssc:StreamingContext):InputDStream[ConsumerRecord[String,String]]={
    val dstream = KafkaUtils.createDirectStream[String,String](
      ssc,LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String,String](Array(topic),kafkaParams)
    )
    dstream
  }
}
