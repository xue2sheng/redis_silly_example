import java.sql.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

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
	private static final int INTERATIONS = 1000;
	private static final int CDR_SIZE = 1024;
	private static JedisPool pool;
	public static boolean isLocalhost()
	{
             String uri = System.getenv("REDISCLOUD_URI");
	     if( uri == null ) return true;
	     
	     if( uri.equals("localhost") ) return true;
	     else return false;
	}
	public static int interations()
	{
             String iterations = System.getenv("INTERATIONS");
	     if( iterations == null ) return INTERATIONS;
	     else return Integer.parseInt(iterations);
	}	
	public static int cdrSize()
	{
             String cdr_size = System.getenv("CDR_SIZE");
	     if( cdr_size == null ) return CDR_SIZE;
	     else return Integer.parseInt(cdr_size);
	}
	public static String fakeCDR()
	{
	     byte[] fake = new byte[cdrSize()]; 
	     for(int i=0; i<cdrSize(); ++i )  
	     {
                 fake[i]=1;
	     }
	     return Arrays.toString(fake);
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

	public static String setCDRs()
	{
	   Jedis jedis = pool.getResource();
	   long lStartTime = System.currentTimeMillis();
	   String fakeCDR = fakeCDR();
	   for(int i=0; i<interations(); ++i)
	   {
	     jedis.set("key" + Integer.toString(i), fakeCDR);
	   }
	   long lEndTime = System.currentTimeMillis();
           long difference = lEndTime - lStartTime;
	   pool.returnResource(jedis);
	   return "Elapsed " + Long.toString(difference) + " milliseconds for SETTING" 
		   + Integer.toString(interations()) + " fake CDRs of "
		   + Integer.toString(cdrSize()) + " bytes";
	}
	public static String getCDRs()
	{
	   Jedis jedis = pool.getResource();
	   long lStartTime = System.currentTimeMillis();
	   String fakeCDR = fakeCDR();
	   for(int i=0; i<interations(); ++i)
	   {
	     jedis.get("key" + Integer.toString(i));
	   }
	   long lEndTime = System.currentTimeMillis();
           long difference = lEndTime - lStartTime;
	   pool.returnResource(jedis);
	   return "Elapsed " + Long.toString(difference) + " milliseconds for GETTING" 
		   + Integer.toString(interations()) + " fake CDRs of "
		   + Integer.toString(cdrSize()) + " bytes";
	}
	
      }
      

  public static void main(String[] args) {

    port(Integer.valueOf(System.getenv("PORT")));
    staticFileLocation("/public");

    get("/", (req, res) -> {
       try { 
	Redis.createPool();
        return "{" + Redis.getUri() + "}<br />" + Redis.setCDRs() + "<br />" + Redis.getCDRs(); 
       } catch(Exception e) {
	 return "Exception: " + e.getMessage();
       }
    });

   }


}
