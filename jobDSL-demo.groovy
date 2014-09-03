job {
    // Job name
    name('demo-1')
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
    // SCM configuration
    scm {
        // Git repository to checkout
        git('git://github.com/sonyxperiadev/pygerrit.git')
    }
    // Build steps
    steps {
        shell('./unittests.py')
    }
}