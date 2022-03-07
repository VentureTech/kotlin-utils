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

package com.proteusframework.build;

import org.ajoberstar.grgit.Grgit
import org.slf4j.LoggerFactory

/**
 * Plugin to provide git information
 *
 * @author Russ Tennant (russ@proteus.co)
 */
public class GitInfo
{
    String branch=''
    String ref=''
    String commit=''
    String author=''

    /**
     * Populate project with a "gitinfo" object.
     * @param project the project.
     * @return the
     */
    @SuppressWarnings(["GroovyUntypedAccess", "GroovyAssignabilityCheck", "GroovyUnusedDeclaration"])
    public static populateProject(def project)
    {
        GitInfo gitInfo = new GitInfo()
        project.ext.gitinfo = gitInfo
        try{
            def repo = project.file('../.git').exists() ? Grgit.open(project.file('..')) : Grgit.open()
            def branch = repo.branch.current
            def commit = repo.head()

            gitInfo.branch = branch.name
            gitInfo.ref = branch.fullName
            gitInfo.commit = commit.id
            gitInfo.author = "${commit.author.name} <${commit.author.email}>".toString()
        } catch(def e) {
            LoggerFactory.getLogger('build').debug(
                'Doesn\'t appear we are a repo. I am okay with it if you are.', e
            )
            println '!!! Remember to setup a git repo at some point.'
        }
    }

}
