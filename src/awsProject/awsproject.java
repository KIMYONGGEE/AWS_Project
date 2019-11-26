package awsProject;
import java.util.Scanner;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Image;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.DryRunResult;
import com.amazonaws.services.ec2.model.DryRunSupportedRequest;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeRegionsResult;
import com.amazonaws.services.ec2.model.Region;
import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.CreateImageRequest;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.CreateTagsResult;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.RebootInstancesResult;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.GetConsoleScreenshotRequest;
import com.amazonaws.services.ec2.model.GetConsoleScreenshotResult;


public class awsproject {

	static AmazonEC2 ec2;

	private static void init() throws Exception {

		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
		try {
			credentialsProvider.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
					+ "Please make sure that your credentials file is at the correct "
					+ "location (~/.aws/credentials), and is in valid format.", e);

		}
		ec2 = AmazonEC2ClientBuilder.standard().withCredentials(credentialsProvider).withRegion("us-east-1") /*
																												 * check
																												 * the
																												 * region
																												 * at
																												 * AWS
																												 * console
																												 */
				.build();
	}

	public static void main(String[] args) throws Exception {
		init();
		String in_id;
		String in_name;
		Scanner menu = new Scanner(System.in);
		Scanner id_string = new Scanner(System.in);
		int number = 0;
		while (true) {
			System.out.println(" ");
			System.out.println(" ");
			System.out.println("------------------------------------------------------------");
			System.out.println(" Amazon AWS Control Panel using SDK ");
			System.out.println(" ");
			System.out.println(" Cloud Computing, Computer Science Department ");
			System.out.println(" at Chungbuk National University ");
			System.out.println("------------------------------------------------------------");
			System.out.println(" 1. list instance   2. available zones ");
			System.out.println(" 3. start instance  4. available regions ");
			System.out.println(" 5. stop instance   6. create instance ");
			System.out.println(" 7. reboot instance 8. Terminate Instance ");
			System.out.println(" 9. List Image      10.Create the AMI");
			System.out.println(" 99. quit ");
			System.out.println("------------------------------------------------------------");
			System.out.print("Enter an integer: ");
			number = menu.nextInt();

			switch (number) {
			case 1:
				listInstances();
				break;
			case 2:
				AvailableZone();
				break;
			case 3:
				System.out.println("Enter Instance Id : ");
				in_id = id_string.nextLine();
				startInstance(in_id);
				break;
			case 4:
				AvailableRegin();
				break;
			case 5:
				System.out.println("Enter Instance id  : ");
				in_id = id_string.nextLine();
				stopInstance(in_id);
				break;
			case 6:
				System.out.println("Enter AMI id  : ");
				in_id = id_string.nextLine();
				System.out.println("Enter AMI name  : ");
				in_name = id_string.nextLine();
				CreateInstance(in_id, in_name);
				break;
			case 7:
				System.out.println("Enter Instance id  : ");
				in_id = id_string.nextLine();
				RebootInstance(in_id);
				break;
			case 8:
				System.out.println("Enter Instance id to Terminate : ");
				in_id = id_string.nextLine();
				TerminateInstance(in_id);
				break;
			case 9:
				ImageList();
				break;
			case 10:
				System.out.println("Enter Instance ID to create an AMI:");
				in_id = id_string.nextLine();
				System.out.println("Enter name for AMI:");
				in_name = id_string.nextLine();
				CreateAMI(in_id,in_name);
				break;
			case 99:
				System.out.println("Shutdown The AWS System");
				System.exit(0);
				break;
			}
		}

	}

	public static void listInstances() {
		System.out.println("Listing instances....");
		boolean done = false;

		DescribeInstancesRequest request = new DescribeInstancesRequest();
		
		while (!done) {
			DescribeInstancesResult response = ec2.describeInstances(request);
			for (Reservation reservation : response.getReservations()) {
				for (Instance instance : reservation.getInstances()) {
					System.out.printf(
							"[ID] %s, " + "[AMI] %s, " + "[type] %s, " + "[state] %10s, " + "[monitoring state] %s",
							instance.getInstanceId(), instance.getImageId(), instance.getInstanceType(),
							instance.getState().getName(), instance.getMonitoring().getState());
				}
				System.out.println();
			}
			request.setNextToken(response.getNextToken());

			if (response.getNextToken() == null) {
				done = true;
			}

		}

	}

	public static void startInstance(String instance_id) {
	

		DryRunSupportedRequest<StartInstancesRequest> dry_request = () -> {
			StartInstancesRequest request = new StartInstancesRequest().withInstanceIds(instance_id);

			return request.getDryRunRequest();
		};
		DryRunResult dry_response = ec2.dryRun(dry_request);

		if (!dry_response.isSuccessful()) {
			System.out.printf("Failed dry run to start instance %s", instance_id);

			throw dry_response.getDryRunResponse();
		}

		StartInstancesRequest request = new StartInstancesRequest().withInstanceIds(instance_id);

		ec2.startInstances(request);

		System.out.printf("Successfully started instance %s", instance_id);
	}

	public static void stopInstance(String instance_id) {
		final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

		DryRunSupportedRequest<StopInstancesRequest> dry_request = () -> {
			StopInstancesRequest request = new StopInstancesRequest().withInstanceIds(instance_id);

			return request.getDryRunRequest();
		};

		DryRunResult dry_response = ec2.dryRun(dry_request);

		if (!dry_response.isSuccessful()) {
			System.out.printf("Failed dry run to stop instance %s", instance_id);
			throw dry_response.getDryRunResponse();
		}

		StopInstancesRequest request = new StopInstancesRequest().withInstanceIds(instance_id);

		ec2.stopInstances(request);

		System.out.printf("Successfully stop instance %s", instance_id);
	}

	public static void AvailableZone() {
		

		DescribeAvailabilityZonesResult zones_response = ec2.describeAvailabilityZones();

		for (AvailabilityZone zone : zones_response.getAvailabilityZones()) {
			System.out.printf("Found availability zone %s " + "with status %s " + "in region %s", zone.getZoneName(),
					zone.getState(), zone.getRegionName());
			System.out.printf("\n");
		}

	}

	public static void AvailableRegin() {


		DescribeRegionsResult regions_response = ec2.describeRegions();

		for (Region region : regions_response.getRegions()) {
			System.out.printf("Found region %s " + "with endpoint %s", region.getRegionName(), region.getEndpoint());
			System.out.printf("\n");
		}

	}
	 public static void CreateInstance(String instance_id, String instance_name)
	    {
	        String name =  instance_name; 
	        String ami_id = instance_id;

	        RunInstancesRequest run_request = new RunInstancesRequest()
	            .withImageId(ami_id)
	            .withInstanceType(InstanceType.T2Micro)
	            .withMaxCount(1)
	            .withMinCount(1)
	            .withKeyName("awskey");

	        RunInstancesResult run_response = ec2.runInstances(run_request);

	        String reservation_id = run_response.getReservation().getInstances().get(0).getInstanceId();
	    
	        System.out.printf(
	            "Successfully started EC2 instance %s based on AMI %s",
	            reservation_id, ami_id);
	    }
	 public static void RebootInstance(String in_id)
	    {

	        String instance_id = in_id;

	      

	        RebootInstancesRequest request = new RebootInstancesRequest()
	            .withInstanceIds(instance_id);

	        RebootInstancesResult response = ec2.rebootInstances(request);

	        System.out.printf(
	            "Successfully rebooted instance %s", instance_id);
	    }
	 public static void TerminateInstance(String in_id)
	 {
		 TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest();
			terminateInstancesRequest.withInstanceIds(in_id);
			ec2.terminateInstances(terminateInstancesRequest);	
			System.out.printf(
		            "Successfully Terminated EC2 instance %s",
					in_id);
	 }
	 
	 public static void ImageList() {
		 System.out.println("Listing Images I10nfo....");
				
		   DescribeImagesRequest request = new DescribeImagesRequest();
		   request.withOwners("self");
		   DescribeImagesResult response = ec2.describeImages(request);
		   
			for (Image AMI : response.getImages()) {
				System.out.println("Image Create Date:" + AMI.getCreationDate());
				System.out.println("Image ID:" + AMI.getImageId());
				System.out.println("Image Status:" + AMI.getState());
				System.out.println("Image Type:" + AMI.getImageType() + "\n");
			}

		}
	 
	 public static void CreateAMI(String instance_id, String AMI_name) {
		 
			CreateImageRequest create = new CreateImageRequest();
			create.withInstanceId(instance_id);
			create.withName(AMI_name);
			
			ec2.createImage(create);
			  System.out.printf(
			            "Successfully Create EC2  %s AMI From %s",
			             AMI_name, instance_id);
	 }
	 
	 

}
