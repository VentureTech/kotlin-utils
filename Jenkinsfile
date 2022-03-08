/*
 * Copyright (c) Interactive Information R & D (I2RD) LLC.
 * All Rights Reserved.
 *
 * This software is confidential and proprietary information of
 * I2RD LLC ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered
 * into with I2RD.
 */

@Library('proteus')
import net.proteusframework.jenkinsbuildlibrary.BuildProps
import net.proteusframework.jenkinsbuildlibrary.BuildLang

node {
    BuildProps buildProps = proteusPreBuild(javaTool: 'JDK 8', defaultBranch: 'master', slackChannel: '#platform')

    proteusBuild buildProps: buildProps, codeLanguage: BuildLang.Kotlin

    proteusTest buildProps: buildProps

    proteusSBOM buildProps: buildProps, projectTags: ['kotlin', 'proteus-platform']

    proteusReportBuildResult buildProps: buildProps
}
