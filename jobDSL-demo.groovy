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

// Get current branch name
def thr = Thread.currentThread()
def build = thr?.executable
def envVarsMap = build.parent.builds[0].properties.get("envVars")
def branch = envVarsMap.get('GIT_BRANCH').split('/')[1]

for ( testcase in ['test_patchset_created', 'test_draft_published', 'test_ref_updated', 'test_merge_failed'] ) {
    job {
        // Job name
        name(branch + '-unittest-' + testcase)
        // Settings from template
        using('template')
        // SCM configuration
        scm {
            // Git repository to checkout
            git('git://github.com/sonyxperiadev/pygerrit.git')
        }
        // Build steps
        steps {
            shell('python unittests.py TestGerritEvents.' + testcase)
        }
    }
}