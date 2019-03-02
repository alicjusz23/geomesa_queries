/***********************************************************************
 * Copyright (c) 2013-2017 Commonwealth Computer Research, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0
 * which accompanies this distribution and is available at
 * http://www.opensource.org/licenses/apache2.0.php.
 ***********************************************************************/
/*
 * Counts how far from gtd are cameras points, after 1990?
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

import java.io.{BufferedWriter, FileWriter, File}

object CountByDay19{

  val gtdParams = Map(
      "accumulo.instance.id"   -> "ala",
      "accumulo.zookeepers"    -> "127.0.0.1",
      "accumulo.user"          -> "root",
      "accumulo.password"      -> "pass",
      "tableName" -> "gtd_europe"
  )
  
  val camerasParams = Map(
    "accumulo.instance.id"   -> "ala",
    "accumulo.zookeepers"    -> "127.0.0.1",
    "accumulo.user"          -> "root",
    "accumulo.password"      -> "pass",
    "tableName" -> "cameras2"  
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
    
    val camerasDataFrame = sparkSession.read
      .format("geomesa")
      .options(camerasParams)
      .option("geomesa.feature", "cameras2")
      .load()
      camerasDataFrame.registerTempTable("cameras")

      val sqlQuery = "select geom from gtd where eventid like '199%'"

      val resultDataFrame = sparkSession.sql(sqlQuery)

      var array = Array[String]()
      var i : Int =0;
    resultDataFrame.collect.foreach {
      row =>
      {
        val rdf = sparkSession.sql("select st_distance(st_closestPoint(st_geometryFromText('" + row(0)  + "'), the_geom), the_geom) from cameras ")
        array = array :+ rdf.first().toString()
        i+=1
      }
    }
    val file = new File("distances")
    val bw = new BufferedWriter(new FileWriter(file))
    for (i <- 0 to array.length-1){
      //println(array(i));
      bw.write(array(i) + '\n')
    }
    bw.close()

  }

}
