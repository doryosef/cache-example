package cactustech.example.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;

public class GenerateData {

	private static final String RESTAURANT_BASE="{\"_id\":\"ID\", \"name\": \"NAME\" ,\"type\":\"TYPE\", \"location\":\"LOCATION\", \"display\": true}";

	private static final List<String> types = Arrays.asList("Burger", "Thai", "Other");
	private static final List<String> locations = Arrays.asList("Beer Sheva", "Tel Aviv", "Dimona", "Haifa", "Ashdod" );

	public static String generateResaurant() {

		Random r = new Random();
		Map<String, String> tokens = new HashMap<String, String>();

		// generate data
		String uuid = UUID.randomUUID().toString();		
		tokens.put("ID", uuid);
		String type = types.get(new Random().nextInt(types.size()));
		tokens.put("TYPE", type);
		String location = locations.get(r.nextInt(locations.size()));
		tokens.put("LOCATION", location);
		String name=RandomStringUtils.randomAlphabetic(r.nextInt(10-3)+3).toLowerCase();
		tokens.put("NAME", name);

		// replace tokens
		String data = RESTAURANT_BASE;
		for(Map.Entry<String,String> token : tokens.entrySet()) {
			data = data.replaceFirst(token.getKey(), token.getValue());	
		}
		return data;
	}

	public static void main(String[] args) throws IOException {
		int dataSize=100000;
		String fileName = "src/test/resources/restaurant_dummy_data.json";
		File dataFile = new File(fileName);
		dataFile.createNewFile();
		try (FileOutputStream fos = new FileOutputStream(dataFile, true)) {
			for(int i=0; i<dataSize; i++ ) {
				byte[] mybytes = (generateResaurant()+System.lineSeparator()).getBytes();
				fos.write(mybytes);
			}
		}
		System.out.println("done generating data");
	}
}
