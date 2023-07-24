pipelineJob('Import-AWS-Deploy') {
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
                                    pipelineJob('EKS-Deploy') {
                                        definition {
                                            cpsScm {
                                                scm {
                                                    git {
                                                        remote {
                                                            url('git@github.com:Inframous/sq-pj4-eks.git')
                                                            credentials('ssh-GitHub')
                                                        }
                                                        branch('main')
                                                    }
                                                }
                                                scriptPath('pipeline-jobs/eks-deploy')
                                                
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

