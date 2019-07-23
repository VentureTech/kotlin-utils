/*
 * Copyright (c) 2019 Interactive Information Research & Development
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.proteusframework.build

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.sqs.AmazonSQSClient
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

/**
 * Utility for posting deployment commands.
 * @author Russ Tennant (russ@proteus.co)
 */
class DeployUtil
{
    static final String DEPLOY_QUEUE = 'deploy_queue'

    AmazonSQSClient client;
    Project project;
    String proteusInstallName

    DeployUtil(Project project, String id, String secret, String proteusInstallName)
    {
        this.project = project;
        this.proteusInstallName = proteusInstallName
        client = new AmazonSQSClient(new BasicAWSCredentials(id, secret))
        println("Creating deploy utility for $id")
    }

    def autoDeploy(String deploymentContext)
    {
        def version = project.findProperty('app_version').replace('-SNAPSHOT', '')
        client.sendMessage(
            project.findProperty(DEPLOY_QUEUE),
            $/{"proteus_install_name": "${proteusInstallName}"" "action": "auto-deploy", data: {"context": "${deploymentContext}", 
"version": "$version"}/$
        )
    }
}

class DeployTask extends DefaultTask
{
    String id
    String secret
    String context = "qa"

    @TaskAction
    def sendMessage() {
        new DeployUtil(getProject(), id, secret).autoDeploy(context)
    }

}
