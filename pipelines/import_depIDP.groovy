pipelineJob('Import-deploy-IDP') {
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
                                    pipelineJob('sq-pj3-Deploy-IDP') {
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
                                                scriptPath('pipeline-jobs/deploy_idp')
                                                
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

