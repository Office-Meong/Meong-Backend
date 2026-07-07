package com.officemeong.infrastructure.kto.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KtoPetDetailItem {

    private String contentid;
    private String acmpyTypeCd;
    private String acmpyNeedMtr;
    private String relaPosesFclty;
    private String relaFrnshPrdlst;
    private String etcAcmpyInfo;
    private String relaAcdntRiskMtr;
    private String acmpyPsblCpam;
    private String relaRntlPrdlst;
    private String relaPurcPrdlst;
}
