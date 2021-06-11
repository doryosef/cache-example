package cactustech.example.provider.impl;




import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import cactustech.example.objects.Restaurant;
import cactustech.example.provider.IRestaurantProvider;

public class MongoRestaurantProvider implements IRestaurantProvider {

	private static Logger logger = LogManager.getLogger(MongoRestaurantProvider.class);
	
	public static final String COLLECTION_RESTAURANT_NAME = "restaurants";
	public static final String DB_NAME = "dummy";

	private MongoCollection<Restaurant> restColl;
	MongoClient mongoClient;

	public MongoRestaurantProvider(String connectionString) {
		
		CodecRegistry pojoCodecRegistry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
		
		MongoClient mongoClient = MongoClients.create(connectionString);
		MongoDatabase database = mongoClient.getDatabase(DB_NAME);
		restColl = database.getCollection(COLLECTION_RESTAURANT_NAME, Restaurant.class).withCodecRegistry(pojoCodecRegistry);
	}

	@Override
	public Set<Restaurant> getRestaurantsByName(String name) {
		logger.debug("mongo searching for restaurant by name: {}", name);
		Set<Restaurant> result = new HashSet<Restaurant>();
		try (MongoCursor<Restaurant> cursor = restColl.find(eq("name", name)).iterator()) {
			while (cursor.hasNext()) {
				result.add(cursor.next());
			}
			cursor.close();
		}
		return result;
	}

	@Override
	public Set<Restaurant> getRestaurantsByLocation(String location) {
		logger.debug("mongo searching for restaurant by location: {}", location);
		Set<Restaurant> result = new HashSet<Restaurant>();
		try (MongoCursor<Restaurant> cursor = restColl.find(eq("location", location)).iterator()) {
			while (cursor.hasNext()) {
				result.add(cursor.next());
			}
			cursor.close();
		}
		return result;
	}
}
