/*
  La plantilla presentada a continuación tiene únicamente carácter referencial.
  Por consiguiente, antes de ejecutar tu pipeline es imprescindible que la completes y valides con el equipo de DevSecOps.
  Para mayor información, consulta el Modelo Operativo de DevSecOps: https://confluence.devsecopsbcp.com/x/apCNHw
*/

@Library('jenkins-sharedlib@master')
import sharedlib.WebAngularMicroAppUtil;

def utils = new WebAngularMicroAppUtil(steps, this);

def project               = 'CRYP';
def deploymentEnvironment = 'cert';
def recipients            = '';

try {
   node {
    stage('Preparation') {
      utils.notifyByMail('START', recipients);
      checkout scm;
      env.project               = "${project}";
      env.deploymentEnvironment = "${deploymentEnvironment}";
      utils.setNodeVersion("NODE18_CHROME113");
      utils.setReportPathsForSonar("coverage/bcp-ng-criptococos-v1/lcov.info");
      utils.setHashicorpVaultEnabled(true);
      utils.setHashicorpVaultEnvironment("${deploymentEnvironment}");
      utils.setHashicorpVaultInstance(true);
      utils.prepare();
      utils.setAzureKeyVaultEnabled(false);
    }

    stage('Start Release') {
      utils.startReleaseForNode("--prod --base-href ./")
    }

    stage("Deploy to " +deploymentEnvironment) {
      def APP_NAME                  = utils.getApplicationNameNode();
      def APP_VERSION               = utils.getApplicationVersionNode();
      def AZURE_RESOURCE_GROUP_NAME = "YOUR_AZURE_RESOURCE_GROUP_NAME";
      def AZURE_WEBAPP_NAME         = "YOUR_AZURE_WEBAPP_NAME";

      utils.setDockerBuildEnvironmentVariables([
        "API_BCP_URL=apisbcp.com",
        "API_EXTERNAL_URL=int.apisdevbcp.com",
        "API_GATEWAY_SCOPE=channel.xxxx.default"
      ]);

      utils.withAzureVaultCredentials([
        [azureCredentialId: "api-gateway-client-id", azureCredentialVariable: "apiGatewayClientId" ],
        [azureCredentialId: "api-gateway-client-secret", azureCredentialVariable: "apiGatewayClientSecret" ],
        [azureCredentialId: "api-external-key", azureCredentialVariable: "apiExternalKey" ],
      ]){

        utils.setWebAppSettingsEnvironmentVariables([
          "API_GATEWAY_CLIENT_ID=${env.apiGatewayClientId}",
          "API_GATEWAY_CLIENT_SECRET=${env.apiGatewayClientSecret}",
          "API_EXTERNAL_KEY=${env.apiExternalKey}",
          "API_BCP_URL=https://bcp-node-api-mock.azurewebsites.net"
        ]);

        utils.deployToAzureWebappContainer(APP_NAME,APP_VERSION,AZURE_RESOURCE_GROUP_NAME,AZURE_WEBAPP_NAME)
      }
    }

    stage('Save Results') {
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
