/***********************************************************************
 * Copyright (c) 2013-2017 Commonwealth Computer Research, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0
 * which accompanies this distribution and is available at
 * http://www.opensource.org/licenses/apache2.0.php.
 ***********************************************************************/
/*
 * Creates temporary view as join of gtd and global urban areas, when they are inside, prints it and counts elements in it and prints this numer
 */


package com.example.geomesa.spark

import java.text.SimpleDateFormat

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

object CountByDay11{

  val gtdParams = Map(
      "accumulo.instance.id"   -> "ala",
      "accumulo.zookeepers"    -> "127.0.0.1",
      "accumulo.user"          -> "root",
      "accumulo.password"      -> "pass",
      "tableName" -> "gtd_europe"
  )
  
  val shpParams = Map(
      "accumulo.instance.id"   -> "ala",
      "accumulo.zookeepers"    -> "127.0.0.1",
      "accumulo.user"          -> "root",
      "accumulo.password"      -> "pass",                                 
      "tableName" -> "shp_eu_sss" 
  )

  def main(args: Array[String]) {
  
 // Create SparkSession
     val sparkSession = SparkSession.builder()
       .appName("geoQuerySpark")
       .config("spark.sql.crossJoin.enabled", "true")
       .getOrCreate()
   
 // Create DataFrame using the "geomesa" format
 
   val gtdDataFrame = sparkSession.read
       .format("geomesa")
       .options(gtdParams)
       .option("geomesa.feature", "gtd")
       .load()
       gtdDataFrame.registerTempTable("gtd")
    
    val shpDataFrame = sparkSession.read
      .format("geomesa")
      .options(shpParams)
      .option("geomesa.feature", "shp_eu_sss")
      .load()
      shpDataFrame.createOrReplaceTempView("shp")

    val sqlQuery  = "create temporary view temp as (select * from gtd left join shp on st_contains(shp.the_geom, gtd.geom))"

    val sqlQuery1 = "select * from temp"

    val sqlQuery2 = "select count(*) from temp"
    sparkSession.sql(sqlQuery)
    val rDataFrame = sparkSession.sql(sqlQuery1)
    rDataFrame.show()
    println("\n")
    val resultDataFrame = sparkSession.sql(sqlQuery2)
    resultDataFrame.show()
   
  }

}
