FROM jenkins/jenkins:lts

ENV JAVA_OPTS -Djenkins.install.runSetupWizard=false
ENV CASC_JENKINS_CONFIG /usr/share/jenkins/casc_files/jenkins_casc_main.yaml
ENV SSH_PRIVATE_FILE_PATH /usr/share/jenkins/casc_files/.ssh/keys/master-controller
ENV AWS_ACCESS_KEY='<aws_access_key_id>'
ENV AWS_SECRET_ACCESS_KEY='<aws_secret_access_key>'

COPY --chown=jenkins:jenkins pipelines/*.groovy /usr/share/jenkins/casc_files/pipelines/
COPY --chown=jenkins:jenkins plugins.txt /usr/share/jenkins/ref/plugins.txt
COPY --chown=jenkins:jenkins master-controller /usr/share/jenkins/casc_files/.ssh/keys/master-controller

RUN jenkins-plugin-cli -f /usr/share/jenkins/ref/plugins.txt

COPY jenkins_casc_main.yaml /usr/share/jenkins/casc_files/jenkins_casc_main.yaml

