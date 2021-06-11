package cactustech.example.provider;

import java.util.Set;

import cactustech.example.objects.Restaurant;

public interface IRestaurantProvider {
	
	public Set<Restaurant> getRestaurantsByName(String name);
	
	public Set<Restaurant> getRestaurantsByLocation(String location);

}
