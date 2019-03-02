/***********************************************************************
 * Copyright (c) 2013-2017 Commonwealth Computer Research, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0
 * which accompanies this distribution and is available at
 * http://www.opensource.org/licenses/apache2.0.php.
 ***********************************************************************/
/*
 * Creates temporary view with column distance based on weapontype, creates buffers based on the distancies, saves it as a table buffer and prints it
 *
 * Before running create schema:
 * /opt/geomesa-accumulo_2.11-2.0.0/bin/geomesa-accumulo create-schema -u root -p pass -c buffer -f buffer -s eventid:String,geom:Polygon:srid=4326,weapontype:String,country_tx:String,city:String
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

object CountByDay13{

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
    
    
   val sqlQuery  = "create temporary view temp as (select eventid, geom, case weapontype when 1 then 20000 when 2 then 5000 when 3 then 7000 when 4 then 10000 when 5 then 300 when 8 then 500 when 9 then 10 when 10 then 1000 when 11 then 10 when 6 then 1100 else 100 end as distance, weapontype, country_tx, city from gtd)"

//    val sqlQuery  = "create temporary view temp as (select if(weapontype=5,5000,10000) as distance, geom from gtd)"

    val sqlQuery2 = "create temporary view temp2 as (select eventid, st_bufferPoint(geom, distance) as geom, weapontype, country_tx, city from temp)" 
   //val sqlQuery1 = "alter view temp add (distance String)"
    //val sqlQuery1 = "alter table buffer add columns (distance String)"

//    val sqlQuery2 = "insert into table buffer values weapontype sum(weapontype, weapontype)"

    val sqlQuery3 = "select * from temp2"

    sparkSession.sql(sqlQuery)
   // sparkSession.sql(sqlQuery1)
    sparkSession.sql(sqlQuery2)
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
