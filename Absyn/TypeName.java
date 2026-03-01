package Absyn;

public class TypeName extends Absyn {
    public String name;

    public TypeName(int p, String n) {
        pos = p;
        name = n;
    }

    public String print(int depth) {
        return "  ".repeat(depth) + "TypeName(" + name + ")";
    }
}
