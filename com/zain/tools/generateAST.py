import sys

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



def define_ast(output_dir, basename, types):

    newlinetabtab = '\n\t\t\t'

    semicolonNewline = ';\n'

    f = open(output_dir, "w+")

    
    f.writelines(add_base_class(basename))
    f.writelines("\n")
    for type in types:
        
        className = type.split(":")[0]

        f.writelines(add_class_definition(basename, className))
        f.writelines("\n")
        paramList = type.split(":")[1].split(',')
        f.writelines(add_class_props(paramList))
        f.writelines("\n")
        f.writelines(add_class_constructor(className, paramList))
        f.writelines("\n")
        f.writelines(add_constructor_body(paramList))
        f.writelines("\n")

    f.writelines("\n}")


if __name__ == '__main__':
    if(len(sys.argv) != 2):
        print("Usage: generate_ast output_directory")
        exit()

    output_dir = sys.argv[1]
    print(output_dir)
    define_ast(output_dir,
     "Expr", 
     [
         "Binary   : Expr left, Token operator, Expr right",
         "Grouping  : Expr Expression",
         "Literal  : Object value",
         "Unary  : Token operator, Expr right" 
     ]
     )