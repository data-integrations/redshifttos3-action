/*
 * Copyright © 2017 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.plugin;

import com.google.common.base.Strings;
import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Macro;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.cdap.api.plugin.PluginConfig;
import io.cdap.cdap.etl.api.FailureCollector;
import io.cdap.cdap.etl.api.PipelineConfigurer;
import io.cdap.cdap.etl.api.action.Action;
import io.cdap.cdap.etl.api.action.ActionContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import javax.annotation.Nullable;

/**
 * RedshiftToS3 Action Plugin - Unloads data from Redshift to S3 bucket using Amazon S3 server-side encryption (SSE-S3).
 */
@Plugin(type = Action.PLUGIN_TYPE)
@Name(RedshiftToS3Action.PLUGIN_NAME)
@Description("Unloads the result of a query for redshift to one or more files on Amazon Simple Storage Service " +
  "(Amazon S3).")
public class RedshiftToS3Action extends Action {
  public static final String PLUGIN_NAME = "RedshiftToS3";
  private static final String JDBC_DRIVER_CLASS = "com.amazon.redshift.jdbc.Driver";
  private static final String COMPRESSION_BZIP2 = "BZIP2";
  private static final String COMPRESSION_GZIP = "GZIP";
  private static final String COMPRESSION_NONE = "NONE";
  private final RedshiftToS3Config config;

  public RedshiftToS3Action(RedshiftToS3Config config) {
    this.config = config;
  }

  @Override
  public void configurePipeline(PipelineConfigurer pipelineConfigurer) throws IllegalArgumentException {
    super.configurePipeline(pipelineConfigurer);
    config.validate(pipelineConfigurer.getStageConfigurer().getFailureCollector());
  }

  @Override
  public void run(ActionContext context) throws Exception {
    FailureCollector collector = context.getFailureCollector();
    config.validate(collector);
    collector.getOrThrowException();

    Connection conn = null;
    Statement stmt = null;
    try {
      //Open a connection to redshift
      Class.forName(JDBC_DRIVER_CLASS);
      Properties props = new Properties();
      props.setProperty("user", config.redshiftMasterUser);
      props.setProperty("password", config.redshiftMasterPassword);
      conn = DriverManager.getConnection(config.redshiftClusterURL, props);
      stmt = conn.createStatement();
      String unloadCommand = buildUnloadCommand();
      stmt.executeUpdate(unloadCommand);
      String s3Path = config.s3DataPath;
      if (unloadCommand.contains("credentials")) {
        s3Path = s3Path.replaceFirst("s3://", "s3n://");
      } else {
        s3Path = s3Path.replaceFirst("s3://", "s3a://");
      }
      // if path is not a directory, add * to the end of path, to use file path globbing to read files in the next stage
      if (!s3Path.endsWith("/")) {
        s3Path += "*";
      }
      context.getArguments().set(config.outputPathToken, s3Path);
    } catch (ClassNotFoundException e) {
      throw new IllegalArgumentException(String.format("Unable to load or register Redshift JDBC driver. %s",
                                                       e.getMessage()), e);
    } catch (SQLException e) {
      throw new IllegalArgumentException(String.format("Error unloading the data from Redshift to the S3 bucket. %s",
                                                       e.getMessage()), e);
    } finally {
      if (stmt != null) {
        stmt.close();
      }
      if (conn != null) {
        conn.close();
      }
    }
  }

  /**
   * Builds and returns the unload command using the provided properties, to unload data from Redshift to AWS S3.
   *
   * @return unload command
   */
  private String buildUnloadCommand() {
    StringBuilder unloadCommand = new StringBuilder();
    unloadCommand.append("unload ('").append(config.query).append("') to '").append(config.s3DataPath).append("'");
    // Check authentication is using keys or role.
    if (!Strings.isNullOrEmpty(config.accessKey) && !Strings.isNullOrEmpty(config.secretAccessKey)) {
      unloadCommand.append(" ");
      unloadCommand.append("credentials 'aws_access_key_id=").append(config.accessKey).append(";aws_secret_access_key=")
        .append(config.secretAccessKey).append("'");
    } else {
      unloadCommand.append(" iam_role '").append(config.iamRole).append("'");
    }
    if (!Strings.isNullOrEmpty(config.delimiter)) {
      unloadCommand.append(" delimiter '").append(config.delimiter).append("'");
    }
    if (!config.parallel) {
      unloadCommand.append(" parallel off");
    }
    if (config.manifest) {
      unloadCommand.append(" manifest");
    }
    if (config.allowOverWrite) {
      unloadCommand.append(" allowoverwrite");
    }
    if (config.addQuotes) {
      unloadCommand.append(" addquotes");
    }
    if (config.escape) {
      unloadCommand.append(" escape");
    }
    String compression = config.compression;
    if (!Strings.isNullOrEmpty(compression)) {
      switch (config.compression.toUpperCase()) {
        case COMPRESSION_BZIP2:
          unloadCommand.append(" ").append(COMPRESSION_BZIP2);
          break;
        case COMPRESSION_GZIP:
          unloadCommand.append(" ").append(COMPRESSION_GZIP);
          break;
        case COMPRESSION_NONE:
          break;
        default:
          throw new IllegalArgumentException("Unsupported compression type " + config.compression);
      }
    }
    unloadCommand.append(";");
    return unloadCommand.toString();
  }

  /**
   * Config class that contains all properties required for running the unload command.
   */
  public static class RedshiftToS3Config extends PluginConfig {
    private static final String IAM_ROLE = "iamRole";
    private static final String ACCESS_KEY = "accessKey";
    private static final String SECRET_ACCESS_KEY = "secretAccessKey";
    private static final String QUERY = "query";

    @Macro
    @Nullable
    @Description("The access Id provided by AWS to access the S3 bucket. Both configurations 'Keys(Access and Secret " +
      "Access keys)' and 'IAM Role' can not be provided or empty at the same time. (Macro-enabled)")
    private String accessKey;

    @Macro
    @Nullable
    @Description("AWS secret access key having access to the S3 bucket. Both configurations 'Keys(Access and Secret " +
      "Access keys)' and 'IAM Role' can not be provided or empty at the same time.(Macro-enabled)")
    private String secretAccessKey;

    @Macro
    @Nullable
    @Description("IAM role having GET,LIST and PUT permissions to the S3 bucket. Both configurations 'Keys(Access " +
      "and Secret Access keys)' and 'IAM Role' can not be provided or empty at the same time.(Macro-enabled)")
    private String iamRole;

    @Macro
    @Description("A SELECT query, the results of which query are unloaded from Redshift table to the S3 bucket.")
    private String query;

    @Macro
    @Description("The full path, including bucket name, to the location on Amazon S3 where Amazon Redshift will " +
      "write the output file objects, including the manifest file if MANIFEST is specified. Should be of the format: " +
      "s3://object-path/name-prefix.")
    private String s3DataPath;

    @Macro
    @Nullable
    @Description("The key used to store the S3 file path for the unloaded file, that will be used later by the source" +
      " to read the data from. Plugins that run at later stages in the pipeline can retrieve the file path using this" +
      " key through macro substitution:${filePath} where \"filePath\" is the key specified. Defaults to \"filePath\"." +
      " (Macro-enabled)")
    private String outputPathToken;

    @Macro
    @Nullable
    @Description("Boolean value to determine if manifest file is to be created during unload. The manifest file " +
      "explicitly lists the data files that are created by the UNLOAD process. Default is false.")
    private Boolean manifest;

    @Nullable
    @Macro
    @Description("Single ASCII character that is used to separate fields in the output file. Deafult is pipe(|).")
    private String delimiter;

    @Macro
    @Nullable
    @Description("Boolean value to determine if UNLOAD writes data in parallel to multiple files, according to the " +
      "number of slices in the cluster. Default is true.")
    private Boolean parallel;

    @Nullable
    @Macro
    @Description("Unloads data into one or more compressed files. Can be one of the following: NONE or BZIP2 or GZIP." +
      "Default is NONE.")
    private String compression;

    @Macro
    @Nullable
    @Description("Boolean value to determine if UNLOAD will overwrite existing files, including the manifest file, " +
      "if the file is already available. Default is false.")
    private Boolean allowOverWrite;

    @Macro
    @Nullable
    @Description("Boolean value to determine if UNLOAD places quotation marks around each unloaded data field, so " +
      "that Amazon Redshift can unload data values that contain the delimiter itself. Default is false.")
    private Boolean addQuotes;

    @Macro
    @Nullable
    @Description("Boolean value to determine if escape character(\\) is to be placed before CHAR and VARCHAR columns " +
      "in delimited unload files, for every occurrence of the following characters: Linefeed \n, Carriage return \r," +
      "Delimiter character specified for the unloaded data, escape character \\ and quote character: \" or '. " +
      "Default is false.")
    private Boolean escape;

    @Macro
    @Description("JDBC Redshift DB url for connecting to the redshift cluster. The url should include the port and " +
      "db name. Should be if format: 'jdbc:redshift://<endpoint-address>:<endpoint-port>/<db-name>'. (Macro-enabled)")
    private String redshiftClusterURL;

    @Macro
    @Description("Master user for the Redshift cluster to connect to. (Macro-enabled)")
    private String redshiftMasterUser;

    @Macro
    @Description("Master password for Redshift cluster to connect to. (Macro-enabled)")
    private String redshiftMasterPassword;

    public RedshiftToS3Config() {
      this.outputPathToken = "filePath";
      this.manifest = false;
      this.parallel = true;
      this.compression = "NONE";
      this.allowOverWrite = false;
      this.addQuotes = false;
      this.escape = false;
    }

    public RedshiftToS3Config(@Nullable String accessKey, @Nullable String secretAccessKey, @Nullable String iamRole,
                              String query, String s3DataPath, @Nullable String outputPathToken,
                              @Nullable Boolean manifest, @Nullable String delimiter, @Nullable Boolean parallel,
                              @Nullable String compression, @Nullable Boolean allowOverWrite,
                              @Nullable Boolean addQuotes, @Nullable Boolean escape, String redshiftClusterURL,
                              String redshiftMasterUser, String redshiftMasterPassword) {
      this.accessKey = accessKey;
      this.secretAccessKey = secretAccessKey;
      this.iamRole = iamRole;
      this.query = query;
      this.s3DataPath = s3DataPath;
      this.outputPathToken = outputPathToken;
      this.manifest = manifest;
      this.delimiter = delimiter;
      this.parallel = parallel;
      this.compression = compression;
      this.allowOverWrite = allowOverWrite;
      this.addQuotes = addQuotes;
      this.escape = escape;
      this.redshiftClusterURL = redshiftClusterURL;
      this.redshiftMasterUser = redshiftMasterUser;
      this.redshiftMasterPassword = redshiftMasterPassword;
    }

    /**
     * Validates the config parameters required for unloading the data.
     */
    private void validate(FailureCollector collector) {
      if (!Strings.isNullOrEmpty(iamRole) || this.containsMacro(IAM_ROLE)) {
        if (!((Strings.isNullOrEmpty(accessKey) && !this.containsMacro(ACCESS_KEY)) &&
          (Strings.isNullOrEmpty(secretAccessKey) && !this.containsMacro(SECRET_ACCESS_KEY)))) {
          collector.addFailure(
            "Both configurations 'Keys'(Access and Secret Access keys) and 'IAM " +
              "Role' can not be provided at the same time.",
            "Either provide the 'Keys' (Access and Secret Access keys) or 'IAM Role' for connecting to S3 bucket.")
            .withConfigProperty(IAM_ROLE).withConfigProperty(ACCESS_KEY).withConfigProperty(SECRET_ACCESS_KEY);
        }
      } else if (Strings.isNullOrEmpty(iamRole)) {
        if (!((!Strings.isNullOrEmpty(accessKey) || this.containsMacro(ACCESS_KEY)) &&
          (!Strings.isNullOrEmpty(secretAccessKey) || this.containsMacro(SECRET_ACCESS_KEY)))) {
          collector.addFailure(
            "Both configurations 'Keys'(Access and Secret Access keys) and " +
              "'IAM Role' can not be empty at the same time.",
            "Either provide the 'Keys'(Access and Secret Access keys) or 'IAM Role' for connecting to S3 bucket.")
            .withConfigProperty(IAM_ROLE).withConfigProperty(ACCESS_KEY).withConfigProperty(SECRET_ACCESS_KEY);
        }
      }
      if (!Strings.isNullOrEmpty(query) && !this.containsMacro(QUERY)) {
        if (!query.toLowerCase().startsWith("select") && !query.toLowerCase().contains("from")) {
          collector.addFailure("specify a valid select statement for query.", null).withConfigProperty(QUERY);
        }
      }
    }
  }
}
