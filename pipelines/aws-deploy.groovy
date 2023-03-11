pipelineJob('AWS-Deploy') {
    definition {
        cps {
        script('''\
            pipeline {
                agent { label 'Prod1' }
                environment {
                    KEY = 'BOTH' // <<--- Set to 'Prod1', 'Prod2' or 'BOTH' to decide to which production server you'd like to deploy.
                    
                    AWS_CREDENTIALS = 'aws-Jenkins-Controller' // << aws credentials
                    AWS_REGION = "eu-central-1" 
                    AWS_BUCKET_NAME = "sq-proj1-bucket"
                    
                    GIT_SSH_CREDENTIALS = 'ssh-GitHub' // <<--- Git SSH Credentials.
                    GIT_REPO_URL_HTTPS = "https://github.com/Inframous/project_1.git" 
                    GIT_REPO_URL_SSH = "git@github.com:Inframous/project_1.git"
                }
                stages {
                    stage('Checkout SCM'){
                        steps {
                            checkout([
                                $class: 'GitSCM',
                                branches: [[name: 'main']],
                                userRemoteConfigs : [[
                                    url: "${GIT_REPO_URL_SSH}",
                                    credentialsId: "${GIT_SSH_CREDENTIALS}"
                                    ]]
                                ])
                        }
                    }
                    stage('Deploying App!') {
                        steps {
                            script {
                                try { 
                                    sh """
                                        sudo docker stop my-app-deployed
                                        sudo docker rm -f my-app-deployed
                                        sudo docker image rm my-app
                                        ' 
                                    """
                                } catch (err) { // Just incase it is the first run, catching the error or any others that might happen.
                                    echo "There might have been an error stopping/removing the container/image..."
                                    echo "If this is the first time running this job, you may ignore this."
                                } finally {
                                    echo "Done with cleanup, deploying app..."
                                }
                                // Building and deploying the app in the Production Server.

                                    // Not sure if I should clone the repo and if I should 'cd projec_1 or not'
                                sh """
                                    sudo docker build -t my-app .
                                    sudo docker run -d -p 80:80 --restart always --name my-app-deployed my-app
                                """
                            }
                        }
                    }
                    stage('Add item to DynamoDB') {
                        // Adding the test results of the previous job to the DynamoDB created:
                        // Downloading the csv from the bucket,
                        // extracting and parsing the latest resulst, 
                        // uploading the parsed data to an item within the DynamoDB table,
                        // deleting the csv file.
                        steps {
                            withAWS(credentials: 'aws-Jenkins-Controller', region: 'eu-central-1') {
                                sh '''
                                    wget https://"${AWS_BUCKET_NAME}".s3.eu-central-1.amazonaws.com/report.csv
                                    input=\$(tail -n 1 report.csv) 
                                    TestId="\$((\$(wc report.csv -l | awk '{ print \$1 }') - 1))"
                                    TestUser="\$(echo \$input | cut -d ',' -f1 | sed 's/ /-/g')"
                                    TestDate="\$(echo \$input | cut -d ',' -f2 )"
                                    TestResult="\$(echo \$input | cut -d ',' -f3 )"

                                    echo \$TestID \$TestUser \$TestDate \$TestResults

                                    aws dynamodb put-item \
                                    --table-name TestTable \
                                    --item \
                                    '{"TestId": {"S": "'\$TestId'"}, "TestDate": {"S": "'\$TestDate'"}, "TestResult": {"S": "'\$TestResult'"}, "TestUser": {"S": "'\$TestUser'"}}' \
                                    --return-consumed-capacity TOTAL
                                    rm report.csv
                                '''
                            }
                        }
                    }
                }
            }'''.stripIndent())
        }
    }
}
