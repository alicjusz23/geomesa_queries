/***********************************************************************
 * Copyright (c) 2013-2017 Commonwealth Computer Research, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License, Version 2.0
 * which accompanies this distribution and is available at
 * http://www.opensource.org/licenses/apache2.0.php.
 ***********************************************************************/

package com.example.geomesa.spark

import java.text.SimpleDateFormat

import org.apache.hadoop.conf.Configuration
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

object CountByDay2 {

  val dsParams = Map(
      "accumulo.instance.id"   -> "ala",
        "accumulo.zookeepers"    -> "127.0.0.1",
          "accumulo.user"          -> "root",
            "accumulo.password"      -> "pass",
            "tableName" -> "gdelt2" )

  def main(args: Array[String]) {
  
    // Create SparkSession
     val sparkSession = SparkSession.builder()
       .appName("testSpark")
         .config("spark.sql.crossJoin.enabled", "true")
           .master("local[*]")
             .getOrCreate()
    
             // Create DataFrame using the "geomesa" format
             val dataFrame = sparkSession.read
               .format("geomesa")
                 .options(dsParams)
                   .option("geomesa.feature", "gdelt")
                     .load()
                    dataFrame.createOrReplaceTempView("gdelt")
    
                     // Query against the "chicago" schema
                     val sqlQuery = "select * from gdelt where st_contains(st_makeBBOX(0.0, 0.0, 90.0, 90.0), geom)"
                     val resultDataFrame = sparkSession.sql(sqlQuery)
    
                     resultDataFrame.show
  
  }

}
