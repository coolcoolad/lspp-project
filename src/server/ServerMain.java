package server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * Created by yangjie on 2017/1/8.
 */
public class ServerMain extends Thread {
    @Override
    public void run() {
        main(new String[]{});
    }

    public static void main(String[] args) {
        try{
            Server server = new Server(8080);

            ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
            servletContextHandler.addServlet(GetNearbyPoiServlet.class, "/getNearbyPoi");

//            ResourceHandler resource_handler = new ResourceHandler();
//            resource_handler.setResourceBase("./web");

            server.setHandler(servletContextHandler);

            server.start();
            server.join();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}
