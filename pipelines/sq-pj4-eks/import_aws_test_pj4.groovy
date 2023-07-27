pipelineJob('Import-AWS-Test') {
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
                                    pipelineJob('AWS-Test') {
                                        triggers {
                                            githubPush()
                                        }
                                        definition {
                                            cpsScm {
                                                scm {
                                                    git {
                                                        remote {
                                                            url('git@github.com:Inframous/sq-pj1-app')
                                                            credentials('ssh-GitHub')
                                                        }
                                                        branch('main')
                                                    }
                                                }
                                                scriptPath('pipelines/sq-pj4-eks/aws-test-pj4')
                                                
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

