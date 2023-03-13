pipelineJob('AWS-Test') {
    definition {
        cps {
        script('''\
            pipeline {
                agent any
                stages {
                    stage('Generate Job') {
                        steps {
                            jobDsl(
                                scriptText: '''
                                    pipelineJob('AWS-Test') {
                                        definition {
                                            cpsScm {
                                                scm {
                                                    git {
                                                        remote {
                                                            url('git@github.com:Inframous/project_1.git')
                                                            credentials('ssh-GitHub')
                                                        }
                                                        branch('main')
                                                    }
                                                }
                                                scriptPath('infrastructure/pipeline/aws-test-done')
                                                
                                            }
                                        }
                                    }
                                '''
                            )
                        }
                    }
                }
            }'''.stripIndent())
        }
    }
}

