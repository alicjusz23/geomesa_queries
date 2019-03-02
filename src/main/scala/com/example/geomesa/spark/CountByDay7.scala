/***********************************************************************
 * Copyright (c) 2013-2017 Commonwealth Computer Research, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0
 * which accompanies this distribution and is available at
 * http://www.opensource.org/licenses/apache2.0.php.
 ***********************************************************************/
/*
 * Prints DE-9IM pattern for gtd and urban areas, if gdt is inside such an area
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

object CountByDay7 {

  val gtdParams = Map(
      "accumulo.instance.id"   -> "ala",
        "accumulo.zookeepers"    -> "127.0.0.1",
          "accumulo.user"          -> "root",
            "accumulo.password"      -> "pass",
            "tableName" -> "gtd" )
  
  val shpParams = Map(
          "accumulo.instance.id"   -> "ala",
          "accumulo.zookeepers"    -> "127.0.0.1",
          "accumulo.user"          -> "root",
          "accumulo.password"      -> "pass",                                 
          "tableName" -> "shp" )


  def main(args: Array[String]) {
  
 // Create SparkSession
     val sparkSession = SparkSession.builder()
       .appName("testSpark")
       .config("spark.sql.crossJoin.enabled", "true")
       .master("local[*]")
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
      .option("geomesa.feature", "shp")
      .load()
      shpDataFrame.createOrReplaceTempView("shp")

    val sqlQuery  = "select st_relate(gtd.geom, shp.the_geom)  from gtd left join shp on st_contains(shp.the_geom, gtd.geom) where st_relate(gtd.geom, shp.the_geom) is not null" 
  //val sqlQuery = "select st_relate(shp.geom, gtd.geom) from gtd left join shp on (gtd.geom && shp.geom) limit 10"

  //zle    
  //   val sqlQuery = "select * from gtd where st_contains((select geom from gtd), (select geom from gdelt))"
  
  //ok
  //   val sqlQuery = "select * from gtd limit 10"
     val resultDataFrame = sparkSession.sql(sqlQuery)
    
     resultDataFrame.show
                    // val stringify = udf((vs: Seq[String]) => s"""[${vs.mkString(",")}]""")
/*                      resultDataFrame.write
                        .format("geomesa")
                        .options(dsParams2)
                        .option("geomesa.feature" , "gdelt")
                        .saveAsTable("result01")
                        */
//                     resultDataFrame.withColumn("geom", stringify(resultDataFrame.col("geom"))).write.csv("/home/ala/Documents/plik.csv")
                     //resultDataFrame.coalesce(1).write.csv("/home/ala/Documents/plik.csv")

  }

}
