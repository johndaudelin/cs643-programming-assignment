# Programming Assignment 1

## AWS Steps

To setup the cloud environment and run this application in the cloud on 2 EC2's, follow the below steps.

First, you will need to login to your AWS Console through the AWS Educate Starter Account link.

### IAM Setup

Navigate to IAM and click on Access Management -> Policies. Find and select the following 3 policies:

- AmazonRekognitionFullAccess
- AmazonS3FullAccess
- AmazonSQSFullAccess

### EC2 Setup

Click on "EC2" in the services section, then click on Launch Instance -> Launch Instance. Select the AMI 'ami-032930428bf1abbff' (should be the 2nd one listed). Select the t2.micro type (free tier eligible). Click Review and Launch.

Under Security Groups, click "Edit Security Groups" and add three rules: one for SSH, one for HTTP, and one for HTTPS. For each one, select a source of "My IP." Hit Review and Launch.

Click Launch. On the dialog that pops up, select "Create a new key pair" and name it "cs643." Hit "Download key pair." Hit Launch Instances, and then hit View Instances. You will probably see a status of "Pending" for your Instance State. While waiting for this to switch to "Running, open a terminal and move the .pem file you downloaded to your home directory. Run the following command to set the correct permissions for the .pem file:

    $ chmod 400 cs643.pem

To connect to your EC2 instance (after the EC2 has finished loading), run the following command in your terminal (replacing <YOUR_INSTANCE_PUBLIC_DNS> with the "Public IPv4 DNS" attribute of your running EC2 instance:

    $ ssh -i "~/cs643.pem" ec2-user@<YOUR_INSTANCE_PUBLIC_DNS>

After you have connected, run the following commands to update Java from 1.7 to 1.8 on the EC2:

    $ sudo yum install java-1.8.0-devel
    $ sudo /usr/sbin/alternatives --config java

Upon running the second command, you should enter the number that corresponds to jre-1.8.0 (either 1 or 2) and hit enter.

At this point, you're ready to clone the EC2 to make your second EC2. Select your already existing instance, and click on Actions -> Create Image. Name the image, "jdaudelin-image," and click "Create Image." Launch a new EC2 instance and be sure to select "jdaudelin-image" as the image for your new EC2 instance. Also, make sure to select the SAME, already existing cs643.pem key pair that you created for the first EC2 instance. Refer to [this link](https://docs.bitnami.com/aws/faq/administration/clone-server/) if any of these steps are confusing.

You will SSH into this second instance the same way you SSH into your first instance, just using the new public DNS of course.

### Credentials Setup

Login to your AWS Educate account, and in Vocareum, click on "Account Details." Click on "Access your credentials" and copy the text that is displayed. SSH into both EC2's that you created in the previous step, and on each one, create a file, `~/.aws/credentials`. Paste those copied credentials into this file on each EC2. You will need to re-copy and paste these credentials onto both EC2's whenever they change (after your session ends).

### Running the application

In the terminal on your LOCAL machine, run the following commands to generate the .jar files for the two applications you will want to run on your EC2's:

    $ git clone https://github.com/johndaudelin/cs643-programming-assignment.git
    $ cd cs643-programming-assignment/text-recognition
    $ mvn clean install package
    $ cd ../car-recognition
    $ mvn clean install package

Run the following command to securely copy the car-recognition .jar file to one of your EC2's, which we'll call EC2-A:

    $ scp -i ~/cs643.pem car-recognition/target/car-recognition-1.0-SNAPSHOT.jar ec2-user@<YOUR_PUBLIC_DNS_FOR_EC2_A>:~/car-recognition.jar

Then run the following command to securely copy the text-recognition .jar file to the OTHER EC2, which we'll call EC2-B:

    $ scp -i ~/cs643.pem text-recognition/target/text-recognition-1.0-SNAPSHOT.jar ec2-user@<YOUR_PUBLIC_DNS_FOR_EC2_B>:~/text-recognition.jar

SSH into EC2-B and run the following commands:

    $ cd ~
    $ java -jar ~/text-recognition.jar

This will begin running the code for text recognition. However, since it depends on items in the SQS queue to process, it will simply wait until we begin running the car recognition code. To begin running the car recognition code, SSH into EC2-A and run the following command:

    $ java -jar ~/car-recognition.jar

Now, both programs will be running simultaneously. The program on EC2-A is processing all images in the S3 bucket (njit-cs-643) and sending the indexes of images that include cars to EC2-B through SQS, which in turn is processing these images to find out which ones include text as well. Finally, once both programs have finished running, you will find a file named `output.txt` on EC2-B in the home directory. This output file will display the indexes of images that contained both cars and text, along with the associated text from each image.

## Local Steps

To setup and run this program locally, follow these steps.

### Java/Maven Installation

Run the following commands:

    sudo apt install openjdk-11-jre-headless
    sudo apt install openjdk-11-jdk-headless
    sudo apt install maven

### Credentials Setup

Login to your AWS Educate account, and in Vocareum, click on "Account Details." Click on "Access your credentials" and copy the text that is displayed. Create a file on your machine, `~/.aws/credentials`. Paste those copied credentials into this file. You will need to re-copy and paste these credentials whenever they change (after your session ends).

### Running locally

After cloning this repository, run the following commands to run DetectText.java:

    $ cd cs643-programming-assignment/text-recognition
    $ mvn clean install package
    $ java -jar target/text-recognition-1.0-SNAPSHOT.jar

While this is running, open another terminal and run the following commands to run DetectCars.java:

    $ cd cs643-programming-assignment/car-recognition
    $ mvn clean install package
    $ java -jar target/car-recognition-1.0-SNAPSHOT.jar

Once DetectText has finished running, you should see a file named "output.txt" in the text-recognition folder with the final results.
