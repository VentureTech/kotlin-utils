

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

String repoUser, repoPassword, gradleOptions, version
boolean isSnapshot, isRelease
def slack = ["Channel": '#platform', 'Domain': 'proteusco', 'CredentialsId' : 'Slack Integration Token']
def atUser=''

node {
    stage("Build") {
        try
        {
            echo "CHANGE_AUTHOR_EMAIL => ${env.CHANGE_AUTHOR_EMAIL}"
            if (env.CHANGE_AUTHOR_EMAIL && env.CHANGE_AUTHOR_EMAIL.indexOf('@') != -1)
            {
                //noinspection GrReassignedInClosureLocalVar
                atUser = "@${env.CHANGE_AUTHOR_EMAIL.substring(0, env.CHANGE_AUTHOR_EMAIL.indexOf('@'))}: "
                echo "atUser => ${atUser}"
            }
        }
        catch (error)
        {
            echo "Unable to get atUser: $error"
        }
        try
        {
            repoUser = repo_venturetech_username
            repoPassword = repo_venturetech_password
        }
        catch (error)
        {
            echo("Unable to get build parameter. Will try env. $error")
        }
        if (!repoUser)
        {
            // try environment
            echo "Checking env: ${env.repo_venturetech_username}"
            repoUser = env.repo_venturetech_username
            repoPassword = env.repo_venturetech_password
        }
        timestamps {
            def jdkHome = tool 'JDK 8'

            // Get some code from a repository
            def scmVars = checkout scm
            echo 'SCM Vars'
            scmVars.each {k, v -> echo "$k => $v"}
            discoverGitReferenceBuild defaultBranch: 'master', latestBuildIfNotFound: true
            mineRepository()
            String gradleProps = readFile('gradle.properties')
            isSnapshot = env.BRANCH_NAME?.startsWith('sprint/') || env.BRANCH_NAME?.startsWith('epic/') ||
                env.BRANCH_NAME?.startsWith('release/')
            isRelease = env.BRANCH_NAME?.equals('master')
            String appVersion = getAppVersion(gradleProps).replace('-SNAPSHOT', '')
            String appName = getAppName(gradleProps)
            echo "Building $appName $appVersion"
            version = "${appVersion}.${currentBuild.number}${isSnapshot ? '-SNAPSHOT' : ''}"
            //noinspection LongLine
            gradleOptions = $/-Prepo_venturetech_username=$repoUser -Prepo_venturetech_password=$repoPassword -Papp_version=$version/$

            currentBuild.displayName = "v${version}"
            withEnv(["JAVA_HOME=$jdkHome"]) {
                // Run the build
                ansiColor('xterm') {
                    try{
                        sh "./gradlew clean assemble $gradleOptions"
                    } catch(err) {
                        currentBuild.result = 'FAILURE'
                        checkForBuildFailure(currentBuild, env, atUser, slack)
                        // If it doesn't build, stop here.
                        throw err
                    }
                }
            }
            archiveArtifacts allowEmptyArchive: true, artifacts: 'build/**/libs/*', fingerprint: true

            recordIssues sourceCodeEncoding: 'UTF-8', sourceDirectory: 'src', enabledForFailure: true, tools: [
                kotlin(),
                taskScanner(highTags: 'FIXME', ignoreCase: true, includePattern: '**/*.java,**/*.kt,**/*.groovy',
                    lowTags: 'FUTURE', normalTags: 'TODO')
            ]

        }
    }

    stage('Unit Tests') {
        timestamps {
            //        stage 'Unit Tests'
            String jdkHome = tool 'JDK 8'
            withEnv(["JAVA_HOME=$jdkHome"]) {
                // Run the build
                ansiColor('xterm') {
                    try{
                        sh "./gradlew test $gradleOptions"
                    } catch(err) {
                        currentBuild.result = 'FAILURE'
                        checkForBuildFailure(currentBuild, env, atUser, slack)
                        throw err
                    }
                }
            }
            junit allowEmptyResults: true, healthScaleFactor: 2.0, keepLongStdio: true,
                testResults: '**/build/**/test-results/**/TEST*.xml'
            recordIssues sourceCodeEncoding: 'UTF-8', enabledForFailure: true, tools: [
                junitParser(pattern: '**/build/**/test-results/**/TEST*.xml')
            ]
        }
    }

}

if(currentBuild.result == 'SUCCESS') {
    try
    {
        if(isSnapshot)
        {
            stage('Publish') {
                timeout(time: 20, unit: 'MINUTES') {
                    //noinspection LongLine
                    slackSend botUser: true, channel: slack.Channel, teamDomain: slack.Domain,
                        tokenCredentialId: slack.CredentialsId, color: 'good',
                        message: "${atUser}${env.JOB_NAME} ${currentBuild.displayName} can be published to the artifactory repo.\n(<${env.BUILD_URL}|Open>)"

                    //noinspection LongLine
                    def params = input id: 'Publish', message: 'Publish Artifacts To Repo Server', ok: 'Publish Artifacts',
                        parameters: [[$class: 'StringParameterDefinition', defaultValue: 'rtennant', description: 'Username to publish artifacts', name: 'publish_venturetech_username'], [$class: 'PasswordParameterDefinition', defaultValue: '', description: 'Password publish artifacts', name: 'publish_venturetech_password']]
                    node {
                        String jdkHome = tool 'JDK 8'
                        withEnv(["JAVA_HOME=$jdkHome", "GRADLE_OPTS=-Xmx2024m -Xms512m"]) {
                            // Run the build
                            ansiColor('xterm') {
                                //noinspection LongLine
                                echo "Publishing artifacts"
                                //noinspection LongLine
                                def additionalArgs = $/artifactoryPublish -Ppublish_venturetech_username=${
                                    params.publish_venturetech_username
                                } -Ppublish_venturetech_password=${params.publish_venturetech_password}/$
                                sh "./gradlew $gradleOptions $additionalArgs"
                            }
                        }
                    }
                }
            }
        }
        if(false && isSnapshot)
        {
            stage('Deploy') {
                timeout(time: 35, unit: 'MINUTES') {
                    //noinspection LongLine
                    slackSend botUser: true, channel: slack.Channel, teamDomain: slack.Domain,
                        tokenCredentialId: slack.CredentialsId, color: 'good',
                        message: "${atUser}${env.JOB_NAME} ${currentBuild.displayName} can be deployed to QA for testing.\n(<${env.BUILD_URL}|Open>)"
                    input id: 'Deploy', message: 'Deploy build to QA Server?', ok: 'Deploy'
                    node {
                        String jdkHome = tool 'JDK 8'
                        withEnv(["JAVA_HOME=$jdkHome"]) {
                            //noinspection LongLine
                            withCredentials(
                                [[$class: 'AmazonWebServicesCredentialsBinding', accessKeyVariable: 'aws_id', credentialsId: 'proteus-sys-jenkins-integration', secretKeyVariable: 'aws_secret']]) {

                                // Run the build
                                ansiColor('xterm') {
                                    //noinspection LongLine
                                    echo "Deploying artifacts"
                                    def additionalArgs = "autoDeploy -Paws_id=${env.aws_id} -Paws_secret=${env.aws_secret}"
                                    sh "./gradlew $gradleOptions $additionalArgs"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    catch(error){
        echo "$error"
        //Skipping publish task
        checkForBuildFailure(currentBuild, env, atUser, slack)
    }
}

checkForBuildFailure(currentBuild, env, atUser, slack)

@NonCPS
def checkForBuildFailure(currentBuild, env, atUser, Map<String, String> slack)
{
    println("CurrentBuild => ${currentBuild}")
    if (currentBuild.resultIsWorseOrEqualTo('UNSTABLE'))
    {
        //noinspection LongLine
        try
        {
            slackSend baseUrl: '', botUser: true, channel: slack.Channel, teamDomain: slack.Domain,
                tokenCredentialId: slack.CredentialsId,  color: 'danger',
                message: "${atUser}${env.JOB_NAME} ${currentBuild.displayName} is ${currentBuild.result}.\n(<${env.BUILD_URL}|Open>)"

        }
        catch (err)
        {
            println "$err"
        }
        step([$class: 'Mailer', notifyEveryUnstableBuild: true, recipients: env.CHANGE_AUTHOR_EMAIL,
              sendToIndividuals: true])
    }
    else if (currentBuild.result != null && currentBuild.result == 'SUCCESS' && currentBuild.previousBuild != null
        && currentBuild.previousBuild?.result != 'SUCCESS')
    {
        //noinspection LongLine
        try
        {
            slackSend baseUrl: '', botUser: true, channel: slack.Channel, teamDomain: slack.Domain,
                tokenCredentialId: slack.CredentialsId,  color: 'good',
                message: "${atUser} ${env.JOB_NAME} ${currentBuild.displayName} is back to normal."
        }
        catch (err)
        {
            println("$err")
        }

    }
}



@NonCPS
def getAppVersion(String text)
{
    def matcher = text =~ /app_version=(.+)/
    return matcher ? matcher[0][1] : '0.0'
}

@NonCPS
def getAppName(String text)
{
    def matcher = text =~ /app_name=(.+)/
    return matcher ? matcher[0][1] : 'App'
}

