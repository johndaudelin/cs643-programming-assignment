// Author: John Daudelin
// CS643-851 Programming Assignment 1
// This code is intended to run on EC2 B
// I used example code from this repository: https://github.com/awsdocs/aws-doc-sdk-examples/blob/master/javav2/example_code/

package com.cs643;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.DetectTextRequest;
import software.amazon.awssdk.services.rekognition.model.DetectTextResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.rekognition.model.S3Object;
import software.amazon.awssdk.services.rekognition.model.TextDetection;
import software.amazon.awssdk.services.rekognition.model.TextTypes;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.ListQueuesRequest;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.QueueNameExistsException;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.List;

public class DetectText {

    public static void main(String[] args) {

        String bucketName = "njit-cs-643";
        String queueName = "cars.fifo"; // Fifo queue so that "-1" is always processed last

        Region region = Region.US_EAST_1;
        S3Client s3 = S3Client.builder().region(region).build();
        RekognitionClient rek = RekognitionClient.builder().region(region).build();
        SqsClient sqs = SqsClient.builder().region(region).build();

        processCarImages(s3, rek, sqs, bucketName, queueName);
    }

    public static void processCarImages(S3Client s3, RekognitionClient rek, SqsClient sqs, String bucketName,
            String queueName) {

        // Poll SQS until the appropriate queue is created (by DetectCars)
        while (true) {
            ListQueuesRequest queuesReq = ListQueuesRequest.builder().queueNamePrefix(queueName).build();
            ListQueuesResponse queuesRes = sqs.listQueues(queuesReq);

            if (queuesRes.queueUrls().size() != 0) {
                break;
            }
        }

        // Retrieve the appropriate queueUrl
        String queueUrl = "";
        try {
            GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder().queueName(queueName).build();

            queueUrl = sqs.getQueueUrl(getQueueRequest).queueUrl();
        } catch (QueueNameExistsException e) {
            throw e;
        }

        // Process images from the queue until "-1" message is received
        try {
            boolean endOfQueue = false;
            while (!endOfQueue) {
                // Retrieve next message
                ReceiveMessageRequest receiveMessageRequest = ReceiveMessageRequest.builder().queueUrl(queueUrl)
                        .maxNumberOfMessages(1).build();
                List<Message> messages = sqs.receiveMessage(receiveMessageRequest).messages();
                if (messages.size() > 0) {
                    Message message = messages.get(0);
                    String label = message.body();

                    if (label.equals("-1")) {
                        System.out.println("Found -1");
                        endOfQueue = true;
                    } else {
                        Image img = Image.builder().s3Object(S3Object.builder().bucket(bucketName).name(label).build())
                                .build();

                        DetectTextRequest request = DetectTextRequest.builder().image(img).build();

                        DetectTextResponse result = rek.detectText(request);
                        List<TextDetection> textDetections = result.textDetections();
                        if (textDetections.size() == 0) {
                            System.out.println(label + " has no text");
                        } else {
                            System.out.print(label + ":");
                            for (TextDetection textDetection : textDetections) {
                                if (textDetection.type().equals(TextTypes.WORD)) {
                                    System.out.print(" " + textDetection.detectedText());
                                }
                            }
                            System.out.println();
                        }
                    }

                    // Delete the message in the queue now that it's been handled
                    DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder().queueUrl(queueUrl)
                            .receiptHandle(message.receiptHandle()).build();
                    sqs.deleteMessage(deleteMessageRequest);
                }
            }
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            System.exit(1);
        }
    }
}