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

import io.cdap.cdap.etl.api.FailureCollector;
import io.cdap.cdap.etl.api.validation.CauseAttributes;
import io.cdap.cdap.etl.mock.common.MockPipelineConfigurer;
import org.junit.Assert;
import org.junit.Test;


/**
 * Unit tests for {@link io.cdap.plugin.RedshiftToS3Action.RedshiftToS3Config}
 */
public class RedshiftToS3ConfigTest {
  private static final String IAM_ROLE = "iamRole";
  private static final String ACCESS_KEY = "accessKey";
  private static final String SECRET_ACCESS_KEY = "secretAccessKey";
  private static final String QUERY = "query";

  @Test
  public void testIfBothKeysAndRoleIsNotPresent() {
    RedshiftToS3Action.RedshiftToS3Config config =
      new RedshiftToS3Action.RedshiftToS3Config(null, null, null, "select * from testTable",
                                                "s3://mybucket/test/redshift_", null, null, null, null, null, null,
                                                null, null,
                                                "jdbc:redshift://x.y.us-west-1.redshift.amazonaws.com:5439/dev",
                                                "masterUser", "masterPassword");
    MockPipelineConfigurer configurer = new MockPipelineConfigurer(null);
    FailureCollector collector = configurer.getStageConfigurer().getFailureCollector();
    new RedshiftToS3Action(config).configurePipeline(configurer);
    Assert.assertEquals(1, collector.getValidationFailures().size());
    Assert.assertEquals(IAM_ROLE, collector.getValidationFailures().get(0).getCauses().get(0)
      .getAttribute(CauseAttributes.STAGE_CONFIG));
    Assert.assertEquals(ACCESS_KEY, collector.getValidationFailures().get(0).getCauses().get(1)
      .getAttribute(CauseAttributes.STAGE_CONFIG));
    Assert.assertEquals(SECRET_ACCESS_KEY, collector.getValidationFailures().get(0).getCauses().get(2)
      .getAttribute(CauseAttributes.STAGE_CONFIG));
  }

  @Test
  public void testMissingCredentials() {
    RedshiftToS3Action.RedshiftToS3Config config =
      new RedshiftToS3Action.RedshiftToS3Config(null, "secretAccessKey", null, "select * from testTable",
                                                "s3://mybucket/test/redshift_", null, null, null,
                                                null, null, null, null, null,
                                                "jdbc:redshift://x.y.us-west-1.redshift.amazonaws.com:5439/dev",
                                                "masterUser", "masterPassword");
    MockPipelineConfigurer configurer = new MockPipelineConfigurer(null);
    FailureCollector collector = configurer.getStageConfigurer().getFailureCollector();
    new RedshiftToS3Action(config).configurePipeline(configurer);
    Assert.assertEquals(1, collector.getValidationFailures().size());
    Assert.assertEquals(IAM_ROLE, collector.getValidationFailures().get(0).getCauses().get(0)
      .getAttribute(CauseAttributes.STAGE_CONFIG));
    Assert.assertEquals(ACCESS_KEY, collector.getValidationFailures().get(0).getCauses().get(1)
      .getAttribute(CauseAttributes.STAGE_CONFIG));
    Assert.assertEquals(SECRET_ACCESS_KEY, collector.getValidationFailures().get(0).getCauses().get(2)
      .getAttribute(CauseAttributes.STAGE_CONFIG));
  }

  @Test
  public void testBothKeysAndRoleArePresent() {
    RedshiftToS3Action.RedshiftToS3Config config =
      new RedshiftToS3Action.RedshiftToS3Config("accessKey", "secretAccessKey", "arn:aws:iam::123456789120:role/MyRole",
                                                "select * from testTable", "s3://mybucket/test/redshift_", null, null,
                                                null, null, null, null, null, null,
                                                "jdbc:redshift://x.y.us-west-1.redshift.amazonaws.com:5439/dev",
                                                "masterUser", "masterPassword");
    MockPipelineConfigurer configurer = new MockPipelineConfigurer(null);
    FailureCollector collector = configurer.getStageConfigurer().getFailureCollector();
    new RedshiftToS3Action(config).configurePipeline(configurer);
    Assert.assertEquals(1, collector.getValidationFailures().size());
    Assert.assertEquals(IAM_ROLE, collector.getValidationFailures().get(0).getCauses().get(0)
      .getAttribute(CauseAttributes.STAGE_CONFIG));
    Assert.assertEquals(ACCESS_KEY, collector.getValidationFailures().get(0).getCauses().get(1)
      .getAttribute(CauseAttributes.STAGE_CONFIG));
    Assert.assertEquals(SECRET_ACCESS_KEY, collector.getValidationFailures().get(0).getCauses().get(2)
      .getAttribute(CauseAttributes.STAGE_CONFIG));
  }

  @Test
  public void testKeysAndRoleArePresentUsingMacros() {
    RedshiftToS3Action.RedshiftToS3Config config =
      new RedshiftToS3Action.RedshiftToS3Config("accessKey", "secretAccessKey", "${iamRole}",
                                                "select * from testTable", "s3://mybucket/test/redshift_", null, null,
                                                null, null, null, null, null, null,
                                                "jdbc:redshift://x.y.us-west-1.redshift.amazonaws.com:5439/dev",
                                                "masterUser", "masterPassword");
    MockPipelineConfigurer configurer = new MockPipelineConfigurer(null);
    FailureCollector collector = configurer.getStageConfigurer().getFailureCollector();
    new RedshiftToS3Action(config).configurePipeline(configurer);
    Assert.assertEquals(1, collector.getValidationFailures().size());
    Assert.assertEquals(IAM_ROLE, collector.getValidationFailures().get(0).getCauses().get(0)
      .getAttribute(CauseAttributes.STAGE_CONFIG));
    Assert.assertEquals(ACCESS_KEY, collector.getValidationFailures().get(0).getCauses().get(1)
      .getAttribute(CauseAttributes.STAGE_CONFIG));
    Assert.assertEquals(SECRET_ACCESS_KEY, collector.getValidationFailures().get(0).getCauses().get(2)
      .getAttribute(CauseAttributes.STAGE_CONFIG));
  }

  @Test
  public void testInvalidQuery() {
    RedshiftToS3Action.RedshiftToS3Config config =
      new RedshiftToS3Action.RedshiftToS3Config("accessKey", "secretAccessKey", "",
                                                "drop table testTable", "s3://mybucket/test/redshift_", null, null,
                                                null, null, null, null, null, null,
                                                "jdbc:redshift://x.y.us-west-1.redshift.amazonaws.com:5439/dev",
                                                "masterUser", "masterPassword");
    MockPipelineConfigurer configurer = new MockPipelineConfigurer(null);
    FailureCollector collector = configurer.getStageConfigurer().getFailureCollector();
    new RedshiftToS3Action(config).configurePipeline(configurer);
    Assert.assertEquals(1, collector.getValidationFailures().size());
    Assert.assertEquals(QUERY, collector.getValidationFailures().get(0).getCauses().get(0)
      .getAttribute(CauseAttributes.STAGE_CONFIG));
  }
}
