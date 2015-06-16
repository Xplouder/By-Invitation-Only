package pt.ipleiria.estg.es2.byinvitationonly.Models;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;

public class Contact implements Serializable {

    private String email;
    private String name;

    public Contact() {
        email = "";
        name = "";
    }

    public Contact(String name, String email) {
        this.email = email;
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isValid() {
        return !(email.isEmpty() || name.isEmpty());
    }

    public String getContactInAscii() {
        byte[] e = email.getBytes(StandardCharsets.US_ASCII);
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (byte c : e) {
            sb.append(c);
            if (i++ != e.length - 1) {
                sb.append("_");
            }
        }
        sb.append("__");
        e = name.getBytes(StandardCharsets.US_ASCII);
        i = 0;
        for (byte c : e) {
            sb.append(c);
            if (i++ != e.length - 1) {
                sb.append("_");
            }
        }
        return sb.toString();
    }
}
