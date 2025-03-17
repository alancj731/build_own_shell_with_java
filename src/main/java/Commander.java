public class Commander {
    public String command;
    public String[] args;
    public String output;

    public   Commander(String command, String[] args) {
        this.command = command;
        this.args = args;
        this.output = ":empty output";
    }

    public Commander(){
        this.command = "";
        this.args = new String[0];
        this.output = ":empty output";
    }

    public void set(String command, String[] args){
        this.command = command;
        this.args = args;
    }

    public void showArgs(){
        System.out.println("args:" + this.args.length);
        for (String item : this.args) {
            System.out.println(item);
        }
    }

    public String run(){
        this.showArgs();
        return this.command + this.output;
    }
}