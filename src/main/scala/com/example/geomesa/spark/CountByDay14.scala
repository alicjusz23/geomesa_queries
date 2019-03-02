/***********************************************************************
 * Copyright (c) 2013-2017 Commonwealth Computer Research, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0
 * which accompanies this distribution and is available at
 * http://www.opensource.org/licenses/apache2.0.php.
 ***********************************************************************/
/*
 * Creates temporary view with column distance based on weapontype, creates buffers based on the distancies, saves it as a table buffer and prints it
 * Before running create schema:
 * /opt/geomesa-accumulo_2.11-2.0.0/bin/geomesa-accumulo create-schema -u root -p pass -c buffer -f buffer -s eventid:String,geom:Polygon:srid=4326,weapontype:String
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

object CountByDay14{

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
    

    val sqlQuery = "select count(*) as success from gtd where success=1"

    val sqlQuery2 = "select count(*) as nkill_1 from gtd where nkill > 1"

    val sqlQuery3 = "select count(*) as suicide from gtd where suicide=1"

    val resultDataFrame = sparkSession.sql(sqlQuery)
    val resultDataFrame2 = sparkSession.sql(sqlQuery2)
    val resultDataFrame3 = sparkSession.sql(sqlQuery3)

    resultDataFrame.show()
    resultDataFrame2.show()
    resultDataFrame3.show()

  }

}
