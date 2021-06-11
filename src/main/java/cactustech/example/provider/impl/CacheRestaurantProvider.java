package cactustech.example.provider.impl;

import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;

import cactustech.example.objects.Restaurant;
import cactustech.example.provider.IRestaurantProvider;
import redis.clients.jedis.Jedis;

public class CacheRestaurantProvider implements IRestaurantProvider {

	private static Logger logger = LogManager.getLogger(CacheRestaurantProvider.class);

	// keys
	private final String KEY_DELIMITER = ".";
	private final String PREFIX = "restaurant";

	// db clients 
	private IRestaurantProvider innerProvider;
	private Jedis redisClient;

	private Gson gson;

	public CacheRestaurantProvider(IRestaurantProvider innerProvider, String cacheConnectionString) {
		this.innerProvider = innerProvider;
		redisClient = new Jedis(cacheConnectionString);
		gson = new Gson();
	}

	private String getKey(String subname, String token) {
		return PREFIX + KEY_DELIMITER + subname + KEY_DELIMITER + StringUtils.replaceChars(token, ' ', '_');  
	}

	@Override
	public Set<Restaurant> getRestaurantsByName(String name) {
		String key = getKey("name", name);
		if(redisClient.exists(key)) {
			return getDataFromCache(key);
		}
		Set<Restaurant> dbResults = innerProvider.getRestaurantsByName(name);
		saveDataToCache(key, dbResults);
		return null;
	}

	@Override
	public Set<Restaurant> getRestaurantsByLocation(String location) {
		String key = getKey("location", location);
		if(redisClient.exists(key)) {
			return getDataFromCache(key);
		}
		// results not exists in cache retrieving to mongo
		logger.debug("cache miss key: " + key);
		Set<Restaurant> dbResults = innerProvider.getRestaurantsByLocation(location); 
		saveDataToCache(key, dbResults);
		return dbResults;
	}

	private void saveDataToCache(String key, Set<Restaurant> dbResults) {
		// converting the object to string for serialization into redis
		String[] saveToCache = dbResults.stream().
				map(rest -> gson.toJson(rest)).toArray(String[]::new);
		logger.debug("saving cache key: " + key);
		redisClient.sadd(key, saveToCache);
	}
	
	private Set<Restaurant> getDataFromCache(String key) {
		logger.debug("cache found key: " + key);
		Set<String> cacheResult = redisClient.smembers(key);
		
		// converting json string to object
		Set<Restaurant> results = cacheResult.stream().
				map(rest -> gson.fromJson(rest, Restaurant.class)).collect(Collectors.toSet());
		return results;
	}

}
