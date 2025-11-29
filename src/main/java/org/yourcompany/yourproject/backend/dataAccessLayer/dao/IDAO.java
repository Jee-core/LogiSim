package org.yourcompany.yourproject.backend.dataAccessLayer.dao;

import java.util.ArrayList;
import java.util.Hashtable;

public interface IDAO {
    public int save(Hashtable<String, String> data);

    public boolean delete(String id);

    public Hashtable<String, String> load(String id);

    public ArrayList<Hashtable<String, String>> load();
}
