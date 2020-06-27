/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.jblew.mmdj2.scene;

import com.fasterxml.jackson.annotation.JsonIgnore;
import pl.jblew.mmdj2.AppContext;

/**
 *
 * @author teofil
 */
public class Scene {
    public Device [] devices;
    
    @JsonIgnore
    public boolean isSceneReady() {
        return true;
    }
    
    public void start(AppContext context) {
        
    }
    
}
