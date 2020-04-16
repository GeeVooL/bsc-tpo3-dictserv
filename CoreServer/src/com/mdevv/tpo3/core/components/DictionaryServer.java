package com.mdevv.tpo3.core.components;

import java.net.InetAddress;
import java.util.Objects;

public class DictionaryServer {
    private String language;
    private InetAddress address;
    private int port;

    public DictionaryServer(String language, InetAddress address, int port) {
        this.language = language;
        this.address = address;
        this.port = port;
    }

    public String getLanguage() {
        return language;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (getClass() != o.getClass()) return false;

        DictionaryServer other = (DictionaryServer) o;
        return Objects.equals(language, other.language)
                && Objects.equals(address, other.address)
                && Objects.equals(port, other.port);
    }
}
