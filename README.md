<a href="https://cdap-users.herokuapp.com/"><img alt="Join CDAP community" src="https://cdap-users.herokuapp.com/badge.svg?t=redshifttos3-action"/></a> [![Build Status](https://travis-ci.org/hydrator/to-utf8-action.svg?branch=develop)](https://travis-ci.org/hydrator/redshifttos3-action) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) <img alt="CDAP Action" src="https://cdap-users.herokuapp.com/assets/cdap-action.svg"/> []() <img src="https://cdap-users.herokuapp.com/assets/cm-available.svg"/>

RedshiftToS3 Action Plugin
==========================

Description
-----------
The RedshiftToS3 Action runs the UNLOAD command on AWS to save the results of a query from Redshift to one or more 
files on Amazon Simple Storage Service (Amazon S3). For more information on the UNLOAD command, please see 
the [AWS Documentation](http://docs.aws.amazon.com/redshift/latest/dg/r_UNLOAD.html).


Use Case
--------
This action is used whenever you need to quickly move data from Redshift to an S3 bucket before processing in a pipeline.
For example, a financial customer would like to quickly unload financial reports into S3 that have been generated from
processing that is happening in Redshift. The pipeline would have a Redshift to S3 action at the beginning,
and then leverage the S3 source to read that data into a processing pipeline.

Properties
----------
| Configuration | Required | Default | Description |
| :------------ | :------: | :-----: | :---------- |
| **Query** | **Y** | None | A SELECT query, the results of which are unloaded from Redshift table to the S3 bucket. | 
| **Redshift Cluster URL** | **Y** | None | JDBC Redshift DB url for connecting to the redshift cluster. The URL should include the port and database name. It should be in the format: ``jdbc:redshift://<endpoint-address>:<endpoint-port>/<db-name>``. This is only used to issue the UNLOAD command, not to execute the query. This plugin leverages the ``com.amazon.redshift.jdbc42.Driver`` for making connections. | 
| **Redshift Master User** | **Y** | None | Master user for the Redshift cluster to connect to.
| **Redshift Master Password** | **Y** | None | Master password for Redshift cluster to connect to.
| **S3 Data Path** | **Y** | None | The full path, including bucket name, to the location on Amazon S3 where Amazon Redshift will write the output file objects, including the manifest file if MANIFEST is specified. It should be in the format: ``s3://object-path/name-prefix``. This supports globbing syntax such as ``prefix-*``. To read an entire directory, ensure the path ends with a trailing ``/``.
| **Access Key** | **N** | None | The Access Key provided by AWS so that the Redshift cluster can write to the S3 location.
| **Secret Access Key** | **N** | None | AWS Secret Key provided by AWS so that the Redshift cluster can write to the S3 location.
| **IAM Role** | **N** | None | IAM role having GET, LIST, and PUT permissions to the S3 bucket. The IAM Role should be in the form of ``arn:aws:iam::<aws-account-id>:role/<role-name>``. For more information about IAM Roles and the UNLOAD command, see the [AWS Documentation](http://docs.aws.amazon.com/redshift/latest/mgmt/copy-unload-iam-role.html).
| **Output Path Token** | **N** | ``filePath`` | The macro key used to store the S3 file path for the unloaded file(s). Plugins that run at later stages in the pipeline can retrieve the file path using this key through macro substitution. For example, you could use ``${filePath}`` if ``filePath`` is the key specified.
| **Create Manifest?** | **N** | false | Used to determine if a manifest file is to be created during the unload. The manifest file explicitly lists the data files that are created by the UNLOAD process. It will be created in the same directory as the UNLOADed files.
| **Delimiter** | **N** | &#124; | Single ASCII character that is used to separate fields in the output file.
| **Parallel** | **N** | true | Used to determine if UNLOAD writes data in parallel to multiple files, according to the number of slices in the cluster.
| **Compression?** | **N** | NONE | Unloads data into one or more compressed files. Can be one of the following: NONE, BZIP2 or GZIP.
| **Allow Overwrite?** | **N** | false | Used to determine if UNLOAD will overwrite existing files, including the manifest file, if the file is already available.
| **Add Quotes?** | **N** | false | Used to determine if UNLOAD places quotation marks around each unloaded data field, so that Redshift can unload data values that contain the delimiter itself.
| **Escape?** | **N** | false | Used to determine if escape character (\\) is to be placed before CHAR and VARCHAR columns in delimited unload file for the following characters: Linefeed ``\n``, Carriage return ``\r``, delimiter, escape character \\, and quote character: " or '.

Usage Notes
-----------
1. Both Access/Secret Keys and IAM Role should not be provided at the same time. Only one authentication mechanism should be provided.
1. The Redshift table from which user wants to unload the data should already exist in the database specified by the ``Redshift Cluster URL``.
1. The Amazon S3 bucket where Amazon Redshift will write the output files **must reside** in the same region as your cluster.
1. S3 data path should start with ``s3://`` and not with the ``s3n://`` or ``s3a://`` URI scheme.

Getting Started
---------------

Prerequisites
--------------
CDAP version 4.1.x or higher.

Building Plugins
----------------
You get started with RedshiftToS3 action plugin by building directly from the latest source code::

   git clone git@github.com:hydrator/redshifttos3-action.git
   cd redshifttos3-action
   mvn clean package

After the build completes, you will have a JAR for each plugin under each
``<plugin-name>/target/`` directory.

Deploying Plugins
-----------------
You can deploy a plugin using the CDAP CLI::

  > load artifact <target/plugin-jar> config-file <resources/plugin-config>

  > load artifact target/redshifttos3-action-plugin-<version>.jar \
         config-file target/redshifttos3-action-plugin-<version>.json

You can build without running tests: ``mvn clean install -DskipTests``

Mailing Lists
-------------
CDAP User Group and Development Discussions:

- `cdap-user@googlegroups.com <https://groups.google.com/d/forum/cdap-user>`__

The *cdap-user* mailing list is primarily for users using the product to develop
applications or building plugins for appplications. You can expect questions from
users, release announcements, and any other discussions that we think will be helpful
to the users.

IRC Channel
-----------
CDAP IRC Channel: #cdap on irc.freenode.net


License and Trademarks
======================

Copyright © 2017 Cask Data, Inc.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the
License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
either express or implied. See the License for the specific language governing permissions
and limitations under the License.

Cask is a trademark of Cask Data, Inc. All rights reserved.

Apache, Apache HBase, and HBase are trademarks of The Apache Software Foundation. Used with
permission. No endorsement by The Apache Software Foundation is implied by the use of these marks.
