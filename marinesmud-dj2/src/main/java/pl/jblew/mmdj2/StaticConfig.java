/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.mmdj2;

/**
 *
 * @author teofil
 */
public final class StaticConfig {
    public static final String CONFIG_PATH = "config.json";
    public static final boolean SAVE_LOAD_CONFIG = false;
    public static final String DEFAULT_KEY_PEM_PATH = System.getProperty("user.dir")+"/ssl/default.key.pkcs8.pem";
    public static final String DEFAULT_CERT_PEM_PATH = System.getProperty("user.dir")+"/ssl/default.cert.pem";
    public static final String DEFAULT_SSL_PASSWORD = "cert";
    
    private StaticConfig() {}
}
