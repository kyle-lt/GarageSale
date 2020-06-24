# GarageSale
## Overview
This project was developed in order to get hands-on experience instrumenting a Java Spring Boot application using the OpenTelemetry SDK by hand, as well as instrumenting with an Enterprise-class APM agent (AppDynamics).
There is no guarantee that this applicaiton is built to any best practices or standards, and in certain cases is explicitly designed to not be performant, and so from the angle of tracing and monitoring, it's all good.
 
## Prerequisites
// TODO
In order to use this extension, you do need a [Standalone __JAVA__ Machine Agent](https://docs.appdynamics.com/display/PRO45/Standalone+Machine+Agents) or [__SIM__ Agent](https://docs.appdynamics.com/display/PRO45/Server+Visibility).  For more details on downloading these products, please  visit https://download.appdynamics.com/.

In order to build this extension, you'll need:
- Java 1.8+
- Maven 3.x

## Installation
1. Clone this repository to your local repository.
2. To build, run `mvn clean install` and find the `Snmp-Trap-Receiver-Extension-1.0.zip` file in the `target` folder.
3. Unzip and copy that directory to `<MACHINE_AGENT_HOME>/monitors`

Please place the extension in the "__monitors__" directory of your __Machine Agent__ installation directory. Do not place the extension in the "__extensions__" directory of your __Machine Agent__ installation directory.

## Configuration
### config.yml
Configure the extension by editing the `config.yml` file in `<MACHINE_AGENT_HOME>/monitors/SnmpTrapReceiver/`
1. If not using SIM, or if metrics and events should correlate to an Application Tier, configure the "tier" under which the metrics need to be reported. This can be done by changing the value of `<COMPONENT_ID>` in

     `metricPrefix: "Server|Component:<COMPONENT_ID>|Custom Metrics|SNMP Trap Receiver"`

2. Configure the `machineAgentConnection` to the Machine Agent HTTP Listener.<br/>For example,
 
    ```
      # Machine Agent HTTP Listener
      # All of the values shoule be strings wrapped in double quotes - see defaults for examples
      # host should remain as localhost, or perhaps loopback "127.0.0.1"
      host: "localhost"
      # port default matches Machine Agent default of "8293". If using different port for
      # the machine agent, e.g., passing JVM arg -Dmetric.http.listener.port=8080, port
      # value should be set as "8080"
      port: "8293"
    ```
 3. Configure the `snmpConnection` information where applicable.<br/>For example,
    ```
      # SNMP Trap Receiver Connection
      # All of the values shoule be strings wrapped in double quotes - see defaults for examples
      snmpConnection:
        # snmpProtocol can be "tcp" or "udp"
        snmpProtocol: "udp"
        snmpIP: "0.0.0.0"
        # Port values less than 1024 require Machine Agent to run as root/sudo
        snmpPort: "16200"
        # The below configurations are for v3 only - if not using v3, leave as-is
        snmpUsername: "username"
        # Auth Protocol Options are "MD5" or "SHA"
        snmpAuthProtocol: "MD5"
        snmpAuthPassPhrase: "authpassphrase"
        # Privacy/Encryption Protocol Options are "AES128", "AES192", "AES256", "DES", or "3DES"
        snmpPrivacyProtocol: "AES128"
        snmpPrivacyPassPhrase: "privacypassphrase"
    ```
 4. When starting the machine agent, pass the [HTTP Listener configurations](https://docs.appdynamics.com/display/PRO45/Standalone+Machine+Agent+HTTP+Listener)
 
Please copy all the contents of the config.yml file and go to http://www.yamllint.com/ . On reaching the website, paste the contents and press the “Go” button on the bottom left.

## Metrics
By default, the extension assumes SIM, and uses the following default configuration:
    
    
      # Use this format if using SIM (Server Visibility enabled) Machine Agent
      metricPrefix: "Custom Metrics|SNMP Trap Receiver"
    
    
Metrics will be reported under the following metric tree:

`Application Infrastructure Performance|Root|Individual Nodes|<SIM_SERVER_NAME>|Custom Metrics|SNMP Trap Receiver`

Alternatively, you can map metrics to a Tier within an application. If doing so, we strongly recommend using the tier specific metric prefix so that metrics are reported only to a specified tier. Please change the metric prefix in your `config.yaml`

    
      # Use this format if using plain Machine Agent
      # This will create it in specific Tier/Component. Make sure to replace <COMPONENT_ID> 
      # with the appropriate one from your environment.
      metricPrefix: "Server|Component:<COMPONENT_ID>|Custom Metrics|SNMP Trap Receiver"
    
To find the <COMPONENT_ID> in your environment, please follow the instructions [here](https://docs.appdynamics.com/display/PRO45/Build+a+Monitoring+Extension+Using+Java)

Metrics will now be seen under the following metric tree:

`Application Infrastructure Performance|<COMPONENT_ID>|Custom Metrics|SNMP Trap Receiver`

## Testing
This repo contains some artifacts to ease setup for testing, and issuing test calls to confirm functionality.  In order to use these artifacts, you'll need:
- Docker
- Docker-Compose
- Internet Connection (to pull a Docker image from Docker Hub)

### containerDeploy.sh
- Script to build and deploy a Docker container with a SIM machine agent and this extension with `DEBUG` logging enabled.
- Edit `docker-compose.yml` to point to your AppD Controller of choice.

### sendTestTraps.sh
- Script to send test TRAPs and INFORMs for all SNMP versions to the extension running in the Docker container to verify functionality

## Credentials Encryption
Please visit [this page](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-Password-Encryption-with-Extensions/ta-p/29397) to get detailed instructions on password encryption. The steps in this document will guide you through the whole process.

## Extensions Workbench
Workbench is an inbuilt feature provided with each extension in order to assist you to fine tune the extension setup before you actually deploy it on the controller. Please review the following document on [How to use the Extensions WorkBench](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-the-Extensions-WorkBench/ta-p/30130)

## Troubleshooting
Please follow the steps listed in this [troubleshooting-document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) in order to troubleshoot your issue. These are a set of common issues that customers might have faced during the installation of the extension. If these don't solve your issue, please follow the last step on the [troubleshooting-document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) to contact the support team.

## Support Tickets
If after going through the [Troubleshooting Document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) you have not been able to get your extension working, please file a ticket and add the following information.

Please provide the following in order for us to assist you better.

1. Stop the running machine agent.
2. Delete all existing logs under `<MachineAgent>/logs`.
3. Please enable debug logging by editing the file `<MachineAgent>/conf/logging/log4j.xml`. Change the level value of the following `<logger>` elements to `debug`.
   </br>`<logger name="com.singularity">`
   </br>`<logger name="com.appdynamics">`
4. Start the machine agent and please let it run for 10 mins. Then zip and upload all the logs in the directory `<MachineAgent>/logs/*`.
5. Attach the zipped `<MachineAgent>/conf/*` directory here.
6. Attach the zipped `<MachineAgent>/monitors/ExtensionFolderYouAreHavingIssuesWith` directory here.
For any support related questions, you can also contact [help@appdynamics.com](mailto:help@appdynamics.com).

## Contributing
Always feel free to fork and contribute any changes directly via [GitHub](https://github.com/Appdynamics/snmp-trap-receiver-extension).

## Version
Name |	Version
---|---
Extension Version |	1.0.0
Controller Compatibility | 4.5.x or Later
Last Update |	06/09/2020

