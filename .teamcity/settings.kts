import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.powerShell
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.finishBuildTrigger
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.schedule
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2019.2"

project {
    description = "Test for 2271902"

    vcsRoot(BbrepoA)
    vcsRoot(RepoB)
    vcsRoot(RepoC)
    vcsRoot(RepoD)

    buildType(BuildD)
    buildType(BbBuildA)
    buildType(BuildC)
    buildType(BuildB)
    buildType(BuildA)
    buildTypesOrder = arrayListOf(BuildA, BuildB, BuildC, BuildD, BbBuildA)
}

object BbBuildA : BuildType({
    name = "BB Build A"

    vcs {
        root(BbrepoA)

        cleanCheckout = true
    }

    triggers {
        vcs {
        }
    }
})

object BuildA : BuildType({
    name = "Build A"

    artifactRules = "Build_A_*.txt"

    params {
        param("bld_cmd", "make hello install clean")
    }

    vcs {
        root(DslContext.settingsRoot)

        cleanCheckout = true
    }

    steps {
        script {
            scriptContent = """
                echo "repoA file.txt content:"
                cat file.txt
                sleep 120
            """.trimIndent()
        }
        script {
            name = "Create artifact"
            scriptContent = "echo %build.number% > Build_A_%build.number%.txt"
        }
        script {
            name = "Fail step"
            enabled = false
            scriptContent = "exit 1"
        }
    }

    triggers {
        schedule {
            enabled = false
            schedulingPolicy = cron {
                minutes = "0/2"
            }
            branchFilter = "+:master"
            triggerBuild = always()
            withPendingChangesOnly = false
        }
        vcs {
            perCheckinTriggering = true
            groupCheckinsByCommitter = true
            enableQueueOptimization = false
        }
    }
})

object BuildB : BuildType({
    name = "Build B"

    artifactRules = "Build_B_*.txt"

    vcs {
        root(RepoB)

        cleanCheckout = true
    }

    steps {
        script {
            scriptContent = """
                echo "file.txt content:"
                cat file.txt
                echo "Build A artifact content:"
                cat Build_A_*.txt
                sleep 3
            """.trimIndent()
        }
        powerShell {
            name = "End step on a multiple of 5 minute of the hour"
            enabled = false
            scriptMode = script {
                content = """
                    While(${'$'}true)
                    {
                    	${'$'}dt = get-date
                    	Do
                    	{
                    		sleep -seconds 1
                    		${'$'}dt = get-date
                    	}
                    	
                    	Until (${'$'}dt.second -eq 0)
                    	if (${'$'}dt.minute -eq 00) { exit 0 }
                    	if (${'$'}dt.minute -eq 05) { exit 0 }
                    	if (${'$'}dt.minute -eq 10) { exit 0 }
                    	if (${'$'}dt.minute -eq 15) { exit 0 }
                    	if (${'$'}dt.minute -eq 20) { exit 0 }
                    	if (${'$'}dt.minute -eq 25) { exit 0 }
                    	if (${'$'}dt.minute -eq 30) { exit 0 }
                    	if (${'$'}dt.minute -eq 35) { exit 0 }
                    	if (${'$'}dt.minute -eq 40) { exit 0 }
                    	if (${'$'}dt.minute -eq 45) { exit 0 }
                    	if (${'$'}dt.minute -eq 50) { exit 0 }
                    	if (${'$'}dt.minute -eq 55) { exit 0 }
                    }
                """.trimIndent()
            }
        }
        script {
            name = "Create artifact"
            scriptContent = """
                echo %build.number% > Build_B_%build.number%.txt
                echo "Build A artifact is taken from build:" >> Build_B_%build.number%.txt
                cat Build_A_*.txt >> Build_B_%build.number%.txt
            """.trimIndent()
        }
    }

    triggers {
        schedule {
            enabled = false
            schedulingPolicy = cron {
                minutes = "0/5"
            }
            triggerBuild = always()
        }
        finishBuildTrigger {
            enabled = false
            buildType = "${BuildA.id}"
            branchFilter = "+:*"
        }
    }
})

object BuildC : BuildType({
    name = "Build C"

    vcs {
        root(RepoC)

        cleanCheckout = true
    }

    steps {
        script {
            scriptContent = """
                hostname
                ls /usr/bin
                echo "file.txt content:"
                cat file.txt
            """.trimIndent()
            dockerImage = "alpine:latest"
            dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
        }
    }

    triggers {
        schedule {
            enabled = false
            schedulingPolicy = cron {
                minutes = "0/5"
            }
            triggerBuild = always()
        }
        vcs {
        }
    }
})

object BuildD : BuildType({
    name = "Build D"

    steps {
        script {
            scriptContent = "echo OK!"
            dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
        }
    }

    triggers {
        vcs {
            branchFilter = ""
            watchChangesInDependencies = true
            enableQueueOptimization = false
        }
    }

    dependencies {
        snapshot(BuildA) {
        }
    }
})

object BbrepoA : GitVcsRoot({
    name = "bbrepoA"
    url = "https://acherenkovjb@bitbucket.org/acherenkovjb/bbrepoa.git"
    branch = "refs/heads/develop/6.6.x"
    authMethod = password {
        userName = "acherenkovjb"
        password = "credentialsJSON:f1d911e4-4338-4451-90b1-2c1a65605525"
    }
})

object RepoB : GitVcsRoot({
    name = "repoB"
    url = "https://github.com/tolache/repoB"
    branchSpec = "+:refs/heads/*"
    authMethod = password {
        userName = "tolache"
        password = "credentialsJSON:7d8cca8e-bc35-4156-a965-0b32123691bc"
    }
})

object RepoC : GitVcsRoot({
    name = "repoC"
    url = "https://github.com/tolache/repoC"
    branchSpec = "+:refs/heads/(master)"
    authMethod = password {
        userName = "tolache"
        password = "credentialsJSON:7d8cca8e-bc35-4156-a965-0b32123691bc"
    }
})

object RepoD : GitVcsRoot({
    name = "repoD"
    url = "https://github.com/tolache/repoD"
    branchSpec = "refs/heads/develop"
    authMethod = password {
        userName = "tolache"
        password = "credentialsJSON:7d8cca8e-bc35-4156-a965-0b32123691bc"
    }
})
