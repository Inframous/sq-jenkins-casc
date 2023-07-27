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
                                    pipelineJob('Deploy-IDP') {
                                        triggers {
                                            githubPush()
                                        }
                                        definition {
                                            cpsScm {
                                                scm {
                                                    git {
                                                        remote {
                                                            url('git@github.com:Inframous/sq-pj2-idp')
                                                            credentials('ssh-GitHub')
                                                        }
                                                        branch('main')
                                                    }
                                                }
                                                scriptPath('pipelines/sq-pj4-eks/Jenkinsfile')
                                                
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

