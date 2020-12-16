import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.freeDiskSpace
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.sshAgent
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.ScriptBuildStep
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.projectFeatures.buildReportTab
import jetbrains.buildServer.configs.kotlin.v2019_2.projectFeatures.dockerRegistry
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

version = "2020.2"

project {

    vcsRoot(Bank)
    vcsRoot(AcjbAzureDevops)
    vcsRoot(BbrepoA)
    vcsRoot(RepoB)
    vcsRoot(RepoC)

    buildType(BuildD)
    buildType(BbBuildA)
    buildType(BuildC)
    buildType(BuildB)
//     buildType(BuildA)

    template(BuildATemplate)

    features {
        buildReportTab {
            id = "PROJECT_EXT_16"
            title = "Tola's Custom Report 2"
            startPage = "report.zip!/report2.htm"
        }
        dockerRegistry {
            id = "PROJECT_EXT_21"
            name = "tolaregistry"
            url = "http://unit-905.labs.intellij.net:8083"
            userName = "admin"
            password = "credentialsJSON:eca3d0a1-0303-4a95-bc66-f386c7b79571"
        }
        feature {
            id = "PROJECT_EXT_25"
            type = "project-graphs"
            param("series", """
                [
                  {
                    "type": "valueType",
                    "title": "Time Spent in Queue",
                    "sourceBuildTypeId": "BuildChain_BuildA",
                    "key": "TimeSpentInQueue"
                  }
                ]
            """.trimIndent())
            param("format", "text")
            param("title", "New chart title")
            param("seriesTitle", "Serie")
        }
        feature {
            id = "PROJECT_EXT_26"
            type = "project-graphs"
            param("series", """
                [
                  {
                    "type": "valueType",
                    "title": "Time Spent in Queue",
                    "sourceBuildTypeId": "BuildChain_BuildB",
                    "key": "TimeSpentInQueue"
                  }
                ]
            """.trimIndent())
            param("format", "text")
            param("title", "New chart title")
            param("seriesTitle", "Serie")
        }
        buildReportTab {
            id = "PROJECT_EXT_28"
            title = "Tola's Custom Report 3"
            startPage = "report.zip!/report3.htm"
        }
        buildReportTab {
            id = "PROJECT_EXT_3"
            title = "Tola's Custom Report 1"
            startPage = "report.zip!/report1.htm"
        }
        feature {
            id = "PROJECT_EXT_32"
            type = "active_storage"
            param("active.storage.feature.id", "PROJECT_EXT_30")
        }
    }
    buildTypesOrder = arrayListOf(BuildA, BuildB, BuildC, BuildD, BbBuildA)
}

object BbBuildA : BuildType({
    name = "BB Build A"

    vcs {
        root(BbrepoA, "+:dir1")

        cleanCheckout = true
    }

    steps {
        script {
            scriptContent = """
                echo "Listing files:"
                ls
            """.trimIndent()
        }
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
        param("teamcity.vcsTrigger.runBuildInNewEmptyBranch", "true")
    }

    vcs {
        root(DslContext.settingsRoot)
        root(AcjbAzureDevops)

        checkoutMode = CheckoutMode.ON_AGENT
        cleanCheckout = true
    }

    steps {
        script {
            name = "Create artifact"
            scriptContent = """
                echo %build.number% > Build_A_%build.number%.txt
                sleep 1
            """.trimIndent()
        }
    }

    triggers {
        vcs {
            perCheckinTriggering = true
            enableQueueOptimization = false
        }
    }

    failureConditions {
        supportTestRetry = true
    }

    features {
        commitStatusPublisher {
            vcsRootExtId = "${DslContext.settingsRoot.id}"
            publisher = github {
                githubUrl = "https://api.github.com"
                authType = personalToken {
                    token = "credentialsJSON:7d8cca8e-bc35-4156-a965-0b32123691bc"
                }
            }
            param("github_oauth_user", "tolache")
        }
        commitStatusPublisher {
            publisher = tfs {
                serverUrl = "https://dev.azure.com/anatolycherenkov"
                authType = "token"
                accessToken = "credentialsJSON:b7d31e4b-7370-43af-92c3-65a389684cd6"
            }
        }
    }

    cleanup {
        keepRule {
            id = "KEEP_RULE_3"
            keepAtLeast = days(180) {
                since = lastBuild()
            }
            dataToKeep = everything()
        }
        baseRule {
            option("disableCleanupPolicies", true)
        }
    }
})

object BuildB : BuildType({
    name = "Build B"

    artifactRules = "Build_B_*.txt"

    params {
        param("env.NAntHome", """C:\nant-0.92""")
        param("myParam", "111")
    }

    vcs {
        root(RepoB)

        cleanCheckout = true
    }

    steps {
        script {
            name = "Create artifact"
            scriptContent = """
                echo %build.number% > Build_B_%build.number%.txt
                sleep 1200
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

    features {
        freeDiskSpace {
            failBuild = false
        }
    }

    dependencies {
        snapshot(BuildA) {
            synchronizeRevisions = false
        }
    }

    cleanup {
        baseRule {
            preventDependencyCleanup = false
        }
    }
})

object BuildC : BuildType({
    name = "Build C"

    artifactRules = """
        *.htm => report.zip
        *.png => report.zip
        *.js  => report.zip
        test.png => screenshots
    """.trimIndent()

    params {
        param("github.server.url", "https://github.com")
        param("github.repo", "tolache/repoC")
        param("branch", "develop")
        param("art.rules", "Build_B*.txt")
    }

    vcs {
        root(RepoC)

        cleanCheckout = true
    }

    steps {
        script {
            scriptContent = """
                set /p fileContent=<file.txt
                
                echo ^<!DOCTYPE html^> > report1.htm
                echo ^<html^> >> report1.htm
                echo ^<body^> >> report1.htm
                
                echo ^<h1^>Yes, this is a report.^</h1^> >> report1.htm
                
                echo ^<p^>The content of file.txt is: %%fileContent%%^</p^> >> report1.htm
                
                echo ^</body^> >> report1.htm
                echo ^</html^> >> report1.htm
                
                
                
                echo ^<!DOCTYPE html^> > report2.htm
                echo ^<html^> >> report2.htm
                echo ^<body^> >> report2.htm
                
                echo ^<h1^>Yes, this is another report.^</h1^> >> report2.htm
                
                echo ^<p^>The content of file.txt is: %%fileContent%%^</p^> >> report2.htm
                
                echo ^</body^> >> report2.htm
                echo ^</html^> >> report2.htm
            """.trimIndent()
            dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
        }
        script {
            enabled = false
            scriptContent = """
                echo "Build B's myParam is:"
                echo ${BuildB.depParamRefs["myParam"]}
            """.trimIndent()
        }
        script {
            name = "Tests"
            scriptContent = """
                echo "##teamcity[testStarted name='FailingTest']"
                echo "##teamcity[testFailed name='FailingTest' message='Test failed intentionally.' details='This test is always failing (intentionally).']"
                echo "##teamcity[testMetadata testName='FailingTest' type='image' value='screenshots/test.png"
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
        vcs {
        }
    }

    dependencies {
        dependency(BuildB) {
            snapshot {
            }

            artifacts {
                artifactRules = "%art.rules%"
            }
        }
    }

    cleanup {
        baseRule {
            preventDependencyCleanup = false
        }
    }
})

object BuildD : BuildType({
    name = "Build D"

    steps {
        script {
            scriptContent = "echo OK!"
        }
        script {
            name = "Push to repoA"
            enabled = false
            scriptContent = """
                cd C:\Users\Anatoly.Cherenkov\repos\repoA
                echo "Original file.txt:"
                cat file.txt
                set /p myvar=<file.txt
                set /a myvar=%%myvar%%+1
                echo %%myvar%% > file.txt
                echo "New file.txt:"
                cat file.txt
                echo "Adding file.txt..."
                git add file.txt
                echo "Committing..."
                git commit -m "Update file.txt to %%myvar%%"
                echo "Pushing..."
                git push origin master
            """.trimIndent()
            dockerImagePlatform = ScriptBuildStep.ImagePlatform.Linux
        }
    }

    features {
        sshAgent {
            teamcitySshKey = "github.pem"
        }
    }

    dependencies {
        snapshot(BuildC) {
        }
    }

    cleanup {
        baseRule {
            preventDependencyCleanup = false
        }
    }
})

object BuildATemplate : Template({
    name = "Build A Template"
    description = "Template for Build A"

    params {
        param("branch", "integration")
        param("system.branch", "%branch%")
        param("env.branch", "%branch%")
    }
})

object AcjbAzureDevops : GitVcsRoot({
    name = "ACJB Azure Devops"
    url = "https://anatolycherenkov@dev.azure.com/anatolycherenkov/testproject1/_git/testproject1"
    branch = "refs/heads/master"
    branchSpec = "refs/heads/*"
    authMethod = password {
        userName = "anatolycherenkov"
        password = "credentialsJSON:6599e99d-0eab-4aa8-b192-ab4fa964202c"
    }
})

object Bank : GitVcsRoot({
    name = "Bank"
    url = "git@github.com:tolache/Bank.git"
    pushUrl = "git@github.com:tolache/Bank.git"
    branch = "refs/heads/master"
    branchSpec = """
        +:refs/heads/*
        +:refs/tags/*
    """.trimIndent()
    useTagsAsBranches = true
    authMethod = uploadedKey {
        userName = "git"
        uploadedKey = "github.pem"
    }
})

object BbrepoA : GitVcsRoot({
    name = "bbrepoA"
    url = "https://acherenkovjb@bitbucket.org/acherenkovjb/bbrepoa.git"
    branch = "refs/heads/master"
    authMethod = password {
        userName = "acherenkovjb"
        password = "credentialsJSON:f1d911e4-4338-4451-90b1-2c1a65605525"
    }
})

object RepoB : GitVcsRoot({
    name = "repoB"
    url = "git@github.com:tolache/repoB.git"
    branch = "refs/heads/master"
    branchSpec = "+:refs/heads/*"
    authMethod = uploadedKey {
        userName = "git"
        uploadedKey = "github.pem"
    }
})

object RepoC : GitVcsRoot({
    name = "repoC"
    url = "https://github.com/tolache/repoC"
    branch = "refs/heads/master"
    branchSpec = "+:refs/heads/*"
    authMethod = password {
        userName = "tolache"
        password = "credentialsJSON:7d8cca8e-bc35-4156-a965-0b32123691bc"
    }
})
