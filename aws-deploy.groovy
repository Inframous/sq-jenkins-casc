pipelineJob('AWS-Deploy') {
    definition {
        cps {
        script('''\
            PROD_IPS = ["10.123.1.12", "10.123.1.13"] // <<--- Input Prod1 & Prod2 aws-private-ip-address.
            pipeline {
                agent { label 'J_Agent' }
                environment {
                    KEY = 'BOTH' // <<--- Set ONE, TWO or BOTH to decide to which production server you'd like to deploy.
                    SSH_CREDENTIALS = 'ssh-Jenkins-Controller' // << ssh credentials to connect to Prod1,Prod2 EC2
                    
                    AWS_CREDENTIALS = 'aws-Jenkins-Controller' // << aws credentials
                    AWS_REGION = "eu-central-1" 
                    AWS_BUCKET_NAME = "sq-proj1-bucket"
                    
                    SERVER_USERNAME = "ubuntu"
                    
                    GIT_SSH_CREDENTIALS = 'ssh-GitHub' // <<--- Git SSH Credentials.
                    GIT_REPO_URL_HTTPS = "https://github.com/Inframous/project_1.git" 
                    GIT_REPO_URL_SSH = "git@github.com:Inframous/project_1.git"
                }
                stages {
                    stage('Deploy to servers') {
                        steps {
                            // creating a list of cases
                            script {
                                def prod_ips = []
                                switch (KEY) {
                                    case "ONE":
                                        prod_ips = [PROD_IPS[0]]
                                        break
                                    case "TWO":
                                        prod_ips = [PROD_IPS[1]]
                                        break
                                    default:
                                        prod_ips = PROD_IPS
                                        break
                                }
                                
                                // In whichever is the selected case (number of machines to deploy to) Run the following :
                                prod_ips.each { ip ->
                                    // Stopping & deleteing the running container, and removing its image incase this isn't the first run.
                                    try { 
                                        sshagent(credentials: ["${SSH_CREDENTIALS}"]) {
                                            sh """
                                                ssh "${SERVER_USERNAME}"@"${ip}" '
                                                sudo docker stop my-app-deployed
                                                sudo docker rm -f my-app-deployed
                                                sudo docker image rm my-app
                                                ' 
                                            """
                                        }
                                    } catch (err) { // Just incase it is the first run, catching the error or any others that might happen.
                                        echo "There might have been an error stopping/removing the container/image on ${ip}..."
                                    } finally {
                                        echo "Done with cleanup, deploying app..."
                                    }
                                    // Building and deploying the app in the Production Server.
                                    sshagent(credentials: ["${SSH_CREDENTIALS}"]) {
                                        sh """
                                            ssh "${SERVER_USERNAME}"@"${ip}" 'git clone "${GIT_REPO_URL_HTTPS}"
                                            cd project_1 && sudo docker build -t my-app .
                                            sudo docker run -d -p 5000:5000 --restart always --name my-app-deployed my-app'
                                        """
                                    }
                                }
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
                                sh """
                                    wget https://"${AWS_BUCKET_NAME}".s3.eu-central-1.amazonaws.com/report.csv
                                    input=\$(tail -n 1 report.csv) 
                                    TestId="\$((\$(wc report.csv -l | awk '{ print \$1 }') + 1))"
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
                                """
                            }
                        }
                    }
                }
            }'''.stripIndent())
        }
    }
}
