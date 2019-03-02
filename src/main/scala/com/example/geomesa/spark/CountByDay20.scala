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

object CountByDay20{

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

  //case class listClass(v: Double)

  def main(args: Array[String]) {
  
 // Create SparkSession
     val sparkSession = SparkSession.builder()
       .appName("geoQuerySpark")
       .config("spark.sql.crossJoin.enabled", "true")
       .getOrCreate()

  import sparkSession.implicits._

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

      val sqlQuery = "select geom from gtd where eventid like '19%'"
      val sqlQuery2 = "select geom from gtd where eventid like '20%'"

      val resultDataFrame = sparkSession.sql(sqlQuery)
      val resultDataFrame2 = sparkSession.sql(sqlQuery2)

      var distList = List[Double]()
      var distList2 = List[Double]()
      var i : Int =0;
    
      //val listDF = Seq.empty[listClass].toDF
      //listDF.printSchema()
      //listDF.registerTempTable("listTable")
      //sparkSession.sql("create temporary view listTable (dist double)")


      //first query
      resultDataFrame.collect.foreach {
        row =>
        {
          val rdf = sparkSession.sql("select st_distance(st_closestPoint(st_geometryFromText('" + row(0)  + "'), the_geom), the_geom) from cameras ")
          println("\nala\n\n")
          //rdf.printSchema()
          //listDF.union(rdf.toDF())
          //val newRow = rdf.first()
          //sparkSession.sql("Insert into listTable values (" + rdf.first().getDouble(0).toString() + ")")
          //listDF.map(newRow => (newRow(0)))
          //listDF.show()
          distList = distList :+ rdf.first().toString().substring(1,10).toDouble
          i+=1
        }
      }
      //val listDF = sparkSession.sql("select * from listTable")
      //var listDF = sparkSession.createDataFrame(sparkSession.sparkContext.makeRDD(List(Row.fromSeq(array))))
      var listDF = distList.toDF()
      listDF.show()
      //listDF.printSchema()
      
      
      //second query
      i=0
      resultDataFrame2.collect.foreach {
        row =>
        {
          val rdf = sparkSession.sql("select st_distance(st_closestPoint(st_geometryFromText('" + row(0)  + "'), the_geom), the_geom) from cameras ")
          distList2 = distList2 :+ rdf.first().toString().substring(1,10).toDouble
          i+=1
        }
      }
      var listDF2 = distList2.toDF()
      
      listDF.select(avg("value")).show()
      listDF2.select(avg("value")).show()
    /*
    val file = new File("distances")
    val bw = new BufferedWriter(new FileWriter(file))
    for (i <- 0 to array.length-1){
      
      bw.write(array(i) + '\n')
    }
    bw.close()
    * /
    println("\n")
    rdf.show()
    println("\n")
    rdf.select(avg("col1")).show()
*/
  }
}
