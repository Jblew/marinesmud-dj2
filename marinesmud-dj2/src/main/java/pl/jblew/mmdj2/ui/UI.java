/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.mmdj2.ui;

import java.util.HashMap;
import java.util.Map;
import pl.jblew.marinesmud.framework.services.Service;
import pl.jblew.marinesmud.framework.services.ServiceManager;
import pl.jblew.marinesmud.framework.services.ServiceSingletonProvider;
import pl.jblew.marinesmud.framework.webserver.remotecomponents.LogComponent;
import pl.jblew.marinesmud.framework.webserver.remotecomponents.RemoteComponent;
import pl.jblew.marinesmud.framework.webserver.remotecomponents.RemoteComponents;
import pl.jblew.mmdj2.AppContext;

/**
 *
 * @author teofil
 */
public class UI implements Service {

    private final Map<String, RemoteComponent> componentsToInject = new HashMap<>();

    public UI(AppContext context) {
        RemoteComponents rc = context.getServiceManager().getService(RemoteComponents.class);
        componentsToInject.put("component-log-top", new LogComponent(rc));
    }

    public String injectComponents(String template) {
        synchronized (componentsToInject) {
            for (String key : componentsToInject.keySet()) {
                template = template.replace("{{" + key + "}}", componentsToInject.get(key).getHtml());
            }
        }
        return template;
    }
    
    public static class Provider extends ServiceSingletonProvider<UI> {     
        @Override
        protected UI provideService(ServiceManager sm) {
            return new UI(sm.getService(AppContext.class));
        }

        @Override
        public Class<? extends UI> getServiceClass() {
            return UI.class;
        }
    }
}
