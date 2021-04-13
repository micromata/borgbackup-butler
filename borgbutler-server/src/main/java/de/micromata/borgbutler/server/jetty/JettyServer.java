package de.micromata.borgbutler.server.jetty;

import de.micromata.borgbutler.server.ServerConfiguration;
import de.micromata.borgbutler.server.RunningMode;
import de.micromata.borgbutler.server.rest.ConfigurationRest;
import de.micromata.borgbutler.server.user.UserFilter;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.*;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.eclipse.jetty.util.resource.Resource;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;

public class JettyServer {
    private static Logger log = LoggerFactory.getLogger(JettyServer.class);
    private static String BIND_ADDRESS = "127.0.0.1";
    private static final int MAX_PORT_NUMBER = 65535;
    private Server server;
    private int port;

    static {
        String bindAddress = System.getProperty("bindAddress");
        if (bindAddress != null) {
            BIND_ADDRESS = bindAddress;
        }
        log.info("Binding server to address: " + BIND_ADDRESS);
    }

    public static String getBindAddress() {
        return BIND_ADDRESS;
    }

    public void start(String... restPackageNames) {
        port = findFreePort();
        if (port == -1) {
            return;
        }
        log.info("Starting web server on port " + port);
        server = new Server();

        ServerConnector connector = new ServerConnector(server);
        connector.setHost(BIND_ADDRESS);
        connector.setPort(port);
        server.setConnectors(new Connector[]{connector});

        ServletContextHandler ctx =
                new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        ctx.setContextPath("/");

        ResourceConfig resourceConfig = new ResourceConfig();
        String[] packageNames = {ConfigurationRest.class.getPackage().getName()};
        if (restPackageNames != null && restPackageNames.length > 0) {
            packageNames = (String[]) ArrayUtils.addAll(packageNames, restPackageNames);
        }
        resourceConfig.packages(packageNames);
        resourceConfig.register(MultiPartFeature.class)
                .register(JacksonFeature.class);
        //   .register(LoggingFilter.class)
        //   .property("jersey.config.server.tracing.type", "ALL")
        //   .property("jersey.config.server.tracing.threshold", "VERBOSE"))
        ServletHolder jerseyServlet = new ServletHolder(
                new ServletContainer(resourceConfig));
        jerseyServlet.setInitOrder(1);
        ctx.addServlet(jerseyServlet, "/rest/*");
        ctx.addFilter(UserFilter.class, "/rest/*", EnumSet.of(DispatcherType.INCLUDE, DispatcherType.REQUEST));
        // Following code doesn't work:
        // jerseyServlet.setInitParameter("useFileMappedBuffer", "false");
        // jerseyServlet.setInitParameter("cacheControl","max-age=0,public");

        try {
            Path path;
            if (RunningMode.isDevelopmentMode()) {
                path = Paths.get(ServerConfiguration.getApplicationHome(), "borgbutler-webapp", "build");
            } else {
                path = Paths.get(ServerConfiguration.getApplicationHome(), "web");
            }
            if (!Files.exists(path)) {
                log.error("********** Fatal: Can't find web archive: " + path.toAbsolutePath());
            }
            URL url = path.toUri().toURL();
            log.debug("Using web directory: " + url);
            ctx.setBaseResource(Resource.newResource(url));
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
            return;
        }
        ctx.setWelcomeFiles(new String[]{"index.html"});
        ctx.setInitParameter(DefaultServlet.CONTEXT_INIT + "cacheControl", "no-store,no-cache,must-revalidate");//"max-age=5,public");
        ctx.setInitParameter(DefaultServlet.CONTEXT_INIT + "useFileMappedBuffer", "false");
        ctx.addServlet(DefaultServlet.class, "/");

        ErrorPageErrorHandler errorHandler = new ErrorPageErrorHandler();
        errorHandler.addErrorPage(404, "/");
        ctx.setErrorHandler(errorHandler);

        if (RunningMode.isDevelopmentMode() || ServerConfiguration.get().isWebDevelopmentMode()) {
            log.warn("*********************************");
            log.warn("***********            **********");
            log.warn("*********** ATTENTION! **********");
            log.warn("***********            **********");
            log.warn("*********** Running in **********");
            log.warn("*********** dev mode!  **********");
            log.warn("***********            **********");
            log.warn("*********************************");
            log.warn("Don't deliver this app in dev mode due to security reasons (CrossOriginFilter is set)!");

            FilterHolder filterHolder = ctx.addFilter(CrossOriginFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
            filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
            filterHolder.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
            filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,POST,HEAD");
            filterHolder.setInitParameter(CrossOriginFilter.ALLOWED_HEADERS_PARAM, "X-Requested-With,Content-Type,Accept,Origin");
        }

        server.setHandler(ctx);

        try {
            server.start();
        } catch (Exception ex) {
            log.error("Can't start jetty: " + ex.getMessage(), ex);
        }
    }

    public void stop() {
        log.info("Stopping web server.");
        try {
            server.stop();
        } catch (Exception ex) {
            log.error("Can't stop web server: " + ex.getMessage(), ex);
        }
        if (server != null) {
            server.destroy();
        }
    }

    private int findFreePort() {
        int port = ServerConfiguration.get().getPort();
        return findFreePort(port);
    }

    private int findFreePort(int startPort) {
        int port = startPort > 0 ? startPort : 1;
        if (port > MAX_PORT_NUMBER) {
            log.warn("Port can't be higher than " + MAX_PORT_NUMBER + ": " + port + ". It's a possible mis-configuration.");
            port = ServerConfiguration.WEBSERVER_PORT_DEFAULT;
        }
        for (int i = port; i < port + 10; i++) {
            try (ServerSocket socket = new ServerSocket()) {
                socket.bind(new InetSocketAddress(BIND_ADDRESS, i));
                return i;
            } catch (Exception ex) {
                log.info("Port " + i + " already in use or not available. Trying next port.");
                continue; // try next port
            }
        }
        if (startPort != ServerConfiguration.WEBSERVER_PORT_DEFAULT) {
            log.info("Trying to fix port due to a possible mis-configuration.");
            return findFreePort(ServerConfiguration.WEBSERVER_PORT_DEFAULT);
        }
        log.error("No free port found! Giving up.");
        return -1;
    }

    public int getPort() {
        return port;
    }

    public String getUrl() {
        return "http://" + BIND_ADDRESS + ":" + port + "/";
    }
}

