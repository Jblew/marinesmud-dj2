/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.mmdj2.web.modules;

import io.netty.handler.codec.http.FullHttpRequest;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import pl.jblew.marinesmud.framework.crypto.CredentialsManager;
import pl.jblew.marinesmud.framework.webserver.HttpsSession;
import pl.jblew.marinesmud.framework.webserver.modules.AbstractAuthenticatedModule;
import pl.jblew.marinesmud.framework.webserver.remotecomponents.RemoteComponentsUtil;
import pl.jblew.marinesmud.framework.webserver.websockets.CommonEventBusWSPoster;
import pl.jblew.mmdj2.User;
import pl.jblew.mmdj2.web.webroot.Webroot;

/**
 *
 * @author teofil
 */
public class AppJSModule  extends AbstractAuthenticatedModule<User> {
    public AppJSModule(CredentialsManager<User> credentials) {
        super(Webroot.class, credentials);
    }
    
    
    @Override
    public boolean checkAccess(Path subpath, FullHttpRequest req, User user) {
        return (user != null);
    }

    @Override
    public byte [] getResponse(Path subpath, FullHttpRequest req, HttpsSession session, User user) {
        return RemoteComponentsUtil.getJavascript().getBytes(StandardCharsets.UTF_8);
    }
}
