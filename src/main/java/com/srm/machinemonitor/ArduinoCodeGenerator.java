package com.srm.machinemonitor;

public class ArduinoCodeGenerator {

    private int key=0;
    private String machineName="machine1";

    public ArduinoCodeGenerator(int key, String machineName) {
        this.key = key;
        this.machineName = machineName;
    }

    public ArduinoCodeGenerator() {
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public String getCode(){
        // TODO Generate code
        return "code";
    }
}
