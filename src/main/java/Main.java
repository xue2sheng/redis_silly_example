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

     // can throw
     private static class Redis {
	private static JedisPool pool;
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
	public static void createPool() throws URISyntaxException
	{
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
	}

	public static String getFoo()
	{
	 Jedis jedis = pool.getResource();
         String value = jedis.get("foo");
         pool.returnResource(jedis); 
         return value;
	}
	
	public static String setFoo(String value)
	{
	 Jedis jedis = pool.getResource();
         String result = jedis.set("foo", value);
         pool.returnResource(jedis); 
         return result;
	}
      }
      

  public static void main(String[] args) {


    port(Integer.valueOf(System.getenv("PORT")));
    staticFileLocation("/public");

    get("/redis", (req, res) -> {
       try { 
	Redis.createPool();
        return "{" + Redis.getUri() + "} value = " + Redis.getFoo(); 
       } catch(Exception e) {
	 return "Exception: " + e.getMessage();
       }
    });

    put("/redis", (req, res) -> {
       try { 
	String value = req.queryParams("foo");
	if( value != null )
	{
	  Redis.createPool();
          return "[" + Redis.getUri() + "] set 'foo' => " + Redis.setFoo(value);
	} else {
          return "[" + Redis.getUri() + "] 'foo' param not found" ; 
	}

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
