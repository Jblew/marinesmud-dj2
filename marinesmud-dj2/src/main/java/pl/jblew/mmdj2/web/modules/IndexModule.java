/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.mmdj2.web.modules;

import io.netty.handler.codec.http.FullHttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import pl.jblew.marinesmud.framework.crypto.CredentialsManager;
import pl.jblew.marinesmud.framework.webserver.HttpsSession;
import pl.jblew.marinesmud.framework.webserver.modules.AbstractAuthenticatedModule;
import pl.jblew.marinesmud.framework.webserver.modules.HttpErrorCodeException;
import pl.jblew.marinesmud.framework.webserver.websockets.CommonEventBusWSPoster;
import pl.jblew.mmdj2.AppContext;
import pl.jblew.mmdj2.User;
import pl.jblew.mmdj2.ui.UI;
import pl.jblew.mmdj2.web.webroot.Webroot;

/**
 *
 * @author teofil
 */
public class IndexModule extends AbstractAuthenticatedModule<User> {
    private final AppContext context;
    
    public IndexModule(CredentialsManager<User> credentials, AppContext context) {
        super(Webroot.class, "default", "login.form", credentials);
        this.context = context;
    }
    
    
    @Override
    public boolean checkAccess(Path subpath, FullHttpRequest req, User user) {
        return (user != null);
    }

    @Override
    public byte [] getResponse(Path subpath, FullHttpRequest req, HttpsSession session, User user) {        
        String out = this.templateLoader.getOrLoadTemplate("app");
        
        if(context.getServiceManager().isLoadable(UI.class)) {
            UI ui = context.getServiceManager().getService(UI.class);
            out = ui.injectComponents(out);
        }
        
        return out.getBytes(StandardCharsets.UTF_8);
    }
}
