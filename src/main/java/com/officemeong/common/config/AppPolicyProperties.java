package com.officemeong.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.policy")
@Getter
@Setter
public class AppPolicyProperties {
    private String termsUrl;
    private String privacyUrl;
    private String inquiryUrl;
}
