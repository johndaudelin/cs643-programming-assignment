// Author: John Daudelin
// CS643-851 Programming Assignment 1
// This code is intended to run on EC2 A
// I used example code from this repository: https://github.com/awsdocs/aws-doc-sdk-examples/blob/master/javav2/example_code/

package com.cs643;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.Label;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.ListQueuesRequest;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;
import software.amazon.awssdk.services.sqs.model.QueueNameExistsException;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.util.List;
import java.util.Map;

public class DetectCars {

    public static void main(String[] args) {

        String bucketName = "njit-cs-643";
        String queueName = "cars.fifo"; // Fifo queue so that "-1" is always processed last
        String queueGroup = "group1";

        Region region = Region.US_EAST_1;
        S3Client s3 = S3Client.builder().region(region).build();
        RekognitionClient rek = RekognitionClient.builder().region(region).build();
        SqsClient sqs = SqsClient.builder().region(region).build();

        processBucketImages(s3, rek, sqs, bucketName, queueName, queueGroup);
    }

    public static void processBucketImages(S3Client s3, RekognitionClient rek, SqsClient sqs, String bucketName,
            String queueName, String queueGroup) {

        // Create queue or retrieve the queueUrl if it already exists.
        String queueUrl = "";
        try {
            ListQueuesRequest queuesReq = ListQueuesRequest.builder().queueNamePrefix(queueName).build();
            ListQueuesResponse queuesRes = sqs.listQueues(queuesReq);

            if (queuesRes.queueUrls().size() == 0) {
                CreateQueueRequest request = CreateQueueRequest.builder()
                        .attributesWithStrings(Map.of("FifoQueue", "true", "ContentBasedDeduplication", "true"))
                        .queueName(queueName).build();
                sqs.createQueue(request);

                GetQueueUrlRequest getQueueRequest = GetQueueUrlRequest.builder().queueName(queueName).build();
                queueUrl = sqs.getQueueUrl(getQueueRequest).queueUrl();
            } else {
                queueUrl = queuesRes.queueUrls().get(0);
            }
        } catch (QueueNameExistsException e) {
            throw e;
        }

        // Process the 10 images in the S3 bucket
        try {
            ListObjectsV2Request listObjectsReqManual = ListObjectsV2Request.builder().bucket(bucketName).maxKeys(10)
                    .build();
            ListObjectsV2Response listObjResponse = s3.listObjectsV2(listObjectsReqManual);

            for (S3Object obj : listObjResponse.contents()) {
                System.out.println("Processing " + obj.key());

                Image img = Image.builder().s3Object(software.amazon.awssdk.services.rekognition.model.S3Object
                        .builder().bucket(bucketName).name(obj.key()).build()).build();
                DetectLabelsRequest request = DetectLabelsRequest.builder().image(img).minConfidence((float) 90)
                        .build();
                DetectLabelsResponse result = rek.detectLabels(request);
                List<Label> labels = result.labels();

                for (Label label : labels) {
                    if (label.name().equals("Car")) {
                        sqs.sendMessage(SendMessageRequest.builder().messageGroupId(queueGroup).queueUrl(queueUrl)
                                .messageBody(obj.key()).build());
                        break;
                    }
                }
            }

            // Signal the end of image processing by sending "-1" to the queue
            sqs.sendMessage(SendMessageRequest.builder().queueUrl(queueUrl).messageGroupId(queueGroup).messageBody("-1")
                    .build());
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            System.exit(1);
        }
    }
}