# Redshift To S3 Action


Description
-----------
RedshiftToS3 Action that unloads the result of a query from redshift to one or more files on Amazon Simple Storage
Service (Amazon S3).


Use Case
--------
This sink is used whenever you need to unload data from Redshift to S3 bucket.
For example, a financial customer would like to quickly unload financial reports into S3 that have been generated from
processing that is happening in Redshift. The pipeline would have a Redshift to S3 action at the beginning,
and then leverage the s3 source to read that data into a processing pipeline.


Properties
----------

**accessKey:** The access Id provided by AWS to access S3 bucket. Both configurations 'Keys(Access and Secret Access
keys)' and 'IAM Role' can not be provided or empty at the same time.(Macro-enabled)

**secretAccessKey:** AWS secret access key secret required to access S3 bucket. Both configurations 'Keys(Access and
Secret Access keys)' and 'IAM Role' can not be provided or empty at the same time.(Macro-enabled)

**iamRole:** IAM role having GET,LIST and PUT permissions to the S3 bucket. The IAM Role can be used only if the cluster
corresponding to the ``s3DataPath`` is being hosted on AWS servers. Both configurations 'Keys(Access and Secret Access
keys)' and 'IAM Role' can not be provided or empty at the same time.(Macro-enabled)

**query:** A SELECT query, the results of which query are unloaded from Redshift table to the S3 bucket.

**s3DataPath:** The full path, including bucket name, to the location on Amazon S3 where Amazon Redshift will
write the output file objects, including the manifest file if MANIFEST is specified. Should be of the format:
``s3://object-path/name-prefix``.

**outputPathToken:** The key used to store the S3 file path for the unloaded file, that will be used later by the source
 to read the data from. Plugins that run at later stages in the pipeline can retrieve the file path using this key
through macro substitution:${filePath} where ``filePath`` is the key specified. Defaults to ``filePath``. (Macro-enabled)

**manifest:** Boolean value to determine if manifest file is to be created during unload. The manifest file explicitly
lists the data files that are created by the UNLOAD process. Default is false

**delimiter:** Single ASCII character that is used to separate fields in the output file. Deafult is pipe(|).

**parallel:** Boolean value to determine if UNLOAD writes data in parallel to multiple files, according to the number
of slices in the cluster. Default is true.

**compression:** Unloads data into one or more compressed files of type GZIPor BZIP2. Can be one of the following: NONE,
 BZIP2 or GZIP. Default is NONE.

**allowOverWrite:** Boolean value to determine if UNLOAD will overwrite existing files, including the manifest file, if
the file is already available. Default is false.

**addQuotes:** Boolean value to determine if UNLOAD places quotation marks around each unloaded data field, so that
Amazon Redshift can unload data values that contain the delimiter itself. Default is false.

**escape:** Boolean value to determine if escape character(\) is to be placed before CHAR and VARCHAR columns in
delimited unload files, for every occurrence of the following characters: Linefeed ``\n``, Carriage return ``\r``,
delimiter character specified for the unloaded data, escape character \ and quote character: " or '. Default is false.

**redshiftClusterURL:** JDBC Redshift DB url for connecting to the redshift cluster. The url should include the port
and db name. Should be if format: ``jdbc:redshift://<endpoint-address>:<endpoint-port>/<db-name>``. (Macro-enabled)

**redshiftMasterUser:** Master user for the Redshift cluster to connect to. (Macro-enabled)

**redshiftMasterPassword:** Master password for Redshift cluster to connect to. (Macro-enabled)


Conditions
----------
1. Both configurations 'Keys(Access and Secret Access keys)' and 'IAM Role' can not be provided or empty at the same time.
Either keys ``(accessKey and secretAccessKey)`` or IAM Role ``(iamRole)`` must be specified for connecting to the S3 bucket.

2. Redshift table from which user wants to unload the data, should already exist in the database, in the Redshift cluster
specified by the ``redshiftClusterURL``.

3. The Amazon S3 bucket where Amazon Redshift will write the output files must reside in the same region as your cluster.

4. S3 data path should start with ``s3://`` and not with the ``s3n://`` or ``s3a://`` URI scheme.

Example
-------
This example connects to a Redshift cluster using the ``redshiftClusterURL, redshiftMasterUser and redshiftMasterPassword``
and to the S3 instance using the credentials ``accessKey`` and ``secretAccessKey``. Result of the query will be unloaded
to the S3 bucket provided through ``s3DataPath``.

    {
        "name": "RedshiftToS3Action",
        "type": "action",
        "properties": {
            "accessKey": "accessKey",
            "secretAccessKey": "secretAccessKey",
            "iamRole": "",
            "query": "select * from venue",
            "s3DataPath": "s3://mybucket/test_",
            "outputPathToken": "",
            "manifest": "false",
            "delimiter": ",",
            "parallel": "false",
            "compression" : "NONE",
            "allowOverwrite" : "true",
            "addQuotes": "true",
            "escape": "false",
            "redshiftClusterURL" : "jdbc:redshift://x.y.us-west-1.redshift.amazonaws.com:5439/redshiftdb",
            "redshiftMasterUser" : "admin",
            "redshiftMasterPassword": "admin"
        }
    }