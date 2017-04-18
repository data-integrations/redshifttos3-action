# Redshift To S3 Action


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
