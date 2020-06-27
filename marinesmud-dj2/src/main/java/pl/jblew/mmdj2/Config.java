/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.mmdj2;

import java.util.LinkedList;
import java.util.List;
import pl.jblew.marinesmud.framework.crypto.CredentialsManager;
import pl.jblew.marinesmud.framework.services.ServiceProvider;
import pl.jblew.marinesmud.framework.webserver.WebServerConfig;
import pl.jblew.mmdj2.scene.Scene;

/**
 *
 * @author teofil
 */
public class Config {
    public ServiceProvider [] services = new ServiceProvider[] {
        
    };
    public WebServerConfig webServerConfig = new WebServerConfig();
    public Scene scene = new Scene();
    public List<CredentialsManager.UserEntry<User>> credentials = new LinkedList<>();
    public String defaultUser = "admin";
    public String defaultPassword = "admin";
    
    public Config() {
        webServerConfig.listenDomain = "0.0.0.0";
        webServerConfig.externalDomain = "localhost";
        webServerConfig.cookieDomain = null;
        webServerConfig.useTemporarySelfSignedCertificate = false;
        webServerConfig.sslCertPemFile = StaticConfig.DEFAULT_CERT_PEM_PATH;
        webServerConfig.sslKeyPemFile = StaticConfig.DEFAULT_KEY_PEM_PATH;
        webServerConfig.sslPassword = StaticConfig.DEFAULT_SSL_PASSWORD;
    }
}
