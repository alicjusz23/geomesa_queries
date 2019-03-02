/***********************************************************************
 * Copyright (c) 2013-2017 Commonwealth Computer Research, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0
 * which accompanies this distribution and is available at
 * http://www.opensource.org/licenses/apache2.0.php.
 ***********************************************************************/
/*
 * Creates table tempor with joined gtd and global urban areas, when they are inside, prints 10 top rows
 * Very small tables, each ~50 rows
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

object CountByDay10{

  val gtdParams = Map(
      "accumulo.instance.id"   -> "ala",
      "accumulo.zookeepers"    -> "127.0.0.1",
      "accumulo.user"          -> "root",
      "accumulo.password"      -> "pass",
      "tableName" -> "gtd_europe_sss"
  )
  
  val shpParams = Map(
      "accumulo.instance.id"   -> "ala",
      "accumulo.zookeepers"    -> "127.0.0.1",
      "accumulo.user"          -> "root",
      "accumulo.password"      -> "pass",                                 
      "tableName" -> "shp_eu_sss" )

  val dsParams2 = Map(
    "accumulo.instance.id"   -> "ala",
    "accumulo.zookeepers"    -> "127.0.0.1",
    "accumulo.user"          -> "root",
    "accumulo.password"      -> "pass",
    "tableName" -> "tempor"
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

  //  val sqlQuery  = "create temporary view temp as (select * from gtd left join shp_small on st_contains(shp_small.the_geom, gtd.geom))"

    val sqlQuery  = "select * from gtd left join shp on st_contains(shp.the_geom, gtd.geom) is not null" 
//    val sqlQuery = "select count(*) from gtd" 

//    val sqlQuery2 = "select (*) from tempor"    

    //sparkSession.sql(sqlQuery)
    val resultDataFrame = sparkSession.sql(sqlQuery)
    val resultNewDataFrame = resultDataFrame.drop("__fid__")
    resultNewDataFrame.printSchema()
   
    resultNewDataFrame.write
      .format("geomesa")
      .options(dsParams2)
      .option("geomesa.feature" , "tempor")
      .saveAsTable("tempor")
   
    val sqlQuery3 = "select * from tempor"
    val temporDataFrame = sparkSession.sql(sqlQuery3)
    temporDataFrame.show(10)

  }

}
