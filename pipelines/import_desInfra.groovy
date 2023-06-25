pipelineJob('Import-awsDestroyInfra') {
    definition {
        cps {
        script('''\
            pipeline {
                agent any
                stages {
                    stage('Generate Job') {
                        steps {
                            jobDsl(
                                scriptText: \'\'\'
                                    pipelineJob('sq-pj3-DestroyInfra') {
                                        definition {
                                            cpsScm {
                                                scm {
                                                    git {
                                                        remote {
                                                            url('git@github.com:Inframous/sq-pj3-infra')
                                                            credentials('ssh-GitHub')
                                                        }
                                                        branch('main')
                                                    }
                                                }
                                                scriptPath('extra_code/pipeline-jobs/awsDestroyContainers')
                                                
                                            }
                                        }
                                    }
                                \'\'\'
                            )
                        }
                    }
                }
            }'''.stripIndent())
        }
    }
}

