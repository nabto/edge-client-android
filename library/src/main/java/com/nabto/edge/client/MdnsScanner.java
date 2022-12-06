package com.nabto.edge.client;

public interface MdnsScanner {
    public void start();
    public void stop();
    public boolean isStarted();
    public void addMdnsResultReceiver(MdnsResultListener receiver);
    public void removeMdnsResultReceiver(MdnsResultListener receiver);
}