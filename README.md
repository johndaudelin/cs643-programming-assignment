# Programming Assignment 1

## IAM Setup

Navigate to IAM and click on Access Management -> Policies. Find and select the following 3 policies:

- AmazonRekognitionFullAccess
- AmazonS3FullAccess
- AmazonSQSFullAccess

## EC2 Setup

Click on "EC2" in the services section, then click on "Launch EC2." Select the AMI 'ami-032930428bf1abbff' (should be the 2nd one listed). Select the t2.micro type (free tier eligible).

Under Security Groups, click "Edit Security Groups" and add three rules: one for SSH, one for HTTP, and one for HTTPS. For each one, select a source of "My IP."

Under Instance Details, click "Edit Instance Details." Change the number of instances to 2 instead of 1.

Click Launch. On the dialog that pops up, select "Create a new key pair" and name it "cs643." Hit "Download key pair." Open a termianl and move the .pem file to your home directory. Run the following command to set the correct permissions for the .pem file:

    $ chmod 400 cs643.pem

To connect to your EC2 instance (after is finished loading), run the following command in your terminal:

    $ ssh -i "~/cs643.pem" ec2-user@<YOUR_INSTANCE_PUBLIC_DNS>

After you have connected, run the following commands to update Java from 1.7 to 1.8:

    $ sudo yum install java-1.8.0-devel
    $ sudo /usr/sbin/alternatives --config java
    $ sudo /usr/sbin/alternatives --config javac

THE FOLLOWING STEP MAY BE OPTIONAL....

Install Apache Maven on the EC2:

    $ sudo wget https://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
    $ sudo sed -i s/\$releasever/6/g /etc/yum.repos.d/epel-apache-maven.repo
    $ sudo yum install -y apache-maven

## AWS Setup

Login to AWS Educate account, and in Vocareum, click on "Account Details." Click on "Access your credentials" and copy the text that is displayed. On your EC2, create a file, `~/.aws/credentials`, and paste those copied credentials into this file. Re-copy and paste these credentials whenever they change (after your session ends).

If testing locally, do the same steps as above except on your local machine.

## Local Java Setup

Run the following commands:

    sudo apt install openjdk-11-jre-headless
    sudo apt install openjdk-11-jdk-headless
    sudo apt install maven

## AWS SDK Setup

THIS ENTIRE SECTION IS PROBABLY UNNECESSARY...

Download the AWS SDK for Java by running the following commands:

    $ cd ~
    $ git clone https://github.com/aws/aws-sdk-java-v2.git
    $ cd aws-sdk-java-v2

Install the entire SDK with the following command:

    $ mvn clean install

## Maven Project Setup

After cloning this repository, run the following commands to generate a .jar file in the target/ directory and then run the DetectCars.java code:

    $ cd cs643-programming-assignment/assignment1
    $ mvn clean install package
    $ java -jar target/assignment1-1.0-SNAPSHOT.jar
