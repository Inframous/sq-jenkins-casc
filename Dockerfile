FROM jenkins/jenkins:lts

ENV JAVA_OPTS -Djenkins.install.runSetupWizard=false
ENV CASC_JENKINS_CONFIG /var/jenkins_home/casc.yaml
ENV SSH_PRIVATE_FILE_PATH /var/jenkins_home/.ssh/keys/sq-proj1-ssh
ENV AWS_ACCESS_KEY='<aws_access_key_id>'
ENV AWS_SECRET_ACCESS_KEY='<aws_secret_access_key>'


COPY --chown=jenkins:jenkins plugins.txt /usr/share/jenkins/ref/plugins.txt
COPY --chown=jenkins:jenkins sq-proj1-ssh /var/jenkins_home/.ssh/keys/sq-proj1-ssh

RUN jenkins-plugin-cli -f /usr/share/jenkins/ref/plugins.txt

COPY casc.yaml /var/jenkins_home/casc.yaml

