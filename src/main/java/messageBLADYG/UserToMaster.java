/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package messageBLADYG;

import java.io.Serializable;

public class UserToMaster implements Serializable {

    private Object parapetres;
    private String action;

    public UserToMaster() {
    }

    public UserToMaster(Object parapetres, String action) {
        this.parapetres = parapetres;
        this.action = action;
    }

    public Object getParapetres() {
        return parapetres;
    }

    public void setParapetres(Object parapetres) {
        this.parapetres = parapetres;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
    

}
