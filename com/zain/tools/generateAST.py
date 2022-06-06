import sys

def define_ast(output_dir, basename, *types):
    for type in types:
        print(type)



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