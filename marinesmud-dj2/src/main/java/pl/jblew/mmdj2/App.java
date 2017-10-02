package pl.jblew.mmdj2;

import com.google.common.eventbus.AsyncEventBus;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.jblew.marinesmud.framework.crypto.CredentialsManager;
import pl.jblew.marinesmud.framework.crypto.PasswordStorage;
import pl.jblew.marinesmud.framework.json.JSONObjectLoader;
import pl.jblew.marinesmud.framework.services.ServiceInjector;
import pl.jblew.marinesmud.framework.services.ServiceManager;
import pl.jblew.marinesmud.framework.util.SingleLineFormatter;
import pl.jblew.marinesmud.framework.webserver.RoutingHttpResponder;
import pl.jblew.marinesmud.framework.webserver.modules.WebModule;
import pl.jblew.marinesmud.framework.webserver.WebServer;
import pl.jblew.marinesmud.framework.webserver.remotecomponents.RemoteComponents;
import pl.jblew.marinesmud.framework.webserver.util.ClasspathStaticFileLoader;
import pl.jblew.marinesmud.framework.webserver.websockets.CommonEventBusWSPoster;
import pl.jblew.marinesmud.framework.webserver.websockets.JSONFrameHandler;
import pl.jblew.marinesmud.framework.webserver.websockets.WebSocketFrameHandler;
import pl.jblew.mmdj2.ui.UI;
import pl.jblew.mmdj2.web.modules.AppJSModule;
import pl.jblew.mmdj2.web.modules.IndexModule;
import pl.jblew.mmdj2.web.webroot.Webroot;
/**
 * Hello world!
 *
 */
public class App {

    public App() {

    }

    public void start() throws IOException, PasswordStorage.CannotPerformOperationException, InterruptedException {
        init1_setLoggingFormatters();
        

        //LOAD CONFIG
        JSONObjectLoader<Config> configLoader = new JSONObjectLoader(Config.class, new File(StaticConfig.CONFIG_PATH));
        Config config = configLoader.loadObject(new Config());
        //configLoader.save(config);

        //CREATE CONTEXT
        ServiceManager serviceManager = new ServiceManager(config.services);
        AppContext context = new AppContext(config.scene, serviceManager);
        serviceManager.addProvider(new ServiceInjector(context, AppContext.class));
        serviceManager.addProvider(new UI.Provider());
        
        
        RemoteComponents rc = serviceManager.getService(RemoteComponents.class);
        WebSocketFrameHandler wsHandler = rc.initialize(new AsyncEventBus(context.getCommonExecutor()));

        //LOAD WEB SERVER & WEBSOCKETS
        CredentialsManager<User> credentialsManager;
        if (config.credentials.isEmpty()) {
            List<CredentialsManager.UserEntry<User>> users = new LinkedList<>();
            users.add(new CredentialsManager.UserEntry<User>(config.defaultUser, config.defaultPassword, new User(config.defaultUser)));
            credentialsManager = new CredentialsManager<>(users);
            context.executeInContextThread(() -> {
                context.getState().defaultPassword = true;
                context.getState().severeWarnings.add("Default password is enabled!");
                Logger.getLogger(App.class.getName()).severe("[SECURITY WARNING] Default password is enabled!. To disable it – please add user to config file.");
            });
        } else {
            credentialsManager = new CredentialsManager<>(config.credentials);
        }

        Logger.getLogger(App.class.getName()).info("Starting web server...");
        Map<String, WebModule> webModules = new HashMap<>();
        webModules.put("index", new IndexModule(credentialsManager, context));
        webModules.put("app.js", new AppJSModule(credentialsManager));

        
        RoutingHttpResponder router = new RoutingHttpResponder(webModules);
        WebServer webServer = new WebServer(config.webServerConfig, router, new ClasspathStaticFileLoader(Webroot.class), wsHandler);

        try {
            //START SCENE
            context.executeInContextThreadAndWait(() -> {
                try {
                    Logger.getLogger(App.class.getName()).info("Starting scene...");
                    config.scene.start(context);
                    Logger.getLogger(App.class.getName()).info("House started");
                } catch (Exception ex) {
                    Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
        } catch (InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

        //SCHEDULE CONFIG SAVING
        /*context.scheduleInContext(() -> {
            try {
                configLoader.save(config);
            } catch (IOException ex) {
                Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            }
        }, 60, TimeUnit.SECONDS);*/

        //ADD SHUTDOWN HOOKS
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                context.stop();
                webServer.stop();
            }
        });

        //LAUNCH WEB SERVER
        Logger.getLogger("io.netty").setLevel(Level.OFF);
        webServer.start();

        AtomicBoolean ready = new AtomicBoolean(false);
        context.executeInContextThreadAndWait(() -> {
            ready.set(context.getScene().isSceneReady());
        });
        if (!ready.get()) {
            Logger.getLogger(App.class.getName()).severe("House is not ready! Exitting.");
            System.exit(1);
        }
    }

    public static void main(String[] args) throws Exception {
        new App().start();
    }

    private void init1_setLoggingFormatters() {
        for (Handler h : Logger.getLogger("").getHandlers()) {
            h.setFormatter(new SingleLineFormatter(false));
        }
    }
}
