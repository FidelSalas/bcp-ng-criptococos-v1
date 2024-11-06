/*
  La plantilla presentada a continuación tiene únicamente carácter referencial.
  Por consiguiente, antes de ejecutar tu pipeline es imprescindible que la completes y valides con el equipo de DevSecOps.
  Para mayor información, consulta el Modelo Operativo de DevSecOps: https://confluence.devsecopsbcp.com/x/apCNHw
*/

@Library('jenkins-sharedlib@master')
import sharedlib.WebAngularMicroAppUtil;

def utils = new WebAngularMicroAppUtil(steps, this);

def project               = 'CRYP';
def deploymentEnvironment = 'dev';
def recipients            = '';

try {
   node { 
    stage('Preparation') {
      utils.notifyByMail('START', recipients);
      checkout scm;
      env.project               = "${project}";
      env.deploymentEnvironment = "${deploymentEnvironment}";
      env.buildTimestamp        = utils.getBuildTimestamp();
      utils.setNodeVersion("NODE18_CHROME113");
      utils.setReportPathsForSonar("coverage/bcp-ng-criptococos-v1/lcov.info");
      utils.setHashicorpVaultEnabled(true);
      utils.setHashicorpVaultEnvironment("${deploymentEnvironment}");
      utils.setHashicorpVaultInstance(true);
      utils.setHashicorpVaultNamespace("${project}".toLowerCase());
      utils.prepare();
      utils.setAzureKeyVaultEnabled(false);
    }

    stage('Build') {
      utils.build();
    }

    stage('QA') {
      utils.executeQA();
    }

    stage('SAST Analysis') {
      utils.executeSast();
    }

    stage('Deploy Artifact') {
      utils.uploadArtifact();
    }

    stage('Execute SCA') {
      utils.executeXraySCA();
    }

    stage('Deploy Azure') {
      def APP_NAME                  = utils.getApplicationNameNode();
      def APP_VERSION               = utils.getApplicationVersionNode();
      def AZURE_RESOURCE_GROUP_NAME = "YOUR_AZURE_RESOURCE_GROUP_NAME";
      def AZURE_WEBAPP_NAME         = "YOUR_AZURE_WEBAPP_NAME";
      def API_BCP_URL               = "YOUR_API_BCP_URL";

      utils.setDockerBuildEnvironmentVariables([
        "API_BCP_URL=${API_BCP_URL}"
      ]);

      utils.deployToAzureWebappContainer(APP_NAME,APP_VERSION,AZURE_RESOURCE_GROUP_NAME,AZURE_WEBAPP_NAME,true);
    }

    stage('Results') {
      utils.saveResult("tgz");
    }

    stage('Post Execution') {
      utils.executePostExecutionTasks();
      utils.notifyByMail('SUCCESS', recipients);
    }
  }
} catch(Exception e) {
  node {
    utils.executeOnErrorExecutionTasks();
    utils.notifyByMail('FAIL', recipients);
    throw e;
  }
}
