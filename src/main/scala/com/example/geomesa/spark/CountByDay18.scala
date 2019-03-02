/***********************************************************************
 * Copyright (c) 2013-2017 Commonwealth Computer Research, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0
 * which accompanies this distribution and is available at
 * http://www.opensource.org/licenses/apache2.0.php.
 ***********************************************************************/
/*
 * Counts, how many times gtd from 90s crosses itself
 * 
*/


package com.example.geomesa.spark

import org.apache.hadoop.conf.Configuration
import org.apache.spark.sql.functions.udf
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SparkSession}
import org.geotools.data.{DataStoreFinder, Query}
import org.geotools.factory.CommonFactoryFinder
import org.geotools.filter.text.ecql.ECQL
import org.locationtech.geomesa.accumulo.data._
import org.locationtech.geomesa.spark.GeoMesaSpark
import org.opengis.feature.simple.SimpleFeature

import scala.collection.JavaConversions._

object CountByDay18 {

  val gtdParams = Map(
      "accumulo.instance.id"   -> "ala",
      "accumulo.zookeepers"    -> "127.0.0.1",
      "accumulo.user"          -> "root",
      "accumulo.password"      -> "pass",
      "tableName" -> "buffer" )
  

  def main(args: Array[String]) {
  
 // Create SparkSession
     val sparkSession = SparkSession.builder()
       .appName("geoQuerySpark")
       .config("spark.sql.crossJoin.enabled", "true")
       .getOrCreate()
   
 // Create DataFrame using the "geomesa" format
 
   val bufferDataFrame = sparkSession.read
       .format("geomesa")
       .options(gtdParams)
       .option("geomesa.feature", "buffer")
       .load()
       bufferDataFrame.registerTempTable("buffer")
      
  //val sqlQuery = "select count(*) from buffer where st_contains(geom, (select geom from buffer))"

    val sqlQuery = "select geom from buffer where eventid like '199%'"

    //val sss = "st_makeBBOX(0.0, 0.0, 90.0, 90.0)"

    val resultDataFrame = sparkSession.sql(sqlQuery)

    var array = Array[String]()
    var i : Int =0;
    resultDataFrame.collect.foreach { 
      row => 
      {
        val rdf = sparkSession.sql("select count(*) from buffer where st_contains(st_geometryFromText('" + row(0)  + "'), geom)")
        //rdf.show()
        array = array :+ rdf.first().toString()
        i+=1
      }
    }
    for (i <- 0 to array.length-1){
      println(array(i));
    }
//    for (i in resultDataFrame){
//      println(sparkSession.sessionState)

  }

}
