import java.sql.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

import java.net.URI;
import java.net.URISyntaxException;

import static spark.Spark.*;
import spark.template.freemarker.FreeMarkerEngine;
import spark.ModelAndView;
import static spark.Spark.get;

import static javax.measure.unit.SI.KILOGRAM;
import javax.measure.quantity.Mass;
import org.jscience.physics.model.RelativisticModel;
import org.jscience.physics.amount.Amount;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import spark.Request;
import spark.Response;


public class Main {
	private static Object pool;
	
     private static class Redis {
	public static boolean isLocalhost()
	{
             String uri = System.getenv("REDISCLOUD_URI");
	     if( uri == null ) return true;
	     
	     if( uri.equals("localhost") ) return true;
	     else return false;
	}
        public static String getUri()	
	{
	   return isLocalhost() ? "localhost" : System.getenv("REDISCLOUD_URI");
	}
      }
      

  public static void main(String[] args) {


    port(Integer.valueOf(System.getenv("PORT")));
    staticFileLocation("/public");

    get("/redis", (req, res) -> {

     try { 
         JedisPool pool;
         if( Redis.isLocalhost() )
         {
           pool = new JedisPool(new JedisPoolConfig(), Redis.getUri());
         } else {
            URI redisUri = new URI(Redis.getUri());
            pool = new JedisPool(new JedisPoolConfig(),
	                 redisUri.getHost(),
		         redisUri.getPort(),
		         2000,
		         redisUri.getUserInfo().split(":",2)[1]
	                 );
	 }
     
	 Jedis jedis = pool.getResource();
         //jedis.set("foo", "bar");
         String value = jedis.get("foo");
         //return the instance to the pool when you're done
         pool.returnResource(jedis); 
         return "{" + Redis.getUri() + "} value = " + value; 

       } catch(Exception e) {
	 return "Exception: " + e.getMessage();
       }
    
    });



    get("/hello", (req, res) -> {
	    RelativisticModel.select();
	    String energy = System.getenv().get("ENERGY");
	    Amount<Mass> m = Amount.valueOf(energy).to(KILOGRAM);
	    return "E=mc^2: " + energy + " = " + m.toString();
    });

    get("/", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("message", "Hello World!");

            return new ModelAndView(attributes, "index.ftl");
        }, new FreeMarkerEngine());


   }


}
