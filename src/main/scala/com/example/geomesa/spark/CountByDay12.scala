/***********************************************************************
 * Copyright (c) 2013-2017 Commonwealth Computer Research, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0
 * which accompanies this distribution and is available at
 * http://www.opensource.org/licenses/apache2.0.php.
 ***********************************************************************/
/*
 * Creates temporary view as bufferf of gtd points and weapon type, saves it to new table in accumulo
 * NEED to create SFT before running, if does not exists
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

object CountByDay12{

  val gtdParams = Map(
      "accumulo.instance.id"   -> "ala",
      "accumulo.zookeepers"    -> "127.0.0.1",
      "accumulo.user"          -> "root",
      "accumulo.password"      -> "pass",
      "tableName" -> "gtd_europe"
  )
  
  val dsParams2 = Map(
    "accumulo.instance.id"   -> "ala",
    "accumulo.zookeepers"    -> "127.0.0.1",
    "accumulo.user"          -> "root",
    "accumulo.password"      -> "pass",
    "tableName" -> "buffer"  
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
    
    
    val sqlQuery  = "create temporary view temp as (select st_bufferPoint(geom, 1000.0) as geom, weapontype from gtd)"

    val sqlQuery3 = "select * from temp"

    sparkSession.sql(sqlQuery)
    val resultDataFrame = sparkSession.sql(sqlQuery3)

    resultDataFrame.write
      .format("geomesa")
      .options(dsParams2)
      .option("geomesa.feature" , "buffer")
   //   .option("geomesa.mixed.geometries", "Boolean.TRUE")
      .saveAsTable("buffer")

    println("\nala")
    resultDataFrame.printSchema()
    resultDataFrame.show()
   
  }

}
