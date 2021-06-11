package cactustech.example;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import cactustech.example.provider.IRestaurantProvider;
import cactustech.example.provider.impl.CacheRestaurantProvider;
import cactustech.example.provider.impl.MongoRestaurantProvider;
import de.flapdoodle.embed.mongo.MongoImportExecutable;
import de.flapdoodle.embed.mongo.MongoImportProcess;
import de.flapdoodle.embed.mongo.MongoImportStarter;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongoImportConfig;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongoImportConfigBuilder;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import redis.embedded.RedisServer;

public class AppTest 
{
	private static Logger logger = LogManager.getLogger(AppTest.class);
	
	private static MongodExecutable mongodExecutable;
	private static MongodProcess mongod;
	private static RedisServer redisServer;

	private static void setupMongo() throws UnknownHostException, IOException {
		MongodStarter starter = MongodStarter.getDefaultInstance();
		String bindIp = "localhost";
		int port = 27017;
		IMongodConfig mongodConfig = new MongodConfigBuilder()
				.version(Version.Main.PRODUCTION)
				.net(new Net(bindIp, port, Network.localhostIsIPv6()))
				.build();
		mongodExecutable = null;
		MongoImportProcess mongoImport = null;

		try {
			mongodExecutable = starter.prepare(mongodConfig);
			mongod = mongodExecutable.start();

			/* if import not working for you try to download 
			 * https://indy.fulgan.com/SSL/openssl-1.0.2s-x64_86-win64.zip
			 * and copy the files to windows\system32 
			 * check issue
			 * https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo/issues/167
			 */

			// import data
			String dataFilePath =  new File(AppTest.class.getClassLoader().getResource("restaurant_dummy_data.json").getFile()).getPath();
			IMongoImportConfig mongoImportConfig = new MongoImportConfigBuilder()
					.version(Version.Main.PRODUCTION)
					.net(new Net(bindIp, port, Network.localhostIsIPv6()))
					.db("dummy")
					.collection("restaurants")
					.jsonArray(false)
					.upsert(true)
					.dropCollection(true)
					.importFile(dataFilePath)
					.build();

			MongoImportExecutable mongoImportExecutable = MongoImportStarter.getDefaultInstance().prepare(mongoImportConfig);
			mongoImport = mongoImportExecutable.start();
		}catch(Exception e ){
			System.out.println(e.getMessage());

		} finally {
			if(mongoImport != null) {
				mongoImport.stop();
			}
		}
	}

	private static void setupRedis() throws IOException {
		redisServer = new RedisServer();
		redisServer.start();
	}

	@BeforeAll
	public static void setup() throws UnknownHostException, IOException {
		setupMongo();
		setupRedis();
	}

	@Test
	public void shouldCacheQueryTimeBeSmaller()
	{
		String mongoConnString = "mongodb://localhost:27017";
		String cacheConnString = "localhost";
		IRestaurantProvider restProvider = new MongoRestaurantProvider(mongoConnString);
		IRestaurantProvider cacheProvider = new CacheRestaurantProvider(restProvider, cacheConnString);
		
		long start = System.currentTimeMillis();
		cacheProvider.getRestaurantsByLocation("Beer Sheva");
		long elapsedTime = System.currentTimeMillis() - start;
		logger.info("time taking data from mongo: {}", elapsedTime);
		
		long startCache = System.currentTimeMillis();
		cacheProvider.getRestaurantsByLocation("Beer Sheva");
		long elapsedTimeCache = System.currentTimeMillis() - startCache;
		logger.info("time taking data from cache: {}", elapsedTimeCache);
		assertTrue( elapsedTimeCache < elapsedTime );
	}

	@AfterAll
	public static void teardown() {
		if(redisServer != null) {
			System.out.println("stopping redis server");
			redisServer.stop();	
		}
		if(mongod != null ) {
			System.out.println("stopping monngod");
			mongod.stop();
			mongodExecutable.stop();
		}
	}
}
