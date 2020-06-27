/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.mmdj2.web.modules;

import pl.jblew.marinesmud.framework.webserver.TemplateLoader;
import pl.jblew.marinesmud.framework.webserver.modules.WebModule;
import pl.jblew.mmdj2.web.webroot.Webroot;

/**
 *
 * @author teofil
 */
public abstract class AbstractModule implements WebModule {
    public static final TemplateLoader TEMPLATE_LOADER = new TemplateLoader(Webroot.class);
    
    public String render(String title, String body) {
        return TEMPLATE_LOADER.getOrLoadTemplate("default").replace("{{title}}", title).replace("{{body}}", body);
    }
}
