package com.test.vertex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.templ.ThymeleafTemplateEngine;


public class TestingHttpServer
    extends AbstractVerticle
{
	
	private static final Logger logger = LoggerFactory.getLogger(TestingHttpServer.class);
	// https://github.com/vert-x3/vertx-examples/blob/master/web-examples/src/main/java/io/vertx/example/web/templating/thymeleaf/templates/index.html
	// https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html#using-and-displaying-variables
	private ThymeleafTemplateEngine templateEngine = ThymeleafTemplateEngine.create();
	
	public TestingHttpServer()
	{
		templateEngine = ThymeleafTemplateEngine.create();
		configureThymeleafEngine(templateEngine);
	}
	@Override
	public void start(Future<Void> future)
	    throws Exception
	{
		SockJSHandler sockJSHandler = SockJSHandler.create(vertx);
		BridgeOptions bridgeOptions = new BridgeOptions()
				.addInboundPermitted(new PermittedOptions().setAddress("app.markdown"))
				.addOutboundPermitted(new PermittedOptions().setAddress("page.saved"));
		sockJSHandler.bridge(bridgeOptions);
		
		Router router = Router.router(vertx);
		router.route("/eventbus/*").handler(sockJSHandler);
		
		router.route().handler(CookieHandler.create());
		router.route().handler(BodyHandler.create());
		router.route().handler(routingContext -> {
			logger.debug("incoming request received");
			final HttpServerResponse response = routingContext.response();
			routingContext.put("message", "what on earth is a test");
			templateEngine.render(routingContext, "templates/", "index", ar -> {
				if (ar.succeeded())
				{
					response.putHeader("content-type", "text/html");
					response.end(ar.result());
				}
				else
				{
					routingContext.fail(ar.cause());
				}
			});

		});
		
		vertx.eventBus().consumer("app.markdown", msg -> {
//			String html = Processor.process(msg.body());
//			msg.reply(html);
		});

		vertx
		    .createHttpServer()
		    .requestHandler(router::accept)
		    .listen(8080, result -> {
			    if (result.succeeded())
			    {
				    future.complete();
			    }
			    else
			    {
				    future.fail(result.cause());
			    }
		    });
	}

	private void configureThymeleafEngine(ThymeleafTemplateEngine engine)
	{
		ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
		templateResolver.setPrefix("/");
		templateResolver.setSuffix(".html");
		engine.getThymeleafTemplateEngine().setTemplateResolver(templateResolver);

//		customMessageResolver customMessageResolver = new CustomMessageResolver();
//		engine.getThymeleafTemplateEngine().setMessageResolver(customMessageResolver);
	}
}
