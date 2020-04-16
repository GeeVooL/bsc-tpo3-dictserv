package com.mdevv.tpo3.core.components;

import java.util.*;

public class DictionaryServersListing {
    private Map<String, List<DictionaryServer>> servers = new HashMap<>();

    public synchronized void add(DictionaryServer server) {
        servers.computeIfAbsent(server.getLanguage(), k -> new ArrayList<DictionaryServer>());
        servers.get(server.getLanguage()).add(server);
    }

    public synchronized void remove(DictionaryServer server) {
        // If a list of servers for the given language does not exist,
        // return without any action.
        if (servers.get(server.getLanguage()) == null) {
            return;
        }

        servers.get(server.getLanguage()).remove(server);

        // If we deleted the last server on the list,
        // remove the list as well.
        int listSizeAfterDeletion = servers.get(server.getLanguage()).size();
        if (listSizeAfterDeletion == 0) {
            servers.remove(server.getLanguage());
        }
    }

    public synchronized DictionaryServer get(String language) {
        List<DictionaryServer> serverList = servers.get(language);

        // Return empty value if no servers for the given language exists
        if (serverList == null) return null;

        int index = new Random().nextInt(serverList.size());
        return servers.get(language).get(index);
    }
}
