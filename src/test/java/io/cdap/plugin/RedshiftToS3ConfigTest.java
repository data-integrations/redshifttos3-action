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

import io.cdap.cdap.etl.mock.common.MockPipelineConfigurer;
import org.junit.Assert;
import org.junit.Test;


/**
 * Unit tests for {@link io.cdap.plugin.RedshiftToS3Action.RedshiftToS3Config}
 */
public class RedshiftToS3ConfigTest {
  @Test
  public void testIfBothKeysAndRoleIsNotPresent() throws Exception {
    RedshiftToS3Action.RedshiftToS3Config config =
      new RedshiftToS3Action.RedshiftToS3Config(null, null, null, "select * from testTable",
                                                "s3://mybucket/test/redshift_", null, null, null, null, null, null,
                                                null, null,
                                                "jdbc:redshift://x.y.us-west-1.redshift.amazonaws.com:5439/dev",
                                                "masterUser", "masterPassword");
    MockPipelineConfigurer configurer = new MockPipelineConfigurer(null);
    try {
      new RedshiftToS3Action(config).configurePipeline(configurer);
      Assert.fail();
    } catch (IllegalArgumentException e) {
      Assert.assertEquals("Both configurations 'Keys'(Access and Secret Access keys) and 'IAM Role' can not be " +
                            "empty at the same time. Either provide the 'Keys'(Access and Secret Access keys) or " +
                            "'IAM Role' for connecting to S3 bucket.", e.getMessage());
    }
  }

  @Test
  public void testMissingCredentials() throws Exception {
    RedshiftToS3Action.RedshiftToS3Config config =
      new RedshiftToS3Action.RedshiftToS3Config(null, "secretAccessKey", null, "select * from testTable",
                                                "s3://mybucket/test/redshift_", null, null, null,
                                                null, null, null, null, null,
                                                "jdbc:redshift://x.y.us-west-1.redshift.amazonaws.com:5439/dev",
                                                "masterUser", "masterPassword");
    MockPipelineConfigurer configurer = new MockPipelineConfigurer(null);
    try {
      new RedshiftToS3Action(config).configurePipeline(configurer);
      Assert.fail();
    } catch (IllegalArgumentException e) {
      Assert.assertEquals("Both configurations 'Keys'(Access and Secret Access keys) and 'IAM Role' can not be " +
                            "empty at the same time. Either provide the 'Keys'(Access and Secret Access keys) or " +
                            "'IAM Role' for connecting to S3 bucket.", e.getMessage());
    }
  }

  @Test
  public void testBothKeysAndRoleArePresent() throws Exception {
    RedshiftToS3Action.RedshiftToS3Config config =
      new RedshiftToS3Action.RedshiftToS3Config("accessKey", "secretAccessKey", "arn:aws:iam::123456789120:role/MyRole",
                                                "select * from testTable", "s3://mybucket/test/redshift_", null, null,
                                                null, null, null, null, null, null,
                                                "jdbc:redshift://x.y.us-west-1.redshift.amazonaws.com:5439/dev",
                                                "masterUser", "masterPassword");
    MockPipelineConfigurer configurer = new MockPipelineConfigurer(null);
    try {
      new RedshiftToS3Action(config).configurePipeline(configurer);
      Assert.fail();
    } catch (IllegalArgumentException e) {
      Assert.assertEquals("Both configurations 'Keys'(Access and Secret Access keys) and 'IAM Role' can not be " +
                            "provided at the same time. Either provide the 'Keys'(Access and Secret Access keys) or " +
                            "'IAM Role' for connecting to S3 bucket.", e.getMessage());
    }
  }

  @Test
  public void testKeysAndRoleArePresentUsingMacros() throws Exception {
    RedshiftToS3Action.RedshiftToS3Config config =
      new RedshiftToS3Action.RedshiftToS3Config("accessKey", "secretAccessKey", "${iamRole}",
                                                "select * from testTable", "s3://mybucket/test/redshift_", null, null,
                                                null, null, null, null, null, null,
                                                "jdbc:redshift://x.y.us-west-1.redshift.amazonaws.com:5439/dev",
                                                "masterUser", "masterPassword");
    MockPipelineConfigurer configurer = new MockPipelineConfigurer(null);
    try {
      new RedshiftToS3Action(config).configurePipeline(configurer);
      Assert.fail();
    } catch (IllegalArgumentException e) {
      Assert.assertEquals("Both configurations 'Keys'(Access and Secret Access keys) and 'IAM Role' can not be " +
                            "provided at the same time. Either provide the 'Keys'(Access and Secret Access keys) or " +
                            "'IAM Role' for connecting to S3 bucket.", e.getMessage());
    }
  }

  @Test
  public void testInvalidQuery() throws Exception {
    RedshiftToS3Action.RedshiftToS3Config config =
      new RedshiftToS3Action.RedshiftToS3Config("accessKey", "secretAccessKey", "",
                                                "drop table testTable", "s3://mybucket/test/redshift_", null, null,
                                                null, null, null, null, null, null,
                                                "jdbc:redshift://x.y.us-west-1.redshift.amazonaws.com:5439/dev",
                                                "masterUser", "masterPassword");
    MockPipelineConfigurer configurer = new MockPipelineConfigurer(null);
    try {
      new RedshiftToS3Action(config).configurePipeline(configurer);
      Assert.fail();
    } catch (IllegalArgumentException e) {
      Assert.assertEquals("Please specify a valid select statement for query.", e.getMessage());
    }
  }
}
