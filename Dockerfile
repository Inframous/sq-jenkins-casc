FROM jenkins/jenkins:lts

ENV JAVA_OPTS -Djenkins.install.runSetupWizard=false
ENV CASC_JENKINS_CONFIG /var/jenkins_home/jenkins_casc_main.yaml
ENV SSH_PRIVATE_FILE_PATH /var/jenkins_home/.ssh/keys/master-controller
ENV AWS_ACCESS_KEY='<aws_access_key_id>'
ENV AWS_SECRET_ACCESS_KEY='<aws_secret_access_key>'

COPY --chown=jenkins:jenkins pipelines/*.groovy /var/jenkins_home/pipelines/
COPY --chown=jenkins:jenkins plugins.txt /usr/share/jenkins/ref/plugins.txt
COPY --chown=jenkins:jenkins master-controller /var/jenkins_home/.ssh/keys/master-controller

RUN jenkins-plugin-cli -f /usr/share/jenkins/ref/plugins.txt
COPY jenkins_casc_main.yaml /var/jenkins_home/jenkins_casc_main.yaml


