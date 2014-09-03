// Get current branch name
def thr = Thread.currentThread()
def build = thr?.executable
def envVarsMap = build.parent.builds[0].properties.get("envVars")
def gitBranch = envVarsMap.get('GIT_BRANCH').split('/')[1]

def template = job {
    // Job name
    name('template')
    // Where to execute
    label('master')
    // Discrad old builds
    logRotator(daysToKeepInt = 10, numToKeepInt = 10, artifactDaysToKeepInt = 5, artifactNumToKeepInt = 5)
    // Execute concurrent builds if necessary
    concurrentBuild()
    // Try 3 times if checkout failed before failing a build
    checkoutRetryCount()
    // Different environment settings
    wrappers {
        // Add timestaps to console
        timestamps()
        // Colorful console output 
        colorizeOutput('xterm')
        // Build timeout
        timeout {
            absolute(minutes = 60)
            failBuild()
        }
    }
}

def ciutilsTemplate = job {
    using('template')
    // Job name
    name('ciutilsTemplate')
    scm {
        git {
            remote {
                url('ssh://gerritmirror:29418/platform/tools/ciutils')
                refspec('${GERRIT_REFSPEC}')
                credentials('eanddev')
            }
            wipeOutWorkspace()
            branch('${GERRIT_PATCHSET_REVISION}')
        }
    }
    // Gerrit strategy, currently not supported by plugin
    configure { project ->
        project/scm/extensions << 'hudson.plugins.git.extensions.impl.BuildChooserSetting' {
	    buildChooser(class: 'com.sonyericsson.hudson.plugins.gerrit.trigger.hudsontrigger.GerritTriggerBuildChooser') {
                separator('#')
            }
        }
    }
    // Configure Gerrit trigger
    triggers {
        gerrit {
    	    events {
                ChangeRestored
                DraftPublished
	        PatchsetCreated
    	    }
    	    project('plain:platform/tools/ciutils', ['plain:' + gitBranch])
        }
    }
    // We don't want this template job to be executed
    disabled()   
}

job {
    name(gitBranch + '-ciutils-review')
    using('ciutilsTemplate')
    disabled(false)
    steps {
        shell(readFileFromWorkspace('ciutils.sh'))
        environmentVariables {
	    propertiesFile('prop.env')
        }
    }
    publishers {
        cobertura('coverage.xml')
	violations {
   	    pylint(0, 10, 999, 'pylint.log')
	}
    }
}