pipelineJob('AWS-Test') {
    definition {
        sandbox {
            cps {
            script('''\
                pipeline {
                    agent { label "Prod1" }
                    environment {
                        SSH_CREDENTIALS = "ssh-Jenkins-Controller"
                        AWS_CREDENTIALS = "aws-Jenkins-Controller"
                        AWS_BUCKET_NAME = "sq-proj1-bucket"
                        SERVER_USERNAME = "ubuntu"
                        TRIGGER_DEPLOY_JOB_NAME = "AWS-Deploy"
                        GIT_SSH_CREDENTIALS = 'ssh-GitHub'
                        GIT_REPO_URL_HTTPS = "https://github.com/Inframous/project_1.git"
                        GIT_REPO_URL_SSH = "git@github.com:Inframous/project_1.git"
                    }
                    triggers {
                        githubPush()
                    }
                    stages {
                        // Cloning the repo, this also connects the job with GitHub's WebHook
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
                        stage('build'){
                            // Building the app's image
                            steps{
                                sh """
                                sudo docker build -t my-app .
                                """
                                }
                            }
                        stage('test'){
                            // Launching a container and running a test.
                            steps{
                                script {
                                    def result = sh(script: 'sudo docker run --rm -p 5000:5000 my-app /bin/sh -c "python -m unittest discover"', returnStatus: true)
                                    if (result == 0) {
                                    env.RESULT = "SUCCESS"
                                        }
                                    else {
                                        env.RESULT = "FAIL"
                                        }
                                    }
                                }
                            }
                        stage('Logging') {
                            steps {
                                script {
                                    // Setting up a variable with the current date and time.
                                def now = new Date().format("dd/MM/yy-HH:mm", TimeZone.getTimeZone('UTC'))
                                    env.NOW = now
                                    // Trying to download report.csv from the bucket
                                def report_file = sh(script: "wget https://${AWS_BUCKET_NAME}.s3.eu-central-1.amazonaws.com/report.csv", returnStatus: true)
                                    // If this is the first run, the CSV file won't be there, so we'll create it.
                                if (report_file != 0) {
                                wrap([$class: 'BuildUser']) {
                                sh 'echo "user_name,date,test_result" > report.csv'
                                    }
                                }
                                    // Adding the current build's info to the file (Using the 'build user vars plugin')
                                wrap([$class: 'BuildUser']) {
                                sh 'echo "$BUILD_USER","$NOW","$RESULT" >> report.csv'
                                    }
                                }
                            }
                        }
                        stage('Upload to AWS') {
                            steps { // Uploading the file to the S3 bucket
                                withAWS(region: 'eu-central-1', credentials: "${AWS_CREDENTIALS}") {
                                sh 'echo "Uploading content with AWS creds"'
                                    s3Upload(pathStyleAccessEnabled: true, payloadSigningEnabled: true, file:'report.csv', bucket: "${AWS_BUCKET_NAME}")
                                }
                                // Making sure that the file's acl is set to public
                                withAWS(region: 'eu-central-1', credentials: "${AWS_CREDENTIALS}") {
                                sh "aws s3api put-object-acl --bucket  ${AWS_BUCKET_NAME} --key 'report.csv' --acl public-read"
                                }
                            }
                        }
                        stage('Deploy') {
                            // If all went well (with the tests) run the next (Deploy) job.
                            when {
                                expression {
                                    env.RESULT == 'SUCCESS' 
                                }
                            }
                            steps {
                                build job: "${TRIGGER_DEPLOY_JOB_NAME}", wait: false
                            }
                        }
                    }
                }'''.stripIndent())
            }
        }

    }
}
