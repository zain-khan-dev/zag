import sys



def generate_visitor_interface(paramList, basename):

    def add_visitor_class_def():
        return f"""{add_indent(1)}interface Visitor<R>""" + "{\n"


    def add_visitor_params():
        print( basename)
        params = []
        for param in paramList:
            currentParam = param.split(':')[0].strip()
            
            params.append(add_indent(2) + " R visit" + currentParam + basename + "(" + currentParam + " " + basename.lower() + ");\n ")

        return ''.join(params) + add_indent(1) + "}"
        # return ""


    

    return add_visitor_class_def() + add_visitor_params()



def add_base_class(basename):
    return (f"abstract class {basename} "+ "{")


def add_indent(times):
    return ''.join(['\t']*times)

def add_class_definition(base_class, class_name):
    return (add_indent(1) + f"""static class {class_name.strip(' ')} extends {base_class.strip(' ')}""" + "{")


def add_class_props(paramList):

    return ('\n' + ''.join([add_indent(2) + 'final ' + param.strip(' ') + ";\n" for param in  paramList]))


def add_class_constructor(className, paramList):
    return (add_indent(2) + f"""{className.strip(' ')}({','.join(paramList)})""" + " {")


def add_constructor_body(paramList):
    newline = "\n"
    return (f"""{''.join([f"{add_indent(3)}this.{param.split(' ')[2].strip(' ')} = {param.split(' ')[2].strip(' ')};{newline}" for param in paramList])}""") + (add_indent(2)  +"}\n") + (add_indent(1) + "}")



def add_visitor_override(classname, basename):
    return f"""{add_indent(2)}@Override\n""" + add_indent(2) + "<R> R accept(Visitor<R> visitor) {\n" + add_indent(4) + f"return visitor.visit{classname}{basename}(this);\n" + add_indent(2) + "}\n"


def define_ast(output_dir, basename, types):

    newlinetabtab = '\n\t\t\t'

    semicolonNewline = ';\n'

    f = open(output_dir, "w+")


    f.writelines(add_base_class(basename))
    f.writelines("\n")

    f.writelines(generate_visitor_interface(types, basename))
    f.writelines("\n")
    for type in types:
        
        className = type.split(":")[0]

        f.writelines(add_class_definition(basename, className))
        f.writelines("\n")
        f.writelines("\n")
        f.writelines(add_visitor_override(className.strip(), basename))
        paramList = type.split(":")[1].split(',')
        f.writelines(add_class_props(paramList))
        f.writelines("\n")
        f.writelines(add_class_constructor(className, paramList))
        f.writelines("\n")
        f.writelines(add_constructor_body(paramList))
        
        f.writelines("\n")

    f.writelines("\n")
    f.writelines(add_indent(1) + "abstract <R> R accept(Visitor<R> visitor);")

    f.writelines("\n}")


if __name__ == '__main__':
    if(len(sys.argv) != 2):
        print("Usage: generate_ast output_directory")
        exit()

    output_dir = sys.argv[1]
    paramList = [
         "Binary   : Expr left, Token operator, Expr right",
         "Grouping  : Expr expression",
         "Literal  : Object value",
         "Unary  : Token operator, Expr right",
         "Assign: Token name, Expr value",
         "Variable : Token name",
         "logical : Expr left, Token operator, Expr right",
         "Call    : Expr funcName, Token paren, List<Expr> arguments",
         "Get     : Expr object, Token name",
         "Set     : Expr object, Token name, Expr value",
         "This    : Token keyword",
     ]
    basename = "Expr"
    # paramList = [
    #   "Expression : Expr expression",
    #   "Print      : Expr expression",
    #   "Var        : Token name, Expr initializer",
    #   "Block      : List<Stmt> statements",
    #   "If         : Expr condition, Stmt ifCondition, Stmt elseCondition",
    #   "While      : Expr condition, Stmt body",
    #   "Function   : Token name, List<Token> parameters, List<Stmt> body",
    #   "Return     : Token keyword, Expr value",
    #   "Class      : Token name, List<Stmt.Function>  methods"
    # ]

    # basename = "Stmt"
    print(output_dir)
    define_ast(output_dir,basename, paramList)
    # print(generate_visitor_interface(paramList, basename))