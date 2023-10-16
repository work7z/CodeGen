package cc.codegen.client.dto;

public class BasisRepository {
    private Core core;
    private java.util.ArrayList<String> jars;
    private Runtime runtime;

    public Core getCore(){
        return this.core;
    }
    public void setCore(Core core){
        this.core = core;
    }
    public java.util.ArrayList<String> getJars(){
        return this.jars;
    }
    public void setJars(java.util.ArrayList<String> jars){
        this.jars = jars;
    }
    public Runtime getRuntime(){
        return this.runtime;
    }
    public void setRuntime(Runtime runtime){
        this.runtime = runtime;
    }
}
