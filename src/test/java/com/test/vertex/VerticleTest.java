package com.test.vertex;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;


@RunWith(VertxUnitRunner.class)
public class VerticleTest
{

	private Vertx vertx;

	@Before
	public void setUp(TestContext context)
	{
		vertx = Vertx.vertx();
		vertx.deployVerticle(TestingHttpServer.class.getName(), context.asyncAssertSuccess());
	}

	public void tearDown(TestContext context)
	{
		vertx.close(context.asyncAssertSuccess());
	}

	@Test
	public void testMyapp(TestContext context)
	{
		final Async async = context.async();
		vertx
		    .createHttpClient()
		    .getNow(8080, "localhost", "/", response -> {
			    response.handler(body -> {
			    	System.out.println(body.toString());
				    context.assertTrue(body.toString().contains("earth"));
				    async.complete();
			    });
		    });
	}

}
