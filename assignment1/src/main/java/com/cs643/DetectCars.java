package com.cs643;

// Much of this code taken from the example code at https://github.com/awsdocs/aws-doc-sdk-examples/blob/master/javav2/example_code/s3/src/main/java/com/example/s3/S3ObjectOperations.java

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DetectCars {

    public static void main(String[] args) {

        String bucketName = "njit-cs-643";
        String keyName = "1.jpg";
        String outputPath = "output.txt";

        Region region = Region.US_EAST_1;
        S3Client s3 = S3Client.builder()
                .region(region)
                .build();

        getObjectBytes(s3,bucketName,keyName, outputPath);
    }

    public static void getObjectBytes (S3Client s3, String bucketName, String keyName, String outputPath ) {

        try {
            // create a GetObjectRequest instance
            GetObjectRequest objectRequest = GetObjectRequest
                    .builder()
                    .key(keyName)
                    .bucket(bucketName)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(objectRequest);
            byte[] data = objectBytes.asByteArray();

            //Write the data to a local file
            File myFile = new File(outputPath);
            OutputStream os = new FileOutputStream(myFile);
            os.write(data);
            System.out.println("Successfully obtained bytes from an S3 object");

            // Close the file
            os.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception e) {
          System.err.println(e.getLocalizedMessage());
           System.exit(1);
        }
    }
}