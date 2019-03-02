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
import org.apache.spark.sql.functions._
import org.apache.spark.sql._
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
import scala.math._

//import sqlContext.implicits._

import java.io.{BufferedWriter, FileWriter, File}

object CountByDay21{

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


  //function to calculate distancies df
  def calcDistDF(sparkSession:SparkSession, sqlQuery:String) : DataFrame = {
    var distList = List[Double]()
    var i : Int =0;
    import sparkSession.implicits._

    var resultDataFrame = sparkSession.sql(sqlQuery)
    resultDataFrame.collect.foreach {
      row =>
      {
        val rdf = sparkSession.sql("select st_distance(st_closestPoint(the_geom, st_geometryFromText('" + row(0)  + "')), st_geometryFromText('" + row(0) + "')) from cameras ")
        //rdf.show()
        //rdf.printSchema()
        distList = distList :+ rdf.first().toString().substring(1,10).toDouble
        i+=1
      }
    }
    var listDF = distList.toDF()
    //listDF.show()
    return listDF
  }


  def main(args: Array[String]) {
  
 // Create SparkSession
     val sparkSession = SparkSession.builder()
       .appName("geoQuerySpark")
       .config("spark.sql.crossJoin.enabled", "true")
       .getOrCreate()

  //1degree to meters:
  val dConst = 111340

  import sparkSession.implicits._

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

      val sqlQuery = "select geom from gtd where eventid like '197%'"
      val sqlQuery2 = "select geom from gtd where eventid like '198%'"
      val sqlQuery3 = "select geom from gtd where eventid like '199%'"
      val sqlQuery4 = "select geom from gtd where eventid like '20%'"

      
      var listDF = calcDistDF(sparkSession, sqlQuery)
      var listDF2 = calcDistDF(sparkSession, sqlQuery2)
      var listDF3 = calcDistDF(sparkSession, sqlQuery3)
      var listDF4 = calcDistDF(sparkSession, sqlQuery4)

      println("Average distance from camers 1970s:")
      listDF.select(avg("value")*dConst).show()      
      println("Average distance from camers 1980s:")
      listDF2.select(avg("value")*dConst).show()
      println("Average distance from camers 1990s:")
      listDF.select(avg("value")*dConst).show()
      println("Average distance from camers 2000s and 2010s:")
      listDF2.select(avg("value")*dConst).show()
  }
}
