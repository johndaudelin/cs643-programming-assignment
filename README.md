# Programming Assignment 1

## IAM Setup

Navigate to IAM and click on Access Management -> Policies. Find and select the following 3 policies:

- AmazonRekognitionFullAccess
- AmazonS3FullAccess
- AmazonSQSFullAccess

## EC2 Setup

Click on "EC2" in the services section, then click on "Launch EC2." Select the AMI 'ami-032930428bf1abbff' (should be the 2nd one listed). Select the t2.micro type (free tier eligible).

Under Security Groups, click "Edit Security Groups" and add three rules: one for SSH, one for HTTP, and one for HTTPS. For each one, select a source of "My IP."

Under Instance Details, click "Edit Instance Details." Change the number of instances to 2 instead of 1. Change the IAM role to the "CS643_Role" that you created in the previous step.

Click Launch. On the dialog that pops up, select "Create a new key pair" and name it "cs643." Hit "Download key pair." Open a termianl and move the .pem file to your home directory. Run the following command to set the correct permissions for the .pem file:

    $ chmod 400 cs643.pem

To connect to your EC2 instance (after is finished loading), run the following command in your terminal:

    $ ssh -i "~/cs643.pem" ec2-user@<YOUR_INSTANCE_PUBLIC_DNS>

After you have connected, run the following commands to update Java from 1.7 to 1.8:

    $ sudo yum install java-1.8.0-devel
    $ sudo /usr/sbin/alternatives --config java
    $ sudo /usr/sbin/alternatives --config javac

Install Apache Maven on the EC2:

    $ sudo wget https://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
    $ sudo sed -i s/\$releasever/6/g /etc/yum.repos.d/epel-apache-maven.repo
    $ sudo yum install -y apache-maven

## AWS Setup

Login to AWS Educate account, and in Vocareum, click on "Account Details." Click on "Access your credentials" and copy the text that is displayed. On your EC2, create a file, `~/.aws/credentials`, and paste those copied credentials into this file. Re-copy and paste these credentials whenever they change (after your session ends).

## Local Java Setup

Run the following commands:

    sudo apt install openjdk-11-jre-headless
    sudo apt install openjdk-11-jdk-headless
    sudo apt install maven

## Maven Project Setup

Run the following command:

    mvn org.apache.maven.plugins:maven-archetype-plugin:3.1.0:generate -DarchetypeGroupId=software.amazon.awssdk -DarchetypeArtifactId=archetype-lambda -DarchetypeVersion=2.14.3 -Dservice=s3 -Dregion=US_EAST_1 -DgroupId=com.cs643 -DartifactId=assignment1

Add the following dependencies for SQS, S3, and Rekognition to the "dependencies" object in the pom.xml file that is created:

    <dependency>
        <groupId>software.amazon.awssdk</groupId>
        <artifactId>s3</artifactId>
    </dependency>

    <dependency>
        <groupId>software.amazon.awssdk</groupId>
        <artifactId>rekognition</artifactId>
        <version>2.13.31</version>
    </dependency>

    <dependency>
        <groupId>software.amazon.awssdk</groupId>
        <artifactId>sqs</artifactId>
    </dependency>

Run the following command to generate a .jar file in the target/ directory and then run the DetectCars.java code:

    mvn package
    java -cp target/assignment1-1.0-SNAPSHOT.jar com.cs643.DetectCars
