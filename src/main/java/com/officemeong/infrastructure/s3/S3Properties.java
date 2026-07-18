package com.officemeong.infrastructure.s3;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cloud.aws.s3")
@Getter
@Setter
public class S3Properties {
    private String bucket;
    private String region;
    private String accessKey;
    private String secretKey;
}
