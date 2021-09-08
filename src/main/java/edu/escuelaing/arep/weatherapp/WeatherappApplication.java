package edu.escuelaing.arep.weatherapp;

public class WeatherappApplication {

	public static void main(String[] args) {
    }

	static int getPort() {
        if (System.getenv("PORT") != null) {
            return Integer.parseInt(System.getenv("PORT"));
        }
        return 35000; //returns default port if heroku-port isn't set (i.e. on localhost)
    }

}
