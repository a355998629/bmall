package com.bw.bmall.realtime.app

import java.text.SimpleDateFormat
import java.util
import java.util.Date

import com.alibaba.fastjson.JSON
import com.bw.bmall.common.constant.BmallConstant
import com.bw.bmall.realtime.bean.StartUpLog
import com.bw.bmall.realtime.util.{JedisUtil, MyKafkaUtils}
import org.apache.hadoop.conf.Configuration
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.SparkConf
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.{CanCommitOffsets, HasOffsetRanges, OffsetRange}
import org.apache.spark.streaming.{Durations, StreamingContext}
import redis.clients.jedis.Jedis
import org.apache.phoenix.spark._
/**
  * 统计uv的数量
  * 1.消费kafka中的数据
  * 2.整理数据 case class
  * 3.根据redis清单 进行过滤
  * 4.把用户访问清单再报错到redis中
  * 5.把真正的明细数据保存到hbase中
  */
object DauApp {
  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setMaster("local[*]")
      .setAppName("dauApp")
    val ssc: StreamingContext = new StreamingContext(conf,Durations.seconds(5))

    val inputDStream:InputDStream[ConsumerRecord[String,String]] =
      MyKafkaUtils.getKafkaStream(BmallConstant.KAFKA_STARTUP,ssc)

    inputDStream.foreachRDD(rdd=> {
      val offsets: Array[OffsetRange] = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
      if(!rdd.isEmpty()){
        val record: RDD[String] = rdd.map(_.value())
        val startLog: RDD[StartUpLog] = record.map(json => {
          val log: StartUpLog = JSON.parseObject(json, classOf[StartUpLog])
          val sdf: SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH")
          val arr: Array[String] = sdf.format(new Date(log.ts)).split(" ")
          log.logDate = arr(0)
          log.logHour = arr(1)
          log
        })
        //3.过滤数据 如果之前统计过 我们不再统计
        val jedis: Jedis = JedisUtil.getJedisClient
        val key: String = "dua" + new SimpleDateFormat("yyyy-MM-dd").format(new Date())
        val midSet: util.Set[String] = jedis.smembers(key)
        jedis.close()
        println("##########今天已经统计过的mid："+midSet)
        //广播到每个节点
        val br: Broadcast[util.Set[String]] = ssc.sparkContext.broadcast(midSet)
        //对当前批次数据进行过滤
        println("#########过滤之前："+startLog.count())
        val filtered: RDD[StartUpLog] = startLog.filter(log => {
          !br.value.contains(log.mid)
        })
        println("过滤完的数据："+filtered.count())

        //4 先将今天访问的mid 存到redis中
        filtered.foreachPartition(itr=>{
          val jedis = JedisUtil.getJedisClient
          itr.foreach(log=>{
            //存到redis中的是什么类型（String,list,set,hset zset） key是什么  value是什么类型
            val key = "dua"+log.logDate
            jedis.sadd(key,log.mid)
          })
          jedis.close()
        })
        //到此为止批次与批次之间 不会出现重复数据 但是每个批次之中 重复数据我们没考虑到
        val grouped: RDD[(String, Iterable[StartUpLog])] = filtered.map(log=>(log.mid,log)).groupByKey()
        val redsult: RDD[StartUpLog] = grouped.flatMap {
          case (mid, itr) => {
            itr.toList.take(1)
          }
        }

        //往hbase中插数据  面向phoenix插入
        redsult.saveToPhoenix("BMALL_DAU",Seq(
          "MID","UID","APPID","AREA","OS","CH","LOGTYPE","VS","LOGDATE","LOGHOUR","TS"
        ),new Configuration(),
          Some("bw66,bw67,bw68:2181"))

        //手动更新偏移量
        inputDStream.asInstanceOf[CanCommitOffsets].commitAsync(offsets)
      }

    })


    ssc.start()
    ssc.awaitTermination()
  }
}
